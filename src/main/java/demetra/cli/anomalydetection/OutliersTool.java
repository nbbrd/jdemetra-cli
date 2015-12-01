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
import ec.tss.TsMoniker;
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.Value;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface OutliersTool {

    public enum DefaultSpec {

        TR0, TR1, TR2, TR3, TR4, TR5;
    }

    @Value
    public static class Options {

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
    OutliersTs create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    OutliersTsCollection create(@Nonnull TsCollectionInformation info, @Nonnull Options options);

    @Nonnull
    public static OutliersTool getDefault() {
        return Lookup.getDefault().lookup(OutliersTool.class);
    }
}
