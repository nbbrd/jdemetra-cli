/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.cli.research;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.IDataBlock;
import ec.tstoolkit.data.IReadDataBlock;
import ec.tstoolkit.maths.realfunctions.IParametricMapping;
import ec.tstoolkit.maths.realfunctions.ParamValidation;
import ec.tstoolkit.ssf.DefaultCompositeModel;
import ec.tstoolkit.ssf.ICompositeModel;
import ec.tstoolkit.ssf.SsfComposite;
import ec.tstoolkit.ssf.implementation.SsfHarrisonStevens;
import ec.tstoolkit.ssf.implementation.SsfLocalLinearTrend;
import ec.tstoolkit.ssf.implementation.SsfNoise;
import ec.tstoolkit.structural.Component;

/**
 *
 * @author Admin
 */
public class HsMapping implements IParametricMapping<SsfComposite> {

    private final int[] noisySeasons;
    private final Component fixed;

    public HsMapping(int[] noisy, Component fixed) {
        this.noisySeasons = noisy;
        this.fixed = fixed;
    }

    /**
     * sqrt(noise, level, slope, seasnoisy, seaslow)
     *
     * @param p
     * @return
     */
    @Override
    public SsfComposite map(IReadDataBlock p) {
        SsfLocalLinearTrend lt = new SsfLocalLinearTrend(var(Component.Level, p), var(Component.Slope, p));
        SsfNoise n = new SsfNoise(var(Component.Noise, p));
        double[] var = new double[12];
        double elow = p.get(3), vlow=elow*elow;
        for (int i = 0; i < 12; ++i) {
            var[i] = vlow;
        }
        double vhigh = var(Component.Seasonal, p);
        for (int i = 0; i < noisySeasons.length; ++i) {
            var[noisySeasons[i]] = vhigh;
        }
        SsfHarrisonStevens s = new SsfHarrisonStevens(var);
        SsfComposite composite = new SsfComposite(new DefaultCompositeModel(lt, n, s));
        return composite;
    }

    private double var(Component cmp, IReadDataBlock p) {
        int i = pos(cmp);
        if (i >= 0) {
            double e = p.get(i);
            return e * e;
        } else {
            return 1;
        }
    }

    private int pos(Component cmp) {
        if (cmp == fixed) {
            return -1;
        }
        int i = 0;
        if (cmp == Component.Noise) {
            return 0;
        } else if (fixed != Component.Noise) {
            ++i;
        }
        if (cmp == Component.Level) {
            return i;
        } else if (fixed != Component.Level) {
            ++i;
        }
        if (cmp == Component.Slope) {
            return i;
        } else if (fixed != Component.Slope) {
            ++i;
        }
        return i;
    }

    private double getSvarLow(double[] var) {
        for (int i = 0; i < 12; ++i) {
            if (!isNoisy(i)) {
                return var[i];
            }
        }
        return 0;
    }

    private double getSvarNoisy(double[] var) {
        for (int i = 0; i < 12; ++i) {
            if (isNoisy(i)) {
                return var[i];
            }
        }
        return 0;
    }

    private boolean isNoisy(int p) {
        for (int j = 0; j < noisySeasons.length; ++j) {
            if (p == noisySeasons[j]) {
                return true;
            }
        }
        return false;

    }

    @Override
    public IReadDataBlock map(SsfComposite t) {
        double[] p = new double[4];
        ICompositeModel cm = t.getCompositeModel();
        SsfLocalLinearTrend lt = (SsfLocalLinearTrend) cm.getComponent(0);
        SsfNoise n = (SsfNoise) cm.getComponent(1);
        SsfHarrisonStevens s = (SsfHarrisonStevens) cm.getComponent(2);
        // noise
        int i = 0;
        if (fixed != Component.Noise) {
            p[0] = Math.sqrt(n.getVariance());
            ++i;
        }
        if (fixed != Component.Level) {
            p[i] = Math.sqrt(lt.getVariance());
            ++i;
        }
        if (fixed != Component.Slope) {
            p[i] = Math.sqrt(lt.getSlopeVariance());
            ++i;
        }
        double[] svar=s.getVariances();
        if (fixed != Component.Seasonal) {
            p[i] = Math.sqrt(getSvarNoisy(svar));
        }
        p[3] = Math.sqrt(getSvarLow(svar));
        
        return new DataBlock(p);
    }

    @Override
    public boolean checkBoundaries(IReadDataBlock inparams) {
        for (int i = 0; i < inparams.getLength(); ++i) {
            if (Math.abs(inparams.get(i)) > 10) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double epsilon(IReadDataBlock inparams, int idx) {
        double p = inparams.get(idx);
        if (p < 0) {
            if (p < -1) {
                return p * .001;
            } else {
                return .001;
            }
        } else if (p > 1) {
            return -p * .001;
        } else {
            return -.001;
        }
    }

    @Override
    public int getDim() {
        return 4;
    }

    @Override
    public double lbound(int idx) {
        return -10;
    }

    @Override
    public double ubound(int idx) {
        return 10;
    }

    @Override
    public ParamValidation validate(IDataBlock ioparams) {
        boolean changed = false;
        for (int i = 0; i < ioparams.getLength(); ++i) {
            double p = ioparams.get(i);
            if (p > 10) {
                ioparams.set(i, 10 - 1 / p);
                changed = true;
            } else if (p < -10) {
                ioparams.set(i, -10 - 1 / p);
                changed = true;
            }
        }
        return changed ? ParamValidation.Changed : ParamValidation.Valid;
    }

    @Override
    public String getDescription(int idx) {
        return "stde" + (idx + 1);
    }
}
