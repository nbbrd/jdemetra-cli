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
public final class Ts2SpreadSheet {

    @CommandRegistration
    static Command CMD = OptionsParsingCommand.<Options>builder()
            .name("ts2spreadsheet")
            .parser(Parser::new)
            .executor(Executor::new)
            .so(o -> o.so)
            .build();

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
        public void exec(Options params) throws Exception {
            TsCollectionInformation info = XmlUtil.readValue(params.input, XmlTsCollection.class);
            Optional<Book.Factory> factory = getFactory(params.outputFile);
            if (factory.isPresent()) {
                store(factory.get(), info, params.outputFile, params.exportOptions);
            } else {
                throw new IllegalArgumentException("Cannot handle file '" + params.outputFile.toString() + "'");
            }
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
            Options result = new Options();
            result.input = input.value(o);
            result.outputFile = nonOptionFile;
            result.exportOptions = exportOptions.value(o);
            result.so = so.value(o);
            return result;
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
