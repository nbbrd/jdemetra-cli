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
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.net.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SerializerFactory.class)
public final class JsonSerializerFactory implements SerializerFactory {

    @Override
    public boolean canHandle(MediaType mediaType, Class<?> type) {
        return mediaType.is(MediaType.JSON_UTF_8);
    }

    @Override
    public <X> Serializer<X> create(Class<X> type, boolean formattedOutput) {
        return new JacksonSerializer(type, formattedOutput);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class JacksonSerializer<X> implements Serializer<X> {

        private final Class<X> type;
        private final boolean formattedOutput;

        public JacksonSerializer(Class<X> type, boolean formattedOutput) {
            this.type = type;
            this.formattedOutput = formattedOutput;
        }

        private ObjectWriter getWriter() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
            return formattedOutput ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
        }

        private ObjectReader getReader() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
            return mapper.reader(type);
        }

        @Override
        public void serialize(X value, File output) throws IOException {
            getWriter().writeValue(output, value);
        }

        @Override
        public void serialize(X value, OutputStream output) throws IOException {
            getWriter().writeValue(new NonCloseableOutputStream(output), value);
        }

        @Override
        public X deserialize(File input) throws IOException {
            return (X) getReader().readValue(input);
        }

        @Override
        public X deserialize(InputStream input) throws IOException {
            return (X) getReader().readValue(input);
        }
    }

    private static final class NonCloseableOutputStream extends OutputStream {

        private final OutputStream delegate;

        public NonCloseableOutputStream(OutputStream stream) {
            this.delegate = stream;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            // do not close delegate
        }
    }
    //</editor-fold>
}
