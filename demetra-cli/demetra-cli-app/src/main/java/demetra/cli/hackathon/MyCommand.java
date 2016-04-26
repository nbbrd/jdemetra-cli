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
package demetra.cli.hackathon;

import com.google.common.annotations.VisibleForTesting;
import be.nbb.cli.util.joptsimple.JOptSimpleArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.joptsimple.ComposedOptionSpec;
import demetra.cli.helpers.CsvOutputOptions;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.StandardOptions;
import static demetra.cli.helpers.CsvOutputOptions.newCsvOutputOptionsSpec;
import demetra.cli.helpers.XmlUtil;
import ec.tstoolkit.information.InformationSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import joptsimple.OptionSpec;
import lombok.AllArgsConstructor;
import org.openide.util.NbBundle;

/**
 * Computes outliers from time series.
 *
 * @author Philippe Charles
 */
public final class MyCommand implements BasicCommand<MyCommand.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, MyCommand::new, o -> o.so);
    }

    private List<String> items() {
        return Arrays.asList(MyCommandTool.items);
    }

    @AllArgsConstructor
    public static class Parameters {

        StandardOptions so;
        public InputOptions input;
        public MyCommandTool.Options spec;
        public CsvOutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        List<InformationSet> output = MyCommandTool.getDefault().create(input, params.spec);

        params.output.write(output, items(), false);
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<MyCommandTool.Options> spec = new MyCommandOptionsSpec(parser);
        private final ComposedOptionSpec<CsvOutputOptions> output = newCsvOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    @NbBundle.Messages(
            {
                "mycommand.str=Text option",
                "mycommand.integer=Int option",
                "mycommand.double=Double option",
                "mycommand.items=Comma-separated list of items"
            })
    private static final class MyCommandOptionsSpec implements ComposedOptionSpec<MyCommandTool.Options> {

        private final OptionSpec<String> myStr;
        private final OptionSpec<String> myItems;
        private final OptionSpec<Double> myDouble;
        private final OptionSpec<Integer> myInteger;

        public MyCommandOptionsSpec(OptionParser p) {
            this.myStr = p
                    .accepts("str", Bundle.mycommand_str())
                    .withOptionalArg()
                    .ofType(String.class);
            this.myInteger = p
                    .accepts("n", Bundle.mycommand_integer())
                    .withOptionalArg()
                    .ofType(Integer.class);
            this.myDouble = p
                    .accepts("d", Bundle.mycommand_double())
                    .withOptionalArg()
                    .ofType(Double.class);
            this.myItems = p
                    .accepts("items", Bundle.mycommand_items())
                    .withOptionalArg()
                    .withValuesSeparatedBy(",")
                    .ofType(String.class);
        }

        @Override
        public MyCommandTool.Options value(OptionSet o) {
            Integer i=myInteger.value(o);
            Double d=myDouble.value(o);
            List<String> items=myItems.values(o);
            return new MyCommandTool.Options( myStr.value(o), 
                    i == null ? 0 : i , d == null ? 0 : d, items == null ? Collections.EMPTY_LIST : items);
        }
    }
}
