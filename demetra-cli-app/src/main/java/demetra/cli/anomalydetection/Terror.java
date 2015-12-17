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
package demetra.cli.anomalydetection;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import demetra.cli.anomalydetection.AnomalyDetectionTool.CheckLastOptions;
import be.nbb.cli.util.BasicArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import be.nbb.cli.util.InputOptions;
import static be.nbb.cli.util.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.util.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import static java.util.Arrays.asList;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.proc.CommandRegistration;
import be.nbb.cli.util.ComposedOptionSpec;
import demetra.cli.helpers.CsvOutputOptions;
import static demetra.cli.helpers.CsvOutputOptions.newCsvOutputOptionsSpec;
import demetra.cli.helpers.XmlUtil;
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
public final class Terror implements BasicCommand<Terror.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Terror::new, o -> o.so);
    }

    private List<String> items(int n) {
        List<String> items = new ArrayList<>();
        items.add("series");
        for (int i = 0; i < n; ++i) {
            int j = i + 1;
            items.add("value" + j);
            items.add("forecast" + j);
            items.add("score" + j);
        }
        return items;
    }

    @AllArgsConstructor
    public static class Parameters {

        StandardOptions so;
        public InputOptions input;
        public CheckLastOptions spec;
        public CsvOutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        List<InformationSet> output = AnomalyDetectionTool.getDefault().getCheckLast(input, params.spec);

        params.output.write(output, items(params.spec.getNBacks()), false);
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<CheckLastOptions> spec = new CheckLastOptionsSpec(parser);
        private final ComposedOptionSpec<CsvOutputOptions> output = newCsvOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    @NbBundle.Messages({
        "# {0} - spec list",
        "terror.defaultSpec=Default spec [{0}]",
        "terror.critVal=Critical value",
        "terror.nBacks=N. obs. back",})
    private static final class CheckLastOptionsSpec implements ComposedOptionSpec<CheckLastOptions> {

        private final OptionSpec<AnomalyDetectionTool.DefaultSpec> defaultSpec;
        private final OptionSpec<Double> critVal;
        private final OptionSpec<Integer> nBacks;

        public CheckLastOptionsSpec(OptionParser p) {
            Joiner joiner = Joiner.on(", ");
            this.defaultSpec = p
                    .acceptsAll(asList("s", "default-spec"), Bundle.terror_defaultSpec(joiner.join(AnomalyDetectionTool.DefaultSpec.values())))
                    .withRequiredArg()
                    .ofType(AnomalyDetectionTool.DefaultSpec.class)
                    .defaultsTo(AnomalyDetectionTool.DefaultSpec.TRfull);
            this.critVal = p
                    .acceptsAll(asList("c", "critical-value"), Bundle.terror_critVal())
                    .withRequiredArg()
                    .ofType(Double.class)
                    .defaultsTo(0d);
            this.nBacks = p
                    .acceptsAll(asList("n", "nbacks"), Bundle.terror_nBacks())
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(1);
        }

        @Override
        public CheckLastOptions value(OptionSet o) {
            return new CheckLastOptions(defaultSpec.value(o), critVal.value(o), nBacks.value(o));
        }
    }
}
