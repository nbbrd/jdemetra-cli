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
package demetra.cli.jd3tests;

import com.google.common.annotations.VisibleForTesting;
import be.nbb.cli.util.joptsimple.JOptSimpleArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import be.nbb.cli.util.InputOptions;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.proc.CommandRegistration;
import be.nbb.cli.util.joptsimple.ComposedOptionSpec;
import demetra.cli.helpers.CsvOutputOptions;
import static demetra.cli.helpers.CsvOutputOptions.newCsvOutputOptionsSpec;
import demetra.cli.helpers.XmlUtil;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.List;
import joptsimple.OptionSpec;
import lombok.AllArgsConstructor;
import org.openide.util.NbBundle;

/**
 * Compare the precision of different estimation procedures of SARIMA models 
 *
 * @author Jean Palate
 */
public final class Ts2ArmaTests implements BasicCommand<Ts2ArmaTests.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Ts2ArmaTests::new, o -> o.so);
    }

    private List<String> items() {
        List<String> items = new ArrayList<>();
        items.add("series");
        return items;
    }

    @AllArgsConstructor
    public static class Parameters {

        StandardOptions so;
        public InputOptions input;
        public ArmaTestsTool.Options spec;
        public CsvOutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

        if (params.so.isVerbose()) {
            System.err.println("Processing " + input.items.size() + " time series");
        }

        List<InformationSet> output = ArmaTestsTool.getDefault().create(input, params.spec);

        params.output.write(output, false);
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<ArmaTestsTool.Options> spec = new ArmaTestsOptionsSpec(parser);
        private final ComposedOptionSpec<CsvOutputOptions> output = newCsvOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), spec.value(o), output.value(o));
        }
    }

    @NbBundle.Messages(
            {
        "ts2armatests.p=Regular AR order",
        "ts2armatests.bp=Seasonal AR order",
        "ts2armatests.q=Regular MA order",
        "ts2armatests.bq=Seasonal MA order"
            })
    private static final class ArmaTestsOptionsSpec implements ComposedOptionSpec<ArmaTestsTool.Options> {

        private final OptionSpec<Integer> p;
        private final OptionSpec<Integer> q;
        private final OptionSpec<Integer> bp;
        private final OptionSpec<Integer> bq;
        
        public ArmaTestsOptionsSpec(OptionParser p) {
            this.p = p.accepts("p").withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.q = p.accepts("q").withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.bp = p.accepts("bp").withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.bq = p.accepts("bq").withRequiredArg().ofType(Integer.class).defaultsTo(1);
        }

        @Override
        public ArmaTestsTool.Options value(OptionSet o) {
            return new ArmaTestsTool.Options(p.value(o), bp.value(o), q.value(o), bq.value(o));
        }
    }
}
