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
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.modelling.arima.RegArimaEstimator;
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
        SarmaSpecification spec = new SarmaSpecification(info.data.getFrequency().intValue());
        spec.setP(options.getP());
        spec.setBP(options.getBp());
        spec.setQ(options.getQ());
        spec.setBQ(options.getBq());
        RegArimaModel<SarimaModel> regarima=new RegArimaModel<SarimaModel>();
        regarima.setY(info.data);
        regarima.setArima(new SarimaModel(spec));
        try {
            RegArimaEstimator gls=new RegArimaEstimator(new SarimaMapping(new SarimaSpecification(spec), false));
            //GlsSarimaMonitor gls = new GlsSarimaMonitor();
            RegArimaEstimation<SarimaModel> m = gls.process(regarima);
            double ll=m.likelihood.getLogLikelihood();
                    
            IReadDataBlock parameters = m.model.getArima().getParameters();
            double[] pml = new double[spec.getParametersCount()];
            parameters.copyTo(pml, 0);
            result.setMl(pml);
            HannanRissanen hr = new HannanRissanen();
            hr.process(info.data, spec);
            parameters = hr.getModel().getParameters();
            double[] phr = new double[spec.getParametersCount()];
            parameters.copyTo(phr, 0);
            result.setHr(phr);
            
            regarima.setArima(hr.getModel());
            double hrll = regarima.computeLikelihood().getLogLikelihood();
            result.setEhr(ll-hrll);
            
        } catch (Exception err) {
        }
        try {
        } catch (Exception err) {
        }
        try {
        } catch (Exception err) {
        }
        return result;
    }

    //</editor-fold>
}
