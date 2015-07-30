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
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.OutlierType;
import java.util.Set;
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

    public TramoSpecification newTramoSpecification() {
        TramoSpecification result = this.getDefaultSpec().newInstance();
        result.getOutliers().setCriticalValue(this.getCriticalValue());
        result.getTransform().setFunction(this.getTransformation());
        result.getOutliers().clearTypes();
        result.getOutliers().addRange(this.getOutlierTypes());
        return result;
    }

    public OutliersFactory newOutliersFactory() {
        return OutliersFactory.smart(newTramoSpecification());
    }
}
