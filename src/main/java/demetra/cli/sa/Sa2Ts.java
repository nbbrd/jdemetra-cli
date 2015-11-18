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
import joptsimple.OptionSet;

/**
 *
 * @author Philippe Charles
 */
public final class Sa2Ts extends StandardApp<Sa2Ts.Parameters> {

    public static void main(String[] args) {
        new Sa2Ts().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        SaTsCollection input = params.input.readValue(XmlSaTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.getItems().size() + " items");
        }

        TsCollectionInformation output = input.toTsCollection();

        params.output.writeValue(XmlTsCollection.class, output);
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.input = input.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }
}
