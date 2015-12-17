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
package be.nbb.cli.util;

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
import javax.annotation.Nonnull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface BasicSerializer<T> {

    void serialize(@Nonnull T value, @Nonnull File output) throws IOException;

    void serialize(@Nonnull T value, @Nonnull OutputStream output) throws IOException;

    @Nonnull
    T deserialize(@Nonnull File input) throws IOException;

    @Nonnull
    T deserialize(@Nonnull InputStream input) throws IOException;

    @Nonnull
    public static <X> BasicSerializer<X> of(@Nonnull MediaType type, @Nonnull Class<X> x, boolean formattedOutput) {
        if (MediaType.XML_UTF_8.equals(type)) {
            return jaxb(x, formattedOutput);
        }
        if (MediaType.JSON_UTF_8.equals(type)) {
            return jackson(x, formattedOutput);
        }
        throw new RuntimeException("Don't know how to handle " + type);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    static <X> BasicSerializer<X> jackson(final Class<X> x, final boolean formattedOutput) {
        return new BasicSerializer<X>() {

            private ObjectWriter getWriter() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
                return formattedOutput ? mapper.writerWithDefaultPrettyPrinter() : mapper.writer();
            }

            private ObjectReader getReader() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector(TypeFactory.defaultInstance()));
                return mapper.reader(x);
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
        };
    }

    static <X> BasicSerializer<X> jaxb(final Class<X> x, final boolean formattedOutput) {
        return new BasicSerializer<X>() {
            private Marshaller getMarshaller() throws JAXBException {
                JAXBContext context = JAXBContext.newInstance(x);
                Marshaller result = context.createMarshaller();
                result.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
                return result;
            }

            private Unmarshaller getUnmarshaller() throws JAXBException {
                JAXBContext context = JAXBContext.newInstance(x);
                return context.createUnmarshaller();
            }

            @Override
            public void serialize(X value, File output) throws IOException {
                try {
                    getMarshaller().marshal(value, output);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }

            @Override
            public void serialize(X value, OutputStream output) throws IOException {
                try {
                    getMarshaller().marshal(value, output);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }

            @Override
            public X deserialize(File input) throws IOException {
                try {
                    return (X) getUnmarshaller().unmarshal(input);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }

            @Override
            public X deserialize(InputStream input) throws IOException {
                try {
                    return (X) getUnmarshaller().unmarshal(input);
                } catch (JAXBException ex) {
                    throw new IOException(ex);
                }
            }
        };
    }

    static final class NonCloseableOutputStream extends OutputStream {

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
