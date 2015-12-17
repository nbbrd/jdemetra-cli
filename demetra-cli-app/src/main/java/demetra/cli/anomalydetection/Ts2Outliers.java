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
import demetra.cli.anomalydetection.AnomalyDetectionTool.DefaultSpec;
import demetra.cli.anomalydetection.AnomalyDetectionTool.OutliersOptions;
import demetra.cli.anomalydetection.AnomalyDetectionTool.OutliersTsCollection;
import be.nbb.cli.util.joptsimple.JOptSimpleArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import be.nbb.cli.util.InputOptions;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
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
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.proc.CommandRegistration;
import be.nbb.cli.util.joptsimple.ComposedOptionSpec;
import demetra.cli.helpers.XmlUtil;
import lombok.AllArgsConstructor;
import org.openide.util.NbBundle;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class Ts2Outliers implements BasicCommand<Ts2Outliers.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Ts2Outliers::new, o -> o.so);
    }

    @AllArgsConstructor
    public static class Parameters {

        StandardOptions so;
        public InputOptions input;
        public OutliersOptions spec;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        OutliersTsCollection output = AnomalyDetectionTool.getDefault().getOutliers(input, params.spec);

        XmlUtil.writeValue(params.output, XmlOutliersTsCollection.class, output);
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<AnomalyDetectionTool.OutliersOptions> spec = new OutliersOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), spec.value(o), output.value(o));
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
