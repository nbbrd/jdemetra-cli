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
import com.google.common.base.Joiner;
import ec.tss.xml.XmlTs;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.design.VisibleForTesting;
import java.util.EnumSet;
import java.util.Set;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 * Computes descriptive statistics from time series.
 *
 * @author Philippe Charles
 */
public final class Ts2DStats {

    @CommandRegistration
    static Command CMD = OptionsParsingCommand.<Options>builder()
            .name("ts2dstats")
            .parser(Parser::new)
            .executor(Executor::new)
            .so(o -> o.so)
            .build();

    public static final class Options {

        StandardOptions so;
        public InputOptions input;
        public Set<DStatsItem> items;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        @Override
        public void exec(Options params) throws Exception {
            XmlTsCollection col = params.input.read(XmlTsCollection.class);
            XmlDStatsTsCollection result = process(col);
            filter(result, params.items);
            params.output.write(XmlDStatsTsCollection.class, result);
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
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final ComposedOptionSpec<EnumSet<DStatsItem>> items = new ItemsToIncludeOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            Options result = new Options();
            result.input = input.value(o);
            result.items = items.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    @NbBundle.Messages({
        "# {0} - spec list",
        "ts2dstats.items=Comma-separated list of items to include [{0}]"
    })
    private static final class ItemsToIncludeOptionsSpec implements ComposedOptionSpec<EnumSet<DStatsItem>> {

        private final OptionSpec<DStatsItem> items;

        public ItemsToIncludeOptionsSpec(OptionParser p) {
            Joiner joiner = Joiner.on(", ");
            this.items = p
                    .accepts("include", Bundle.ts2dstats_items(joiner.join(DStatsItem.values())))
                    .withRequiredArg()
                    .ofType(DStatsItem.class)
                    .withValuesSeparatedBy(',');
        }

        @Override
        public EnumSet<DStatsItem> value(OptionSet o) {
            return o.has(items) ? EnumSet.copyOf(items.values(o)) : EnumSet.allOf(DStatsItem.class);
        }
    }
}
