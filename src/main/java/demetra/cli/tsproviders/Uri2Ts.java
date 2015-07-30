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
import com.google.common.collect.Iterables;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.ITsProvider;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.IFileLoader;
import ec.tss.xml.XmlTsCollection;
import java.net.URI;
import java.util.ServiceLoader;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Uri2Ts extends StandardApp<Uri2Ts.Parameters> {

    public static void main(String[] args) {
        new Uri2Ts().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public URI uri;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        Iterable<ITsProvider> providers = ServiceLoader.load(ITsProvider.class);
        for (IFileLoader o : Iterables.filter(providers, IFileLoader.class)) {
            XProviders.applyWorkingDir(o);
        }
        TsCollectionInformation result = XProviders.getTsCollection(providers, params.uri, TsInformationType.All);
        params.output.writeValue(XmlTsCollection.class, result);
        for (ITsProvider o : providers) {
            o.dispose();
        }
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionSpec<URI> uri = parser.nonOptions("uri").ofType(URI.class);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.uri = options.has(uri) ? uri.value(options) : null;
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }
}
