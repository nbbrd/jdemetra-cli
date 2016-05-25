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
package be.nbb.cli.util.jackson;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.spi.FileTypeDetector;
import java.util.Locale;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = FileTypeDetector.class)
public final class JacksonFileTypeDetector extends FileTypeDetector {

    @Override
    public String probeContentType(Path path) throws IOException {
        switch (getExtension(path)) {
            case "xml":
                return "text/xml";
            case "json":
                return "application/json";
            case "yaml":
            case "yml":
                return "application/yaml";
            default:
                return null;
        }
    }

    private static String getExtension(Path file) {
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        int index = fileName.lastIndexOf(".");
        return index != -1 ? fileName.substring(index + 1) : "";
    }
}
