/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.cli.command.joptsimple;

import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nonnull;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class JOptSimpleUtil {

    @Nonnull
    public <T> ValueConverter<T> converterOf(@Nonnull Class<T> type, @Nonnull Function<String, T> parser) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(parser);
        return new ValueConverter<T>() {
            @Override
            public T convert(String value) {
                try {
                    return parser.apply(value);
                } catch (RuntimeException ex) {
                    throw new ValueConversionException("Failed to convert '" + value + "' to type '" + type + "'", ex);
                }
            }

            @Override
            public Class<? extends T> valueType() {
                return type;
            }

            @Override
            public String valuePattern() {
                return null;
            }
        };
    }
}
