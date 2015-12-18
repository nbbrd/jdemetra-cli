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
package be.nbb.cli.util.jaxb;

import be.nbb.cli.util.Serializer;
import be.nbb.cli.util.SerializerFactory;
import com.google.common.net.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = SerializerFactory.class)
public final class XmlSerializerFactory implements SerializerFactory {

    @Override
    public boolean canHandle(MediaType mediaType, Class<?> type) {
        return MediaType.XML_UTF_8.is(mediaType);
    }

    @Override
    public <X> Serializer<X> create(Class<X> type, boolean formattedOutput) {
        try {
            JAXBContext context = JAXBContext.newInstance(type);
            return new JaxbSerializer(context, formattedOutput);
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class JaxbSerializer<X> implements Serializer<X> {

        private final JAXBContext context;
        private final boolean formattedOutput;

        public JaxbSerializer(JAXBContext context, boolean formattedOutput) {
            this.context = context;
            this.formattedOutput = formattedOutput;
        }

        private Marshaller getMarshaller() throws JAXBException {
            Marshaller result = context.createMarshaller();
            result.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formattedOutput);
            return result;
        }

        private Unmarshaller getUnmarshaller() throws JAXBException {
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
    }
    //</editor-fold>
}
