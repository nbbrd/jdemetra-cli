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
import ec.tstoolkit.modelling.arima.CheckLast;
import static ec.tstoolkit.modelling.arima.CheckLast.MAX_MISSING_COUNT;
import static ec.tstoolkit.modelling.arima.CheckLast.MAX_REPEAT_COUNT;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = AnomalyDetectionTool.class)
public final class AnomalyDetectionToolImpl implements AnomalyDetectionTool {

    @Override
    public OutliersTs getOutliers(TsInformation info, OutliersOptions options) {
        OutliersTs result = new OutliersTs();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        String error = checkData(info.data);
        if (error == null) {
            PreprocessingModel model = newPreprocessor(options).process(info.data, null);
            if (model != null) {
                OutlierEstimation[] outliers = model.outliersEstimation(true, false);
                if (outliers != null) {
                    result.setOutliers(Arrays.asList(outliers));
                    result.setInvalidDataCause(null);
                } else {
                    result.setOutliers(null);
                    result.setInvalidDataCause("Bug: missing likelihood");
                }
            } else {
                result.setOutliers(null);
                result.setInvalidDataCause("Bug: no preprocessing model");
            }
        } else {
            result.setOutliers(null);
            result.setInvalidDataCause(error);
        }
        return result;
    }

    @Override
    public OutliersTsCollection getOutliers(TsCollectionInformation info, OutliersOptions options) {
        OutliersTsCollection result = new OutliersTsCollection();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        result.setItems(info.items.parallelStream().map(o -> getOutliers(o, options)).collect(Collectors.toList()));
        return result;
    }

    @Override
    public CheckLastTs getCheckLast(TsInformation info, CheckLastOptions options) {
        CheckLastTs result = new CheckLastTs();
        result.setName(info.name);
        String error = checkData(info.data);
        if (error == null) {
            CheckLast cl = new CheckLast(newPreprocessor(options));
            cl.setBackCount(options.getNBacks());
            if (cl.check(info.data)) {
                result.setScores(cl.getScores());
                result.setValues(cl.getActualValues());
                result.setForecasts(cl.getForecastsValues());
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

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @Nonnull
    static TramoSpecification newInstance(DefaultSpec o) {
        switch (o) {
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
            case TRfull:
                return TramoSpecification.TRfull.clone();
            default:
                throw new RuntimeException();
        }
    }

    private static TramoSpecification newTramoSpecification(OutliersOptions o) {
        TramoSpecification result = newInstance(o.getDefaultSpec());
        result.getOutliers().setCriticalValue(o.getCriticalValue());
        result.getTransform().setFunction(o.getTransformation());
        result.getOutliers().clearTypes();
        result.getOutliers().addRange(o.getOutlierTypes());
        return result;
    }

    private static IPreprocessor newPreprocessor(OutliersOptions o) {
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

    private static TramoSpecification newTramoSpecification(CheckLastOptions o) {
        return AnomalyDetectionToolImpl.newInstance(o.getDefaultSpec());
    }

    private static IPreprocessor newPreprocessor(CheckLastOptions o) {
        return newTramoSpecification(o).build();
    }
    //</editor-fold>
}
