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
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.BasicCliLauncher;
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newInputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
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
import lombok.Value;
import demetra.cli.helpers.BasicCommand;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class Ts2Outliers implements BasicCommand<Ts2Outliers.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Ts2Outliers::new, o -> o.so);
    }

    @Value
    public static class Parameters {

        StandardOptions so;
        InputOptions input;
        OutliersOptions spec;
        OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = params.input.readValue(XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        OutliersTsCollection output = OutliersTsCollection.create(input, params.spec);

        params.output.writeValue(XmlOutliersTsCollection.class, output);
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionsSpec<OutliersOptions> spec = new OutliersOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    private static final class OutliersOptionsSpec implements OptionsSpec<OutliersOptions> {

        private final OptionSpec<DefaultSpec> defaultSpec;
        private final OptionSpec<Double> critVal;
        private final OptionSpec<DefaultTransformationType> transformation;
        private final OptionSpec<OutlierType> outlierTypes;

        public OutliersOptionsSpec(OptionParser p) {
            this.defaultSpec = p
                    .acceptsAll(asList("s", "default-spec"), "Default spec " + BasicArgsParser.toString(DefaultSpec.values()))
                    .withRequiredArg()
                    .ofType(DefaultSpec.class)
                    .defaultsTo(DefaultSpec.TR4);
            this.critVal = p
                    .acceptsAll(asList("c", "critical-value"), "Critical value")
                    .withRequiredArg()
                    .ofType(Double.class)
                    .defaultsTo(0d);
            this.transformation = p
                    .acceptsAll(asList("t", "transformation"), "Transformation " + BasicArgsParser.toString(DefaultTransformationType.values()))
                    .withRequiredArg()
                    .ofType(DefaultTransformationType.class)
                    .defaultsTo(DefaultTransformationType.None);
            this.outlierTypes = p
                    .acceptsAll(asList("x", "outlier-types"), "Comma-separated list of outlier types " + BasicArgsParser.toString(AO, LS, TC, SO))
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
