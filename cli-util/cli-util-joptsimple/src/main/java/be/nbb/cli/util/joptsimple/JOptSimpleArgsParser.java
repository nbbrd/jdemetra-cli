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
package be.nbb.cli.util.joptsimple;

import be.nbb.cli.util.ArgsParser;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.internal.Classes;
import joptsimple.internal.Strings;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public abstract class JOptSimpleArgsParser<T> implements ArgsParser<T> {

    protected final OptionParser parser = newOptionParser();

    abstract protected T parse(OptionSet options);

    @Override
    public T parse(String... args) throws IllegalArgumentException {
        try {
            return parse(parser.parse(args));
        } catch (OptionException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    public void printHelp(PrintStream stream) {
        try {
            parser.printHelpOn(stream);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static OptionParser newOptionParser() {
        OptionParser result = new OptionParser();
        result.formatHelpWith(new OrderedByInsertionFormatter());
        return result;
    }

    private static final class OrderedByInsertionFormatter extends BuiltinHelpFormatter {

        public OrderedByInsertionFormatter() {
            this(80, 2);
        }

        public OrderedByInsertionFormatter(int desiredOverallWidth, int desiredColumnSeparatorWidth) {
            super(desiredOverallWidth, desiredColumnSeparatorWidth);
        }

        @Override
        public String format(Map<String, ? extends OptionDescriptor> options) {
            LinkedHashSet<OptionDescriptor> sortedSet = new LinkedHashSet(options.values());
            StringBuilder result = new StringBuilder();
            UsageFactory.appendUsage(result, "Usage:", sortedSet);
            result.append(Strings.LINE_SEPARATOR);
            addRows(sortedSet);
            result.append(formattedHelpOutput());
            return result.toString();
        }

        @Override
        protected String nonOptionOutput() {
            return null;
        }

        @Override
        protected void appendTypeIndicator(StringBuilder buffer, String typeIndicator, String description, char start, char end) {
            super.appendTypeIndicator(buffer, Strings.isNullOrEmpty(description) ? typeIndicator : null, description, start, end);
        }
    }

    private static final class UsageFactory {

        private static void appendUsage(StringBuilder sb, String name, LinkedHashSet<OptionDescriptor> sortedSet) {
            sb.append(name);
            String prefix = Strings.repeat(' ', sb.length());
            int length = prefix.length();
            for (OptionDescriptor od : sortedSet) {
                String tmp = getItem(od);
                if (!tmp.isEmpty()) {
                    length += tmp.length() + 1;
                    if (length >= 80) {
                        sb.append(Strings.LINE_SEPARATOR).append(prefix);
                        length = prefix.length() + tmp.length();
                    }
                    sb.append(" ").append(tmp);
                }
            }
            sb.append(Strings.LINE_SEPARATOR);
        }

        private static String getItem(OptionDescriptor od) {
            StringBuilder sb = new StringBuilder();
            if (od.representsNonOptions()) {
                if (shouldShowNonOptionArgumentDisplay(od)) {
                    appendArgumentDescription(sb, od);
                }
            } else if (!od.isRequired()) {
                sb.append("[");
                appendDetails(sb, od);
                sb.append("]");
            } else {
                sb.append("<");
                appendDetails(sb, od);
                sb.append(">");
            }
            return sb.toString();
        }

        private static boolean shouldShowNonOptionArgumentDisplay(OptionDescriptor nonOptionDescriptor) {
            return !Strings.isNullOrEmpty(nonOptionDescriptor.description())
                    || !Strings.isNullOrEmpty(nonOptionDescriptor.argumentTypeIndicator())
                    || !Strings.isNullOrEmpty(nonOptionDescriptor.argumentDescription());
        }

        private static void appendDetails(StringBuilder sb, OptionDescriptor od) {
            if (od.requiresArgument()) {
                if (od.options().size() == 1) {
                    sb.append(dash(od.options().get(0))).append("=");
                    appendArgumentDescription(sb, od);
                } else {
                    sb.append("(");
                    appendOptionNames(sb, od.options());
                    sb.append(") ");
                    appendArgumentDescription(sb, od);
                }
            } else {
                appendOptionNames(sb, od.options());
            }
        }

        private static void appendArgumentDescription(StringBuilder sb, OptionDescriptor od) {
            sb.append("<");
            if (od.argumentDescription().isEmpty()) {
                if (!Strings.isNullOrEmpty(od.argumentTypeIndicator())) {
                    sb.append(Classes.shortNameOf(od.argumentTypeIndicator()));
                } else {
                    sb.append("string");
                }
            } else {
                sb.append(od.argumentDescription());
            }
            sb.append(">");
        }

        private static void appendOptionNames(StringBuilder sb, List<String> options) {
            sb.append(options.stream().map(UsageFactory::dash).collect(Collectors.joining(" | ")));
        }

        private static String dash(String option) {
            return (option.length() == 1 ? "-" : "--") + option;
        }
    }
    //</editor-fold>
}
