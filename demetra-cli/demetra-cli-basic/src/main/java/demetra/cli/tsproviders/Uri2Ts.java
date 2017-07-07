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
import static demetra.cli.helpers.Categories.IO_CATEGORY;
import demetra.cli.helpers.XmlUtil;
import ec.tss.ITsProvider;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import java.net.URI;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Retrieves time series from an URI.
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public final class Uri2Ts {

    @CommandRegistration(name = "uri2ts", category = IO_CATEGORY, description = "Retrieve time series from an URI")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static final class Options {

        StandardOptions so;
        public URI uri;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final Supplier<Iterable<ITsProvider>> providers = () -> ServiceLoader.load(ITsProvider.class);

        @Override
        public void exec(Options o) throws Exception {
            TsCollectionInformation result = ProviderTool.of(providers.get()).withWorkingDir().get(o.uri);
            XmlUtil.writeValue(o.output, XmlTsCollection.class, result);
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionSpec<URI> uri = parser.nonOptions("uri").ofType(URI.class);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            return new Options(so.value(o), o.has(uri) ? uri.value(o) : null, output.value(o));
        }
    }
}
