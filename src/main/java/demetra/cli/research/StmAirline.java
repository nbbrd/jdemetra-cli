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

import demetra.cli.tests.*;
import com.google.common.annotations.VisibleForTesting;
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.BasicCliLauncher;
import demetra.cli.helpers.InputOptions;
import static demetra.cli.helpers.ComposedOptionSpec.newInputOptionsSpec;
import static demetra.cli.helpers.ComposedOptionSpec.newStandardOptionsSpec;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import demetra.cli.helpers.BasicCommand;
import demetra.cli.helpers.ComposedOptionSpec;
import demetra.cli.helpers.CsvOutputOptions;
import static demetra.cli.helpers.CsvOutputOptions.newCsvOutputOptionsSpec;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.openide.util.NbBundle;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class StmAirline implements BasicCommand<StmAirline.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, StmAirline::new, o -> o.so);
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

    @AllArgsConstructor
    public static class Parameters {

        StandardOptions so;
        public InputOptions input;
        public StmAirlineTool.Options spec;
        public CsvOutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = params.input.readValue(XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        List<InformationSet> output = StmAirlineTool.getDefault().create(input, params.spec);

        params.output.write(output, items(), false);
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<StmAirlineTool.Options> spec = new SeasonalityTestsOptionsSpec(parser);
        private final ComposedOptionSpec<CsvOutputOptions> output = newCsvOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    @NbBundle.Messages({})
    private static final class SeasonalityTestsOptionsSpec implements ComposedOptionSpec<StmAirlineTool.Options> {

        public SeasonalityTestsOptionsSpec(OptionParser p) {
        }

        @Override
        public StmAirlineTool.Options value(OptionSet o) {
            return new StmAirlineTool.Options();
        }
    }
}
