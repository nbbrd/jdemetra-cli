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

import com.google.common.base.StandardSystemProperty;
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
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class ProviderTool {

    @Nonnull
    public static ProviderTool of(@Nonnull Iterable<? extends ITsProvider> providers) {
        return new ProviderTool(providers);
    }

    @Nonnull
    public static ProviderTool of(@Nonnull ITsProvider... providers) {
        return new ProviderTool(Arrays.asList(providers));
    }

    private final Iterable<? extends ITsProvider> providers;
    private File[] paths;
    private TsInformationType scope;

    private ProviderTool(Iterable<? extends ITsProvider> providers) {
        this.providers = providers;
        this.paths = new File[0];
        this.scope = TsInformationType.All;
    }

    public ProviderTool withWorkingDir() {
        paths = new File[]{new File(StandardSystemProperty.USER_DIR.value())};
        return this;
    }

    public ProviderTool withScope(@Nonnull TsInformationType scope) {
        this.scope = scope;
        return this;
    }

    @Nonnull
    public TsCollectionInformation get(@Nonnull URI uri) throws IOException {
        Optional<DataSource> dataSource = DataSource.uriParser().parseValue(uri.toString());
        if (dataSource.isPresent()) {
            return get(dataSource.get());
        }
        Optional<DataSet> dataSet = DataSet.uriParser().parseValue(uri.toString());
        if (dataSet.isPresent()) {
            return get(dataSet.get());
        }
        throw new IOException("Invalid URI '" + uri + "'");
    }

    @Nonnull
    public TsCollectionInformation get(@Nonnull String provider, @Nonnull Object bean) throws IOException {
        IDataSourceLoader p = lookup(IDataSourceLoader.class, provider);
        return loadTsCollection(p, p.encodeBean(bean));
    }

    @Nonnull
    public TsCollectionInformation get(@Nonnull DataSource dataSource) throws IOException {
        IDataSourceProvider p = lookup(IDataSourceProvider.class, dataSource.getProviderName());
        return loadTsCollection(p, dataSource);
    }

    @Nonnull
    public TsCollectionInformation get(@Nonnull DataSet dataSet) throws IOException {
        IDataSourceProvider p = lookup(IDataSourceProvider.class, dataSet.getDataSource().getProviderName());
        switch (dataSet.getKind()) {
            case COLLECTION:
                return loadTsCollection(p, dataSet);
            case DUMMY:
                return new TsCollectionInformation();
            case SERIES:
                TsCollectionInformation result = new TsCollectionInformation();
                result.items.add(loadTs(p, dataSet));
                return result;
            default:
                throw new RuntimeException();
        }
    }

    private <X extends IDataSourceProvider> X lookup(Class<X> type, String name) throws IOException {
        for (ITsProvider o : providers) {
            if (type.isInstance(o) && o.getSource().equals(name)) {
                return type.cast(o);
            }
        }
        throw new IOException("Cannot find provider '" + name + "' of type '" + type + "'");
    }

    private boolean withPaths(IDataSourceProvider p, Predicate<IDataSourceProvider> filler) {
        if (p instanceof IFileLoader) {
            IFileLoader l = (IFileLoader) p;
            File[] saved = l.getPaths();
            l.setPaths(paths);
            try {
                return filler.test(p);
            } finally {
                l.setPaths(saved);
            }
        } else {
            return filler.test(p);
        }
    }

    private TsCollectionInformation loadTsCollection(IDataSourceProvider p, DataSource dataSource) throws IOException {
        TsCollectionInformation result = new TsCollectionInformation(p.toMoniker(dataSource), scope);
        if (!withPaths(p, o -> o.get(result))) {
            throw new IOException("Failed to get data", new RuntimeException(result.invalidDataCause));
        }
        return result;
    }

    private TsCollectionInformation loadTsCollection(IDataSourceProvider p, DataSet dataSet) throws IOException {
        TsCollectionInformation result = new TsCollectionInformation(p.toMoniker(dataSet), scope);
        if (!withPaths(p, o -> o.get(result))) {
            throw new IOException("Failed to get data", new RuntimeException(result.invalidDataCause));
        }
        return result;
    }

    private TsInformation loadTs(IDataSourceProvider p, DataSet dataSet) throws IOException {
        TsInformation result = new TsInformation("", p.toMoniker(dataSet), scope);
        if (!withPaths(p, o -> o.get(result))) {
            throw new IOException("Failed to get data", new RuntimeException(result.invalidDataCause));
        }
        return result;
    }
}
