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

import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcessing;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import lombok.Value;

/**
 *
 * @author Philippe Charles
 */
@Value
public class SaOptions {

    String algorithm;
    String spec;

    @Nonnull
    IProcessing<TsData, CompositeResults> newProcessing() {
        switch (getAlgorithm().toLowerCase()) {
            case "tramoseats":
                TramoSeatsSpecification s = TramoSeatsSpecification.fromString(getSpec());
                if (!s.isSystem()) {
                    throw new IllegalArgumentException("Specification not found: '" + getSpec() + "'");
                }
                return TramoSeatsProcessingFactory.instance.generateProcessing(s, null);
            case "x13":
                X13Specification sx = X13Specification.fromString(getSpec());
                if (!sx.isSystem()) {
                    throw new IllegalArgumentException("Specification not found: '" + getSpec() + "'");
                }
                return X13ProcessingFactory.instance.generateProcessing(sx, null);
            default:
                throw new IllegalArgumentException("Unrecognized algorithm (" + getAlgorithm() + ") !");
        }
    }

    @Nonnull
    static Map<String, TsData> processData(@Nonnull TsData input, IProcessing<TsData, CompositeResults> processing) {
        CompositeResults results = processing.process(input);
        if (results == null) {
            throw new IllegalArgumentException("The processing returned no results !");
        }
        Map<String, TsData> result = new HashMap<>();
        for (String id : new String[]{"sa", "t", "s", "i"}) {
            if (results.contains(id)) {
                result.put(id, results.getData(id, TsData.class));
            }
        }
        return result;
    }
}
