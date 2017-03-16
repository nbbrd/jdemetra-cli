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

import ec.benchmarking.simplets.TsCholette;
import ec.benchmarking.simplets.TsExpander;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public interface BenchmarkingTool {

    @lombok.Value
    public static final class DentonOptions {

        boolean multiplicative;
        boolean modified;
        int differencing;
        TsAggregationType aggregationType;
    }

    TsData computeDenton(@Nonnull TsData x, @Nonnull TsData y, @Nonnull DentonOptions options);

    TsData computeDenton(@Nonnull TsFrequency freq, @Nonnull TsData y, @Nonnull DentonOptions options);

    @lombok.Value
    public static final class SsfDentonOptions {

        boolean multiplicative;
        TsAggregationType aggregationType;
    }

    TsData computeSsfDenton(@Nonnull TsData x, @Nonnull TsData y, @Nonnull SsfDentonOptions options);

    @lombok.Value
    public static final class CholetteOptions {

        double rho;
        double lambda;
        TsCholette.BiasCorrection bias;
        TsAggregationType aggregationType;
    }

    TsData computeCholette(@Nonnull TsData x, @Nonnull TsData y, @Nonnull CholetteOptions options);

    @lombok.Value
    public static final class ExpanderOptions {

        boolean useParameter;
        double parameter;
        boolean trend;
        boolean constant;
        TsExpander.Model model;
        int differencing;
        TsAggregationType aggregationType;
    }

    TsData expand(@Nonnull TsFrequency freq, @Nonnull TsData y, ExpanderOptions options);

    TsData expand(@Nonnull TsDomain domain, @Nonnull TsData y, ExpanderOptions options);

    @Nonnull
    public static BenchmarkingTool getDefault() {
        return new BenchmarkingToolImpl();
    }
}
