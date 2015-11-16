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
package demetra.cli.sa;

import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.common.random.RandomBean;
import ec.tss.tsproviders.common.random.RandomProvider;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class Ts2SaTest {

    static TsCollectionInformation getData() {
        RandomProvider p = new RandomProvider();
        RandomBean bean = p.newBean();
        bean.setCount(100);
        TsCollectionInformation col = new TsCollectionInformation(p.toMoniker(p.encodeBean(bean)), TsInformationType.All);
        p.get(col);
        p.dispose();
        return col;
    }

    @Test
    public void testProcess() {
        TsCollectionInformation input = getData();
        SaOptions options = new SaOptions("tramoseats", "RSA1");

        List<Map<String, TsData>> output = Ts2Sa.process(input, options, false);
        for (Map<String, TsData> o : output) {
            for (Entry<String, TsData> x : o.entrySet()) {
                System.out.println(x.getKey() + " > " + x.getValue().getObsCount());
            }
        }
    }

}
