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
import be.nbb.cli.command.CommandReference;
import be.nbb.cli.command.CommandRegistry;
import be.nbb.cli.util.Utils;
import demetra.cli.helpers.Categories;
import java.io.IOException;
import java.nio.file.Paths;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.slf4j.Slf4j
@lombok.experimental.UtilityClass
public class JDemetraCli {

    public void main(String[] args) {
        initContext();
        int status = CommandRegistry.builder()
                .name(AppassemblerProperty.APP_NAME.value())
                .description("Command line interface for JDemetra+")
                .commands(Lookup.getDefault().lookupAll(CommandReference.class))
                .categories(JDemetraCli::getCategoryLabel)
                .build()
                .exec(args);
        System.exit(status);
    }

    private void initContext() {
        try {
            Utils.loadSystemProperties(Paths.get(AppassemblerProperty.BASEDIR.value(), "etc", "system.properties"));
        } catch (IOException ex) {
            log.warn("While loading system properties", ex);
        }
    }

    private String getCategoryLabel(String category) {
        switch (category) {
            case Categories.IO_CATEGORY:
                return "I/O commands";
        }
        return category;
    }
}
