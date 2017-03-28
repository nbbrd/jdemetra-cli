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
import com.google.common.collect.ImmutableList;
import demetra.cli.helpers.XmlUtil;
import ec.tss.ITsProvider;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.IFileLoader;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import java.net.URI;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Retrieves time series from an URI.
 *
 * @author Philippe Charles
 */
public final class Uri2Ts {

    @CommandRegistration
    static Command CMD = OptionsParsingCommand.<Options>builder()
            .name("uri2ts")
            .parser(Parser::new)
            .executor(Executor::new)
            .so(o -> o.so)
            .build();

    public static final class Options {

        StandardOptions so;
        public URI uri;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final ProviderTool tool = ProviderTool.getDefault();
        final Supplier<Iterable<ITsProvider>> providers = () -> ServiceLoader.load(ITsProvider.class);

        @Override
        public void exec(Options params) throws Exception {
            List<ITsProvider> providerList = ImmutableList.copyOf(providers.get());
            providerList.stream()
                    .filter(IFileLoader.class::isInstance)
                    .forEach(o -> tool.applyWorkingDir((IFileLoader) o));
            TsCollectionInformation result = tool.getTsCollection(providerList, params.uri, TsInformationType.All);
            XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionSpec<URI> uri = parser.nonOptions("uri").ofType(URI.class);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            Options result = new Options();
            result.uri = o.has(uri) ? uri.value(o) : null;
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }
}
