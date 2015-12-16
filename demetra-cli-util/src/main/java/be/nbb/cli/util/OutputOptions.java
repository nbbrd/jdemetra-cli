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
@XmlJavaTypeAdapter(OutputOptions.XmlAdapter.class)
public final class OutputOptions {

    private final Optional<File> file;
    private final MediaType mediaType;
    private final boolean formatted;

    @Nonnull
    public static OutputOptions of(@Nonnull File file, @Nonnull MediaType mediaType, boolean formatted) {
        return new OutputOptions(Optional.of(file), mediaType, formatted);
    }

    @Nonnull
    public static OutputOptions create(Optional<File> file, Optional<String> mediaType, boolean formatted) {
        return new OutputOptions(file, Utils.getMediaType(mediaType, file), formatted);
    }

    @Nonnull
    public <X> void write(@Nonnull Class<X> clazz, @Nonnull X value) throws IOException {
        BasicSerializer<X> serializer = BasicSerializer.of(getMediaType(), clazz, isFormatted());
        if (getFile().isPresent()) {
            serializer.serialize(value, getFile().get());
        } else {
            serializer.serialize(value, System.out);
        }
    }

    @XmlRootElement
    public static final class XmlBean {

        @XmlAttribute
        public String file;
        @XmlAttribute
        public String mediaType;
        @XmlAttribute
        public boolean formatted;
    }

    public static final class XmlAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<XmlBean, OutputOptions> {

        @Override
        public OutputOptions unmarshal(XmlBean v) throws Exception {
            return new OutputOptions(Optional.ofNullable(v.file).map(Utils.TO_FILE), MediaType.parse(v.mediaType), v.formatted);
        }

        @Override
        public XmlBean marshal(OutputOptions v) throws Exception {
            XmlBean result = new XmlBean();
            result.file = v.getFile().map(Utils.FROM_FILE).orElse(null);
            result.mediaType = v.getMediaType().toString();
            result.formatted = v.isFormatted();
            return result;
        }
    }
}
