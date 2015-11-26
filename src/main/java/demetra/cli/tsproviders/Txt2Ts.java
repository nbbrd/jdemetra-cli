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
import ec.tss.tsproviders.utils.DataFormat;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import demetra.cli.helpers.BasicCommand;
import ec.tss.tsproviders.common.txt.TxtBean;
import ec.tss.tsproviders.common.txt.TxtBean.Delimiter;
import ec.tss.tsproviders.common.txt.TxtBean.TextQualifier;
import ec.tss.tsproviders.common.txt.TxtProvider;
import java.io.File;
import java.nio.charset.Charset;
import static java.util.Arrays.asList;

/**
 * Retrieves time series from a text file such as CSV or TSV.
 *
 * @author Philippe Charles
 */
public final class Txt2Ts implements BasicCommand<Txt2Ts.Parameters> {

    public static void main(String[] args) {
        BasicCliLauncher.run(args, Parser::new, Txt2Ts::new, o -> o.so);
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public TxtBean input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        TxtProvider provider = new TxtProvider();
        XProviders.applyWorkingDir(provider);
        TsCollectionInformation result = XProviders.getTsCollection(provider, params.input, TsInformationType.All);
        params.output.writeValue(XmlTsCollection.class, result);
        provider.dispose();
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<TxtBean> input = new TxtOptionsSpec(parser);
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

    private static final class TxtOptionsSpec implements OptionsSpec<TxtBean> {

        private final OptionSpec<File> file;
        private final OptionSpec<String> locale;
        private final OptionSpec<String> datePattern;
        private final OptionSpec<String> numberPattern;
        private final OptionSpec<String> charset;
        private final OptionSpec<Delimiter> delimiter;
        private final OptionSpec<Boolean> headers;
        private final OptionSpec<Integer> skipLines;
        private final OptionSpec<TextQualifier> textQualifier;
        private final OptionSpec<TsFrequency> frequency;
        private final OptionSpec<TsAggregationType> aggregationType;
        private final OptionSpec<Boolean> cleanMissing;

        public TxtOptionsSpec(OptionParser p) {
            this.file = p.nonOptions("Input file").ofType(File.class);
            this.locale = p.acceptsAll(asList("l", "locale"), "Locale used to parse dates and numbers")
                    .withRequiredArg().ofType(String.class);
            this.datePattern = p.acceptsAll(asList("d", "datePattern"), "Pattern used to parse dates")
                    .withRequiredArg().ofType(String.class);
            this.numberPattern = p.acceptsAll(asList("n", "numberPattern"), "Pattern used to parse numbers")
                    .withRequiredArg().ofType(String.class);
            this.charset = p.accepts("charset", "charset")
                    .withRequiredArg().ofType(String.class).defaultsTo(Charset.defaultCharset().name());
            this.delimiter = p.accepts("delimiter", "delimiter")
                    .withRequiredArg().ofType(Delimiter.class).defaultsTo(Delimiter.TAB);
            this.headers = p.accepts("headers", "headers")
                    .withRequiredArg().ofType(Boolean.class).defaultsTo(true);
            this.skipLines = p.accepts("skipLines", "skipLines")
                    .withRequiredArg().ofType(Integer.class).defaultsTo(0);
            this.textQualifier = p.accepts("textQualifier", "textQualifier")
                    .withRequiredArg().ofType(TextQualifier.class).defaultsTo(TextQualifier.NONE);
            this.frequency = p.accepts("frequency", "Time series frequency")
                    .withRequiredArg().ofType(TsFrequency.class).defaultsTo(TsFrequency.Undefined);
            this.aggregationType = p.accepts("a", "Aggregation type used in conjonction with the frequency")
                    .withRequiredArg().ofType(TsAggregationType.class).defaultsTo(TsAggregationType.None);
            this.cleanMissing = p.accepts("c", "Cleans the missing values")
                    .withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.TRUE).describedAs("Clean missing");
        }

        @Override
        public TxtBean value(OptionSet o) {
            TxtBean result = new TxtBean();
            result.setFile(file.value(o));
            result.setDataFormat(DataFormat.create(locale.value(o), datePattern.value(o), numberPattern.value(o)));
            result.setCharset(Charset.forName(charset.value(o)));
            result.setDelimiter(delimiter.value(o));
            result.setHeaders(headers.value(o));
            result.setSkipLines(skipLines.value(o));
            result.setTextQualifier(textQualifier.value(o));
            result.setFrequency(frequency.value(o));
            result.setAggregationType(aggregationType.value(o));
            result.setCleanMissing(cleanMissing.value(o));
            return result;
        }
    }
}
