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
package demetra.cli.helpers;

import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.OutputOptions;
import ec.tss.TsCollectionInformation;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.IXmlInfoConverter;
import ec.tss.xml.XmlTsCollection;
import java.io.IOException;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public class XmlUtil {

    @Nonnull
    public static TsCollectionInformation readTsCollection(@Nonnull InputOptions options) throws IOException {
        return readValue(options, XmlTsCollection.class);
    }

    @Nonnull
    public static <Y, X extends IXmlConverter<Y>> Y readValue(@Nonnull InputOptions options, @Nonnull Class<X> clazz) throws IOException {
        return options.read(clazz).create();
    }

    @Nonnull
    public static void writeTsCollection(@Nonnull OutputOptions options, @Nonnull TsCollectionInformation value) throws IOException {
        writeValue(options, XmlTsCollection.class, value);
    }

    @Nonnull
    public static <Y, X extends IXmlInfoConverter<Y>> void writeValue(@Nonnull OutputOptions options, @Nonnull Class<X> clazz, @Nonnull Y value) throws IOException {
        try {
            X tmp = clazz.newInstance();
            tmp.copy(value);
            options.write(clazz, tmp);
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }
}
