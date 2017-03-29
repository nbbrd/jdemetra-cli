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

import be.nbb.cli.command.Command;
import be.nbb.cli.command.core.OptionsExecutor;
import be.nbb.cli.command.core.OptionsParsingCommand;
import be.nbb.cli.command.joptsimple.ComposedOptionSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import demetra.cli.helpers.XmlUtil;
import demetra.xml.TsPeriodSelectorAdapter;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class TsCrop {

    @CommandRegistration(name = "tscrop")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    public static final class Options {

        StandardOptions so;
        public InputOptions input;
        @XmlJavaTypeAdapter(TsPeriodSelectorAdapter.class)
        public TsPeriodSelector periodSelector;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        @Override
        public void exec(Options params) throws Exception {
            TsCollectionInformation result = XmlUtil.readValue(params.input, XmlTsCollection.class);
            if (params.periodSelector.getType() != PeriodSelectorType.All) {
                selectPeriods(result, params.periodSelector);
            }
            XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
        }

        @VisibleForTesting
        static void selectPeriods(TsCollectionInformation info, TsPeriodSelector selector) {
            info.items.forEach(o -> o.data = o.hasData() ? o.data.select(selector) : null);
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<TsPeriodSelector> periodSelector = new PeriodSelectorOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            Options result = new Options();
            result.input = input.value(o);
            result.periodSelector = periodSelector.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    private static final class PeriodSelectorOptionsSpec implements ComposedOptionSpec<TsPeriodSelector> {

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
}
