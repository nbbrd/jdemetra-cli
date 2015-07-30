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
import com.google.common.net.MediaType;
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newInputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.StandardOptions;
import demetra.cli.helpers.Utils;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.util.chart.ColorScheme;
import ec.util.chart.ObsFunction;
import ec.util.chart.SeriesFunction;
import ec.util.chart.impl.SmartColorScheme;
import ec.util.chart.swing.JTimeSeriesChart;
import ec.util.chart.swing.SwingColorSchemeSupport;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import static java.util.Arrays.asList;
import java.util.ServiceLoader;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.jfree.data.xy.IntervalXYDataset;

/**
 *
 * @author Philippe Charles
 */
public final class Ts2Chart extends StandardApp<Ts2Chart.Parameters> {

    public static void main(String[] args) {
        new Ts2Chart().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public File outputFile;
        public MediaType mediaType;
        public ChartOptions chart;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation info = params.input.readValue(XmlTsCollection.class);

        JTimeSeriesChart chart = new JTimeSeriesChart();
        applyContent(chart, info);
        applyOptions(chart, params.chart);
        chart.doLayout();

        try (OutputStream stream = Files.newOutputStream(params.outputFile.toPath())) {
            chart.writeImage(params.mediaType.withoutParameters().toString(), stream);
        }
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    private void applyContent(JTimeSeriesChart chart, TsCollectionInformation info) {
        chart.setTitle(info.name);
        chart.setDataset(getDataset(info));
        chart.setSeriesFormatter(getSeriesFormatter(info));
        chart.setObsFormatter(getObsFormatter(info));
    }

    private void applyOptions(JTimeSeriesChart chart, ChartOptions options) {
        chart.setSize(options.getWidth(), options.getHeight());
        chart.setColorSchemeSupport(SwingColorSchemeSupport.from(getColorScheme(options.getColorScheme())));
        if (!options.getTitle().isEmpty()) {
            chart.setTitle(options.getTitle());
        }
    }

    private ObsFunction<String> getObsFormatter(final TsCollectionInformation info) {
        return new ObsFunction<String>() {
            @Override
            public String apply(int series, int obs) {
                TsData data = info.items.get(series).data;
                return data.getDomain().get(obs) + " : " + data.get(obs);
            }
        };
    }

    private SeriesFunction<String> getSeriesFormatter(final TsCollectionInformation info) {
        return new SeriesFunction<String>() {
            @Override
            public String apply(int series) {
                return info.items.get(series).name;
            }
        };
    }

    private ColorScheme getColorScheme(String name) {
        for (ColorScheme o : ServiceLoader.load(ColorScheme.class)) {
            if (o.getName().equals(name)) {
                return o;
            }
        }
        return new SmartColorScheme();
    }

    private IntervalXYDataset getDataset(TsCollectionInformation info) {
        TsXYDatasets.Builder result = TsXYDatasets.builder();
        for (TsInformation o : info.items) {
            if (o.hasData()) {
                result.add(o.name, o.data);
            }
        }
        return result.build();
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionSpec<File> outputFile = parser.nonOptions("Output file").ofType(File.class);
        private final OptionSpec<String> mediaType = parser
                .acceptsAll(asList("ot", "output-type"), "Output media type")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("Media type");
        private final ChartOptionsSpec chart = new ChartOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.so = so.value(options);
            result.input = input.value(options);
            result.outputFile = options.has(outputFile) ? outputFile.value(options) : null;
            result.mediaType = options.has(mediaType) ? MediaType.parse(mediaType.value(options)) : (result.outputFile != null ? Utils.getMediaType(result.outputFile) : MediaType.ANY_APPLICATION_TYPE);
            result.chart = chart.value(options);
            return result;
        }
    }

    private static final class ChartOptionsSpec extends OptionsSpec<ChartOptions> {

        private final OptionSpec<Integer> width;
        private final OptionSpec<Integer> height;
        private final OptionSpec<String> colorScheme;
        private final OptionSpec<String> title;

        public ChartOptionsSpec(OptionParser parser) {
            this.width = parser
                    .acceptsAll(asList("w", "width"), "Width")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(400);
            this.height = parser
                    .acceptsAll(asList("h", "height"), "Height")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(300);
            this.colorScheme = parser
                    .acceptsAll(asList("c", "color-scheme"), "Color scheme")
                    .withRequiredArg()
                    .ofType(String.class)
                    .defaultsTo(SmartColorScheme.NAME);
            this.title = parser
                    .acceptsAll(asList("t", "title"), "Title")
                    .withRequiredArg()
                    .ofType(String.class)
                    .defaultsTo("");
        }

        @Override
        public ChartOptions value(OptionSet options) {
            return new ChartOptions(width.value(options), height.value(options), colorScheme.value(options), title.value(options));
        }
    }
}
