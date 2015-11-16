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
package demetra.cli.helpers;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class ForkJoinTasks {

    private ForkJoinTasks() {
        // static class
    }

    @Nonnull
    public static <X, Y> ForkJoinTask<List<Y>> asTask(@Nonnull Function<List<X>, List<Y>> processor, @Nonnegative int threshold, @Nonnull List<X> input) {
        return new ExtRecursiveTask<>(processor, threshold, input);
    }

    @Nonnull
    public static <X, Y> List<Y> invoke(@Nonnull Function<List<X>, List<Y>> processor, @Nonnegative int threshold, @Nonnull List<X> input) {
        if (Runtime.getRuntime().availableProcessors() == 1 || input.size() < threshold) {
            return processor.apply(input);
        }
        ForkJoinPool pool = new ForkJoinPool();
        List<Y> result = pool.invoke(asTask(processor, threshold, input));
        pool.shutdown();
        return result;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final class ExtRecursiveTask<X, Y> extends RecursiveTask<List<Y>> {

        private final Function<List<X>, List<Y>> resource;
        private final int threshold;
        private final List<X> input;

        public ExtRecursiveTask(Function<List<X>, List<Y>> resource, int threshold, List<X> input) {
            this.resource = resource;
            this.threshold = threshold;
            this.input = input;
        }

        @Override
        protected List<Y> compute() {
            int size = input.size();
            if (input.size() < threshold) {
                return resource.apply(input);
            }
            int center = size / 2;
            ExtRecursiveTask<X, Y> s2 = new ExtRecursiveTask(resource, threshold, input.subList(center, size));
            s2.fork();
            ExtRecursiveTask<X, Y> s1 = new ExtRecursiveTask(resource, threshold, input.subList(0, center));
            List<Y> result = new ArrayList<>(size);
            result.addAll(s1.compute());
            result.addAll(s2.join());
            return result;
        }
    }
    //</editor-fold>
}
