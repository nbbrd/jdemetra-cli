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

import ec.util.chart.impl.SmartColorScheme;
import java.io.File;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class Ts2ChartTest {

    @Test
    public void testParser() {
        Ts2Chart.Parser p = new Ts2Chart.Parser();

        assertThatThrownBy(() -> p.parse()).isInstanceOf(IllegalArgumentException.class);
        assertThat(p.parse("img.jpeg").outputFile).isEqualTo(new File("img.jpeg"));

        assertThat(p.parse("img.jpeg").chart)
                .isEqualTo(new ChartTool.Options(400, 300, SmartColorScheme.NAME, "", true));
        assertThat(p.parse("img.jpeg", "-w=100", "-h=200", "-c=hello", "-t=my_title", "-l=false").chart)
                .isEqualTo(new ChartTool.Options(100, 200, "hello", "my_title", false));
    }
}
