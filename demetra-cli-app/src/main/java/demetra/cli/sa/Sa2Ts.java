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
import joptsimple.OptionSet;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.proc.CommandRegistration;
import be.nbb.cli.util.ComposedOptionSpec;
import demetra.cli.helpers.XmlUtil;

/**
 * Converts seasonal adjustment report to time series.
 *
 * @author Philippe Charles
 */
public final class Sa2Ts implements BasicCommand<Sa2Ts.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Sa2Ts::new, o -> o.so);
    }

    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        SaTool.SaTsCollection input = XmlUtil.readValue(params.input, XmlSaTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.getItems().size() + " items");
        }

        TsCollectionInformation output = SaTool.getDefault().toTsCollection(input);

        XmlUtil.writeValue(params.output, XmlTsCollection.class, output);
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
            result.input = input.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }
}
