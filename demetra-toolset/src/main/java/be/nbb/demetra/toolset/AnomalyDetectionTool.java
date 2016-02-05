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

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsMoniker;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.Value;

/**
 *
 * @author Philippe Charles
 */
public interface AnomalyDetectionTool {

    public enum DefaultSpec {

        TR0, TR1, TR2, TR3, TR4, TR5, TRfull;
    }

    //<editor-fold defaultstate="collapsed" desc="Outliers API">
    @Value
    public static class OutliersOptions {

        DefaultSpec defaultSpec;
        double criticalValue;
        DefaultTransformationType transformation;
        Set<OutlierType> outlierTypes;
    }

    @Data
    public static class OutliersTs {

        String name;
        TsMoniker moniker;
        List<OutlierEstimation> outliers;
        String invalidDataCause;
    }

    @Data
    public static class OutliersTsCollection {

        String name;
        TsMoniker moniker;
        List<OutliersTs> items;
    }

    @Nonnull
    OutliersTs getOutliers(@Nonnull TsInformation info, @Nonnull OutliersOptions options);

    @Nonnull
    default OutliersTsCollection getOutliers(@Nonnull TsCollectionInformation info, @Nonnull OutliersOptions options) {
        OutliersTsCollection result = new OutliersTsCollection();
        result.setName(info.name);
        result.setMoniker(info.moniker);
        result.setItems(info.items.parallelStream().map(o -> getOutliers(o, options)).collect(Collectors.toList()));
        return result;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="CheckLast API">
    @Value
    public static class CheckLastOptions {

        DefaultSpec defaultSpec;
        double criticalValue;
        int nBacks;
    }

    @Data
    public static class CheckLastTs implements Record {

        private String name;
        private double[] scores;
        private double[] forecasts;
        private double[] values;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);
            if (scores != null) {
                for (int i = 0; i < scores.length; ++i) {
                    int j = i + 1;
                    info.set("score" + j, scores[i]);
                    if (forecasts != null) {
                        info.set("forecast" + j, forecasts[i]);
                    }
                    if (values != null) {
                        info.set("value" + j, values[i]);
                    }
                }
            }
            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            }
            return info;
        }
    }

    @Nonnull
    CheckLastTs getCheckLast(@Nonnull TsInformation info, @Nonnull CheckLastOptions options);

    @Nonnull
    default List<InformationSet> getCheckLast(TsCollectionInformation info, CheckLastOptions options) {
        return info.items.parallelStream().map(o -> getCheckLast(o, options).generate()).collect(Collectors.toList());
    }
    //</editor-fold>

    @Nonnull
    public static AnomalyDetectionTool getDefault() {
        return new AnomalyDetectionToolImpl();
    }
}
