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
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import be.nbb.demetra.toolset.AnomalyDetectionTool;
import be.nbb.demetra.toolset.AnomalyDetectionTool.DefaultSpec;
import be.nbb.demetra.toolset.AnomalyDetectionTool.OutliersOptions;
import be.nbb.demetra.toolset.AnomalyDetectionTool.OutliersTsCollection;
import com.google.common.base.Joiner;
import demetra.cli.helpers.XmlUtil;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.regression.OutlierType;
import static ec.tstoolkit.timeseries.regression.OutlierType.AO;
import static ec.tstoolkit.timeseries.regression.OutlierType.LS;
import static ec.tstoolkit.timeseries.regression.OutlierType.SO;
import static ec.tstoolkit.timeseries.regression.OutlierType.TC;
import static java.util.Arrays.asList;
import java.util.EnumSet;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class Ts2Outliers {

    @CommandRegistration(name = "ts2outliers")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    @lombok.AllArgsConstructor
    public static class Options {

        StandardOptions so;
        public InputOptions input;
        public OutliersOptions spec;
        public OutputOptions output;
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

            OutliersTsCollection output = tool.getOutliers(input, params.spec);

            XmlUtil.writeValue(params.output, XmlOutliersTsCollection.class, output);
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<AnomalyDetectionTool.OutliersOptions> spec = new OutliersOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            return new Options(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    @NbBundle.Messages({
        "# {0} - spec list",
        "ts2outliers.defaultSpec=Default spec [{0}]",
        "ts2outliers.critVal=Critical value",
        "# {0} - transformation list",
        "ts2outliers.transformation=Transformation [{0}]",
        "# {0} - outlier types",
        "ts2outliers.outlierTypes=Comma-separated list of outlier types [{0}]"
    })
    private static final class OutliersOptionsSpec implements ComposedOptionSpec<OutliersOptions> {

        private final OptionSpec<DefaultSpec> defaultSpec;
        private final OptionSpec<Double> critVal;
        private final OptionSpec<DefaultTransformationType> transformation;
        private final OptionSpec<OutlierType> outlierTypes;

        public OutliersOptionsSpec(OptionParser p) {
            Joiner joiner = Joiner.on(", ");
            this.defaultSpec = p
                    .acceptsAll(asList("s", "default-spec"), Bundle.ts2outliers_defaultSpec(joiner.join(DefaultSpec.values())))
                    .withRequiredArg()
                    .ofType(DefaultSpec.class)
                    .defaultsTo(DefaultSpec.TRfull);
            this.critVal = p
                    .acceptsAll(asList("c", "critical-value"), Bundle.ts2outliers_critVal())
                    .withRequiredArg()
                    .ofType(Double.class)
                    .defaultsTo(0d);
            this.transformation = p
                    .acceptsAll(asList("t", "transformation"), Bundle.ts2outliers_transformation(joiner.join(DefaultTransformationType.values())))
                    .withRequiredArg()
                    .ofType(DefaultTransformationType.class)
                    .defaultsTo(DefaultTransformationType.None);
            this.outlierTypes = p
                    .acceptsAll(asList("x", "outlier-types"), Bundle.ts2outliers_outlierTypes(joiner.join(AO, LS, TC, SO)))
                    .withRequiredArg()
                    .ofType(OutlierType.class)
                    .withValuesSeparatedBy(',')
                    .defaultsTo(AO, LS, TC);
        }

        @Override
        public OutliersOptions value(OptionSet o) {
            return new OutliersOptions(defaultSpec.value(o), critVal.value(o), transformation.value(o), EnumSet.copyOf(outlierTypes.values(o)));
        }
    }
}
