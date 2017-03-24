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
package demetra.cli.research;

import be.nbb.cli.util.joptsimple.JOptSimpleArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import be.nbb.cli.util.InputOptions;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.proc.CommandRegistration;
import be.nbb.cli.util.joptsimple.ComposedOptionSpec;
import demetra.cli.helpers.CsvOutputOptions;
import static demetra.cli.helpers.CsvOutputOptions.newCsvOutputOptionsSpec;
import demetra.cli.helpers.XmlUtil;
import demetra.cli.research.HsTool.HsResults;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.information.InformationSet;
import java.util.Arrays;
import java.util.List;
import org.openide.util.NbBundle;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class HsCommand implements BasicCommand<HsCommand.Parameters> {

    @CommandRegistration(name = "ts2hs")
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, HsCommand::new, o -> o.so);
    }

    private List<String> items() {
        return Arrays.asList(HsResults.items);
    }

    @lombok.AllArgsConstructor
    public static class Parameters {

        StandardOptions so;
        public InputOptions input;
        public HsTool.Options spec;
        public CsvOutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        List<InformationSet> output = HsTool.getDefault().create(input, params.spec);

        params.output.write(output, items(), false);
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<HsTool.Options> spec = new HsOptionsSpec(parser);
        private final ComposedOptionSpec<CsvOutputOptions> output = newCsvOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    @NbBundle.Messages({})
    private static final class HsOptionsSpec implements ComposedOptionSpec<HsTool.Options> {

        public HsOptionsSpec(OptionParser p) {
        }

        @Override
        public HsTool.Options value(OptionSet o) {
            return new HsTool.Options();
        }
    }
}
