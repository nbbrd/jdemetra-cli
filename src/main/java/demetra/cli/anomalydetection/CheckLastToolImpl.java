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
import ec.tss.TsInformation;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.CheckLast;
import static ec.tstoolkit.modelling.arima.CheckLast.MAX_MISSING_COUNT;
import static ec.tstoolkit.modelling.arima.CheckLast.MAX_REPEAT_COUNT;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = CheckLastTool.class)
public final class CheckLastToolImpl implements CheckLastTool {

    @Override
    public CheckLastTs create(TsInformation info, Options options) {
       CheckLastTs result = new CheckLastTs();
        result.setName(info.name);
        String error = checkData(info.data);
        if (error == null) {
            CheckLast cl=new CheckLast(newPreprocessor(options));
            cl.setBackCount(options.getNBacks());
            if (cl.check(info.data)) {
                double[][] r=new double[3][];
                r[0]=cl.getScores();
                r[1]=cl.getActualValues();
                r[2]=cl.getForecastsValues();
            } else {
                result.setScores(null);
                result.setInvalidDataCause("Check last failed");
            }
        } else {
            result.setScores(null);
            result.setInvalidDataCause(error);
        }
        return result;
    }

    private static TramoSpecification newTramoSpecification(Options o) {
        return OutliersToolImpl.newInstance(o.getDefaultSpec());
    }

    private static IPreprocessor newPreprocessor(Options o) {
        return newTramoSpecification(o).build();
    }

    @Nullable
    private static String checkData(@Nullable final TsData y) {
        if (y == null || y.isEmpty()) {
            return "Missing data";
        }
        int nz = y.getObsCount();
        int ifreq = y.getFrequency().intValue();
        if (nz < Math.max(8, 3 * ifreq)) {
            return "Not enough obs";
        }
        int nrepeat = y.getValues().getRepeatCount();
        if (nrepeat > MAX_REPEAT_COUNT * nz / 100) {
            return "Too much repeated obs";
        }
        int nm = y.getValues().getMissingValuesCount();
        if (nm > MAX_MISSING_COUNT * nz / 100) {
            return "Too much missing obs";
        }
        return null;
    }
    //</editor-fold>
}
