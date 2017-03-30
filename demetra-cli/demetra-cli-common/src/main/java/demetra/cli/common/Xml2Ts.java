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
import demetra.cli.tsproviders.TsProviderOptionSpecs;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.common.xml.XmlBean;
import ec.tss.tsproviders.common.xml.XmlProvider;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import java.io.File;
import java.nio.charset.Charset;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Xml2Ts {

    @CommandRegistration(name = "xml2ts", category = IO_CATEGORY, description = "Retrieve time series from an XML file")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    public static final class Options {

        StandardOptions so;
        public XmlBean xml;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final ProviderTool tool = ProviderTool.getDefault();

        @Override
        public void exec(Options params) throws Exception {
            try (XmlProvider p = new XmlProvider()) {
                tool.applyWorkingDir(p);
                TsCollectionInformation result = tool.getTsCollection(p, params.xml, TsInformationType.All);
                XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
            }
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<XmlBean> xml = new XmlOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet options) {
            Options result = new Options();
            result.xml = xml.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }

    private static final class XmlOptionsSpec implements ComposedOptionSpec<XmlBean> {

        private final ComposedOptionSpec<File> file;
        private final OptionSpec<String> charset;

        public XmlOptionsSpec(OptionParser p) {
            this.file = TsProviderOptionSpecs.newInputFileSpec(p);
            this.charset = p
                    .accepts("charset")
                    .withRequiredArg()
                    .defaultsTo("");
        }

        @Override
        public XmlBean value(OptionSet o) {
            XmlBean input = new XmlBean();
            input.setFile(file.value(o));
            input.setCharset(o.has(charset) ? Charset.forName(charset.value(o)) : null);
            return input;
        }
    }
}
