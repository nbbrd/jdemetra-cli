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
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
public final class BasicCliLauncher<T> {

    private Supplier<ArgsParser<T>> parserSupplier = () -> null;
    private Supplier<BasicCommand<T>> commandSupplier = () -> null;
    private Function<T, StandardOptions> toSo = o -> new StandardOptions(false, false, false);

    @Nonnull
    public BasicCliLauncher<T> parser(@Nonnull Supplier<ArgsParser<T>> parser) {
        this.parserSupplier = parser;
        return this;
    }

    @Nonnull
    public BasicCliLauncher<T> command(@Nonnull Supplier<BasicCommand<T>> command) {
        this.commandSupplier = command;
        return this;
    }

    @Nonnull
    public BasicCliLauncher<T> standardOptions(@Nonnull Function<T, StandardOptions> toSo) {
        this.toSo = toSo;
        return this;
    }

    public void launch(@Nonnull String[] args) {
        ArgsParser<T> parser = parserSupplier.get();
        T params = null;
        try {
            params = parser.parse(args);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }

        StandardOptions so = toSo.apply(params);

        if (so.isShowHelp()) {
            printHelp(System.out, parser);
            System.exit(0);
        }

        if (so.isShowVersion()) {
            printVersion(System.out, commandSupplier.get());
            System.exit(0);
        }

        try {
            Stopwatch stopwatch = Stopwatch.createUnstarted();
            if (so.isVerbose()) {
                printParams(params, System.err);
                stopwatch.start();
            }
            commandSupplier.get().exec(params);
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

    private static <T> void printHelp(@Nonnull PrintStream stream, @Nonnull ArgsParser<T> parser) {
        parser.printHelp(stream);
    }

    private static void printVersion(@Nonnull PrintStream stream, @Nonnull BasicCommand<?> command) {
        Utils.printVersion(command.getClass(), stream);
    }

    private static <T> void printParams(@Nonnull T params, @Nonnull PrintStream stream) {
        BasicSerializer serializer = BasicSerializer.of(JSON_UTF_8, params.getClass(), true);
        try {
            serializer.serialize(params, stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        stream.println();
    }

    public static <T> void run(@Nonnull String[] args, @Nonnull Supplier<ArgsParser<T>> parser, @Nonnull Supplier<BasicCommand<T>> command, @Nonnull Function<T, StandardOptions> toSo) {
        new BasicCliLauncher<T>().parser(parser).command(command).standardOptions(toSo).launch(args);
    }
}
