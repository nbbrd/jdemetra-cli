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
package demetra.cli.various;

import static com.google.common.net.MediaType.XML_UTF_8;
import demetra.cli.helpers.InputOptions;
import demetra.cli.helpers.OutputOptions;
import demetra.cli.helpers.StandardOptions;
import demetra.cli.tsproviders.ProviderToolImpl;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformationType;
import ec.tss.tsproviders.common.random.RandomBean;
import ec.tss.tsproviders.common.random.RandomProvider;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class TsFilterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    static TsCollectionInformation getSample() {
        RandomBean bean = new RandomBean();
        bean.setCount(3);
        bean.setLength(24);
        return new ProviderToolImpl().getTsCollection(new RandomProvider(), bean, TsInformationType.All);
    }

    static void write(File file, TsCollectionInformation col) throws IOException {
        OutputOptions.of(file, XML_UTF_8, false).writeValue(XmlTsCollection.class, col);
    }

    static TsCollectionInformation read(File file) throws IOException {
        return InputOptions.of(file, XML_UTF_8).readValue(XmlTsCollection.class);
    }

    @Test
    public void testExec() throws Exception {
        TsFilter app = new TsFilter();

        File in = folder.newFile();
        File out = folder.newFile();

        write(in, getSample());

        TsFilter.Parameters options = new TsFilter.Parameters();
        options.input = InputOptions.of(in, XML_UTF_8);
        options.output = OutputOptions.of(out, XML_UTF_8, false);
        options.so = new StandardOptions(false, false, false);
        options.periodSelector = new TsPeriodSelector();
        options.itemsToRemove = EnumSet.noneOf(TsItem.class);

        app.exec(options);

        TsCollectionInformation result = read(out);

        Assertions.assertThat(result.items).hasSize(3);
    }

    @Test
    public void testRemoveItems() {
        TsCollectionInformation info = getSample();
        TsFilter.removeItems(info, EnumSet.noneOf(TsItem.class));

        Assertions.assertThat(info.items).hasSize(3);
    }
}
