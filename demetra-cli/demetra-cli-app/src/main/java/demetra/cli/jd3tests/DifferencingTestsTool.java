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

import demetra.cli.tests.*;
import be.nbb.demetra.toolset.Record;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.design.ServiceDefinition;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.StatisticalTest;
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
public interface DifferencingTestsTool {

    @Value
    public static class Options {

    }

    @Data
    public static class DifferencingTestsResults implements Record {

        private String name;
        private int dauto, bdauto, dtramo, bdtramo, dx13, bdx13, dsimple, bdsimple;
        private boolean mauto, mtramo, mx13, msimple;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);
            info.set("dauto", dauto);
            info.set("bdauto", bdauto);
            info.set("mauto", mauto);
            info.set("dtramo", dtramo);
            info.set("bdtramo", bdtramo);
            info.set("mtramo", mtramo);
            info.set("dx13", dx13);
            info.set("bdx13", bdx13);
            info.set("mx13", mx13);
            info.set("dsimple", dsimple);
            info.set("bdsimple", bdsimple);
            info.set("msimple", msimple);
            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            }
            return info;
        }
    }

    @Nonnull
    DifferencingTestsResults create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.parallelStream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static DifferencingTestsTool getDefault() {
        return Lookup.getDefault().lookup(DifferencingTestsTool.class);
    }
}
