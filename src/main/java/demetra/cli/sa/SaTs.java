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

import ec.tss.TsInformation;
import ec.tss.TsMoniker;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Data;

/**
 *
 * @author Philippe Charles
 */
@Data
public class SaTs {

    private String name;
    private TsMoniker moniker;
    private String algorithm;
    private String spec;
    private Map<String, TsData> data;
    private String invalidDataCause;

    @Nonnull
    public static SaTs create(@Nonnull TsInformation info, @Nonnull SaOptions options) {
        SaTs result = new SaTs();
        result.name = info.name;
        result.moniker = info.moniker;
        result.algorithm = options.getAlgorithm();
        result.spec = options.getSpec();
        if (info.data != null && !info.data.isEmpty()) {
            CompositeResults results = options.newProcessing().process(info.data);
            if (results != null) {
                result.data = options.getItems().stream()
                        .filter(o -> (results.contains(o)))
                        .collect(Collectors.toMap(o -> o, o -> results.getData(o, TsData.class)));
                result.invalidDataCause = null;
            } else {
                result.data = null;
                result.invalidDataCause = "The processing returned no results !";
            }
        } else {
            result.data = Collections.emptyMap();
            result.invalidDataCause = null;
        }
        return result;
    }

    @Nonnull
    public List<TsInformation> toTs() {
        MetaDataBuilder b = new MetaDataBuilder("sa_");
        return data.entrySet().stream().map(o -> {
            TsInformation item = new TsInformation();
            item.metaData = b.clear()
                    .put("algorithm", algorithm)
                    .put("spec", spec)
                    .put("name", name)
                    .put("identifier", moniker.getId())
                    .put("source", moniker.getSource())
                    .put("id", o.getKey())
                    .build();
            item.name = name + " #" + o.getKey();
            item.data = o.getValue();
            return item;
        }).collect(Collectors.toList());
    }
}
