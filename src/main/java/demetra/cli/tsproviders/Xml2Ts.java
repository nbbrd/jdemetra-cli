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
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.BasicCliLauncher;
import demetra.cli.helpers.BasicCommand;
import static demetra.cli.helpers.ComposedOptionSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.ComposedOptionSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.common.xml.XmlBean;
import ec.tss.tsproviders.common.xml.XmlProvider;
import ec.tss.xml.XmlTsCollection;
import java.io.File;
import java.nio.charset.Charset;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import demetra.cli.helpers.ComposedOptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Xml2Ts implements BasicCommand<Xml2Ts.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Xml2Ts::new, o -> o.so);
    }

    public static final class Parameters {

        StandardOptions so;
        public XmlBean xml;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        XmlProvider provider = new XmlProvider();
        XProviders.applyWorkingDir(provider);
        TsCollectionInformation result = XProviders.getTsCollection(provider, params.xml, TsInformationType.All);
        params.output.writeValue(XmlTsCollection.class, result);
        provider.dispose();
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<XmlBean> xml = new XmlOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
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
