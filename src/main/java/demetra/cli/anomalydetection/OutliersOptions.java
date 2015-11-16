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

import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.Value;

/**
 *
 * @author Philippe Charles
 */
@Value
public class OutliersOptions {

    DefaultSpec defaultSpec;
    double criticalValue;
    DefaultTransformationType transformation;
    Set<OutlierType> outlierTypes;

    @Nonnull
    TramoSpecification newTramoSpecification() {
        TramoSpecification result = this.getDefaultSpec().newInstance();
        result.getOutliers().setCriticalValue(this.getCriticalValue());
        result.getTransform().setFunction(this.getTransformation());
        result.getOutliers().clearTypes();
        result.getOutliers().addRange(this.getOutlierTypes());
        return result;
    }

    @Nonnull
    IPreprocessor newPreprocessor() {
        return newTramoSpecification().build();
    }

    static OutlierEstimation[] processData(TsData data, IPreprocessor preprocessor) {
        if (data != null && !data.isEmpty()) {
            PreprocessingModel model = preprocessor.process(data, null);
            if (model != null) {
                return model.outliersEstimation(true, false);
            } else {
                // BUG
                return null;
            }
        }
        return new OutlierEstimation[0];
    }
}
