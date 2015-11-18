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

import ec.tss.TsInformation;
import ec.tss.TsMoniker;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import lombok.Data;

/**
 *
 * @author Philippe Charles
 */
@Data
public class OutliersTs {

    private String name;
    private TsMoniker moniker;
    private List<OutlierEstimation> outliers;
    private String invalidDataCause;

    @Nonnull
    public static OutliersTs create(@Nonnull TsInformation info, @Nonnull OutliersOptions options) {
        OutliersTs result = new OutliersTs();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        if (info.data != null && !info.data.isEmpty()) {
            PreprocessingModel model = options.newPreprocessor().process(info.data, null);
            if (model != null) {
                result.setOutliers(Arrays.asList(model.outliersEstimation(true, false)));
                result.setInvalidDataCause(null);
            } else {
                // BUG
                result.setOutliers(null);
                result.setInvalidDataCause("No preprocessing model");
            }
        } else {
            result.setOutliers(Collections.emptyList());
            result.setInvalidDataCause(null);
        }
        return result;
    }
}
