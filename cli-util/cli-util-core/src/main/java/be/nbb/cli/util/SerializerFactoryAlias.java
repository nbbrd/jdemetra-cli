/*
 * Copyright 2016 National Bank of Belgium
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

import java.util.Collection;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class SerializerFactoryAlias {

    @Nonnull
    public static SerializerFactoryAlias of(@Nonnull Class<?> fromType, @Nonnull MediaType fromMediaType, @Nonnull MediaType toMediaType) {
        return new SerializerFactoryAlias(fromType, fromMediaType, toMediaType);
    }

    private final Class<?> fromType;
    private final MediaType fromMediaType;
    private final MediaType toMediaType;

    private SerializerFactoryAlias(Class<?> fromType, MediaType fromMediaType, MediaType toMediaType) {
        this.fromType = fromType;
        this.fromMediaType = fromMediaType;
        this.toMediaType = toMediaType;
    }

    public boolean canHandle(@Nonnull MediaType mediaType, @Nonnull Class<?> type, @Nonnull Collection<? extends SerializerFactory> factories) {
        return fromType.equals(type) && fromMediaType.isCompatible(mediaType)
                && factories.stream().anyMatch(o -> o.canHandle(toMediaType, type));
    }

    @Nonnull
    public <X> Serializer<X> create(@Nonnull Class<X> type, boolean formattedOutput, @Nonnull Collection<? extends SerializerFactory> factories) {
        return factories.stream()
                .filter(o -> o.canHandle(toMediaType, type))
                .findFirst()
                .map(o -> o.create(type, formattedOutput))
                .orElseThrow(RuntimeException::new);
    }
}
