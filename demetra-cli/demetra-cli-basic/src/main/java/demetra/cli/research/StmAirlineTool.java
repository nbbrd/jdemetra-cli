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
package demetra.cli.research;

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
public interface StmAirlineTool {

    @lombok.Value
    public static class Options {

    }

    @lombok.Data
    public static class StmResults implements Record {
        
        public static String[] items=new String[]{"series", "th", "bth", "cochran", "nvar", "lvar", "svar", "seasvar", "distance", "airse", "stmse"};

        private String name;
        private double th, bth, nvar, lvar, svar, seasvar, distance, rmsefcasts, airser, stmser;
        private double cochran;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);
            
            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            }else{
                info.set("th", th);
                info.set("bth", bth);
                info.set("cochran", cochran);
                info.set("nvar", nvar);
                info.set("lvar", lvar);
                info.set("svar", svar);
                info.set("seasvar", seasvar);
                info.set("distance", distance);
                info.set("airse", airser);
                info.set("stmse", stmser);
            }
            return info;
        }
    }

    @Nonnull
    StmResults create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.parallelStream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static StmAirlineTool getDefault() {
        return Lookup.getDefault().lookup(StmAirlineTool.class);
    }
}
