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
package be.nbb.cli.util.jackson;

import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
enum JacksonModule {

    jackson_core("com.fasterxml.jackson.core.JsonFactory"),
    jackson_databind("com.fasterxml.jackson.databind.ObjectMapper"),
    jackson_module_jaxb_annotations("com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector"),
    jackson_datatype_jdk8("com.fasterxml.jackson.datatype.jdk8.Jdk8Module"),
    jackson_dataformat_yaml("com.fasterxml.jackson.dataformat.yaml.YAMLFactory");

    private final boolean available;

    private JacksonModule(String classPath) {
        this.available = isClassAvailable(classPath);
    }

    public boolean isAvailable() {
        return available;
    }

    private static boolean isClassAvailable(@Nonnull String classPath) {
        try {
            Class.forName(classPath);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
