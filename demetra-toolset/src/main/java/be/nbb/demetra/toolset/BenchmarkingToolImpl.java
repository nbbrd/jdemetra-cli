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

import ec.benchmarking.denton.DentonMethod;
import ec.benchmarking.simplets.AbstractTsBenchmarking;
import ec.benchmarking.simplets.TsCholette;
import ec.benchmarking.simplets.TsDenton;
import ec.benchmarking.simplets.TsExpander;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Philippe Charles
 */
public final class BenchmarkingToolImpl implements BenchmarkingTool {

    @Override
    public TsData computeDenton(TsData x, TsData y, DentonOptions options) {
        TsDenton2 result = newTsDenton2(options);
        return result.benchmark(x, y);
    }

    @Override
    public TsData computeDenton(TsFrequency freq, TsData y, DentonOptions options) {
        TsDenton2 result = newTsDenton2(options);
        result.setDefaultFrequency(freq);
        return result.benchmark(null, y);
    }

    @Override
    public TsData computeSsfDenton(TsData x, TsData y, SsfDentonOptions options) {
        TsDenton denton = new TsDenton();
        denton.setAggregationType(options.getAggregationType());
        denton.setMultiplicative(options.isMultiplicative());
        return denton.process(x, y);
    }

    @Override
    public TsData computeCholette(TsData x, TsData y, CholetteOptions options) {
        TsCholette cholette = new TsCholette();
        cholette.setAggregationType(options.getAggregationType());
        cholette.setRho(options.getRho());
        cholette.setLambda(options.getLambda());
        cholette.setBiasCorrection(options.getBias());
        return cholette.process(x, y);
    }

    @Override
    public TsData expand(TsFrequency freq, TsData y, ExpanderOptions options) {
        return newTsExpander(options).expand(y, freq);
    }

    @Override
    public TsData expand(TsDomain domain, TsData y, ExpanderOptions options) {
        return newTsExpander(options).expand(y, domain);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private TsDenton2 newTsDenton2(DentonOptions options) {
        TsDenton2 result = new TsDenton2();
        result.setAggregationType(options.getAggregationType());
        result.setDifferencingOrder(options.getDifferencing());
        result.setModified(options.isModified());
        result.setMultiplicative(options.isMultiplicative());
        return result;
    }

    private TsExpander newTsExpander(ExpanderOptions options) {
        TsExpander result = new TsExpander();
        result.setType(options.getAggregationType());
        result.setModel(options.getModel());
        if (options.isUseParameter()) {
            result.setParameter(options.getParameter());
            result.estimateParameter(false);
        } else {
            result.estimateParameter(true);
        }
        result.useConst(options.isConstant());
        result.useTrend(options.isTrend());
        return result;
    }

    private static final class TsDenton2 extends AbstractTsBenchmarking {

        private boolean mul = true;
        private boolean modified = true;
        private int diff = 1;
        private TsFrequency defFreq = TsFrequency.Quarterly;

        /**
         *
         */
        public TsDenton2() {
        }

        /**
         *
         * @param series
         * @param aggregationConstaints
         * @return
         */
        @Override
        protected TsData benchmark(TsData series, TsData aggregationConstaints) {
            if (aggregationConstaints == null) {
                return null;
            }
            DentonMethod denton = new DentonMethod();
            denton.setAggregationType(getAggregationType());
            denton.setDifferencingOrder(diff);
            denton.setMultiplicative(mul);
            denton.setModifiedDenton(modified);
            int yfreq = aggregationConstaints.getFrequency().intValue();
            int qfreq = series != null ? series.getFrequency().intValue() : defFreq.intValue();
            if (qfreq % yfreq != 0) {
                return null;
            }
            denton.setConversionFactor(qfreq / yfreq);
            TsData tr;
            if (series != null) {
                // Y is limited to q !
                TsPeriodSelector qsel = new TsPeriodSelector();
                qsel.between(series.getStart().firstday(), series.getLastPeriod().lastday());
                aggregationConstaints = aggregationConstaints.select(qsel);
                TsPeriod q0 = series.getStart(), yq0 = new TsPeriod(q0.getFrequency());
                yq0.set(aggregationConstaints.getStart().firstday());
                denton.setOffset(yq0.minus(q0));
                double[] r = denton.process(series, aggregationConstaints);
                return new TsData(series.getStart(), r, false);
            } else {
                TsPeriod qstart = aggregationConstaints.getStart().firstPeriod(defFreq);
                double[] r = denton.process(aggregationConstaints);
                return new TsData(qstart, r, false);
            }
        }

        /**
         *
         * @return
         */
        public boolean isMultiplicative() {
            return mul;
        }

        /**
         *
         * @param value
         */
        public void setMultiplicative(boolean value) {
            mul = value;
        }

        /**
         * @return the modified
         */
        public boolean isModified() {
            return modified;
        }

        /**
         * @param modified the modified to set
         */
        public void setModified(boolean modified) {
            this.modified = modified;
        }

        /**
         * @return the diff
         */
        public int getDifferencingOrder() {
            return diff;
        }

        /**
         * @param diff the diff to set
         */
        public void setDifferencingOrder(int diff) {
            this.diff = diff;
        }

        /**
         * @return the defFreq
         */
        public TsFrequency getDefaultFrequency() {
            return defFreq;
        }

        /**
         * @param defFreq the defFreq to set
         */
        public void setDefaultFrequency(TsFrequency defFreq) {
            this.defFreq = defFreq;
        }
    }
    //</editor-fold>
}
