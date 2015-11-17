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

import com.google.common.net.MediaType;
import static demetra.cli.helpers.OutputOptionsAssert.assertThat;
import java.io.File;
import java.util.Optional;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class Random2TsTest {

    @Test
    public void testParser() {
        Random2Ts.Parser p = new Random2Ts.Parser();

        assertThat(p.parse().output)
                .isNotFormatted()
                .hasMediaType(MediaType.XML_UTF_8)
                .hasFile(Optional.empty());

        assertThat(p.parse("--format").output)
                .isFormatted();

//        assertThat(p.parse("--scope", "Data").output)
//                .hasScope(TsInformationType.Data);
        assertThat(p.parse("--output-type", "application/json").output)
                .hasMediaType(MediaType.JSON_UTF_8);

        assertThat(p.parse("--output", "hello.xml").output)
                .hasFile(Optional.of(new File("hello.xml")));

    }

}
