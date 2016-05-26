/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.cli.helpers;

import be.nbb.cli.util.Serializer;
import be.nbb.cli.util.SerializerFactory;
import be.nbb.cli.util.SerializerFactoryAlias;
import com.google.common.net.MediaType;
import static demetra.cli.helpers.DemetraMediaTypes.TS_COLLECTION_YAML;
import ec.tss.xml.XmlTsCollection;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SerializerFactory.class)
public final class TsCollectionYamlSerializerFactory implements SerializerFactory {

    private final SerializerFactoryAlias alias = SerializerFactoryAlias.of(XmlTsCollection.class, TS_COLLECTION_YAML, MediaType.parse("application/yaml"));

    @Override
    public boolean canHandle(MediaType mediaType, Class<?> type) {
        return alias.canHandle(mediaType, type, Lookup.getDefault().lookupAll(SerializerFactory.class));
    }

    @Override
    public <X> Serializer<X> create(Class<X> type, boolean formattedOutput) {
        return alias.create(type, formattedOutput, Lookup.getDefault().lookupAll(SerializerFactory.class));
    }
}
