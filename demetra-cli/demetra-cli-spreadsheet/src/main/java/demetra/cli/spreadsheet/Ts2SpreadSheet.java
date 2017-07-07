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
package demetra.cli.spreadsheet;

import be.nbb.cli.command.Command;
import be.nbb.cli.command.core.OptionsExecutor;
import be.nbb.cli.command.core.OptionsParsingCommand;
import be.nbb.cli.command.joptsimple.ComposedOptionSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.StandardOptions;
import com.google.common.collect.Streams;
import static demetra.cli.helpers.Categories.IO_CATEGORY;
import demetra.cli.helpers.XmlUtil;
import ec.tss.TsCollectionInformation;
import ec.tss.tsproviders.spreadsheet.engine.SpreadSheetFactory;
import ec.tss.tsproviders.spreadsheet.engine.TsExportOptions;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import ec.util.spreadsheet.Book;
import ec.util.spreadsheet.helpers.ArraySheet;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Converts time series to a spreadsheet file.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class Ts2SpreadSheet {

    @CommandRegistration(name = "ts2spreadsheet", category = IO_CATEGORY, description = "Generate a spreadsheet file from time series")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static final class Options {

        StandardOptions so;
        public InputOptions input;
        public File outputFile;
        public TsExportOptions exportOptions;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final SpreadSheetFactory sheetFactory = SpreadSheetFactory.getDefault();
        final Supplier<Iterable<Book.Factory>> factories = () -> ServiceLoader.load(Book.Factory.class);

        @Override
        public void exec(Options o) throws Exception {
            TsCollectionInformation info = XmlUtil.readValue(o.input, XmlTsCollection.class);
            Book.Factory factory = getFactory(o.outputFile)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot handle file '" + o.outputFile.toString() + "'"));
            store(factory, info, o.outputFile, o.exportOptions);
        }

        private Optional<Book.Factory> getFactory(File file) {
            return Streams.stream(factories.get())
                    .filter(o -> o.canStore() && o.accept(file))
                    .findFirst();
        }

        private void store(Book.Factory factory, TsCollectionInformation info, File outputFile, TsExportOptions exportOptions) throws IOException {
            ArraySheet sheet = sheetFactory.fromTsCollectionInfo(info, exportOptions);
            factory.store(outputFile, sheet.toBook());
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<InputOptions> input = newInputOptionsSpec(parser);
        private final OptionSpec<File> outputFile = parser.nonOptions("Output file").ofType(File.class);
        private final ComposedOptionSpec<TsExportOptions> exportOptions = new TsExportOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            File nonOptionFile = outputFile.value(o);
            if (nonOptionFile == null) {
                throw new IllegalArgumentException("Missing output file");
            }
            return new Options(so.value(o), input.value(o), nonOptionFile, exportOptions.value(o));
        }
    }

    private static final class TsExportOptionsSpec implements ComposedOptionSpec<TsExportOptions> {

        private final OptionSpec<Void> horizontal;
        private final OptionSpec<Void> hideDates;
        private final OptionSpec<Void> hideNames;
        private final OptionSpec<Void> endPeriod;

        public TsExportOptionsSpec(OptionParser p) {
            this.horizontal = p.accepts("horizontal");
            this.hideDates = p.accepts("hideDates");
            this.hideNames = p.accepts("hideNames");
            this.endPeriod = p.accepts("endPeriod");
        }

        @Override
        public TsExportOptions value(OptionSet o) {
            return TsExportOptions.create(!o.has(horizontal), !o.has(hideDates), !o.has(hideNames), !o.has(endPeriod));
        }
    }
}
