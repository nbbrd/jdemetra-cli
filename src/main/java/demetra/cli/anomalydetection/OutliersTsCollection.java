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
package demetra.cli.anomalydetection;

import ec.tss.TsCollectionInformation;
import ec.tss.TsMoniker;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Data;

/**
 *
 * @author Philippe Charles
 */
@Data
public class OutliersTsCollection {

    private String name;
    private TsMoniker moniker;
    private List<OutliersTs> items;

    @Nonnull
    public static OutliersTsCollection create(@Nonnull TsCollectionInformation info, @Nonnull OutliersOptions options) {
        OutliersTsCollection result = new OutliersTsCollection();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        result.setItems(info.items.parallelStream().map(o -> OutliersTs.create(o, options)).collect(Collectors.toList()));
        return result;
    }
}
