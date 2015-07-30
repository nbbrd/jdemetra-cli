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
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.spreadsheet.SpreadSheetBean;
import ec.tss.tsproviders.spreadsheet.SpreadSheetProvider;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class SpreadSheet2Ts extends StandardApp<SpreadSheet2Ts.Parameters> {

    public static void main(String[] args) {
        new SpreadSheet2Ts().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public SpreadSheetBean input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        SpreadSheetProvider provider = new SpreadSheetProvider();
        XProviders.applyWorkingDir(provider);
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
        private final OptionsSpec<SpreadSheetBean> input = new SpreadSheetOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet options) {
            Parameters result = new Parameters();
            result.input = input.value(options);
            result.output = output.value(options);
            result.so = so.value(options);
            return result;
        }
    }

    private static final class SpreadSheetOptionsSpec extends OptionsSpec<SpreadSheetBean> {

        private final OptionSpec<File> inputFile;
        private final OptionSpec<String> locale;
        private final OptionSpec<String> date;
        private final OptionSpec<String> number;
        private final OptionSpec<TsFrequency> freq;
        private final OptionSpec<TsAggregationType> aggregation;
        private final OptionSpec<Boolean> clean;

        public SpreadSheetOptionsSpec(OptionParser parser) {
            this.inputFile = parser.nonOptions("Input file").ofType(File.class);
            this.locale = parser.accepts("l", "Locale used to parse dates and numbers")
                    .withRequiredArg().ofType(String.class).describedAs("Locale");
            this.date = parser.accepts("d", "Pattern used to parse dates")
                    .withRequiredArg().ofType(String.class).describedAs("Date pattern");
            this.number = parser.accepts("n", "Pattern used to parse numbers")
                    .withRequiredArg().ofType(String.class).describedAs("Number pattern");
            this.freq = parser.accepts("f", "Time series frequency")
                    .withRequiredArg().ofType(TsFrequency.class).defaultsTo(TsFrequency.Undefined);
            this.aggregation = parser.accepts("a", "Aggregation type used in conjonction with the frequency")
                    .withRequiredArg().ofType(TsAggregationType.class).defaultsTo(TsAggregationType.None);
            this.clean = parser.accepts("c", "Cleans the missing values")
                    .withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE).describedAs("Clean missing");
        }

        @Override
        public SpreadSheetBean value(OptionSet options) {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(options.has(inputFile) ? inputFile.value(options) : null);
            result.setDataFormat(DataFormat.create(locale.value(options), date.value(options), number.value(options)));
            result.setFrequency(freq.value(options));
            result.setAggregationType(aggregation.value(options));
            result.setCleanMissing(clean.value(options));
            return result;
        }
    }
}
