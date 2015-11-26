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
package demetra.cli.helpers;

import java.io.File;
import static java.util.Arrays.asList;
import java.util.Optional;
import javax.annotation.Nonnull;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface OptionsSpec<T> {

    T value(@Nonnull OptionSet options);

    @Nonnull
    public static OptionsSpec<StandardOptions> newStandardOptionsSpec(@Nonnull OptionParser parser) {
        return new MegaOptionSpecImpl(parser);
    }

    @Nonnull
    public static OptionsSpec<InputOptions> newInputOptionsSpec(@Nonnull OptionParser parser) {
        return new InputParser(parser);
    }

    @Nonnull
    public static OptionsSpec<OutputOptions> newOutputOptionsSpec(@Nonnull OptionParser parser) {
        return new OutputParser(parser);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static <X> Optional<X> optional(OptionSet options, OptionSpec<X> spec) {
        return Optional.ofNullable(options.has(spec) ? spec.value(options) : null);
    }

    static final class MegaOptionSpecImpl implements OptionsSpec<StandardOptions> {

        private final OptionSpec<Void> help;
        private final OptionSpec<Void> version;
        private final OptionSpec<Void> verbose;

        public MegaOptionSpecImpl(OptionParser parser) {
            this.help = parser
                    .acceptsAll(asList("?", "help"), "show help")
                    .forHelp();
            this.version = parser
                    .accepts("version", "show version");
            this.verbose = parser
                    .acceptsAll(asList("v", "verbose"), "Verbose mode");
        }

        @Override
        public StandardOptions value(OptionSet options) {
            return new StandardOptions(options.has(help), options.has(version), options.has(verbose));
        }
    }

    static final class InputParser implements OptionsSpec<InputOptions> {

        private final OptionSpec<File> file;
        private final OptionSpec<String> mediaType;

        public InputParser(OptionParser parser) {
            this.file = parser
                    .acceptsAll(asList("i", "input"), "Input file")
                    .withRequiredArg()
                    .ofType(File.class);
            this.mediaType = parser
                    .acceptsAll(asList("it", "input-type"), "Input media type")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("Media type");
        }

        @Override
        public InputOptions value(OptionSet options) {
            return InputOptions.create(optional(options, file), optional(options, mediaType));
        }
    }

    static final class OutputParser implements OptionsSpec<OutputOptions> {

        private final OptionSpec<File> file;
        private final OptionSpec<String> mediaType;
        private final OptionSpec<Void> formatting;

        public OutputParser(OptionParser parser) {
            this.file = parser
                    .acceptsAll(asList("o", "output"), "Output file")
                    .withRequiredArg()
                    .ofType(File.class);
            this.mediaType = parser
                    .acceptsAll(asList("ot", "output-type"), "Output media type")
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("Media type");
            this.formatting = parser
                    .acceptsAll(asList("f", "format"), "Formatted output");
        }

        @Override
        public OutputOptions value(OptionSet options) {
            return OutputOptions.create(optional(options, file), optional(options, mediaType), options.has(formatting));
        }
    }
    //</editor-fold>
}
