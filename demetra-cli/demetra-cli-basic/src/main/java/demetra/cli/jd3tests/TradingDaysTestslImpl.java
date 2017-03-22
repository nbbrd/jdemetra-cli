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
import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.FriedmanTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.tss.TsInformation;
import ec.tstoolkit.information.StatisticalTest;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = TradingDaysTestsTool.class)
public final class TradingDaysTestslImpl implements TradingDaysTestsTool {

    @Override
    public TradingDaysTestsResults create(TsInformation info, Options options) {
        TradingDaysTestsResults result = new TradingDaysTestsResults();
        result.setName(info.name);
        try{
            FTest ftest=new FTest();
            if ( ftest.test(info.data))
                result.setFtest(StatisticalTest.create(ftest.getFTest()));
        }
        catch (Exception err){}
        try{
            FTest ftest=new FTest();
            if ( ftest.testAMI(info.data))
                result.setFtestami(StatisticalTest.create(ftest.getFTest()));
        }
        catch (Exception err){}
        try{
            KruskalWallisTest  kw=new KruskalWallisTest(info.data);
            result.setKruskalwallis(StatisticalTest.create(kw));
        }
        catch (Exception err){}
        try{
            FriedmanTest  f=new FriedmanTest(info.data.delta(1));
            result.setFriedman(StatisticalTest.create(f));
        }
        catch (Exception err){}
        return result;
    }

    //</editor-fold>
}
