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
package demetra.cli.sa;

import com.google.common.base.Strings;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.IBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
final class MetaDataBuilder implements IBuilder<MetaData> {

    private final String prefix;
    private final Map<String, String> map;

    public MetaDataBuilder(String prefix) {
        this.prefix = prefix;
        this.map = new HashMap<>();
    }

    public MetaDataBuilder clear() {
        map.clear();
        return this;
    }

    public MetaDataBuilder put(@Nonnull String key, @Nullable String value) {
        if (Strings.isNullOrEmpty(value)) {
            map.remove(prefix + key);
        } else {
            map.put(prefix + key, value);
        }
        return this;
    }

    @Override
    public MetaData build() {
        return new MetaData(map);
    }
}
