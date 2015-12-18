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

import com.google.common.net.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;

/**
 *
 * @author Philippe Charles
 */
@Data
@XmlJavaTypeAdapter(InputOptions.XmlAdapter.class)
public final class InputOptions {

    private final Optional<File> file;
    private final MediaType mediaType;

    @Nonnull
    public static InputOptions of(@Nonnull File file, @Nonnull MediaType mediaType) {
        return new InputOptions(Optional.of(file), mediaType);
    }

    @Nonnull
    public static InputOptions create(Optional<File> file, Optional<String> mediaType) {
        return new InputOptions(file, Utils.getMediaType(mediaType, file).orElse(MediaType.XML_UTF_8));
    }

    @Nonnull
    public <X> X read(@Nonnull Class<X> clazz) throws IOException {
        Serializer<X> serializer = SerializerFactory.of(mediaType, clazz, false);
        if (getFile().isPresent()) {
            return serializer.deserialize(getFile().get());
        } else {
            return serializer.deserialize(System.in);
        }
    }

    @XmlRootElement
    public static final class XmlBean {

        @XmlAttribute
        public String file;
        @XmlAttribute
        public String mediaType;
    }

    public static final class XmlAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<XmlBean, InputOptions> {

        @Override
        public InputOptions unmarshal(XmlBean v) throws Exception {
            return new InputOptions(Optional.ofNullable(v.file).map(o -> new File(o)), MediaType.parse(v.mediaType));
        }

        @Override
        public XmlBean marshal(InputOptions v) throws Exception {
            XmlBean result = new XmlBean();
            result.file = v.getFile().map(File::toString).orElse(null);
            result.mediaType = v.getMediaType().toString();
            return result;
        }
    }
}
