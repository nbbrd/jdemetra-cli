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

import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public enum DefaultSpec {

    TR0, TR1, TR2, TR3, TR4, TR5;

    @Nonnull
    public TramoSpecification newInstance() {
        switch (this) {
            case TR0:
                return TramoSpecification.TR0.clone();
            case TR1:
                return TramoSpecification.TR1.clone();
            case TR2:
                return TramoSpecification.TR2.clone();
            case TR3:
                return TramoSpecification.TR3.clone();
            case TR4:
                return TramoSpecification.TR4.clone();
            case TR5:
                return TramoSpecification.TR5.clone();
            default:
                throw new RuntimeException();
        }
    }
}
