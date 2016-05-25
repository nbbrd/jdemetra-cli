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
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import javax.annotation.Nonnull;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Philippe Charles
 */
@UtilityClass
public class Utils {

    public static void printVersion(Class<?> clazz, PrintStream stream) {
        stream.println(clazz.getSimpleName() + " " + getAPIVersion(clazz));
    }

    public static String getAPIVersion(Class<?> clazz) {
        String path = "/META-INF/maven/be.nbb.demetra/demetra-cli/pom.properties";
        try (InputStream stream = clazz.getResourceAsStream(path)) {
            if (stream == null) {
                return "UNKNOWN";
            }
            Properties result = new Properties();
            result.load(stream);
            return (String) result.get("version");
        } catch (IOException e) {
            return "UNKNOWN";
        }
    }

    @Nonnull
    public static Optional<MediaType> getMediaType(@Nonnull Optional<String> mediaType, @Nonnull Optional<File> file) {
        if (mediaType.isPresent()) {
            return Optional.of(MediaType.parse(mediaType.get()));
        }
        if (file.isPresent()) {
            return getMediaType(file.get());
        }
        return Optional.empty();
    }

    @Nonnull
    public static Optional<MediaType> getMediaType(@Nonnull File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            return contentType != null ? Optional.of(MediaType.parse(contentType)) : Optional.empty();
        } catch (IOException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public static void loadSystemProperties(Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {
            Properties properties = new Properties();
            properties.load(stream);
            System.getProperties().putAll(properties);
        }
    }
}
