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
package demetra.cli.chart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.MediaType;
import be.nbb.cli.util.BasicArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import be.nbb.cli.util.InputOptions;
import static be.nbb.cli.util.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.util.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.StandardOptions;
import be.nbb.cli.util.Utils;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import ec.util.chart.impl.SmartColorScheme;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import static java.util.Arrays.asList;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.CommandRegistration;
import be.nbb.cli.util.ComposedOptionSpec;
import demetra.cli.helpers.XmlUtil;
import org.openide.util.NbBundle;

/**
 * Generates a chart from time series.
 *
 * @author Philippe Charles
 */
public final class Ts2Chart implements BasicCommand<Ts2Chart.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Ts2Chart::new, o -> o.so);
    }

    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public File outputFile;
        public MediaType mediaType;
        public ChartTool.Options chart;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TsCollectionInformation input = XmlUtil.readValue(params.input, XmlTsCollection.class);

        try (OutputStream stream = Files.newOutputStream(params.outputFile.toPath())) {
            ChartTool.getDefault().writeChart(input, params.chart, stream, params.mediaType);
        }
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionSpec<File> outputFile = parser.nonOptions("Output file").ofType(File.class);
        private final OptionSpec<String> mediaType = parser
                .acceptsAll(asList("ot", "output-type"), "Output media type")
                .withRequiredArg()
                .ofType(String.class)
                .describedAs("Media type");
        private final ComposedOptionSpec<ChartTool.Options> chart = new ChartOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
            result.so = so.value(o);
            result.input = input.value(o);
            result.outputFile = o.has(outputFile) ? outputFile.value(o) : null;
            result.mediaType = o.has(mediaType) ? MediaType.parse(mediaType.value(o)) : (result.outputFile != null ? Utils.getMediaType(result.outputFile).orElse(MediaType.SVG_UTF_8) : MediaType.SVG_UTF_8);
            result.chart = chart.value(o);
            return result;
        }
    }

    @NbBundle.Messages({
        "ts2chart.width=Width in px",
        "ts2chart.height=Height in px",
        "ts2chart.colorScheme=Color scheme name",
        "ts2chart.title=Title",
        "ts2chart.legend=Show legend"
    })
    private static final class ChartOptionsSpec implements ComposedOptionSpec<ChartTool.Options> {

        private final OptionSpec<Integer> width;
        private final OptionSpec<Integer> height;
        private final OptionSpec<String> colorScheme;
        private final OptionSpec<String> title;
        private final OptionSpec<Boolean> legend;

        public ChartOptionsSpec(OptionParser p) {
            this.width = p
                    .acceptsAll(asList("w", "width"), Bundle.ts2chart_width())
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(400);
            this.height = p
                    .acceptsAll(asList("h", "height"), Bundle.ts2chart_height())
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(300);
            this.colorScheme = p
                    .acceptsAll(asList("c", "color-scheme"), Bundle.ts2chart_colorScheme())
                    .withRequiredArg()
                    .ofType(String.class)
                    .defaultsTo(SmartColorScheme.NAME);
            this.title = p
                    .acceptsAll(asList("t", "title"), Bundle.ts2chart_title())
                    .withRequiredArg()
                    .ofType(String.class);
            this.legend = p
                    .acceptsAll(asList("l", "legend"), Bundle.ts2chart_legend())
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(Boolean.TRUE);
        }

        @Override
        public ChartTool.Options value(OptionSet o) {
            return new ChartTool.Options(width.value(o), height.value(o), colorScheme.value(o), o.has(title) ? title.value(o) : "", legend.value(o));
        }
    }
}
