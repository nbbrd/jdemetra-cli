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

import com.google.common.base.Stopwatch;
import static com.google.common.net.MediaType.JSON_UTF_8;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public abstract class StandardApp<T> {

    abstract public void exec(@Nonnull T params) throws Exception;

    @Nonnull
    abstract protected StandardOptions getStandardOptions(@Nonnull T params);

    protected void printHelp(@Nonnull PrintStream stream, @Nonnull ArgsParser<T> parser) {
        printVersion(stream);
        parser.printHelp(System.out);
    }

    protected void printVersion(@Nonnull PrintStream stream) {
        Utils.printVersion(getClass(), stream);
    }

    protected void printParams(@Nonnull T params, @Nonnull PrintStream stream) {
        BasicSerializer serializer = BasicSerializer.of(JSON_UTF_8, params.getClass(), true);
        try {
            serializer.serialize(params, stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        stream.println();
    }

    public final void run(@Nonnull String[] args, @Nonnull ArgsParser<T> parser) {
        T params = null;
        try {
            params = parser.parse(args);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }

        StandardOptions so = getStandardOptions(params);

        if (so.isShowHelp()) {
            printHelp(System.out, parser);
            System.exit(0);
        }

        if (so.isShowVersion()) {
            printVersion(System.out);
            System.exit(0);
        }

        try {
            Stopwatch stopwatch = Stopwatch.createUnstarted();
            if (so.isVerbose()) {
                printParams(params, System.err);
                stopwatch.start();
            }
            exec(params);
            if (so.isVerbose()) {
                System.err.println("Executed in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
            }
        } catch (Exception ex) {
            if (so.isVerbose()) {
                ex.printStackTrace(System.err);
            } else {
                System.err.println(ex.getMessage());
            }
            System.exit(-1);
        }
    }
}
