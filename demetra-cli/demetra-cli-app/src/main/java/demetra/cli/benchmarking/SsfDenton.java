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
import be.nbb.cli.util.MediaType;
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
import be.nbb.demetra.toolset.BenchmarkingTool.SsfDentonOptions;
import static demetra.cli.benchmarking.Util.toTsCollectionInformation;
import static demetra.cli.benchmarking.Util.zip;
import static demetra.cli.helpers.XmlUtil.readTsCollection;
import static demetra.cli.helpers.XmlUtil.writeTsCollection;
import demetra.xml.MediaTypeAdapter;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.TsAggregationType;
import java.io.File;
import static java.util.Arrays.asList;
import java.util.Map.Entry;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class SsfDenton implements BasicCommand<SsfDenton.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, SsfDenton::new, o -> o.so);
    }

    @lombok.AllArgsConstructor
    public static final class Parameters {

        StandardOptions so;
        public SsfDentonInput input;
        public SsfDentonOptions options;
        public OutputOptions output;
    }

    @lombok.AllArgsConstructor
    public static final class SsfDentonInput {

        public File xFile;
        public File yFile;
        @XmlJavaTypeAdapter(MediaTypeAdapter.class)
        public MediaType mediaType;
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

    private static TsInformation exec(BenchmarkingTool tool, Entry<TsInformation, TsInformation> input, SsfDentonOptions options) {
        TsInformation result = new TsInformation();
        result.name = input.getKey().name;
        result.data = tool.computeSsfDenton(input.getKey().data, input.getValue().data, options);
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

    private static final class InputSpec implements ComposedOptionSpec<SsfDentonInput> {

        private final OptionSpec<File> xFile;
        private final OptionSpec<File> yFile;
        private final OptionSpec<String> mediaType;

        public InputSpec(OptionParser p) {
            this.xFile = p
                    .acceptsAll(asList("x", "series"), "TODO")
                    .withRequiredArg()
                    .ofType(File.class)
                    .required()
                    .describedAs("file");
            this.yFile = p
                    .acceptsAll(asList("y", "constrains"), "TODO")
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
        public SsfDentonInput value(OptionSet o) {
            return new SsfDentonInput(xFile.value(o), yFile.value(o), Utils.getMediaType(optional(o, mediaType), optional(o, yFile)).orElse(MediaType.XML_UTF_8));
        }
    }

    private static final class OptionsSpec implements ComposedOptionSpec<SsfDentonOptions> {

        private final OptionSpec<Boolean> multiplicative;
        private final OptionSpec<TsAggregationType> aggregationType;

        public OptionsSpec(OptionParser p) {
            this.multiplicative = p
                    .acceptsAll(asList("multiplicative"), "TODO")
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(true)
                    .describedAs("bool");
            this.aggregationType = p
                    .acceptsAll(asList("aggregation-type"), "TODO")
                    .withRequiredArg()
                    .ofType(TsAggregationType.class)
                    .defaultsTo(TsAggregationType.Sum)
                    .describedAs("aggregationType");
        }

        @Override
        public SsfDentonOptions value(OptionSet o) {
            return new SsfDentonOptions(multiplicative.value(o), aggregationType.value(o));
        }
    }
}
