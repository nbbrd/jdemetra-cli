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
package demetra.cli.chart;

import com.google.common.net.MediaType;
import ec.tss.TsCollectionInformation;
import ec.tstoolkit.design.ServiceDefinition;
import java.io.IOException;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import lombok.Value;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@ServiceDefinition(isSingleton = true)
public interface ChartTool {

    @Value
    public static class Options {

        int width;
        int height;
        String colorScheme;
        String title;
    }

    void writeChart(@Nonnull TsCollectionInformation col, @Nonnull Options options, @Nonnull OutputStream stream, @Nonnull MediaType mediaType) throws IOException;

    @Nonnull
    public static ChartTool getDefault() {
        return Lookup.getDefault().lookup(ChartTool.class);
    }
}
