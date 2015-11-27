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
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.BasicCliLauncher;
import demetra.cli.helpers.InputOptions;
import static demetra.cli.helpers.ComposedOptionSpec.newInputOptionsSpec;
import static demetra.cli.helpers.ComposedOptionSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.ComposedOptionSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import demetra.cli.helpers.BasicCommand;
import demetra.cli.helpers.ComposedOptionSpec;
import org.openide.util.NbBundle;

/**
 * Computes seasonal adjustment report from time series.
 *
 * @author Philippe Charles
 */
public final class Ts2Sa implements BasicCommand<Ts2Sa.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Ts2Sa::new, o -> o.so);
    }

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

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<SaOptions> saOptions = new SaOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

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

    @NbBundle.Messages({
        "ts2sa.algorithm=Algorithm",
        "ts2sa.spec=Specification",
        "ts2sa.items=Comma-separated list of items to include"
    })
    private static final class SaOptionsSpec implements ComposedOptionSpec<SaOptions> {

        private final OptionSpec<String> algorithm;
        private final OptionSpec<String> spec;
        private final OptionSpec<String> items;

        public SaOptionsSpec(OptionParser p) {
            this.algorithm = p
                    .accepts("algorithm", Bundle.ts2sa_algorithm())
                    .withRequiredArg()
                    .defaultsTo("tramoseats")
                    .ofType(String.class);
            this.spec = p
                    .accepts("spec", Bundle.ts2sa_spec())
                    .withRequiredArg()
                    .defaultsTo("RSA0")
                    .ofType(String.class);
            this.items = p
                    .accepts("items", Bundle.ts2sa_items())
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
