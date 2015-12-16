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

import be.nbb.cli.util.CommandProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.openide.util.Lookup;

/**
 *
 * @author Philippe Charles
 */
public final class JDemetraCli {

    public static void main(String[] args) {
        Collection<? extends CommandProvider> commands = Lookup.getDefault().lookupAll(CommandProvider.class);
        if (args.length == 0) {
            printHelp(commands);
        } else {
            Optional<? extends CommandProvider> cp = commands.stream().filter(o -> o.getName().equals(args[0])).findFirst();
            if (cp.isPresent()) {
                cp.get().exec(Arrays.copyOfRange(args, 1, args.length));
            } else {
                printNotFound(commands, args[0]);
            }
        }
    }

    private static void printHelp(Collection<? extends CommandProvider> commands) {
        System.out.println("usage: dem <command> [<args>]\n");
        System.out.println("Available commands:");
        commands.stream().forEach((o) -> {
            System.out.println("\t" + o.getName());
        });
    }

    private static void printNotFound(Collection<? extends CommandProvider> commands, String item) {
        System.out.println(String.format("dem: '%s' is not a valid command.\n", item));
        System.out.println("Did you mean one of these?");
        commands.stream().filter(o -> o.getName().contains(item)).forEach((o) -> {
            System.out.println("\t" + o.getName());
        });
    }
}
