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
import be.nbb.cli.util.BasicArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import be.nbb.cli.util.InputOptions;
import static be.nbb.cli.util.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.util.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.util.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.ComposedOptionSpec;
import demetra.cli.helpers.XmlUtil;
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
        public SaTool.Options saOptions;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        SaTool.SaTsCollection output = SaTool.getDefault().create(input, params.saOptions);

        XmlUtil.writeValue(params.output, XmlSaTsCollection.class, output);
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<SaTool.Options> saOptions = new SaOptionsSpec(parser);
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
    private static final class SaOptionsSpec implements ComposedOptionSpec<SaTool.Options> {

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
                    .defaultsTo("RSAfull")
                    .ofType(String.class);
            this.items = p
                    .accepts("items", Bundle.ts2sa_items())
                    .withRequiredArg()
                    .withValuesSeparatedBy(",")
                    .defaultsTo("sa", "t", "s", "i")
                    .ofType(String.class);
        }

        @Override
        public SaTool.Options value(OptionSet o) {
            return new SaTool.Options(algorithm.value(o), spec.value(o), items.values(o));
        }
    }
}
