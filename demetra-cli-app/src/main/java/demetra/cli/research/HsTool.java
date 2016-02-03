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
import lombok.Data;
import lombok.Value;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface HsTool {

    @Value
    public static class Options {

    }

    @Data
    public static class HsResults implements Record {

        public static String[] items = new String[]{"series", "nvar", "lvar", "svar", "seasvar1", "seasvar2", "llstm", "llhs", "stmbias", "hsbias", "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9", "n10", "n11", "n12"};

        private String name;
        private int freq;
        private double nvar, lvar, svar, seasvar1, seasvar2, llstm, llhs, stmbias, hsbias;
        private int[] noisy;
        private String invalidDataCause;

        @Override
        public InformationSet generate() {
            InformationSet info = new InformationSet();
            info.set("series", name);

            if (invalidDataCause != null) {
                info.set("error", invalidDataCause);
            } else {
                info.set("nvar", nvar);
                info.set("lvar", lvar);
                info.set("svar", svar);
                info.set("seasvar1", seasvar1);
                info.set("seasvar2", seasvar2);
                info.set("llstm", llstm);
                info.set("llhs", llhs);
                info.set("stmbias", stmbias);
                info.set("hsbias", hsbias);
                for (int i=0; i<freq; ++i){
                    info.set("n"+(i+1), isNoisy(i) ? 1 : 0);
                }
            }
            return info;
        }

        private boolean isNoisy(int pos) {
            for (int i = 0; i < noisy.length; ++i) {
                if (noisy[i] == pos) {
                    return true;
                }
            }
            return false;
        }
    }

    @Nonnull
    HsResults create(@Nonnull TsInformation info, @Nonnull Options options);

    @Nonnull
    default List<InformationSet> create(TsCollectionInformation info, Options options) {
        return info.items.stream().map(o -> create(o, options).generate()).collect(Collectors.toList());
    }

    public static HsTool getDefault() {
        return Lookup.getDefault().lookup(HsTool.class);
    }
}
