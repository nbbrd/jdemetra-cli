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
package be.nbb.cli.command.joptsimple;

import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import java.io.File;
import static java.util.Arrays.asList;
import java.util.Optional;
import javax.annotation.Nonnull;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface ComposedOptionSpec<T> {

    T value(@Nonnull OptionSet options);

    @Nonnull
    public static ComposedOptionSpec<StandardOptions> newStandardOptionsSpec(@Nonnull OptionParser parser) {
        return new StandardOptionsSpec(parser);
    }

    @Nonnull
    public static ComposedOptionSpec<InputOptions> newInputOptionsSpec(@Nonnull OptionParser parser) {
        return new InputOptionsSpec(parser);
    }

    @Nonnull
    public static ComposedOptionSpec<OutputOptions> newOutputOptionsSpec(@Nonnull OptionParser parser) {
        return new OutputOptionsSpec(parser);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static <X> Optional<X> optional(OptionSet options, OptionSpec<X> spec) {
        return Optional.ofNullable(options.has(spec) ? spec.value(options) : null);
    }

    @NbBundle.Messages({
        "standardOptions.help=Show help",
        "standardOptions.version=Show version",
        "standardOptions.verbose=Verbose mode"
    })
    static final class StandardOptionsSpec implements ComposedOptionSpec<StandardOptions> {

        private final OptionSpec<Void> help;
        private final OptionSpec<Void> version;
        private final OptionSpec<Void> verbose;

        private StandardOptionsSpec(OptionParser p) {
            this.help = p
                    .acceptsAll(asList("?", "help"), Bundle.standardOptions_help())
                    .forHelp();
            this.version = p
                    .accepts("version", Bundle.standardOptions_version());
            this.verbose = p
                    .acceptsAll(asList("v", "verbose"), Bundle.standardOptions_verbose());
        }

        @Override
        public StandardOptions value(OptionSet o) {
            return new StandardOptions(o.has(help), o.has(version), o.has(verbose));
        }
    }

    @NbBundle.Messages({
        "inputOptions.file=Input file",
        "inputOptions.mediaType=Input media type"
    })
    static final class InputOptionsSpec implements ComposedOptionSpec<InputOptions> {

        private final OptionSpec<File> file;
        private final OptionSpec<String> mediaType;

        private InputOptionsSpec(OptionParser p) {
            this.file = p
                    .acceptsAll(asList("i", "input"), Bundle.inputOptions_file())
                    .withRequiredArg()
                    .ofType(File.class)
                    .describedAs("file");
            this.mediaType = p
                    .acceptsAll(asList("it", "input-type"), Bundle.inputOptions_mediaType())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("mediaType");
        }

        @Override
        public InputOptions value(OptionSet o) {
            return InputOptions.create(optional(o, file), optional(o, mediaType));
        }
    }

    @NbBundle.Messages({
        "outputOptions.file=Output file",
        "outputOptions.mediaType=Output media type",
        "outputOptions.formatting=Formatted output"
    })
    static final class OutputOptionsSpec implements ComposedOptionSpec<OutputOptions> {

        private final OptionSpec<File> file;
        private final OptionSpec<String> mediaType;
        private final OptionSpec<Void> formatting;

        private OutputOptionsSpec(OptionParser p) {
            this.file = p
                    .acceptsAll(asList("o", "output"), Bundle.outputOptions_file())
                    .withRequiredArg()
                    .ofType(File.class)
                    .describedAs("file");
            this.mediaType = p
                    .acceptsAll(asList("ot", "output-type"), Bundle.outputOptions_mediaType())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("mediaType");
            this.formatting = p
                    .acceptsAll(asList("f", "format"), Bundle.outputOptions_formatting());
        }

        @Override
        public OutputOptions value(OptionSet o) {
            return OutputOptions.create(optional(o, file), optional(o, mediaType), o.has(formatting));
        }
    }
    //</editor-fold>
}
