/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.cli.command.core;

import java.io.PrintStream;
import javax.annotation.Nonnull;

/**
 * http://stackoverflow.com/questions/1183876/what-are-the-best-practices-for-implementing-a-cli-tool-in-perl
 * http://www.gnu.org/prep/standards/html_node/Command_002dLine-Interfaces.html#Command_002dLine-Interfaces
 * http://initscreen.developpez.com/tutoriels/batch/apprendre-la-programmation-de-script-batch/
 *
 * @author Philippe Charles
 * @param <T>
 */
public interface OptionsParser<T> {

    @Nonnull
    T parse(@Nonnull String[] args) throws IllegalArgumentException;

    void printHelp(@Nonnull PrintStream stream);
}
