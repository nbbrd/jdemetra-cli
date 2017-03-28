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

import be.nbb.cli.command.Command;
import be.nbb.cli.util.MediaType;
import be.nbb.cli.util.Serializer;
import be.nbb.cli.util.SerializerFactory;
import be.nbb.cli.util.StandardOptions;
import be.nbb.cli.util.Utils;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 * @param <T>
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder")
public final class OptionsParsingCommand<T> implements Command {

    @lombok.NonNull
    String name;

    String category;

    String description;

    @lombok.NonNull
    Supplier<OptionsParser<T>> parser;

    @lombok.NonNull
    Supplier<OptionsExecutor<T>> executor;

    @lombok.NonNull
    Function<T, StandardOptions> so;

    @Override
    public int exec(String[] args) {
        OptionsParser<T> parserInstance = parser.get();
        T params = null;
        try {
            params = parserInstance.parse(args);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            return -1;
        }

        StandardOptions soInstance = so.apply(params);

        if (soInstance.isShowHelp()) {
            printHelp(System.out, parserInstance);
            return 0;
        }

        if (soInstance.isShowVersion()) {
            printVersion(System.out, executor.get());
            return 0;
        }

        try {
            long startTime = System.currentTimeMillis();
            if (soInstance.isVerbose()) {
                printParams(params, System.err);
            }
            executor.get().exec(params);
            if (soInstance.isVerbose()) {
                System.err.println("Executed in " + (System.currentTimeMillis() - startTime) + "ms");
            }
            return 0;
        } catch (Exception ex) {
            if (soInstance.isVerbose()) {
                ex.printStackTrace(System.err);
            } else {
                System.err.println(ex.getMessage());
            }
            return -1;
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static <T> void printHelp(@Nonnull PrintStream stream, @Nonnull OptionsParser<T> parser) {
        parser.printHelp(stream);
    }

    private static void printVersion(@Nonnull PrintStream stream, @Nonnull OptionsExecutor<?> command) {
        Utils.printVersion(command.getClass(), stream);
    }

    private static <T> void printParams(@Nonnull T params, @Nonnull PrintStream stream) {
        Serializer serializer = SerializerFactory.of(MediaType.JSON_UTF_8, params.getClass(), true);
        try {
            serializer.serialize(params, stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        stream.println();
    }
    //</editor-fold>
}
