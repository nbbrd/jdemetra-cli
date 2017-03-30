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
package demetra.cli.common;

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
import ec.tss.TsInformationType;
import ec.tss.tsproviders.common.txt.TxtBean;
import ec.tss.tsproviders.common.txt.TxtBean.Delimiter;
import ec.tss.tsproviders.common.txt.TxtBean.TextQualifier;
import ec.tss.tsproviders.common.txt.TxtProvider;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import java.io.File;
import java.nio.charset.Charset;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 * Retrieves time series from a text file such as CSV or TSV.
 *
 * @author Philippe Charles
 */
public final class Txt2Ts {

    @CommandRegistration(name = "txt2ts", category = IO_CATEGORY, description = "Retrieve time series from a txt file")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    public static final class Options {

        StandardOptions so;
        public TxtBean input;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final ProviderTool tool = ProviderTool.getDefault();

        @Override
        public void exec(Options params) throws Exception {
            try (TxtProvider p = new TxtProvider()) {
                tool.applyWorkingDir(p);
                TsCollectionInformation result = tool.getTsCollection(p, params.input, TsInformationType.All);
                XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
            }
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<TxtBean> input = new TxtOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            Options result = new Options();
            result.input = input.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    @NbBundle.Messages({
        "txt2ts.charset=Charset used to decode the file",
        "txt2ts.delimiter=Delimiter used to separate entries",
        "txt2ts.headers=Use first line as headers",
        "txt2ts.skipLines=Number of lines to skip",
        "txt2ts.textQualifier=Character used to quote elements",
        "txt2ts.cleanMissing=Clean missing"
    })
    private static final class TxtOptionsSpec implements ComposedOptionSpec<TxtBean> {

        // source
        private final ComposedOptionSpec<File> file;
        private final OptionSpec<String> charset;
        private final OptionSpec<Delimiter> delimiter;
        private final OptionSpec<TextQualifier> textQualifier;
        private final OptionSpec<Integer> skipLines;
        // content
        private final OptionSpec<Boolean> headers;
        private final ComposedOptionSpec<DataFormat> dataFormat;
        private final ComposedOptionSpec<TsDataBuild> tsDataBuild;
        private final OptionSpec<Boolean> cleanMissing;

        public TxtOptionsSpec(OptionParser p) {
            this.file = TsProviderOptionSpecs.newInputFileSpec(p);
            this.charset = p
                    .accepts("charset", Bundle.txt2ts_charset())
                    .withRequiredArg()
                    .ofType(String.class)
                    .defaultsTo(Charset.defaultCharset().name());
            this.delimiter = p
                    .accepts("delimiter", Bundle.txt2ts_delimiter())
                    .withRequiredArg()
                    .ofType(Delimiter.class)
                    .defaultsTo(Delimiter.TAB);
            this.textQualifier = p
                    .accepts("qualifier", Bundle.txt2ts_textQualifier())
                    .withRequiredArg()
                    .ofType(TextQualifier.class)
                    .defaultsTo(TextQualifier.NONE);
            this.skipLines = p
                    .accepts("skip", Bundle.txt2ts_skipLines())
                    .withRequiredArg()
                    .ofType(Integer.class)
                    .defaultsTo(0);
            this.headers = p
                    .accepts("headers", Bundle.txt2ts_headers())
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(true);
            this.dataFormat = TsProviderOptionSpecs.newDataFormatSpec(p);
            this.tsDataBuild = TsProviderOptionSpecs.newTsDataBuildSpec(p);
            this.cleanMissing = p
                    .accepts("clean", Bundle.txt2ts_cleanMissing())
                    .withRequiredArg()
                    .ofType(Boolean.class)
                    .defaultsTo(Boolean.TRUE);
        }

        @Override
        public TxtBean value(OptionSet o) {
            TxtBean result = new TxtBean();
            result.setFile(file.value(o));
            result.setCharset(Charset.forName(charset.value(o)));
            result.setDelimiter(delimiter.value(o));
            result.setTextQualifier(textQualifier.value(o));
            result.setSkipLines(skipLines.value(o));
            result.setHeaders(headers.value(o));
            result.setDataFormat(dataFormat.value(o));
            TsDataBuild tmp = tsDataBuild.value(o);
            result.setFrequency(tmp.getFrequency());
            result.setAggregationType(tmp.getAggregationType());
            result.setCleanMissing(cleanMissing.value(o));
            return result;
        }
    }
}
