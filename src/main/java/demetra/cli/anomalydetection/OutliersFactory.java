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

import com.google.common.base.Supplier;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.Arrays2;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Philippe Charles
 */
public abstract class OutliersFactory {

    public List<OutlierEstimation[]> process(final TsCollectionInformation col, Callback callback) {
        return process(new AbstractList<TsData>() {
            @Override
            public TsData get(int index) {
                TsInformation tmp = col.items.get(index);
                return tmp.hasData() ? tmp.data : null;
            }

            @Override
            public int size() {
                return col.items.size();
            }
        }, callback);
    }

    abstract public List<OutlierEstimation[]> process(List<TsData> input, Callback callback);

    public static class Callback {

        public void publish(int index, OutlierEstimation[][] outliers) {
        }
    }

    public static OutliersFactory smart(final TramoSpecification spec) {
        if (Runtime.getRuntime().availableProcessors() == 1) {
            return normal(spec);
        }
        return new OutliersFactory() {
            @Override
            public List<OutlierEstimation[]> process(List<TsData> input, Callback callback) {
                return (input.size() > Processing.THRESHOLD ? parallel(spec) : normal(spec)).process(input, callback);
            }
        };
    }

    public static OutliersFactory normal(final TramoSpecification spec) {
        return new OutliersFactory() {
            @Override
            public List<OutlierEstimation[]> process(List<TsData> input, Callback callback) {
                IPreprocessor preprocessor = spec.build();
                OutlierEstimation[][] result = new OutlierEstimation[input.size()][];
                for (int i = 0; i < result.length; i++) {
                    result[i] = processData(input.get(i), preprocessor);
                    if (i % 100 == 0 && i != 0) {
                        callback.publish(i, Arrays.copyOfRange(result, i - 100, i));
                    }
                }
                return Arrays.asList(result);
            }
        };
    }

    public static OutliersFactory parallel(final TramoSpecification spec) {
        return new OutliersFactory() {
            @Override
            public List<OutlierEstimation[]> process(List<TsData> input, Callback callback) {
                return Arrays.asList(Processing.create(preprocessorFactory(spec), input, callback).invoke());
            }
        };
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static Supplier<IPreprocessor> preprocessorFactory(final TramoSpecification spec) {
        return new Supplier<IPreprocessor>() {
            @Override
            public IPreprocessor get() {
                // one preprocessor per thread
                return spec.build();
            }
        };
    }

    private static OutlierEstimation[] processData(TsData data, IPreprocessor preprocessor) {
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

    private static final class Processing extends RecursiveTask<OutlierEstimation[][]> {

        public static Processing create(Supplier<IPreprocessor> preprocessorFactory, List<TsData> input, Callback callback) {
            return new Processing(preprocessorFactory, 0, input.size(), input, callback);
        }

        private static final int THRESHOLD = 10;
        private final Supplier<IPreprocessor> ppf;
        private final int index;
        private final int size;
        private final List<TsData> input;
        private final Callback callback;

        private Processing(Supplier<IPreprocessor> preprocessorFactory, int index, int size, List<TsData> input, Callback callback) {
            this.ppf = preprocessorFactory;
            this.index = index;
            this.size = size;
            this.input = input;
            this.callback = callback;
        }

        @Override
        protected OutlierEstimation[][] compute() {
            if (size < THRESHOLD) {
                IPreprocessor preprocessor = ppf.get();
                OutlierEstimation[][] result = new OutlierEstimation[size][];
                for (int i = 0; i < result.length; i++) {
                    result[i] = processData(input.get(index + i), preprocessor);
                }
                callback.publish(index, result);
                return result;
            }
            int center = size / 2;
            Processing s2 = new Processing(ppf, index + center, size - center, input, callback);
            s2.fork();
            Processing s1 = new Processing(ppf, index, center, input, callback);
            return Arrays2.concat(s1.compute(), s2.join());
        }
    }
    //</editor-fold>
}
