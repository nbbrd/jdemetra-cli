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
import ec.tss.TsInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import demetra.cli.helpers.BasicCommand;
import demetra.cli.helpers.ComposedOptionSpec;
import org.openide.util.NbBundle;

/**
 *
 * @author Philippe Charles
 */
public final class TsAggregate implements BasicCommand<TsAggregate.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, TsAggregate::new, o -> o.so);
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public List<Integer> weights;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = params.input.readValue(XmlTsCollection.class);

        if (input.items.size() != params.weights.size()) {
            throw new IllegalArgumentException("Invalid weights list size");
        }

        TsCollectionInformation result = new TsCollectionInformation();

        if (!input.items.isEmpty()) {
            result.items.add(process(input.items, params.weights));
        }

        params.output.writeValue(XmlTsCollection.class, result);
    }

    @VisibleForTesting
    static TsInformation process(List<TsInformation> input, List<Integer> weights) {
        TsInformation result = new TsInformation();
        result.metaData = processMeta(input);
        result.data = processData(input, weights);
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
    static TsData processData(List<TsInformation> input, List<Integer> weights) {
        int totalWeights = weights.get(0);
        TsPeriod minPeriod = input.get(0).data.getStart();
        TsPeriod maxPeriod = input.get(0).data.getLastPeriod();
        for (int i = 1; i < input.size(); i++) {
            TsData item = input.get(i).data;
            totalWeights += weights.get(i);
            minPeriod = min(minPeriod, item.getStart());
            maxPeriod = max(maxPeriod, item.getLastPeriod());
        }

        TsData data = new TsData(minPeriod, maxPeriod.minus(minPeriod) + 1);
        data.getValues().setMissingValues(0);
        for (int i = 0; i < input.size(); i++) {
            TsData item = input.get(i).data;
            int baseIdx = item.getStart().minus(minPeriod);
            for (int j = 0; j < item.getLength(); j++) {
                data.set(baseIdx + j, data.get(j) + item.get(j) * weights.get(i) / totalWeights);
            }
        }
        return data;
    }

    private static TsPeriod min(TsPeriod l, TsPeriod r) {
        return l.isBefore(r) ? l : r;
    }

    private static TsPeriod max(TsPeriod l, TsPeriod r) {
        return l.isAfter(r) ? l : r;
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<TsAggregate.Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<List<Integer>> weights = new WeightsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected TsAggregate.Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
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
    private static final class WeightsSpec implements ComposedOptionSpec<List<Integer>> {

        private final OptionSpec<Integer> weights;

        public WeightsSpec(OptionParser p) {
            this.weights = p
                    .acceptsAll(asList("w", "weights"), Bundle.tsaggregate_weights())
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .withValuesSeparatedBy(',');
        }

        @Override
        public List<Integer> value(OptionSet o) {
            return weights.values(o);
        }
    }
}
