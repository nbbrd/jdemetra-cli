/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.cli.chart;

import be.nbb.cli.util.MediaType;
import java.io.IOException;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class ChartFileTypeDetectorTest {

    @Test
    public void test() throws IOException {
        ChartFileTypeDetector d = new ChartFileTypeDetector();

        assertThat(d.probeContentType(Paths.get("img.jpeg"))).isEqualTo(MediaType.JPEG.toString());
        assertThat(d.probeContentType(Paths.get("img.jpg"))).isEqualTo(MediaType.JPEG.toString());
        assertThat(d.probeContentType(Paths.get("img.png"))).isEqualTo(MediaType.PNG.toString());
        assertThat(d.probeContentType(Paths.get("img.svg"))).isEqualTo(MediaType.SVG_UTF_8.withoutParameters().toString());
        assertThat(d.probeContentType(Paths.get("img.svgz"))).isEqualTo("image/svg+xml-compressed");
    }
}
