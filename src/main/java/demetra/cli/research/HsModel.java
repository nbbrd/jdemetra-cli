/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.cli.research;

import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.ssf.DefaultCompositeModel;
import ec.tstoolkit.ssf.SsfComposite;
import ec.tstoolkit.ssf.implementation.SsfHarrisonStevens;
import ec.tstoolkit.ssf.implementation.SsfLocalLinearTrend;
import ec.tstoolkit.ssf.implementation.SsfNoise;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.Component;
import ec.tstoolkit.utilities.DoubleList;
import lombok.Data;

/**
 *
 * @author Admin
 */
@Data
public class HsModel {

    private double nVar, lVar, sVar, seasVar1, seasVar2;
    private int[] noisySeas;
    private int freq;

    public IReadDataBlock parameters(Component fixed) {
        DoubleList p = new DoubleList();
        if (fixed != Component.Noise) {
            p.add(Math.sqrt(nVar));
        }
        if (fixed != Component.Level) {
            p.add(Math.sqrt(lVar));
        }
        if (fixed != Component.Slope) {
            p.add(Math.sqrt(sVar));
        }
        if (fixed != Component.Seasonal) {
            p.add(Math.sqrt(seasVar1));
        }
        p.add(Math.sqrt(seasVar2));

        return new ReadDataBlock(p.toArray());
    }

    public void setParameters(IReadDataBlock p, Component fixed) {
        int i = 0;
        if (fixed != Component.Noise) {
            nVar = p.get(0) * p.get(0);
            ++i;
        } else {
            nVar = 1;
        }
        if (fixed != Component.Level) {
            lVar = p.get(i) * p.get(i);
            ++i;
        } else {
            lVar = 1;
        }
        if (fixed != Component.Slope) {
            sVar = p.get(i) * p.get(i);
            ++i;
        } else {
            sVar = 1;
        }
        if (fixed != Component.Seasonal) {
            seasVar1 = p.get(i) * p.get(i);
        }
        seasVar2 = p.get(3) * p.get(3);
    }
    
    public Component rescale(){
        double vmax=nVar;
        Component cmax=Component.Noise;
        if (lVar>vmax){
            vmax=lVar;
            cmax=Component.Level;
        }
        if (sVar>vmax){
            vmax=sVar;
            cmax=Component.Slope;
        }
        if (seasVar1>vmax){
            vmax=seasVar1;
            cmax=Component.Seasonal;
        }
        nVar/=vmax;
        lVar/=vmax;
        sVar/=vmax;
        seasVar1/=vmax;
        seasVar2/=vmax;
        return cmax;
    }

    public void initialize(BasicStructuralModel bsm) {
        lVar = bsm.getVariance(Component.Level);
        sVar = bsm.getVariance(Component.Slope);
        nVar = Math.max(0, bsm.getVariance(Component.Noise));
        seasVar2 = seasVar1 = bsm.getVariance(Component.Seasonal);
        freq = bsm.freq;
    }

    public SsfComposite ssf() {
        SsfLocalLinearTrend lt = new SsfLocalLinearTrend(lVar, sVar);
        SsfNoise n = new SsfNoise(nVar);
        double[] var = new double[freq];
        for (int i = 0; i < 12; ++i) {
            var[i] = seasVar1;
        }
        for (int i = 0; i < noisySeas.length; ++i) {
            var[noisySeas[i]] = seasVar2;
        }
        SsfHarrisonStevens s = new SsfHarrisonStevens(var);
        SsfComposite composite = new SsfComposite(new DefaultCompositeModel(lt, n, s));
        return composite;

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Noisy periods");
        for (int i = 0; i < noisySeas.length; ++i) {
            builder.append(' ').append(1 + noisySeas[i]);
        }
        builder.append("\r\n");
        builder.append(lVar).append(' ');
        builder.append(sVar).append(' ');
        builder.append(nVar).append(' ');
        builder.append(seasVar1).append(' ');
        builder.append(seasVar2).append(' ');
        return builder.toString();
    }

}
