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
import demetra.cli.helpers.BasicCliLauncher;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
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
import demetra.cli.helpers.BasicCommand;

/**
 * Retrieves time series from a spreadsheet file.
 *
 * @author Philippe Charles
 */
public final class SpreadSheet2Ts implements BasicCommand<SpreadSheet2Ts.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, SpreadSheet2Ts::new, o -> o.so);
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

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<SpreadSheetBean> input = new SpreadSheetOptionsSpec(parser);
        private final OptionsSpec<OutputOptions> output = newOutputOptionsSpec(parser);

        @Override
        protected Parameters parse(OptionSet o) {
            Parameters result = new Parameters();
            result.input = input.value(o);
            result.output = output.value(o);
            result.so = so.value(o);
            return result;
        }
    }

    private static final class SpreadSheetOptionsSpec implements OptionsSpec<SpreadSheetBean> {

        private final OptionSpec<File> inputFile;
        private final OptionSpec<String> locale;
        private final OptionSpec<String> date;
        private final OptionSpec<String> number;
        private final OptionSpec<TsFrequency> freq;
        private final OptionSpec<TsAggregationType> aggregation;
        private final OptionSpec<Boolean> clean;

        public SpreadSheetOptionsSpec(OptionParser p) {
            this.inputFile = p.nonOptions("Input file").ofType(File.class);
            this.locale = p.accepts("l", "Locale used to parse dates and numbers")
                    .withRequiredArg().ofType(String.class).describedAs("Locale");
            this.date = p.accepts("d", "Pattern used to parse dates")
                    .withRequiredArg().ofType(String.class).describedAs("Date pattern");
            this.number = p.accepts("n", "Pattern used to parse numbers")
                    .withRequiredArg().ofType(String.class).describedAs("Number pattern");
            this.freq = p.accepts("f", "Time series frequency")
                    .withRequiredArg().ofType(TsFrequency.class).defaultsTo(TsFrequency.Undefined);
            this.aggregation = p.accepts("a", "Aggregation type used in conjonction with the frequency")
                    .withRequiredArg().ofType(TsAggregationType.class).defaultsTo(TsAggregationType.None);
            this.clean = p.accepts("c", "Cleans the missing values")
                    .withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE).describedAs("Clean missing");
        }

        @Override
        public SpreadSheetBean value(OptionSet o) {
            SpreadSheetBean result = new SpreadSheetBean();
            result.setFile(o.has(inputFile) ? inputFile.value(o) : null);
            result.setDataFormat(DataFormat.create(locale.value(o), date.value(o), number.value(o)));
            result.setFrequency(freq.value(o));
            result.setAggregationType(aggregation.value(o));
            result.setCleanMissing(clean.value(o));
            return result;
        }
    }
}
