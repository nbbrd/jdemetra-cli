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
import com.google.common.base.Joiner;
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.odbc.OdbcBean;
import ec.tss.tsproviders.odbc.OdbcProvider;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class Odbc2Ts extends StandardApp<Odbc2Ts.Parameters> {

    public static void main(String[] args) {
        new Odbc2Ts().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public OdbcBean input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        OdbcProvider provider = new OdbcProvider();
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
        private final OptionsSpec<OdbcBean> random = new RandomOptionsSpec(parser);
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

    private static final class RandomOptionsSpec extends OptionsSpec<OdbcBean> {

        private final OptionSpec<String> dbName;
        private final OptionSpec<String> tableName;
        private final OptionSpec<String> dimColumns;
        private final OptionSpec<String> periodColumn;
        private final OptionSpec<String> valueColumn;
        private final OptionSpec<String> versionColumn;
        private final OptionSpec<String> locale;
        private final OptionSpec<String> datePattern;
        private final OptionSpec<String> numberPattern;
        private final OptionSpec<TsFrequency> frequency;
        private final OptionSpec<TsAggregationType> aggregationType;

        public RandomOptionsSpec(OptionParser p) {
            this.dbName = p.accepts("dsn").withRequiredArg().ofType(String.class).defaultsTo("");
            this.tableName = p.accepts("table").withRequiredArg().ofType(String.class).defaultsTo("");
            this.dimColumns = p.accepts("dims").withRequiredArg().ofType(String.class).withValuesSeparatedBy(',').defaultsTo("");
            this.periodColumn = p.accepts("period").withRequiredArg().ofType(String.class).defaultsTo("");
            this.valueColumn = p.accepts("value").withRequiredArg().ofType(String.class).defaultsTo("");
            this.versionColumn = p.accepts("rev").withRequiredArg().ofType(String.class).defaultsTo("");
            this.locale = p.accepts("locale").withRequiredArg().ofType(String.class).defaultsTo("");
            this.datePattern = p.accepts("datePattern").withRequiredArg().ofType(String.class).defaultsTo("");
            this.numberPattern = p.accepts("numberPattern").withRequiredArg().ofType(String.class).defaultsTo("");
            this.frequency = p.accepts("freq").withRequiredArg().ofType(TsFrequency.class).defaultsTo(TsFrequency.Undefined);
            this.aggregationType = p.accepts("aggregation").withRequiredArg().ofType(TsAggregationType.class).defaultsTo(TsAggregationType.None);
        }

        @Override
        public OdbcBean value(OptionSet o) {
            OdbcBean result = new OdbcBean();
            result.setDbName(dbName.value(o));
            result.setTableName(tableName.value(o));
            result.setDimColumns(Joiner.on(',').join(dimColumns.values(o)));
            result.setPeriodColumn(periodColumn.value(o));
            result.setValueColumn(valueColumn.value(o));
            result.setVersionColumn(versionColumn.value(o));
            result.setDataFormat(DataFormat.create(locale.value(o), datePattern.value(o), numberPattern.value(o)));
            result.setFrequency(frequency.value(o));
            result.setAggregationType(aggregationType.value(o));
            return result;
        }
    }
}
