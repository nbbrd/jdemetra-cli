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

import demetra.cli.anomalydetection.OutliersTool.DefaultSpec;
import demetra.cli.helpers.Record;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.information.InformationSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.Value;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface CheckLastTool {

    @Value
    public static class Options {

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
                    info.set("score"+j, scores[i]);
                    if (forecasts != null) {
                        info.set("forecast"+j, forecasts[i]);
                    }
                    if (values != null) {
                        info.set("value"+j, values[i]);
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
    CheckLastTs create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.parallelStream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static CheckLastTool getDefault() {
        return Lookup.getDefault().lookup(CheckLastTool.class);
    }
}
