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

import ec.tss.ITsProvider;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.IDataSourceLoader;
import ec.tss.tsproviders.IFileLoader;
import ec.tstoolkit.design.ServiceDefinition;
import java.net.URI;
import javax.annotation.Nonnull;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface ProviderTool {

    @Nonnull
    TsCollectionInformation getTsCollection(@Nonnull Iterable<? extends ITsProvider> providers, @Nonnull URI uri, @Nonnull TsInformationType scope);

    @Nonnull
    TsCollectionInformation getTsCollection(@Nonnull IDataSourceLoader loader, @Nonnull Object bean, @Nonnull TsInformationType scope);

    void applyWorkingDir(@Nonnull IFileLoader provider);

    @Nonnull
    public static ProviderTool getDefault() {
        return Lookup.getDefault().lookup(ProviderTool.class);
    }
}
