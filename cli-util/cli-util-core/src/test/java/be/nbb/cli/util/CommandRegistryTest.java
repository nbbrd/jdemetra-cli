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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class CommandRegistryTest {

    @Test
    public void testFilter() {
        CommandRegistry.BitapFilter filter = new CommandRegistry.BitapFilter("mycommand", 1);
        assertTrue(filter.test("mycommand"));
        assertTrue(filter.test("mycommant"));
        assertTrue(filter.test("mycomand"));
        assertTrue(filter.test("ymcommand"));
        assertFalse(filter.test("hello"));
    }
}
