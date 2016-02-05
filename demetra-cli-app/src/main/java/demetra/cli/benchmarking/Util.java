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
package demetra.cli.benchmarking;

import com.google.common.collect.Maps;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.AbstractList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;
import lombok.experimental.UtilityClass;

/**
 *
 * @author Philippe Charles
 */
@UtilityClass
class Util {

    @Nonnull
    public static List<Entry<TsInformation, TsInformation>> zip(final @Nonnull List<TsInformation> l, final @Nonnull List<TsInformation> r) throws IllegalArgumentException {
        if (l.size() != r.size()) {
            throw new IllegalArgumentException("Expected: " + l.size() + ", found: " + r.size());
        }
        return new AbstractList<Entry<TsInformation, TsInformation>>() {
            @Override
            public Entry<TsInformation, TsInformation> get(int index) {
                return Maps.immutableEntry(l.get(index), r.get(index));
            }

            @Override
            public int size() {
                return l.size();
            }
        };
    }

    @Nonnull
    public static Collector<TsInformation, ?, TsCollectionInformation> toTsCollectionInformation() {
        return Collectors.collectingAndThen(Collectors.toList(), o -> {
            TsCollectionInformation result = new TsCollectionInformation();
            result.items.addAll(o);
            return result;
        });
    }

    @Nonnull
    public static ValueConverter<TsDomain> domainConverter() {
        return new ValueConverter<TsDomain>() {
            @Override
            public TsDomain convert(String value) {
                String[] tmp = value.split(":", -1);
                try {
                    return new TsDomain(TsFrequency.valueOf(Integer.parseInt(tmp[0])), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]));
                } catch (Exception ex) {
                    throw new ValueConversionException("Invalid input", ex);
                }
            }

            @Override
            public Class<? extends TsDomain> valueType() {
                return TsDomain.class;
            }

            @Override
            public String valuePattern() {
                return "freq:year:period:count";
            }
        };
    }
}
