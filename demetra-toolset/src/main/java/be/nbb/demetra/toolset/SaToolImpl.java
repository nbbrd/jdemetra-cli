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
package be.nbb.demetra.toolset;

import com.google.common.base.Strings;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.design.IBuilder;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@VisibleForTesting
final class SaToolImpl implements SaTool {

    @Override
    public SaTs create(TsInformation info, Options options) {
        SaTs result = new SaTs();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        result.setAlgorithm(options.getAlgorithm());
        result.setSpec(options.getSpec());
        if (info.data != null && !info.data.isEmpty()) {
            CompositeResults results = newProcessing(options).process(info.data);
            if (results != null) {
                result.setData(options.getItems().stream()
                        .filter(o -> (results.contains(o)) && results.getData(o, TsData.class) != null)
                        .collect(Collectors.toMap(o -> o, o -> results.getData(o, TsData.class))));
                result.setInvalidDataCause(null);
            } else {
                result.setData(null);
                result.setInvalidDataCause("The processing returned no results !");
            }
        } else {
            result.setData(Collections.emptyMap());
            result.setInvalidDataCause(null);
        }
        return result;
    }

    @Override
    public SaTsCollection create(TsCollectionInformation info, Options options) {
        SaTsCollection result = new SaTsCollection();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        result.setAlgorithm(options.getAlgorithm());
        result.setSpec(options.getSpec());
        result.setItems(info.items.parallelStream().map(o -> create(o, options)).collect(Collectors.toList()));
        return result;
    }

    @Override
    public List<TsInformation> toTs(SaTs ts) {
        MetaDataBuilder b = new MetaDataBuilder("sa_");
        return ts.getData().entrySet().stream().map(o -> {
            TsInformation item = new TsInformation();
            item.metaData = b.clear()
                    .put("algorithm", ts.getAlgorithm())
                    .put("spec", ts.getSpec())
                    .put("name", ts.getName())
                    .put("identifier", ts.getMoniker().getId())
                    .put("source", ts.getMoniker().getSource())
                    .put("id", o.getKey())
                    .build();
            item.name = ts.getName() + " #" + o.getKey();
            item.data = o.getValue();
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public TsCollectionInformation toTsCollection(SaTsCollection col) {
        TsCollectionInformation result = new TsCollectionInformation();
        result.metaData = new MetaDataBuilder("sa_")
                .put("algorithm", col.getAlgorithm())
                .put("spec", col.getSpec())
                .put("name", col.getName())
                .put("identifier", col.getMoniker().getId())
                .put("source", col.getMoniker().getSource())
                .build();
        if (col.getItems() != null) {
            col.getItems().stream().filter(o -> o != null && o.data != null).flatMap(o -> toTs(o).stream()).forEach(o -> result.items.add(o));
        }
        return result;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @Nonnull
    private static IProcessing<TsData, CompositeResults> newProcessing(Options o) {
        switch (o.getAlgorithm().toLowerCase()) {
            case "tramoseats":
                TramoSeatsSpecification s = TramoSeatsSpecification.fromString(o.getSpec());
                if (!s.isSystem()) {
                    throw new IllegalArgumentException("Specification not found: '" + o.getSpec() + "'");
                }
                return TramoSeatsProcessingFactory.instance.generateProcessing(s, null);
            case "x13":
                X13Specification sx = X13Specification.fromString(o.getSpec());
                if (!sx.isSystem()) {
                    throw new IllegalArgumentException("Specification not found: '" + o.getSpec() + "'");
                }
                return X13ProcessingFactory.instance.generateProcessing(sx, null);
            default:
                throw new IllegalArgumentException("Unrecognized algorithm (" + o.getAlgorithm() + ") !");
        }
    }

    private static final class MetaDataBuilder implements IBuilder<MetaData> {

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
    //</editor-fold>
}
