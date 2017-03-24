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
package be.nbb.demetra.toolset;

import com.google.common.base.Optional;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.FluentIterable;
import ec.tss.ITsProvider;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceLoader;
import ec.tss.tsproviders.IDataSourceProvider;
import ec.tss.tsproviders.IFileLoader;
import ec.tstoolkit.design.VisibleForTesting;
import java.io.File;
import java.net.URI;

/**
 *
 * @author Philippe Charles
 */
@VisibleForTesting
final class ProviderToolImpl implements ProviderTool {

    @Override
    public TsCollectionInformation getTsCollection(Iterable<? extends ITsProvider> providers, URI uri, TsInformationType scope) {
        Optional<DataSource> dataSource = DataSource.uriParser().tryParse(uri.toString());
        if (dataSource.isPresent()) {
            Optional<IDataSourceProvider> provider = lookup(providers, IDataSourceProvider.class, dataSource.get().getProviderName());
            if (provider.isPresent()) {
                return loadTsCollection(provider.get(), dataSource.get(), scope);
            }
            throw new IllegalArgumentException(dataSource.get().getProviderName());
        }
        Optional<DataSet> dataSet = DataSet.uriParser().tryParse(uri.toString());
        if (dataSet.isPresent()) {
            Optional<IDataSourceProvider> provider = lookup(providers, IDataSourceProvider.class, dataSet.get().getDataSource().getProviderName());
            if (provider.isPresent()) {
                switch (dataSet.get().getKind()) {
                    case COLLECTION:
                        return loadTsCollection(provider.get(), dataSet.get(), scope);
                    case DUMMY:
                        return new TsCollectionInformation();
                    case SERIES:
                        TsCollectionInformation result = new TsCollectionInformation();
                        result.items.add(loadTs(provider.get(), dataSet.get(), scope));
                        return result;
                }
            }
            throw new IllegalArgumentException(dataSet.get().getDataSource().getProviderName());
        }
        throw new IllegalArgumentException(uri.toString());
    }

    @Override
    public TsCollectionInformation getTsCollection(IDataSourceLoader loader, Object bean, TsInformationType scope) {
        return loadTsCollection((IDataSourceProvider) loader, loader.encodeBean(bean), scope);
    }

    @Override
    public void applyWorkingDir(IFileLoader provider) {
        provider.setPaths(new File[]{new File(StandardSystemProperty.USER_DIR.value())});
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static TsCollectionInformation loadTsCollection(IDataSourceProvider provider, DataSource dataSource, TsInformationType scope) {
        TsCollectionInformation result = new TsCollectionInformation();
        result.type = scope;
        result.moniker = provider.toMoniker(dataSource);
        if (!provider.get(result)) {
            throw new RuntimeException(result.invalidDataCause);
        }
        return result;
    }

    private static TsCollectionInformation loadTsCollection(IDataSourceProvider provider, DataSet dataSet, TsInformationType scope) {
        TsCollectionInformation result = new TsCollectionInformation();
        result.type = scope;
        result.moniker = provider.toMoniker(dataSet);
        if (!provider.get(result)) {
            throw new RuntimeException(result.invalidDataCause);
        }
        return result;
    }

    private static TsInformation loadTs(IDataSourceProvider provider, DataSet dataSet, TsInformationType scope) {
        TsInformation result = new TsInformation();
        result.type = scope;
        result.moniker = provider.toMoniker(dataSet);
        if (!provider.get(result)) {
            throw new RuntimeException(result.invalidDataCause);
        }
        return result;
    }

    private static <X extends IDataSourceProvider> Optional<X> lookup(Iterable<? extends ITsProvider> providers, Class<X> providerClass, final String providerName) {
        return FluentIterable.from(providers)
                .filter(providerClass)
                .firstMatch((X input) -> input.getSource().equals(providerName));
    }
    //</editor-fold>
}
