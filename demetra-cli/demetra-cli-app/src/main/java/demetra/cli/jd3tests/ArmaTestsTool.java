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
import ec.tstoolkit.Parameter;
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
public interface ArmaTestsTool {

    @Value
    public static class Options {
    }

    @Data
    public static class ArmaTestsResults implements Record {

        private String name;
        private int pauto, qauto, bpauto, bqauto;
        private int ptramo, qtramo, bptramo, bqtramo;
        private int px13, qx13, bpx13, bqx13;
        private int pdemetra, qdemetra, bpdemetra, bqdemetra;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);
            info.set("p", pauto);
            info.set("q", qauto);
            info.set("bp", bpauto);
            info.set("bq", bqauto);
            info.set("ptramo", ptramo);
            info.set("qtramo", qtramo);
            info.set("bptramo", bptramo);
            info.set("bqtramo", bqtramo);
            info.set("px13", px13);
            info.set("qx13", qx13);
            info.set("bpx13", bpx13);
            info.set("bqx13", bqx13);
            info.set("pdemetra", pdemetra);
            info.set("qdemetra", qdemetra);
            info.set("bpdemetra", bpdemetra);
            info.set("bqdemetra", bqdemetra);
            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            }
            return info;
        }
    }

    @Nonnull
    ArmaTestsResults create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.parallelStream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static ArmaTestsTool getDefault() {
        return Lookup.getDefault().lookup(ArmaTestsTool.class);
    }
}
