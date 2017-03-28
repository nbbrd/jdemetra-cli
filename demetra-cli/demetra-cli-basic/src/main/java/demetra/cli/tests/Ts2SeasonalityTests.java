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
package demetra.cli.tests;

import be.nbb.cli.command.Command;
import be.nbb.cli.command.core.OptionsExecutor;
import be.nbb.cli.command.core.OptionsParsingCommand;
import be.nbb.cli.command.joptsimple.ComposedOptionSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.StandardOptions;
import demetra.cli.helpers.CsvOutputOptions;
import static demetra.cli.helpers.CsvOutputOptions.newCsvOutputOptionsSpec;
import demetra.cli.helpers.XmlUtil;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.openide.util.NbBundle;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class Ts2SeasonalityTests {

    @CommandRegistration
    static Command CMD = OptionsParsingCommand.<Options>builder()
            .name("ts2seasonalitytests")
            .parser(Parser::new)
            .executor(Executor::new)
            .so(o -> o.so)
            .build();

    @lombok.AllArgsConstructor
    public static class Options {

        StandardOptions so;
        public InputOptions input;
        public SeasonalityTestsTool.Options spec;
        public CsvOutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final SeasonalityTestsTool tool = SeasonalityTestsTool.getDefault();

        @Override
        public void exec(Options params) throws Exception {
            TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

            if (params.so.isVerbose()) {
                System.err.println("Processing " + input.items.size() + " time series");
            }

            List<InformationSet> output = tool.create(input, params.spec);

            params.output.write(output, items(), false);
        }

        private List<String> items() {
            List<String> items = new ArrayList<>();
            items.add("series");
            items.add("ftest:3");
            items.add("ftestami:3");
            items.add("kruskalwallis:3");
            items.add("friedman:3");
            return items;
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<SeasonalityTestsTool.Options> spec = new SeasonalityTestsOptionsSpec(parser);
        private final ComposedOptionSpec<CsvOutputOptions> output = newCsvOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            return new Options(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    @NbBundle.Messages({})
    private static final class SeasonalityTestsOptionsSpec implements ComposedOptionSpec<SeasonalityTestsTool.Options> {

        public SeasonalityTestsOptionsSpec(OptionParser p) {
        }

        @Override
        public SeasonalityTestsTool.Options value(OptionSet o) {
            return new SeasonalityTestsTool.Options();
        }
    }
}
