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
package demetra.cli;

import be.nbb.cli.util.AppassemblerProperty;
import be.nbb.cli.util.Command;
import be.nbb.cli.util.CommandRegistry;
import be.nbb.cli.util.Utils;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@Slf4j
public final class JDemetraCli {

    public static void main(String[] args) {
        try {
            Utils.loadSystemProperties(Paths.get(AppassemblerProperty.BASEDIR.value(), "etc", "system.properties"));
        } catch (IOException ex) {
            log.warn("While loading system properties", ex);
        }
        CommandRegistry.builder()
                .name(AppassemblerProperty.APP_NAME.value())
                .commands(new ArrayList<>(Lookup.getDefault().lookupAll(Command.class)))
                .build()
                .exec(args);
    }
}
