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
package demetra.cli.sa;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import demetra.cli.helpers.StandardApp;
import demetra.cli.helpers.BasicArgsParser;
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newInputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newOutputOptionsSpec;
import static demetra.cli.helpers.OptionsSpec.newStandardOptionsSpec;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tss.xml.XmlTsData;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.IBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;
import joptsimple.OptionSet;

/**
 *
 * @author Philippe Charles
 */
public final class Sa2Ts extends StandardApp<Sa2Ts.Parameters> {

    public static void main(String[] args) {
        new Sa2Ts().run(args, new Parser());
    }

    @XmlRootElement
    public static final class Parameters {

        StandardOptions so;
        public InputOptions input;
        public OutputOptions output;
    }

    @Override
    public void exec(Parameters params) throws Exception {
        XmlSaTsCollection input = params.input.read(XmlSaTsCollection.class);

        TsCollectionInformation data = process(input, params.so.isVerbose());

        params.output.writeValue(XmlTsCollection.class, data);
    }

    @Override
    protected StandardOptions getStandardOptions(Parameters params) {
        return params.so;
    }

    @VisibleForTesting
    static TsCollectionInformation process(XmlSaTsCollection input, boolean verbose) {
        if (verbose) {
            System.err.println("Processing " + input.items.length + " items");
        }
        TsCollectionInformation result = new TsCollectionInformation();
        result.metaData = new MetaDataBuilder("sa_")
                .put("algorithm", input.algorithm)
                .put("spec", input.spec)
                .put("name", input.name)
                .put("identifier", input.identifier)
                .put("source", input.source)
                .build();
        for (XmlSaTs o : input.items) {
            for (XmlTsData data : o.values) {
                TsInformation item = new TsInformation();
                item.metaData = new MetaDataBuilder("sa_")
                        .put("algorithm", o.algorithm)
                        .put("spec", o.spec)
                        .put("name", o.name)
                        .put("identifier", o.identifier)
                        .put("source", o.source)
                        .put("id", data.name)
                        .build();
                item.name = o.name + " #" + data.name;
                item.data = data.create();
                result.items.add(item);
            }
        }
        return result;
    }

    private static final class MetaDataBuilder implements IBuilder<MetaData> {

        private final String prefix;
        private final Map<String, String> map;

        public MetaDataBuilder(String prefix) {
            this.prefix = prefix;
            this.map = new HashMap<>();
        }

        public MetaDataBuilder clear() {
            map.clear();
            return this;
        }

        public MetaDataBuilder put(@Nonnull String key, @Nullable String value) {
            if (Strings.isNullOrEmpty(value)) {
                map.remove(prefix + key);
            } else {
                map.put(prefix + key, value);
            }
            return this;
        }

        @Override
        public MetaData build() {
            return new MetaData(map);
        }
    }

    @VisibleForTesting
    static final class Parser extends BasicArgsParser<Parameters> {

        private final OptionsSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final OptionsSpec<InputOptions> input = newInputOptionsSpec(parser);
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
}
