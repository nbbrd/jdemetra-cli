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
public class SaTsCollection {

    private String name;
    private TsMoniker moniker;
    private String algorithm;
    private String spec;
    private List<SaTs> items;

    @Nonnull
    public static SaTsCollection create(@Nonnull TsCollectionInformation info, @Nonnull SaOptions options) {
        SaTsCollection result = new SaTsCollection();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        result.setAlgorithm(options.getAlgorithm());
        result.setSpec(options.getSpec());
        result.setItems(info.items.parallelStream().map(o -> SaTs.create(o, options)).collect(Collectors.toList()));
        return result;
    }

    @Nonnull
    public TsCollectionInformation toTsCollection() {
        TsCollectionInformation result = new TsCollectionInformation();
        result.metaData = new MetaDataBuilder("sa_")
                .put("algorithm", algorithm)
                .put("spec", spec)
                .put("name", name)
                .put("identifier", moniker.getId())
                .put("source", moniker.getSource())
                .build();
        items.stream().flatMap(o -> o.toTs().stream()).forEach(o -> result.items.add(o));
        return result;
    }
}
