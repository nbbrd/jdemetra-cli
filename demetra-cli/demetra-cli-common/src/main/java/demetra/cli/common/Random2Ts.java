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
package demetra.cli.common;

import be.nbb.cli.command.Command;
import be.nbb.cli.command.core.OptionsExecutor;
import be.nbb.cli.command.core.OptionsParsingCommand;
import be.nbb.cli.command.joptsimple.ComposedOptionSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import be.nbb.demetra.toolset.ProviderTool;
import com.google.common.primitives.Doubles;
import demetra.cli.helpers.XmlUtil;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.common.random.RandomBean;
import ec.tss.tsproviders.common.random.RandomProvider;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.design.VisibleForTesting;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * Creates random time series.
 *
 * @author Philippe Charles
 */
public final class Random2Ts {

    @CommandRegistration
    static Command CMD = OptionsParsingCommand.<Options>builder()
            .name("random2ts")
            .parser(Parser::new)
            .executor(Executor::new)
            .so(o -> o.so)
            .build();

    public static final class Options {

        StandardOptions so;
        public RandomBean input;
        public OutputOptions output;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final ProviderTool tool = ProviderTool.getDefault();

        @Override
        public void exec(Options params) throws Exception {
            try (RandomProvider p = new RandomProvider()) {
                TsCollectionInformation result = tool.getTsCollection(p, params.input, TsInformationType.All);
                XmlUtil.writeValue(params.output, XmlTsCollection.class, result);
            }
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<RandomBean> random = new RandomOptionsSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            Options result = new Options();
            result.input = random.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    private static final class RandomOptionsSpec implements ComposedOptionSpec<RandomBean> {

        private final OptionSpec<Integer> seed;
        private final OptionSpec<Integer> length;
        private final OptionSpec<Integer> p;
        private final OptionSpec<Integer> d;
        private final OptionSpec<Integer> q;
        private final OptionSpec<Integer> s;
        private final OptionSpec<Integer> bp;
        private final OptionSpec<Integer> bd;
        private final OptionSpec<Integer> bq;
        private final OptionSpec<Double> coeff;
        private final OptionSpec<Integer> count;

        public RandomOptionsSpec(OptionParser p) {
            this.seed = p.accepts("seed").withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.length = p.accepts("length").withRequiredArg().ofType(Integer.class).defaultsTo(240);
            this.p = p.accepts("p").withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.d = p.accepts("d").withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.q = p.accepts("q").withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.s = p.accepts("s").withRequiredArg().ofType(Integer.class).defaultsTo(12);
            this.bp = p.accepts("bp").withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.bd = p.accepts("bd").withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.bq = p.accepts("bq").withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.coeff = p.accepts("coeff").withRequiredArg().ofType(Double.class).withValuesSeparatedBy(',').defaultsTo(-.8, -.6);
            this.count = p.accepts("count").withRequiredArg().ofType(Integer.class).defaultsTo(100);
        }

        @Override
        public RandomBean value(OptionSet o) {
            RandomBean result = new RandomBean();
            result.setSeed(seed.value(o));
            result.setLength(length.value(o));
            result.setP(p.value(o));
            result.setD(d.value(o));
            result.setQ(q.value(o));
            result.setS(s.value(o));
            result.setBp(bp.value(o));
            result.setBd(bd.value(o));
            result.setBq(bq.value(o));
            result.setCoeff(Doubles.toArray(coeff.values(o)));
            result.setCount(count.value(o));
            return result;
        }
    }
}
