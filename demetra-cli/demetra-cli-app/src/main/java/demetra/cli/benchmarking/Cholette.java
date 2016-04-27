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
package demetra.cli.benchmarking;

import be.nbb.cli.util.BasicCliLauncher;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import be.nbb.cli.util.Utils;
import be.nbb.cli.util.joptsimple.ComposedOptionSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import static be.nbb.cli.util.joptsimple.ComposedOptionSpec.optional;
import be.nbb.cli.util.joptsimple.JOptSimpleArgsParser;
import be.nbb.cli.util.proc.CommandRegistration;
import be.nbb.demetra.toolset.BenchmarkingTool;
import be.nbb.demetra.toolset.BenchmarkingTool.CholetteOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.MediaType;
import static demetra.cli.benchmarking.Util.toTsCollectionInformation;
import static demetra.cli.benchmarking.Util.zip;
import static demetra.cli.helpers.XmlUtil.readTsCollection;
import static demetra.cli.helpers.XmlUtil.writeTsCollection;
import demetra.xml.MediaTypeAdapter;
import ec.benchmarking.simplets.TsCholette;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.timeseries.TsAggregationType;
import java.io.File;
import static java.util.Arrays.asList;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.AllArgsConstructor;

/**
 *
 * @author Philippe Charles
 */
public final class Cholette implements BasicCommand<Cholette.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Cholette::new, o -> o.so);
    }

    @AllArgsConstructor
    public static final class Parameters {

        StandardOptions so;
        public CholetteInput input;
        public CholetteOptions options;
        public OutputOptions output;
    }

    @AllArgsConstructor
    public static final class CholetteInput {

        private final File xFile;
        private final File yFile;
        @XmlJavaTypeAdapter(MediaTypeAdapter.class)
        private final MediaType mediaType;
    }

    @Override
    public void exec(Parameters p) throws Exception {
        TsCollectionInformation y = readTsCollection(InputOptions.of(p.input.yFile, p.input.mediaType));
        TsCollectionInformation x = readTsCollection(InputOptions.of(p.input.xFile, p.input.mediaType));

        BenchmarkingTool tool = BenchmarkingTool.getDefault();

        TsCollectionInformation result = zip(x.items, y.items)
                .parallelStream()
                .map(o -> exec(tool, o, p.options))
                .collect(toTsCollectionInformation());

        writeTsCollection(p.output, result);
    }

    private static TsInformation exec(BenchmarkingTool tool, Map.Entry<TsInformation, TsInformation> input, CholetteOptions options) {
        TsInformation result = new TsInformation();
        result.name = input.getKey().name;
        result.data = tool.computeCholette(input.getKey().data, input.getValue().data, options);
        return result;
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final InputSpec input = new InputSpec(parser);
        private final OptionsSpec options = new OptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            return new Parameters(so.value(o), input.value(o), options.value(o), output.value(o));
        }
    }

    private static final class InputSpec implements ComposedOptionSpec<CholetteInput> {

        private final OptionSpec<File> xFile;
        private final OptionSpec<File> yFile;
        private final OptionSpec<String> mediaType;

        public InputSpec(OptionParser p) {
            this.xFile = p
                    .acceptsAll(asList("x", "series"), "TODO")
                    .withRequiredArg()
                    .required()
                    .ofType(File.class)
                    .describedAs("file");
            this.yFile = p
                    .acceptsAll(asList("y", "constraints"), "TODO")
                    .withRequiredArg()
                    .ofType(File.class)
                    .required()
                    .describedAs("file");
            this.mediaType = p
                    .acceptsAll(asList("it", "input-type"), "TODO")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("mediaType");
        }

        @Override
        public CholetteInput value(OptionSet o) {
            return new CholetteInput(xFile.value(o), yFile.value(o), Utils.getMediaType(optional(o, mediaType), optional(o, yFile)).orElse(MediaType.XML_UTF_8));
        }
    }

    private static final class OptionsSpec implements ComposedOptionSpec<CholetteOptions> {

        private final OptionSpec<Double> rho;
        private final OptionSpec<Double> lambda;
        private final OptionSpec<TsCholette.BiasCorrection> bias;
        private final OptionSpec<TsAggregationType> aggregationType;

        public OptionsSpec(OptionParser p) {
            this.rho = p
                    .acceptsAll(asList("rho"), "TODO")
                    .withRequiredArg()
                    .ofType(Double.class)
                    .defaultsTo(1d)
                    .describedAs("double");
            this.lambda = p
                    .acceptsAll(asList("lambda"), "TODO")
                    .withRequiredArg()
                    .ofType(Double.class)
                    .defaultsTo(1d)
                    .describedAs("double");
            this.bias = p
                    .acceptsAll(asList("bias"), "TODO")
                    .withRequiredArg()
                    .ofType(TsCholette.BiasCorrection.class)
                    .defaultsTo(TsCholette.BiasCorrection.None)
                    .describedAs("biasCorrection");
            this.aggregationType = p
                    .acceptsAll(asList("aggregation-type"), "TODO")
                    .withRequiredArg()
                    .ofType(TsAggregationType.class)
                    .defaultsTo(TsAggregationType.Sum)
                    .describedAs("aggregationType");
        }

        @Override
        public CholetteOptions value(OptionSet o) {
            return new CholetteOptions(rho.value(o), lambda.value(o), bias.value(o), aggregationType.value(o));
        }
    }
}
