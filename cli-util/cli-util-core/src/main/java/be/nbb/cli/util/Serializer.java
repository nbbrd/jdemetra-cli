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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface Serializer<T> {

    void serialize(@Nonnull T value, @Nonnull OutputStream output) throws IOException;

    default void serialize(@Nonnull T value, @Nonnull File output) throws IOException {
        try (OutputStream stream = new FileOutputStream(output)) {
            serialize(value, stream);
        }
    }

    default void serialize(@Nonnull T value, @Nonnull Path output) throws IOException {
        try (OutputStream stream = Files.newOutputStream(output)) {
            serialize(value, stream);
        }
    }

    @Nonnull
    T deserialize(@Nonnull InputStream input) throws IOException;

    @Nonnull
    default T deserialize(@Nonnull File input) throws IOException {
        try (InputStream stream = new FileInputStream(input)) {
            return deserialize(stream);
        }
    }

    @Nonnull
    default T deserialize(@Nonnull Path input) throws IOException {
        try (InputStream stream = Files.newInputStream(input)) {
            return deserialize(stream);
        }
    }
}
