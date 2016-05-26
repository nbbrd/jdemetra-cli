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

import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.FriedmanTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.tss.TsInformation;
import ec.tstoolkit.arima.estimation.RegArimaEstimation;
import ec.tstoolkit.arima.estimation.RegArimaModel;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
import ec.tstoolkit.modelling.arima.demetra.HannanRissanen2;
import ec.tstoolkit.modelling.arima.tramo.ArmaModule;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.sarima.SarimaSpecification;
import org.openide.util.lookup.ServiceProvider;
import ec.tstoolkit.sarima.SarmaSpecification;
import ec.tstoolkit.sarima.estimation.GlsSarimaMonitor;
import ec.tstoolkit.sarima.estimation.HannanRissanen;
import ec.tstoolkit.sarima.estimation.SarimaMapping;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(service = ArmaTestsTool.class)
public final class ArmaTestslImpl implements ArmaTestsTool {

    @Override
    public ArmaTestsResults create(TsInformation info, Options options) {
        ArmaTestsResults result = new ArmaTestsResults();
        result.setName(info.name);
        try {
            PreprocessingModel model = TramoSpecification.TRfull.build().process(info.data, null);
            SarimaComponent arima = model.description.getArimaComponent();
            result.setPauto(arima.getP());
            result.setQauto(arima.getQ());
            result.setBpauto(arima.getBP());
            result.setBqauto(arima.getBQ());

            // correct data for estimated outliers...
            DataBlock res = model.estimation.getLinearizedData();
            DataBlock dres=new DataBlock(res.getLength()-arima.getDifferencingOrder());
            arima.getDifferencingFilter().filter(res, dres);
           
            int freq = model.description.getFrequency();
            SarmaSpecification maxspec = new SarmaSpecification(freq);
            maxspec.setP(3);
            maxspec.setQ(3);
            maxspec.setBP(1);
            maxspec.setBQ(1);
            try {
                ec.tstoolkit.modelling.arima.tramo.ArmaModule tramo = new ec.tstoolkit.modelling.arima.tramo.ArmaModule();
                tramo.setAcceptingWhiteNoise(true);
                tramo.tramo(dres, maxspec, 0, 0, true);
                ArmaModule.HRBic cur = tramo.getPreferedModels()[0];
                SarmaSpecification spec = cur.getHR().getSpec();
                result.setPtramo(spec.getP());
                result.setQtramo(spec.getQ());
                result.setBptramo(spec.getBP());
                result.setBqtramo(spec.getBQ());
            } catch (Exception err) {
            }
            try {
                ec.tstoolkit.modelling.arima.x13.ArmaModule x13 = new ec.tstoolkit.modelling.arima.x13.ArmaModule();
                SarmaSpecification spec = x13.select(new DataBlock(dres), freq, 3, 1,arima.getD(), arima.getBD() );
                result.setPx13(spec.getP());
                result.setQx13(spec.getQ());
                result.setBpx13(spec.getBP());
                result.setBqx13(spec.getBQ());
            } catch (Exception err) {
            }
        } catch (Exception err) {
        }
        return result;
    }

    //</editor-fold>
}
