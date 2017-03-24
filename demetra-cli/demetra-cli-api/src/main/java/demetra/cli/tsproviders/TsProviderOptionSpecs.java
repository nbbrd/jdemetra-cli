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

import be.nbb.cli.util.joptsimple.ComposedOptionSpec;
import ec.tss.tsproviders.utils.DataFormat;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import javax.annotation.Nonnull;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openide.util.NbBundle;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TsProviderOptionSpecs {

    @Nonnull
    public static ComposedOptionSpec<DataFormat> newDataFormatSpec(@Nonnull OptionParser parser) {
        return new DataFormatSpec(parser);
    }

    @Nonnull
    public static ComposedOptionSpec<TsDataBuild> newTsDataBuildSpec(@Nonnull OptionParser parser) {
        return new TsDataBuildSpec(parser);
    }

    @Nonnull
    public static ComposedOptionSpec<File> newInputFileSpec(@Nonnull OptionParser parser) {
        return new InputFileSpec(parser);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    @NbBundle.Messages({
        "dataFormat.locale=Locale used to parse dates and numbers",
        "dataFormat.datePattern=Pattern used to parse dates",
        "dataFormat.numberPattern=Pattern used to parse numbers"
    })
    private static final class DataFormatSpec implements ComposedOptionSpec<DataFormat> {

        private final OptionSpec<String> locale;
        private final OptionSpec<String> datePattern;
        private final OptionSpec<String> numberPattern;

        private DataFormatSpec(OptionParser p) {
            this.locale = p
                    .accepts("locale", Bundle.dataFormat_locale())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("locale");
            this.datePattern = p
                    .accepts("date", Bundle.dataFormat_datePattern())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("datePattern");
            this.numberPattern = p
                    .accepts("number", Bundle.dataFormat_numberPattern())
                    .withRequiredArg()
                    .ofType(String.class)
                    .describedAs("numberPattern");
        }

        @Override
        public DataFormat value(OptionSet o) {
            return DataFormat.create(locale.value(o), datePattern.value(o), numberPattern.value(o));
        }
    }

    @NbBundle.Messages({
        "tsDataBuild.frequency=Frequency of observations",
        "tsDataBuild.aggregationType=Aggregation method to use when the frequency is defined"
    })
    private static final class TsDataBuildSpec implements ComposedOptionSpec<TsDataBuild> {

        private final OptionSpec<TsFrequency> frequency;
        private final OptionSpec<TsAggregationType> aggregationType;

        private TsDataBuildSpec(OptionParser p) {
            this.frequency = p
                    .accepts("frequency", Bundle.tsDataBuild_frequency())
                    .withRequiredArg()
                    .ofType(TsFrequency.class)
                    .describedAs("freq")
                    .defaultsTo(TsFrequency.Undefined);
            this.aggregationType = p
                    .accepts("aggregation", Bundle.tsDataBuild_aggregationType())
                    .withRequiredArg()
                    .ofType(TsAggregationType.class)
                    .describedAs("aggregationType")
                    .defaultsTo(TsAggregationType.None);
        }

        @Override
        public TsDataBuild value(OptionSet o) {
            return new TsDataBuild(frequency.value(o), aggregationType.value(o));
        }
    }

    @NbBundle.Messages({
        "inputFile.file=.file=Input file"
    })
    private static final class InputFileSpec implements ComposedOptionSpec<File> {

        private final OptionSpec<File> file;

        private InputFileSpec(OptionParser p) {
            this.file = p
                    .nonOptions(Bundle.inputFile_file())
                    .ofType(File.class)
                    .describedAs("file");
        }

        @Override
        public File value(OptionSet o) {
            return o.has(file) ? file.value(o) : null;
        }
    }
    //</editor-fold>
}
