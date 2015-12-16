/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.cli.helpers;

import be.nbb.cli.util.ComposedOptionSpec;
import static be.nbb.cli.util.ComposedOptionSpec.optional;
import ec.tss.formatters.CsvInformationFormatter;
import ec.tstoolkit.information.InformationSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.Data;

/**
 *
 * @author Jean Palate
 */
@Data
public class CsvOutputOptions {

    static final class CsvOutputOptionsSpec implements ComposedOptionSpec<CsvOutputOptions> {

        private final OptionSpec<File> file;

        private CsvOutputOptionsSpec(OptionParser p) {
            this.file = p
                    .acceptsAll(asList("o", "output"), "Output file")
                    .withRequiredArg()
                    .ofType(File.class);
        }

        @Override
        public CsvOutputOptions value(OptionSet o) {
            return new CsvOutputOptions(optional(o, file));
        }
    }

    @Nonnull
    public static ComposedOptionSpec<CsvOutputOptions> newCsvOutputOptionsSpec(@Nonnull OptionParser parser) {
        return new CsvOutputOptionsSpec(parser);
    }

    private final Optional<File> file;

    @Nonnull
    public static CsvOutputOptions of(@Nonnull File file) {
        return new CsvOutputOptions(Optional.of(file));
    }

    public <X> void write(@Nonnull List<InformationSet> info, @Nonnull List<String> items, boolean fullname) throws IOException {
        try (Writer wr = writer()) {
            CsvInformationFormatter fmt = new CsvInformationFormatter();
            fmt.format(wr, info, items, fullname);
        }
    }

    private Writer writer() throws FileNotFoundException {
        if (getFile().isPresent()) {
            FileOutputStream matrix = new FileOutputStream(getFile().get());
            return new OutputStreamWriter(matrix, StandardCharsets.ISO_8859_1);
        } else {
            return new OutputStreamWriter(System.out, StandardCharsets.ISO_8859_1);
        }
    }
}
