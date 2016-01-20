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
package be.nbb.cli.util.jackson;

import be.nbb.cli.util.Serializer;
import be.nbb.cli.util.SerializerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.net.MediaType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SerializerFactory.class)
public final class JsonSerializerFactory implements SerializerFactory {

    private final boolean available = JacksonModule.jackson_core.isAvailable()
            && JacksonModule.jackson_databind.isAvailable()
            && JacksonModule.jackson_datatype_jdk8.isAvailable()
            && JacksonModule.jackson_module_jaxb_annotations.isAvailable();

    @Override
    public boolean canHandle(MediaType mediaType, Class<?> type) {
        return available && MediaType.JSON_UTF_8.is(mediaType);
    }

    @Override
    public <X> Serializer<X> create(Class<X> type, boolean formattedOutput) {
        return new JacksonSerializer(Holder.newMapper(), type, formattedOutput);
    }

    // prevents ClassNotFoundException
    private static final class Holder {

        private static ObjectMapper newMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new Jdk8Module());
            mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
            return mapper;
        }
    }
}
