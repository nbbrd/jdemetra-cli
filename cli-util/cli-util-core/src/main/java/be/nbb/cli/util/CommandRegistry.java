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

import com.google.common.annotations.VisibleForTesting;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Builder;
import lombok.Value;
import org.openide.util.NbBundle;

/**
 *
 * @author Philippe Charles
 */
@Value
@Builder
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

    private final String name;
    private final Collection<? extends Command> commands;

    public void exec(@Nonnull String[] args) {
        if (args.length == 0) {
            printUsage(System.out);
            printAvailableCommands(System.out);
        } else {
            Optional<? extends Command> cp = commands.stream().filter(o -> o.getName().equals(args[0])).findFirst();
            if (cp.isPresent()) {
                cp.get().exec(Arrays.copyOfRange(args, 1, args.length));
            } else {
                printNotFound(System.err, args[0]);
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private void printUsage(PrintStream stream) {
        stream.println(Bundle.commandRegistry_usage(name));
    }

    private void printAvailableCommands(PrintStream stream) {
        stream.println(Bundle.commandRegistry_available());
        commands.stream().forEach((o) -> {
            stream.println("\t" + o.getName());
        });
    }

    private void printNotFound(PrintStream stream, String item) {
        stream.println(Bundle.commandRegistry_invalid(name, item));
        Predicate<String> bitapFilter = new BitapFilter(item, 1);
        List<Command> possibleCommands = commands.stream().filter(o -> bitapFilter.test(o.getName())).collect(Collectors.toList());
        if (possibleCommands.isEmpty()) {
            printAvailableCommands(stream);
        } else {
            stream.println(Bundle.commandRegistry_found());
            possibleCommands.forEach((o) -> {
                stream.println("\t" + o.getName());
            });
        }
    }

    // https://en.wikipedia.org/wiki/Bitap_algorithm
    @VisibleForTesting
    static final class BitapFilter implements Predicate<String> {

        private final int alphabetRange = 128;
        private final long[] r;
        private final long[] patternMask;
        private final int patternLength;
        private final int k;

        public BitapFilter(String pattern, int k) {
            /* Initialize the bit array R */
            this.r = new long[k + 1];
            for (int i = 0; i <= k; i++) {
                r[i] = 1;
            }
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
