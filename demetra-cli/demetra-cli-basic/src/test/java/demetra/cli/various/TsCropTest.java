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

import be.nbb.cli.util.InputOptions;
import static be.nbb.cli.util.MediaType.XML_UTF_8;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import demetra.cli.helpers.XmlUtil;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.xml.XmlTsCollection;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Philippe Charles
 */
public class TsCropTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    static TsCollectionInformation getSample() {
        TsCollectionInformation result = new TsCollectionInformation();
        TsInformation ts = new TsInformation();
        ts.data = TsData.random(TsFrequency.Monthly);
        result.items.add(ts);
        return result;
    }

    static void write(File file, TsCollectionInformation col) throws IOException {
        XmlUtil.writeValue(OutputOptions.of(file, XML_UTF_8, false), XmlTsCollection.class, col);
    }

    static TsCollectionInformation read(File file) throws IOException {
        return XmlUtil.readValue(InputOptions.of(file, XML_UTF_8), XmlTsCollection.class);
    }

    @Test
    public void testExec() throws Exception {
        TsCrop app = new TsCrop();

        File in = folder.newFile();
        File out = folder.newFile();

        write(in, getSample());

        TsCrop.Parameters options = new TsCrop.Parameters();
        options.input = InputOptions.of(in, XML_UTF_8);
        options.output = OutputOptions.of(out, XML_UTF_8, false);
        options.so = new StandardOptions(false, false, false);
        options.periodSelector = new TsPeriodSelector();

        app.exec(options);

        TsCollectionInformation result = read(out);

        Assertions.assertThat(result.items).hasSize(1);
    }
}
