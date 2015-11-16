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
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newInputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardApp;
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

/**
 *
 * @author Philippe Charles
 */
public final class TsAggregate extends StandardApp<TsAggregate.Parameters> {

    public static void main(String[] args) {
        new TsAggregate().run(args, new TsAggregate.Parser());
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

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
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

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionsSpec<List<Integer>> weights = new WeightsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected TsAggregate.Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.input = input.value(options);
            result.weights = weights.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }

    private static final class WeightsSpec extends OptionsSpec<List<Integer>> {

        private final OptionSpec<Integer> itemsToRemove;

        public WeightsSpec(OptionParser parser) {
            this.itemsToRemove = parser
                    .acceptsAll(asList("w", "weights"), "Comma-separated list of weights")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .withValuesSeparatedBy(',');
        }

        @Override
        public List<Integer> value(OptionSet options) {
            return itemsToRemove.values(options);
        }
    }
}
