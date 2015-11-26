/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.cli.various;

import com.google.common.annotations.VisibleForTesting;
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.BasicCliLauncher;
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newInputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import demetra.xml.TsPeriodSelectorAdapter;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import demetra.cli.helpers.BasicCommand;

/**
 *
 * @author Philippe Charles
 */
public final class TsFilter implements BasicCommand<TsFilter.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, TsFilter::new, o -> o.so);
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        @XmlJavaTypeAdapter(TsPeriodSelectorAdapter.class)
        public TsPeriodSelector periodSelector;
        public EnumSet<TsItem> itemsToRemove;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation result = params.input.readValue(XmlTsCollection.class);
        if (params.periodSelector.getType() != PeriodSelectorType.All) {
            selectPeriods(result, params.periodSelector);
        }
        if (!params.itemsToRemove.isEmpty()) {
            removeItems(result, params.itemsToRemove);
        }
        params.output.writeValue(XmlTsCollection.class, result);
    }

    @VisibleForTesting
    static void removeItems(TsCollectionInformation info, Set<TsItem> items) {
        info.name = items.contains(TsItem.name) ? null : info.name;
        info.moniker = items.contains(TsItem.moniker) ? new TsMoniker() : info.moniker;
        info.metaData = items.contains(TsItem.metaData) ? null : info.metaData;
        info.invalidDataCause = items.contains(TsItem.cause) ? null : info.invalidDataCause;
        info.type = items.contains(TsItem.type) ? TsInformationType.UserDefined : info.type;
        info.items.forEach(o -> {
            o.name = items.contains(TsItem.name) ? null : o.name;
            o.moniker = items.contains(TsItem.moniker) ? new TsMoniker() : o.moniker;
            o.metaData = o.hasMetaData() ? (items.contains(TsItem.metaData) ? null : o.metaData) : null;
            o.invalidDataCause = items.contains(TsItem.cause) ? null : o.invalidDataCause;
            o.type = items.contains(TsItem.type) ? TsInformationType.UserDefined : o.type;
            o.data = o.hasData() ? (items.contains(TsItem.data) ? null : o.data) : null;
        });
    }

    @VisibleForTesting
    static void selectPeriods(TsCollectionInformation info, TsPeriodSelector selector) {
        info.items.forEach(o -> {
            o.data = o.hasData() ? o.data.select(selector) : null;
        });
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionsSpec<TsPeriodSelector> periodSelector = new PeriodSelectorOptionsSpec(parser);
        private final OptionsSpec<EnumSet<TsItem>> itemsToRemove = new ItemsToRemoveSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
            result.input = input.value(o);
            result.periodSelector = periodSelector.value(o);
            result.itemsToRemove = itemsToRemove.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    private static final class PeriodSelectorOptionsSpec implements OptionsSpec<TsPeriodSelector> {

        private final OptionSpec<Date> from;
        private final OptionSpec<Date> to;
        private final OptionSpec<Integer> first;
        private final OptionSpec<Integer> last;

        public PeriodSelectorOptionsSpec(OptionParser p) {
            this.from = p.accepts("from").withRequiredArg().ofType(Date.class);
            this.to = p.accepts("to").withRequiredArg().ofType(Date.class);
            this.first = p.accepts("first").withRequiredArg().ofType(Integer.class);
            this.last = p.accepts("last").withRequiredArg().ofType(Integer.class);
        }

        @Override
        public TsPeriodSelector value(OptionSet o) {
            TsPeriodSelector result = new TsPeriodSelector();
            if (o.has(from)) {
                if (o.has(to)) {
                    result.between(new Day(from.value(o)), new Day(to.value(o)));
                } else {
                    result.from(new Day(from.value(o)));
                }
            } else if (o.has(to)) {
                result.to(new Day(to.value(o)));
            } else if (o.has(first)) {
                if (o.has(last)) {
                    result.excluding(first.value(o), last.value(o));
                } else {
                    result.first(first.value(o));
                }
            } else if (o.has(last)) {
                result.last(last.value(o));
            } else {
                result.all();
            }
            return result;
        }
    }

    private static final class ItemsToRemoveSpec implements OptionsSpec<EnumSet<TsItem>> {

        private final OptionSpec<TsItem> itemsToRemove;

        public ItemsToRemoveSpec(OptionParser p) {
            this.itemsToRemove = p
                    .accepts("remove", "Comma-separated list of items to remove " + Arrays.toString(TsItem.values()))
                    .withRequiredArg()
                    .ofType(TsItem.class)
                    .withValuesSeparatedBy(',');
        }

        @Override
        public EnumSet<TsItem> value(OptionSet o) {
            return o.has(itemsToRemove) ? EnumSet.copyOf(itemsToRemove.values(o)) : EnumSet.noneOf(TsItem.class);
        }
    }
}
