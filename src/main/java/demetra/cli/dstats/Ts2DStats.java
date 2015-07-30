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
package demetra.cli.dstats;

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
import ec.tss.xml.XmlTs;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.data.DescriptiveStatistics;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Ts2DStats extends StandardApp<Ts2DStats.Parameters> {

    public static void main(String[] args) {
        new Ts2DStats().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public Set<DStatsItem> items;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        XmlTsCollection col = params.input.read(XmlTsCollection.class);
        XmlDStatsTsCollection result = process(col);
        filter(result, params.items);
        params.output.write(XmlDStatsTsCollection.class, result);
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static XmlDStatsTsCollection process(XmlTsCollection col) {
        XmlDStatsTsCollection result = new XmlDStatsTsCollection();
        result.name = col.name;
        result.source = col.source;
        result.identifier = col.identifier;
        if (col.tslist != null) {
            result.items = new XmlDStatsTs[col.tslist.length];
            for (int i = 0; i < result.items.length; i++) {
                result.items[i] = process(col.tslist[i]);
            }
        }
        return result;
    }

    @VisibleForTesting
    static XmlDStatsTs process(XmlTs ts) {
        XmlDStatsTs result = new XmlDStatsTs();
        result.name = ts.name;
        result.source = ts.source;
        result.identifier = ts.identifier;
        DescriptiveStatistics stats = new DescriptiveStatistics(ts.data);
        result.average = stats.getAverage();
        result.dataCount = stats.getDataCount();
        result.kurtosis = stats.getKurtosis();
        result.max = stats.getMax();
        result.median = stats.getMedian();
        result.min = stats.getMin();
        result.missingValuesCount = stats.getMissingValuesCount();
        result.observationsCount = stats.getObservationsCount();
        result.rmse = stats.getRmse();
        result.skewness = stats.getSkewness();
        result.stdev = stats.getStdev();
        result.sum = stats.getSum();
        result.sumSquare = stats.getSumSquare();
        result.var = stats.getVar();
        return result;
    }

    @VisibleForTesting
    static void filter(XmlDStatsTsCollection input, Set<DStatsItem> items) {
        for (XmlDStatsTs o : input.items) {
            o.average = items.contains(DStatsItem.average) ? o.average : null;
            o.dataCount = items.contains(DStatsItem.dataCount) ? o.dataCount : null;
            o.kurtosis = items.contains(DStatsItem.kurtosis) ? o.kurtosis : null;
            o.max = items.contains(DStatsItem.max) ? o.max : null;
            o.median = items.contains(DStatsItem.median) ? o.median : null;
            o.min = items.contains(DStatsItem.min) ? o.min : null;
            o.missingValuesCount = items.contains(DStatsItem.missingValues) ? o.missingValuesCount : null;
            o.observationsCount = items.contains(DStatsItem.obs) ? o.observationsCount : null;
            o.rmse = items.contains(DStatsItem.rmse) ? o.rmse : null;
            o.skewness = items.contains(DStatsItem.skewness) ? o.skewness : null;
            o.stdev = items.contains(DStatsItem.stdev) ? o.stdev : null;
            o.sum = items.contains(DStatsItem.sum) ? o.sum : null;
            o.sumSquare = items.contains(DStatsItem.sumSquare) ? o.sumSquare : null;
            o.var = items.contains(DStatsItem.var) ? o.var : null;
        }
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionsSpec<EnumSet<DStatsItem>> items = new ItemsToIncludeOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.input = input.value(options);
            result.items = items.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }

    private static final class ItemsToIncludeOptionsSpec extends OptionsSpec<EnumSet<DStatsItem>> {

        private final OptionSpec<DStatsItem> items;

        public ItemsToIncludeOptionsSpec(OptionParser parser) {
            this.items = parser
                    .accepts("include", "Comma-separated list of items to include " + Arrays.toString(DStatsItem.values()))
                    .withRequiredArg()
                    .ofType(DStatsItem.class)
                    .withValuesSeparatedBy(',');
        }

        @Override
        public EnumSet<DStatsItem> value(OptionSet options) {
            return options.has(items) ? EnumSet.copyOf(items.values(options)) : EnumSet.allOf(DStatsItem.class);
        }
    }
}
