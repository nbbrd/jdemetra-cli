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
package demetra.cli.helpers;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.net.MediaType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class Utils {

    private Utils() {
        // static class
    }

    public static void printVersion(Class<?> clazz, PrintStream stream) {
        stream.println(clazz.getSimpleName() + " " + getAPIVersion(clazz));
    }

    public static String getAPIVersion(Class<?> clazz) {
        String path = "/version.prop";
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

    public static MediaType getMediaType(Optional<String> mediaType, Optional<File> file) {
        if (mediaType.isPresent()) {
            MediaType result = MediaType.parse(mediaType.get());
            if (MediaType.XML_UTF_8.is(result)) {
                return MediaType.XML_UTF_8;
            }
            if (MediaType.JSON_UTF_8.is(result)) {
                return MediaType.JSON_UTF_8;
            }
        }
        return file.isPresent() && file.get().getName().toLowerCase(Locale.ROOT).endsWith(".json")
                ? MediaType.JSON_UTF_8
                : MediaType.XML_UTF_8;
    }

    @Nonnull
    public static Optional<MediaType> getMediaType(@Nonnull File file) {
        Optional<MediaType> result = probeMediaType(file);
        return result.isPresent() ? result : getMediaTypeByExtension(file);
    }

    private static Optional<MediaType> probeMediaType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            if (contentType != null) {
                return Optional.of(MediaType.parse(contentType));
            }
        } catch (IOException | IllegalArgumentException ex) {
        }
        return Optional.absent();
    }

    private static Optional<MediaType> getMediaTypeByExtension(File file) {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".json")) {
            return Optional.of(MediaType.JSON_UTF_8);
        }
        if (fileName.endsWith(".xml")) {
            return Optional.of(MediaType.XML_UTF_8);
        }
        if (fileName.endsWith(".png")) {
            return Optional.of(MediaType.PNG);
        }
        if (fileName.endsWith(".svg")) {
            return Optional.of(MediaType.SVG_UTF_8);
        }
        return Optional.absent();
    }

    public static MediaType[] supportedMediaTypes() {
        return new MediaType[]{MediaType.XML_UTF_8, MediaType.JSON_UTF_8};
    }

    static final Function<String, File> TO_FILE = new Function<String, File>() {
        @Override
        public File apply(String input) {
            return new File(input);
        }
    };

    static final Function<File, String> FROM_FILE = new Function<File, String>() {
        @Override
        public String apply(File input) {
            return input.toString();
        }
    };

    public static <X, Y> Function<List<X>, List<Y>> withProgress(final Function<List<X>, List<Y>> func, final int size) {
        return new Function<List<X>, List<Y>>() {
            final AtomicInteger cpt = new AtomicInteger(0);
            final AtomicInteger previous = new AtomicInteger(0);

            @Override
            public List<Y> apply(List<X> input) {
                List<Y> result = func.apply(input);

                int percent = 100 * cpt.addAndGet(input.size()) / size;
                int old = previous.getAndSet(percent);
                if (old != percent) {
                    System.err.println("Processed: " + percent + "%");
                }

                return result;
            }
        };
    }
}
