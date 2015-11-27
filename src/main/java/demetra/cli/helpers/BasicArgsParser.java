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

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Map;
import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public abstract class BasicArgsParser<T> implements ArgsParser<T> {

    protected final OptionParser parser = newOptionParser();

    abstract protected T parse(OptionSet options);

    @Override
    public T parse(String... args) throws IllegalArgumentException {
        try {
            return parse(parser.parse(args));
        } catch (OptionException ex) {
            throw new IllegalArgumentException(ex.getMessage());
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
            addRows(new LinkedHashSet(options.values()));
            return formattedHelpOutput();
        }
    }
    //</editor-fold>
}
