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
package be.nbb.cli.command;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.openide.util.NbBundle;

/**
 *
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
@NbBundle.Messages({
    "# {0} - app name",
    "commandRegistry.usage=usage: {0} <command> [<args>]\n",
    "commandRegistry.available=Available commands:",
    "# {0} - app name",
    "# {1} - command name",
    "commandRegistry.invalid={0}: ''{1}'' is not a valid command.\n",
    "commandRegistry.found=Did you mean one of these?"
})
public final class CommandRegistry {

    @lombok.NonNull
    String name;

    String description;

    @lombok.NonNull
    Collection<? extends Command> commands;

    @lombok.NonNull
    UnaryOperator<String> categories;

    public int exec(@Nonnull String[] args) {
        if (args.length == 0) {
            printUsage(System.out);
            printAvailableCommands(System.out);
            return 0;
        } else {
            String commandName = args[0];
            Optional<? extends Command> cp = getCommandByName(commandName);
            if (cp.isPresent()) {
                return cp.get().exec(Arrays.copyOfRange(args, 1, args.length));
            } else {
                printNotFound(System.err, commandName);
                return 0;
            }
        }
    }

    public static final class Builder {

        private UnaryOperator<String> categories = UnaryOperator.identity();
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private Optional<? extends Command> getCommandByName(String commandName) {
        return commands.stream().filter(getFilterByName(commandName)).findFirst();
    }

    private List<Command> getPossibleCommands(String query) {
        return commands.stream().filter(getFilterByQuery(query)).collect(Collectors.toList());
    }

    private SortedMap<String, List<Command>> getCommandsByCategory() {
        return commands.stream().collect(Collectors.groupingBy((Command o) -> nullToEmpty(o.getCategory()), TreeMap::new, Collectors.toList()));
    }

    private void printUsage(PrintStream stream) {
//        if (!isNullOrEmpty(description)) {
//            stream.println(description);
//        }
        stream.println(Bundle.commandRegistry_usage(name));
    }

    private void printAvailableCommands(PrintStream stream) {
        stream.println(Bundle.commandRegistry_available());
        getCommandsByCategory().forEach((category, list) -> {
            stream.println(categories.apply(category));
            list.sort(Comparator.comparing(Command::getName));
            list.forEach(o -> printCommandSummary(stream, o, getSpacer(list)));
            stream.println();
        });
    }

    private void printCommandSummary(PrintStream stream, Command command, BiConsumer<PrintStream, Command> spacer) {
        stream.append(PREFIX).append(command.getName());
        String commandDescription = command.getDescription();
        if (!isNullOrEmpty(commandDescription)) {
            spacer.accept(stream, command);
            stream.append(commandDescription);
        }
        stream.println();
    }

    private void printNotFound(PrintStream stream, String query) {
        stream.println(Bundle.commandRegistry_invalid(name, query));
        List<Command> possibleCommands = getPossibleCommands(query);
        if (possibleCommands.isEmpty()) {
            printAvailableCommands(stream);
        } else {
            stream.println(Bundle.commandRegistry_found());
            possibleCommands.forEach(o -> printCommandSummary(stream, o, getSpacer(possibleCommands)));
        }
    }

    private static Predicate<Command> getFilterByName(String name) {
        return o -> o.getName().equals(name);
    }

    private static Predicate<Command> getFilterByQuery(String query) {
        Predicate<String> bitapFilter = new BitapFilter(query, 1);
        return o -> bitapFilter.test(o.getName());
    }

    private static BiConsumer<PrintStream, Command> getSpacer(List<Command> list) {
        int maxLength = list.stream().mapToInt(o -> o.getName().length()).max().orElse(0);
        return (stream, o) -> {
            IntStream.range(0, maxLength - o.getName().length()).forEach(i -> stream.append(' '));
            stream.append(PREFIX);
        };
    }

    private static String nullToEmpty(String o) {
        return o != null ? o : "";
    }

    private static boolean isNullOrEmpty(String o) {
        return o == null || o.isEmpty();
    }

    private static final String PREFIX = "   ";

    // https://en.wikipedia.org/wiki/Bitap_algorithm
    static final class BitapFilter implements Predicate<String> {

        private final int alphabetRange = 128;
        private final long[] patternMask;
        private final int patternLength;
        private final int k;

        public BitapFilter(String pattern, int k) {
            /* Initialize the pattern bitmasks */
            this.patternMask = new long[alphabetRange];
            for (int i = 0; i < pattern.length(); ++i) {
                patternMask[(int) pattern.charAt(i)] |= 1 << i;
            }
            this.patternLength = pattern.length();
            this.k = k;
        }

        @Override
        public boolean test(String text) {
            /* Initialize the bit array R */
            long[] r = new long[k + 1];
            for (int i = 0; i <= k; i++) {
                r[i] = 1;
            }
            /* Performs test */
            for (int i = 0; i < text.length(); i++) {
                long old = 0;
                long nextOld = 0;

                for (int d = 0; d <= k; ++d) {
                    // Three operations of the Levenshtein distance
                    long sub = (old | (r[d] & patternMask[text.charAt(i)])) << 1;
                    long ins = old | ((r[d] & patternMask[text.charAt(i)]) << 1);
                    long del = (nextOld | (r[d] & patternMask[text.charAt(i)])) << 1;
                    old = r[d];
                    r[d] = sub | ins | del | 1;
                    nextOld = r[d];
                }
                // When r[k] is full of zeros, it means we matched the pattern
                // (modulo k errors)
                if (0 < (r[k] & (1 << patternLength))) {
                    return true;
                }
            }
            return false;
        }
    }
    //</editor-fold>
}
