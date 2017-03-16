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
import be.nbb.demetra.toolset.BenchmarkingTool.ExpanderOptions;
import com.google.common.annotations.VisibleForTesting;
import static demetra.cli.benchmarking.Util.domainConverter;
import static demetra.cli.benchmarking.Util.toTsCollectionInformation;
import static demetra.cli.helpers.XmlUtil.readTsCollection;
import static demetra.cli.helpers.XmlUtil.writeTsCollection;
import demetra.xml.MediaTypeAdapter;
import ec.benchmarking.simplets.TsExpander;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import static java.util.Arrays.asList;
import java.util.Optional;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.AllArgsConstructor;

/**
 *
 * @author Philippe Charles
 */
public final class Expander implements BasicCommand<Expander.Parameters> {

    @CommandRegistration
    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Expander::new, o -> o.so);
    }

    @AllArgsConstructor
    public static final class Parameters {

        StandardOptions so;
        public ExpanderInput input;
        public ExpanderOptions options;
        public OutputOptions output;
    }

    @AllArgsConstructor
    public static final class ExpanderInput {

        private final Optional<TsDomain> domain;
        private final Optional<TsFrequency> freq;
        private final File yFile;
        @XmlJavaTypeAdapter(MediaTypeAdapter.class)
        private final MediaType mediaType;
    }

    @Override
    public void exec(Parameters p) throws Exception {
        TsCollectionInformation y = readTsCollection(InputOptions.of(p.input.yFile, p.input.mediaType));

        BenchmarkingTool tool = BenchmarkingTool.getDefault();

        if (p.input.domain.isPresent()) {
            TsCollectionInformation result = y.items
                    .parallelStream()
                    .map(o -> exec(tool, p.input.domain.get(), o, p.options))
                    .collect(toTsCollectionInformation());

            writeTsCollection(p.output, result);
        } else if (p.input.freq.isPresent()) {
            TsCollectionInformation result = y.items
                    .parallelStream()
                    .map(o -> exec(tool, p.input.freq.get(), o, p.options))
                    .collect(toTsCollectionInformation());

            writeTsCollection(p.output, result);
        }
    }

    private static TsInformation exec(BenchmarkingTool tool, TsFrequency freq, TsInformation info, ExpanderOptions options) {
        TsInformation result = new TsInformation();
        result.name = info.name;
        result.data = tool.expand(freq, info.data, options);
        return result;
    }

    private static TsInformation exec(BenchmarkingTool tool, TsDomain domain, TsInformation info, ExpanderOptions options) {
        TsInformation result = new TsInformation();
        result.name = info.name;
        result.data = tool.expand(domain, info.data, options);
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

    private static final class InputSpec implements ComposedOptionSpec<ExpanderInput> {

        private final OptionSpec<TsDomain> domain;
        private final OptionSpec<TsFrequency> freq;
        private final OptionSpec<File> yFile;
        private final OptionSpec<String> mediaType;

        public InputSpec(OptionParser p) {
            this.domain = p
                    .accepts("domain", "TODO")
                    .withRequiredArg()
                    .ofType(String.class)
                    .withValuesConvertedBy(domainConverter())
                    .describedAs("domain");
            this.freq = p
                    .accepts("frequency", "TODO")
                    .withRequiredArg()
                    .ofType(TsFrequency.class)
                    .describedAs("freq");
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
        public ExpanderInput value(OptionSet o) {
            return new ExpanderInput(optional(o, domain), optional(o, freq), yFile.value(o), Utils.getMediaType(optional(o, mediaType), optional(o, yFile)).orElse(MediaType.XML_UTF_8));
        }
    }

    private static final class OptionsSpec implements ComposedOptionSpec<ExpanderOptions> {

        private final OptionSpec<Boolean> useParameter;
        private final OptionSpec<Double> parameter;
        private final OptionSpec<Boolean> trend;
        private final OptionSpec<Boolean> constant;
        private final OptionSpec<TsExpander.Model> model;
        private final OptionSpec<Integer> differencing;
        private final OptionSpec<TsAggregationType> aggregationType;

        public OptionsSpec(OptionParser p) {
            this.useParameter = p
                    .acceptsAll(asList("useParameter"), "TODO")
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(false)
                    .describedAs("bool");
            this.parameter = p
                    .acceptsAll(asList("parameter"), "TODO")
                    .withRequiredArg()
                    .ofType(double.class)
                    .defaultsTo(.9)
                    .describedAs("double");
            this.trend = p
                    .acceptsAll(asList("trend"), "TODO")
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(false)
                    .describedAs("bool");
            this.constant = p
                    .acceptsAll(asList("constant"), "TODO")
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(false)
                    .describedAs("bool");
            this.model = p
                    .acceptsAll(asList("model"), "TODO")
                    .withRequiredArg()
                    .ofType(TsExpander.Model.class)
                    .defaultsTo(TsExpander.Model.I1)
                    .describedAs("model");
            this.differencing = p
                    .acceptsAll(asList("differencing"), "TODO")
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(1)
                    .describedAs("int");
            this.aggregationType = p
                    .acceptsAll(asList("aggregation-type"), "TODO")
                    .withRequiredArg()
                    .ofType(TsAggregationType.class)
                    .defaultsTo(TsAggregationType.Sum)
                    .describedAs("aggregationType");
        }

        @Override
        public ExpanderOptions value(OptionSet o) {
            return new ExpanderOptions(useParameter.value(o), parameter.value(o), trend.value(o), constant.value(o), model.value(o), differencing.value(o), aggregationType.value(o));
        }
    }
}
