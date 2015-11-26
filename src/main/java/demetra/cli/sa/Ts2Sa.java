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
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newInputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Computes seasonal adjustment report from time series.
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

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        SaTsCollection output = SaTsCollection.create(input, params.saOptions);

        params.output.writeValue(XmlSaTsCollection.class, output);
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionsSpec<SaOptions> saOptions = new SaOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
            result.input = input.value(o);
            result.saOptions = saOptions.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    private static final class SaOptionsSpec implements OptionsSpec<SaOptions> {

        private final OptionSpec<String> algorithm;
        private final OptionSpec<String> spec;
        private final OptionSpec<String> items;

        public SaOptionsSpec(OptionParser p) {
            this.algorithm = p
                    .accepts("algorithm", "Algorithm")
                    .withRequiredArg()
                    .defaultsTo("tramoseats")
                    .ofType(String.class);
            this.spec = p
                    .accepts("spec", "Specification")
                    .withRequiredArg()
                    .defaultsTo("RSA0")
                    .ofType(String.class);
            this.items = p
                    .accepts("items", "Comma-separated list of items to include")
                    .withRequiredArg()
                    .withValuesSeparatedBy(",")
                    .defaultsTo("sa", "t", "s", "i")
                    .ofType(String.class);
        }

        @Override
        public SaOptions value(OptionSet o) {
            return new SaOptions(algorithm.value(o), spec.value(o), items.values(o));
        }
    }
}
