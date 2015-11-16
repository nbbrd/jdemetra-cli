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
package demetra.cli.sa;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newInputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.ForkJoinTasks;
import demetra.cli.helpers.StandardOptions;
import demetra.cli.helpers.Utils;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Ts2Sa extends StandardApp<Ts2Sa.Parameters> {

    public static void main(String[] args) {
        new Ts2Sa().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public SaOptions saOptions;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = params.input.readValue(XmlTsCollection.class);

        List<Map<String, TsData>> data = process(input, params.saOptions, params.so.isVerbose());

        XmlSaTsCollection output = XmlSaTsCollection.create(input, data, params.saOptions);
        params.output.write(XmlSaTsCollection.class, output);
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static List<Map<String, TsData>> process(TsCollectionInformation input, SaOptions saOptions, boolean verbose) {
        if (verbose) {
            System.err.println("Processing " + input.items.size() + " time series");
        }
        Function<List<TsInformation>, List<Map<String, TsData>>> processor = asFunc(saOptions);
        return ForkJoinTasks.invoke(verbose ? Utils.withProgress(processor, input.items.size()) : processor, 10, input.items);
    }

    private static Function<List<TsInformation>, List<Map<String, TsData>>> asFunc(final SaOptions saOptions) {
        return new Function<List<TsInformation>, List<Map<String, TsData>>>() {
            @Override
            public List<Map<String, TsData>> apply(List<TsInformation> input) {
                return saOptions.process(input);
            }
        };
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionsSpec<SaOptions> saOptions = new SaOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.input = input.value(options);
            result.saOptions = saOptions.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }

    private static final class SaOptionsSpec extends OptionsSpec<SaOptions> {

        private final OptionSpec<String> algorithm;
        private final OptionSpec<String> spec;

        public SaOptionsSpec(OptionParser parser) {
            this.algorithm = parser
                    .accepts("algorithm", "Algorithm")
                    .withRequiredArg()
                    .defaultsTo("tramoseats")
                    .ofType(String.class);
            this.spec = parser
                    .accepts("spec", "Specification")
                    .withRequiredArg()
                    .defaultsTo("RSA0")
                    .ofType(String.class);
        }

        @Override
        public SaOptions value(OptionSet options) {
            return new SaOptions(algorithm.value(options), spec.value(options));
        }
    }
}
