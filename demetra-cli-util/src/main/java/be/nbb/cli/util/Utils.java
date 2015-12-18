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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
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

    public static Optional<MediaType> getMediaType(Optional<String> mediaType, Optional<File> file) {
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
        return MEDIA_TYPE_FACTORIES.stream()
                .map(o -> o.apply(file))
                .filter(o -> o != null)
                .findFirst();
    }

    private static final List<Function<File, MediaType>> MEDIA_TYPE_FACTORIES = Arrays.asList(Utils::probeMediaType, Utils::getMediaTypeByExtension);

    private static MediaType probeMediaType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            return contentType != null ? MediaType.parse(contentType) : null;
        } catch (IOException | IllegalArgumentException ex) {
            return null;
        }
    }

    private static MediaType getMediaTypeByExtension(File file) {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".json")) {
            return MediaType.JSON_UTF_8;
        }
        if (fileName.endsWith(".xml")) {
            return MediaType.XML_UTF_8;
        }
        if (fileName.endsWith(".png")) {
            return MediaType.PNG;
        }
        if (fileName.endsWith(".svg")) {
            return MediaType.SVG_UTF_8;
        }
        if (fileName.endsWith(".yaml")) {
            return YAML;
        }
        return null;
    }

    private static final MediaType YAML = MediaType.parse("application/yaml");
}
