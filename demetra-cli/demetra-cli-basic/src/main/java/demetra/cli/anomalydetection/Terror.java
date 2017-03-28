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
import be.nbb.demetra.toolset.AnomalyDetectionTool;
import be.nbb.demetra.toolset.AnomalyDetectionTool.CheckLastOptions;
import com.google.common.base.Joiner;
import demetra.cli.helpers.CsvOutputOptions;
import static demetra.cli.helpers.CsvOutputOptions.newCsvOutputOptionsSpec;
import demetra.cli.helpers.XmlUtil;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class Terror {

    @CommandRegistration
    static Command CMD = OptionsParsingCommand.<Options>builder()
            .name("terror")
            .parser(Parser::new)
            .executor(Executor::new)
            .so(o -> o.so)
            .build();

    @lombok.AllArgsConstructor
    public static class Options {

        StandardOptions so;
        public InputOptions input;
        public CheckLastOptions spec;
        public CsvOutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final AnomalyDetectionTool tool = AnomalyDetectionTool.getDefault();

        @Override
        public void exec(Options params) throws Exception {
            TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

            if (params.so.isVerbose()) {
                System.err.println("Processing " + input.items.size() + " time series");
            }

            List<InformationSet> output = tool.getCheckLast(input, params.spec);

            params.output.write(output, items(params.spec.getNBacks()), false);
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
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<CheckLastOptions> spec = new CheckLastOptionsSpec(parser);
        private final ComposedOptionSpec<CsvOutputOptions> output = newCsvOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            return new Options(so.value(o), input.value(o), spec.value(o), output.value(o));
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
