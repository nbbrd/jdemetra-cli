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
package demetra.cli.tsproviders;

import com.google.common.annotations.VisibleForTesting;
import be.nbb.cli.util.BasicArgsParser;
import be.nbb.cli.util.BasicCliLauncher;
import static be.nbb.cli.util.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.util.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.spreadsheet.SpreadSheetBean;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProvider;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.xml.XmlTsCollection;
import java.io.File;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import be.nbb.cli.util.BasicCommand;
import be.nbb.cli.util.ComposedOptionSpec;
import demetra.cli.helpers.XmlUtil;
import org.openide.util.NbBundle;

/**
 * Retrieves time series from a spreadsheet file.
 *
 * @author Philippe Charles
 */
public final class SpreadSheet2Ts implements BasicCommand<SpreadSheet2Ts.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, SpreadSheet2Ts::new, o -> o.so);
    }

    public static final class Parameters {

        StandardOptions so;
        public SpreadSheetBean input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        SpreadSheetProvider provider = new SpreadSheetProvider();
        ProviderTool.getDefault().applyWorkingDir(provider);
        TsCollectionInformation result = ProviderTool.getDefault().getTsCollection(provider, params.input, TsInformationType.All);
        XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
        provider.dispose();
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<SpreadSheetBean> input = new SpreadSheetOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
            result.input = input.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
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
