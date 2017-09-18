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
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import be.nbb.demetra.toolset.ProviderTool;
import static demetra.cli.helpers.Categories.IO_CATEGORY;
import demetra.cli.helpers.XmlUtil;
import demetra.cli.tsproviders.TsDataBuild;
import demetra.cli.tsproviders.TsProviderOptionSpecs;
import ec.tss.TsCollectionInformation;
import ec.tss.tsproviders.spreadsheet.SpreadSheetBean;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProvider;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import java.io.File;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 * Retrieves time series from a spreadsheet file.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class SpreadSheet2Ts {

    @CommandRegistration(name = "spreadsheet2ts", category = IO_CATEGORY, description = "Retrieve time series from a spreadsheet file")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static final class Options {

        StandardOptions so;
        public SpreadSheetBean input;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        @Override
        public void exec(Options o) throws Exception {
            try (SpreadSheetProvider p = new SpreadSheetProvider()) {
                TsCollectionInformation result = ProviderTool.of(p).withWorkingDir().get(p.getSource(), o.input);
                XmlUtil.writeValue(o.output, XmlTsCollection.class, result);
            }
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<SpreadSheetBean> input = new SpreadSheetOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            return new Options(so.value(o), input.value(o), output.value(o));
        }
    }

    @NbBundle.Messages({
        "spreadsheet2ts.clean=Cleans the missing values"
    })
    private static final class SpreadSheetOptionsSpec implements ComposedOptionSpec<SpreadSheetBean> {

        // source
        private final ComposedOptionSpec<File> file;
        // options
        private final ComposedOptionSpec<DataFormat> dataFormat;
        private final ComposedOptionSpec<TsDataBuild> tsDataBuild;
        private final OptionSpec<Boolean> clean;

        public SpreadSheetOptionsSpec(OptionParser p) {
            this.file = TsProviderOptionSpecs.newInputFileSpec(p);
            this.dataFormat = TsProviderOptionSpecs.newDataFormatSpec(p);
            this.tsDataBuild = TsProviderOptionSpecs.newTsDataBuildSpec(p);
            this.clean = p
                    .accepts("clean", Bundle.spreadsheet2ts_clean())
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(Boolean.TRUE);
        }

        @Override
        public SpreadSheetBean value(OptionSet o) {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(file.value(o));
            result.setDataFormat(dataFormat.value(o));
            TsDataBuild tmp = tsDataBuild.value(o);
            result.setFrequency(tmp.getFrequency());
            result.setAggregationType(tmp.getAggregationType());
            result.setCleanMissing(clean.value(o));
            return result;
        }
    }
}
