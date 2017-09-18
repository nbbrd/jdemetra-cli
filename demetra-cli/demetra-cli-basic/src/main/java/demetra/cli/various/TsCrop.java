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
@lombok.experimental.UtilityClass
public final class TsCrop {

    @CommandRegistration(name = "tscrop")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
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
        public void exec(Options o) throws Exception {
            TsCollectionInformation result = XmlUtil.readValue(o.input, XmlTsCollection.class);
            applySelector(result, o.periodSelector);
            XmlUtil.writeValue(o.output, XmlTsCollection.class, result);
        }

        @VisibleForTesting
        static void applySelector(TsCollectionInformation info, TsPeriodSelector selector) {
            if (selector.getType() != PeriodSelectorType.All) {
                info.items.forEach(o -> o.data = o.hasData() ? o.data.select(selector) : null);
            }
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
            return new Options(so.value(o), input.value(o), periodSelector.value(o), output.value(o));
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
