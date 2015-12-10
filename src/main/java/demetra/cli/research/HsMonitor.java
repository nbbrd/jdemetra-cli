/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.cli.research;

import ec.satoolkit.algorithm.implementation.StmProcessingFactory;
import ec.satoolkit.diagnostics.CochranTest;
import ec.satoolkit.special.StmEstimation;
import ec.satoolkit.special.StmSpecification;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.data.DescriptiveStatistics;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.maths.realfunctions.levmar.LevenbergMarquardtMethod;
import ec.tstoolkit.modelling.DeterministicComponent;
import ec.tstoolkit.ssf.SsfAlgorithm;
import ec.tstoolkit.ssf.SsfComposite;
import ec.tstoolkit.ssf.SsfData;
import ec.tstoolkit.ssf.SsfFunction;
import ec.tstoolkit.ssf.SsfFunctionInstance;
import ec.tstoolkit.ssf.SsfModel;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.Component;
import ec.tstoolkit.structural.SeasonalModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.utilities.IntList;

/**
 *
 * @author Jean Palate
 */
public class HsMonitor {
    
    private HsModel results;
    private CompositeResults stm;
    private BasicStructuralModel bsm;
    private double llstm, llhs, stmbias, hsbias;
    
    public HsModel getResults(){
        return results;
    }
    
    public BasicStructuralModel getStructuralModel(){
        return bsm;
    }
    
    public boolean process(TsData s) {
        StmSpecification spec = new StmSpecification();
        // step 1: we compute the usual HR model
        spec.getDecompositionSpec().getModelSpecification().setSeasonalModel(SeasonalModel.HarrisonStevens);
        stm = StmProcessingFactory.process(s, spec);
        // we retrieve the corresponding structural model
        DeterministicComponent det = stm.get(StmProcessingFactory.DETERMINISTIC, DeterministicComponent.class);
        StmEstimation estimation = stm.get(StmProcessingFactory.ESTIMATION, StmEstimation.class);
        llstm=estimation.getLikelihood().getLogLikelihood();
        bsm = estimation.getModel();
        Component cmp=bsm.fixMaxVariance(1);
        results = new HsModel();
        results.initialize(bsm);
        results.setNoisySeas(searchNoisyPeriods(estimation));
        return estimate(det.linearizedSeries(), cmp);
    }
    
    private int[] searchNoisyPeriods(StmEstimation estimation) {
        // computes the Cochran test on the residuals
        TsData residuals = estimation.getResiduals();
        CochranTest test = new CochranTest(residuals, false);
        test.calcCochranTest();
        double[] svar = test.getS();
        DescriptiveStatistics stats = new DescriptiveStatistics(svar);
        IntList noisy = new IntList();
        double limit = .5 * (stats.getMax() + stats.getAverage());
        for (int i = 0; i < svar.length; ++i) {
            if (svar[i] >= limit) {
                noisy.add(i);
            }
        }
        return noisy.toArray();
        
    }
    
    private boolean estimate(TsData s, Component fixed) {
        
        HsMapping mapping = new HsMapping(results.getNoisySeas(), fixed);
        IReadDataBlock p = results.parameters(fixed);
        SsfComposite ssf = mapping.map(p);
        SsfModel<SsfComposite> model = new SsfModel(ssf, new SsfData(s, null), null, null);
        SsfFunction<SsfComposite> fn = new SsfFunction<>(
                model,
                mapping,
                new SsfAlgorithm());
        
        LevenbergMarquardtMethod lm = new LevenbergMarquardtMethod();
        lm.minimize(fn, p);
        SsfFunctionInstance<SsfComposite> xfn = (SsfFunctionInstance<SsfComposite>) lm.getResult();
        results.setParameters(xfn.getParameters(), fixed);
        llhs=xfn.getLikelihood().getLogLikelihood();
        return true;
    }

    /**
     * @return the llstm
     */
    public double getLlstm() {
        return llstm;
    }

    /**
     * @return the llhs
     */
    public double getLlhs() {
        return llhs;
    }

    /**
     * @return the stmbias
     */
    public double getStmbias() {
        return stmbias;
    }

    /**
     * @return the hsbias
     */
    public double getHsbias() {
        return hsbias;
    }
    
}
