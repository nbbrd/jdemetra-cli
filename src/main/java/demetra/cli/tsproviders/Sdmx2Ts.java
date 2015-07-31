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
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.sdmx.SdmxBean;
import ec.tss.tsproviders.sdmx.SdmxProvider;
import ec.tss.xml.XmlTsCollection;
import java.io.File;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Sdmx2Ts extends StandardApp<Sdmx2Ts.Parameters> {

    public static void main(String[] args) {
        new Sdmx2Ts().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public SdmxBean sdmx;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        SdmxProvider provider = new SdmxProvider();
        provider.setCompactNaming(true);
        XProviders.applyWorkingDir(provider);
        TsCollectionInformation result = XProviders.getTsCollection(provider, params.sdmx, TsInformationType.All);
        params.output.writeValue(XmlTsCollection.class, result);
        provider.dispose();
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<SdmxBean> sdmx = new SdmxOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.sdmx = sdmx.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }

    private static final class SdmxOptionsSpec extends OptionsSpec<SdmxBean> {

        private final OptionSpec<File> file;
        private final OptionSpec<String> label;

        public SdmxOptionsSpec(OptionParser parser) {
            this.file = parser.nonOptions("Input file").ofType(File.class);
            this.label = parser.accepts("label").withRequiredArg().defaultsTo("");
        }

        @Override
        public SdmxBean value(OptionSet options) {
            SdmxBean input = new SdmxBean();
            input.setFile(options.has(file) ? file.value(options) : null);
            input.setTitleAttribute(label.value(options));
            return input;
        }
    }
}
