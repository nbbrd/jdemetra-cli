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
import javax.annotation.Nonnull;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
public interface SerializerFactory {

    @Nonnull
    boolean canHandle(@Nonnull MediaType mediaType, @Nonnull Class<?> type);

    @Nonnull
    <X> Serializer<X> create(@Nonnull Class<X> type, boolean formattedOutput);

    @Nonnull
    public static <X> Serializer<X> of(@Nonnull MediaType mediaType, @Nonnull Class<X> type, boolean formattedOutput) {
        return Lookup.getDefault().lookupAll(SerializerFactory.class).stream()
                .filter(o -> o.canHandle(mediaType, type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Don't know how to handle media type '" + mediaType + "' for class '" + type.getName() + "'"))
                .create(type, formattedOutput);
    }
}
