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
package be.nbb.cli.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
public enum AppassemblerProperty {

    APP_NAME("app.name"),
    APP_ID("app.id"),
    APP_REPO("app.repo"),
    BASEDIR("basedir");

    private final String key;

    private AppassemblerProperty(String key) {
        this.key = key;
    }

    @Nonnull
    public String key() {
        return key;
    }

    @Nullable
    public String value() {
        return System.getProperty(key);
    }

    @Override
    public String toString() {
        return key() + "=" + value();
    }
}
