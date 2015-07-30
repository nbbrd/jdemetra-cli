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

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
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
import java.io.File;
import java.net.URI;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Philippe Charles
 */
@UtilityClass
public class XProviders {

    public static TsCollectionInformation getTsCollection(Iterable<ITsProvider> providers, URI uri, TsInformationType scope) {
        Optional<DataSource> dataSource = DataSource.uriParser().tryParse(uri.toString());
        if (dataSource.isPresent()) {
            Optional<IDataSourceProvider> provider = lookup(providers, IDataSourceProvider.class, dataSource.get().getProviderName());
            if (provider.isPresent()) {
                return getTsCollection(provider.get(), dataSource.get(), scope);
            }
            throw new IllegalArgumentException(dataSource.get().getProviderName());
        }
        Optional<DataSet> dataSet = DataSet.uriParser().tryParse(uri.toString());
        if (dataSet.isPresent()) {
            Optional<IDataSourceProvider> provider = lookup(providers, IDataSourceProvider.class, dataSet.get().getDataSource().getProviderName());
            if (provider.isPresent()) {
                switch (dataSet.get().getKind()) {
                    case COLLECTION:
                        return getTsCollection(provider.get(), dataSet.get(), scope);
                    case DUMMY:
                        return new TsCollectionInformation();
                    case SERIES:
                        TsCollectionInformation result = new TsCollectionInformation();
                        result.items.add(getTs(provider.get(), dataSet.get(), scope));
                        return result;
                }
            }
            throw new IllegalArgumentException(dataSet.get().getDataSource().getProviderName());
        }
        throw new IllegalArgumentException(uri.toString());
    }

    public static TsCollectionInformation getTsCollection(IDataSourceLoader loader, Object bean, TsInformationType scope) {
        return getTsCollection((IDataSourceProvider) loader, loader.encodeBean(bean), scope);
    }

    public static void applyWorkingDir(IFileLoader provider) {
        provider.setPaths(new File[]{new File(StandardSystemProperty.USER_DIR.value())});
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static TsCollectionInformation getTsCollection(IDataSourceProvider provider, DataSource dataSource, TsInformationType scope) {
        TsCollectionInformation result = new TsCollectionInformation();
        result.type = scope;
        result.moniker = provider.toMoniker(dataSource);
        provider.get(result);
        return result;
    }

    private static TsCollectionInformation getTsCollection(IDataSourceProvider provider, DataSet dataSet, TsInformationType scope) {
        TsCollectionInformation result = new TsCollectionInformation();
        result.type = scope;
        result.moniker = provider.toMoniker(dataSet);
        provider.get(result);
        return result;
    }

    private static TsInformation getTs(IDataSourceProvider provider, DataSet dataSet, TsInformationType scope) {
        TsInformation result = new TsInformation();
        result.type = scope;
        result.moniker = provider.toMoniker(dataSet);
        provider.get(result);
        return result;
    }

    private static <X extends IDataSourceProvider> Optional<X> lookup(Iterable<ITsProvider> providers, Class<X> providerClass, final String providerName) {
        return FluentIterable.from(providers)
                .filter(providerClass)
                .firstMatch(new Predicate<X>() {
                    @Override
                    public boolean apply(X input) {
                        return input.getSource().equals(providerName);
                    }
                });
    }
    //</editor-fold>
}
