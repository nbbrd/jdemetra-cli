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

import ec.satoolkit.algorithm.implementation.StmProcessingFactory;
import ec.satoolkit.diagnostics.FTest;
import ec.satoolkit.diagnostics.FriedmanTest;
import ec.satoolkit.diagnostics.KruskalWallisTest;
import ec.satoolkit.special.StmDecomposition;
import ec.satoolkit.special.StmEstimation;
import ec.satoolkit.special.StmSpecification;
import ec.tss.TsInformation;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.arima.AutoRegressiveDistance;
import ec.tstoolkit.information.StatisticalTest;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaModel;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.Component;
import ec.tstoolkit.ucarima.UcarimaModel;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = StmAirlineTool.class)
public final class StmAirlineToolImpl implements StmAirlineTool {

    @Override
    public StmResults create(TsInformation info, Options options) {
        StmResults result = new StmResults();
        result.setName(info.name);
        try {
            StmSpecification spec=new StmSpecification();
            CompositeResults process = StmProcessingFactory.process(info.data, spec);
            PreprocessingModel pp = process.get(StmProcessingFactory.PREPROCESSING, PreprocessingModel.class);
            SarimaModel arima = pp.estimation.getArima();
            result.setTh(arima.theta(1));
            result.setBth(arima.btheta(1));
            StmEstimation estimation=process.get(StmProcessingFactory.ESTIMATION, StmEstimation.class);
            BasicStructuralModel model = estimation.getModel();
            result.setNvar(model.getVariance(Component.Noise));
            result.setLvar(model.getVariance(Component.Level));
            result.setSvar(model.getVariance(Component.Slope));
            result.setSeasvar(model.getVariance(Component.Seasonal));
            UcarimaModel redmodel = model.computeReducedModel(true);
            double dist = AutoRegressiveDistance.compute(arima, redmodel.getModel(), 36);
            result.setDistance(dist);
            result.setAirser(pp.estimation.getLikelihood().getSer());
            result.setStmser(estimation.getLikelihood().getSer());
        } catch (Exception err) {
            result.setInvalidDataCause(err.getMessage());
        }
        return result;
    }

    //</editor-fold>
}
