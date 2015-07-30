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
package demetra.cli.tsproviders;

import com.google.common.annotations.VisibleForTesting;
import demetra.cli.helpers.BasicArgsParser;
import com.google.common.primitives.Doubles;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.common.random.RandomBean;
import ec.tss.tsproviders.common.random.RandomProvider;
import ec.tss.xml.XmlTsCollection;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Random2Ts extends StandardApp<Random2Ts.Parameters> {

    public static void main(String[] args) {
        new Random2Ts().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public RandomBean input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        RandomProvider provider = new RandomProvider();
        TsCollectionInformation result = XProviders.getTsCollection(provider, params.input, TsInformationType.All);
        params.output.writeValue(XmlTsCollection.class, result);
        provider.dispose();
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<RandomBean> random = new RandomOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.input = random.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }

    private static final class RandomOptionsSpec extends OptionsSpec<RandomBean> {

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

        public RandomOptionsSpec(OptionParser parser) {
            this.seed = parser.accepts("seed")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.length = parser.accepts("length")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(240);
            this.p = parser.accepts("p")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.d = parser.accepts("d")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.q = parser.accepts("q")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.s = parser.accepts("s")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(12);
            this.bp = parser.accepts("bp")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.bd = parser.accepts("bd")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.bq = parser.accepts("bq")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(1);
            this.coeff = parser.accepts("coeff")
                    .withRequiredArg().ofType(Double.class).defaultsTo(-.8, -.6);
            this.count = parser.accepts("count")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(100);
        }

        @Override
        public RandomBean value(OptionSet options) {
            RandomBean result = new RandomBean();
            result.setSeed(seed.value(options));
            result.setLength(length.value(options));
            result.setP(p.value(options));
            result.setD(d.value(options));
            result.setQ(q.value(options));
            result.setS(s.value(options));
            result.setBp(bp.value(options));
            result.setBd(bd.value(options));
            result.setBq(bq.value(options));
            result.setCoeff(Doubles.toArray(coeff.values(options)));
            result.setCount(count.value(options));
            return result;
        }
    }
}
