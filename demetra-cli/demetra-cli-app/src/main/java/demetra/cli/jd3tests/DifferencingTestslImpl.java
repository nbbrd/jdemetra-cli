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

import ec.tss.TsInformation;
import ec.tstoolkit.modelling.arima.ModellingContext;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.modelling.arima.demetra.DifferencingModule;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaSpecification;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = DifferencingTestsTool.class)
public final class DifferencingTestslImpl implements DifferencingTestsTool {

    @Override
    public DifferencingTestsResults create(TsInformation info, Options options) {
        DifferencingTestsResults result = new DifferencingTestsResults();
        result.setName(info.name);
        try {
            PreprocessingModel model = TramoSpecification.TRfull.build().process(info.data, null);
            SarimaComponent arima = model.description.getArimaComponent();
            result.setDauto(arima.getD());
            result.setBdauto(arima.getBD());
            result.setMauto(arima.isMean());
            int freq = model.description.getFrequency();
            ModellingContext context = new ModellingContext();
            context.description = model.description;
            context.estimation = model.estimation;
            context.hasseas=true;
            context.automodelling=true;
            SarimaSpecification xspec = new SarimaSpecification(freq);
            context.description.setSpecification(xspec);
            ec.tstoolkit.modelling.arima.tramo.DifferencingModule dtramo=new ec.tstoolkit.modelling.arima.tramo.DifferencingModule();
            dtramo.process(context);
            result.setDtramo(dtramo.getD());
            result.setBdtramo(dtramo.getBD());
            result.setMtramo(dtramo.isMean());
            context.description = model.description;
            context.estimation = model.estimation;
            context.description.setSpecification(xspec);
            DifferencingModule dsimple=new DifferencingModule();
            dsimple.process(context);
            result.setDsimple(dsimple.getD());
            result.setBdsimple(dsimple.getBD());
            result.setMsimple(dsimple.isMean());
            
        } catch (Exception err) {
        }
        return result;
    }

    //</editor-fold>
}
