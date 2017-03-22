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
package demetra.cli.jd3tests;

import be.nbb.demetra.toolset.Record;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.information.InformationSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface HrTestsTool {

    @lombok.Value
    public static class Options {

        private int p, bp, q, bq;
    }

    @lombok.Data
    public static class HrTestsResults implements Record {

        private String name;
        private double[] ml, hr, nhr;
        private double ehr, enhr;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);
            info.set("ehr", ehr);
            info.set("enhr", enhr);
            if (ml != null) {
                for (int i = 0; i < ml.length; ++i) {
                    info.set("ml" + i, ml[i]);
                }
            }
            if (hr != null) {
                for (int i = 0; i < hr.length; ++i) {
                    info.set("hr" + i, hr[i]);
                }
            }
            if (nhr != null) {
                for (int i = 0; i < nhr.length; ++i) {
                    info.set("nhr" + i, nhr[i]);
                }
            }
            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            }
            return info;
        }
    }

    @Nonnull
    HrTestsResults create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.parallelStream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static HrTestsTool getDefault() {
        return Lookup.getDefault().lookup(HrTestsTool.class);
    }
}
