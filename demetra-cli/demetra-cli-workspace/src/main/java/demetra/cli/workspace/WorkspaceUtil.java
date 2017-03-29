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
package demetra.cli.workspace;

import be.nbb.cli.command.Command;
import be.nbb.cli.command.core.OptionsExecutor;
import be.nbb.cli.command.core.OptionsParsingCommand;
import be.nbb.cli.command.joptsimple.ComposedOptionSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.StandardOptions;
import demetra.cli.tsproviders.TsProviderOptionSpecs;
import ec.demetra.workspace.file.FileWorkspace;
import ec.tss.sa.ISaProcessingFactory;
import ec.tss.sa.SaManager;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.LinearId;
import ec.tstoolkit.utilities.TreeOfIds;
import ec.tstoolkit.utilities.Trees;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 *
 * @author Philippe Charles
 */
public final class WorkspaceUtil {

    @CommandRegistration(name = "workspace")
    static final Command CMD = OptionsParsingCommand.of(Parser::new, Executor::new, o -> o.so);

    public static final class Options {

        StandardOptions so;
        public File file;
        public boolean tree;
        public boolean check;
        public boolean monikers;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        final WorkspaceTool tool = new WorkspaceToolImpl();

        public Executor() {
            ServiceLoader.load(ISaProcessingFactory.class).forEach(SaManager.instance::add);
        }

        @Override
        public void exec(Options params) throws Exception {
            try (FileWorkspace ws = FileWorkspace.open(params.file.toPath())) {
                if (params.tree) {
                    printTree(tool, ws);
                }
                if (params.check) {
                    checkContent(tool, ws);
                }
                if (params.monikers) {
                    printMonikers(tool, ws);
                }
                if (!params.tree && !params.check && !params.monikers) {
                    System.out.println("File: " + ws.getFile());
                    System.out.println("Root folder: " + ws.getRootFolder());
                    System.out.println("File format: " + ws.getFileFormat());
                }
            }
        }

        private static void printTree(WorkspaceTool tool, FileWorkspace ws) throws IOException {
            Id root = new LinearId(ws.getName());
            TreeOfIds tree = tool.getItemTree(ws);
            Trees.prettyPrint(root,
                    o -> Stream.of(o == root ? tree.roots() : tree.children((Id) o)),
                    Integer.MAX_VALUE,
                    o -> o == root ? root.tail() : o.tail(), System.out);
        }

        private static void checkContent(WorkspaceTool tool, FileWorkspace ws) throws IOException {
            List<WorkspaceTool.CheckResult> result = tool.checkContent(ws);
            if (result.isEmpty()) {
                System.out.println("Content is valid");
            } else {
                result.stream().forEach(o -> System.out.println(o.getItem().getLabel() + " > " + o.getReason()));
            }
        }

        private static void printMonikers(WorkspaceTool tool, FileWorkspace ws) throws IOException {
            List<WorkspaceTool.MonikerRef> result = tool.getMonikers(ws);
            if (result.isEmpty()) {
                System.out.println("No moniker found");
            } else {
                result.stream()
                        .collect(Collectors.groupingBy(WorkspaceTool.MonikerRef::getItem))
                        .forEach((k, v) -> {
                            System.out.println(k.getLabel());
                            v.forEach(o -> System.out.println(" " + o.getMoniker()));
                        });
            }
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<File> file = TsProviderOptionSpecs.newInputFileSpec(parser);
        private final OptionSpec<Void> tree = parser.accepts("tree");
        private final OptionSpec<Void> check = parser.accepts("check");
        private final OptionSpec<Void> monikers = parser.accepts("monikers");

        @Override
        protected Options parse(OptionSet o) {
            Options result = new Options();
            result.so = so.value(o);
            result.file = file.value(o);
            result.tree = o.has(tree);
            result.check = o.has(check);
            result.monikers = o.has(monikers);
            return result;
        }
    }
}
