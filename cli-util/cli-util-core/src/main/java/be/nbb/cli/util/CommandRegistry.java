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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author Philippe Charles
 */
@Value
@Builder
public final class CommandRegistry {

    private final String name;
    private final List<Command> commands;

    public void exec(@Nonnull String[] args) {
        if (args.length == 0) {
            printUsage();
            printAvailableCommands();
        } else {
            Optional<? extends Command> cp = commands.stream().filter(o -> o.getName().equals(args[0])).findFirst();
            if (cp.isPresent()) {
                cp.get().exec(Arrays.copyOfRange(args, 1, args.length));
            } else {
                printNotFound(args[0]);
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private void printUsage() {
        System.out.println(String.format("usage: %s <command> [<args>]\n", name));
    }

    private void printAvailableCommands() {
        System.out.println("Available commands:");
        commands.stream().forEach((o) -> {
            System.out.println("\t" + o.getName());
        });
    }

    private void printNotFound(String item) {
        System.err.println(String.format("%s: '%s' is not a valid command.\n", name, item));
        List<Command> possibleCommands = commands.stream().filter(o -> o.getName().contains(item)).collect(Collectors.toList());
        if (possibleCommands.isEmpty()) {
            printAvailableCommands();
        } else {
            System.err.println("Did you mean one of these?");
            possibleCommands.forEach((o) -> {
                System.err.println("\t" + o.getName());
            });
        }
    }
    //</editor-fold>
}
