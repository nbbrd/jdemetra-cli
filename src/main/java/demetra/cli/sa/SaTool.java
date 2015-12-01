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

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsMoniker;
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.Value;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface SaTool {

    @Value
    public static class Options {

        String algorithm;
        String spec;
        List<String> items;
    }

    @Data
    public static class SaTs {

        String name;
        TsMoniker moniker;
        String algorithm;
        String spec;
        Map<String, TsData> data;
        String invalidDataCause;
    }

    @Data
    public static class SaTsCollection {

        String name;
        TsMoniker moniker;
        String algorithm;
        String spec;
        List<SaTs> items;
    }

    @Nonnull
    SaTs create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    SaTsCollection create(@Nonnull TsCollectionInformation info, @Nonnull Options options);

    @Nonnull
    List<TsInformation> toTs(@Nonnull SaTs ts);

    @Nonnull
    TsCollectionInformation toTsCollection(@Nonnull SaTsCollection col);

    @Nonnull
    public static SaTool getDefault() {
        return Lookup.getDefault().lookup(SaTool.class);
    }
}
