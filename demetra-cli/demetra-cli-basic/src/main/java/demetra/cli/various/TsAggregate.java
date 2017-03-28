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
package demetra.cli.various;

import be.nbb.cli.command.Command;
import be.nbb.cli.command.core.OptionsExecutor;
import be.nbb.cli.command.core.OptionsParsingCommand;
import be.nbb.cli.command.joptsimple.ComposedOptionSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import demetra.cli.helpers.XmlUtil;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.simplets.TsAggregator;
import ec.tstoolkit.timeseries.simplets.TsData;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 *
 * @author Philippe Charles
 */
public final class TsAggregate {

    @CommandRegistration
    static Command CMD = OptionsParsingCommand.<Options>builder()
            .name("tsaggregate")
            .parser(Parser::new)
            .executor(Executor::new)
            .so(o -> o.so)
            .build();

    public static final class Options {

        StandardOptions so;
        public InputOptions input;
        public List<Double> weights;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        @Override
        public void exec(Options params) throws Exception {
            TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

            if (!params.weights.isEmpty() && input.items.size() != params.weights.size()) {
                throw new IllegalArgumentException("Invalid weights list size");
            }

            TsCollectionInformation result = new TsCollectionInformation();

            if (!params.weights.isEmpty()) {
                result.items.add(process(input.items, params.weights));
            } else {
                result.items.add(process(input.items, null));
            }

            XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
        }

        @VisibleForTesting
        static TsInformation process(List<TsInformation> input, List<Double> weights) {
            TsInformation result = new TsInformation();
            result.metaData = processMeta(input);
            result.data = processData(input, weights);
            result.name = "aggregate";
            return result;
        }

        @VisibleForTesting
        static MetaData processMeta(List<TsInformation> input) {
            Map<String, String> tmp = new HashMap<>();
            if (input.get(0).metaData != null) {
                tmp.putAll(input.get(0).metaData);
                for (int i = 1; i < input.size(); i++) {
                    for (Entry<String, String> entry : input.get(i).metaData.entrySet()) {
                        if (tmp.containsKey(entry.getKey())) {
                            if (!tmp.get(entry.getKey()).equals(entry.getValue())) {
                                tmp.remove(entry.getKey());
                            }
                        }
                    }
                }
            }
            return new MetaData(tmp);
        }

        @VisibleForTesting
        static TsData processData(List<TsInformation> input, List<Double> weights) {
            TsAggregator agg = new TsAggregator();
            for (int i = 1; i < input.size(); i++) {
                if (weights != null) {
                    agg.add(input.get(i).data, weights.get(i));
                } else {
                    agg.add(input.get(i).data);
                }
            }

            return agg.sum();
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<TsAggregate.Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<List<Double>> weights = new WeightsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected TsAggregate.Options parse(OptionSet o) {
            Options result = new Options();
            result.input = input.value(o);
            result.weights = weights.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    @NbBundle.Messages({
        "tsaggregate.weights=Comma-separated list of weights"
    })
    private static final class WeightsSpec implements ComposedOptionSpec<List<Double>> {

        private final OptionSpec<Double> weights;

        public WeightsSpec(OptionParser p) {
            this.weights = p
                    .acceptsAll(asList("w", "weights"), Bundle.tsaggregate_weights())
                    .withRequiredArg()
                    .ofType(Double.class)
                    .withValuesSeparatedBy(',');
        }

        @Override
        public List<Double> value(OptionSet o) {
            return weights.values(o);
        }
    }
}
