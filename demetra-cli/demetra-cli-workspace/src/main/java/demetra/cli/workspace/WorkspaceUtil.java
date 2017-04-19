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
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newInputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newOutputOptionsSpec;
import static be.nbb.cli.command.joptsimple.ComposedOptionSpec.newStandardOptionsSpec;
import be.nbb.cli.command.joptsimple.JOptSimpleParser;
import be.nbb.cli.command.proc.CommandRegistration;
import be.nbb.cli.util.InputOptions;
import be.nbb.cli.util.OutputOptions;
import be.nbb.cli.util.StandardOptions;
import demetra.cli.tsproviders.TsProviderOptionSpecs;
import ec.demetra.workspace.file.FileWorkspace;
import ec.tss.TsMoniker;
import ec.tss.sa.ISaProcessingFactory;
import ec.tss.sa.SaManager;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.utilities.Id;
import ec.tstoolkit.utilities.LinearId;
import ec.tstoolkit.utilities.TreeOfIds;
import ec.tstoolkit.utilities.Trees;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
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
        public OutputOptions output;
        public boolean tree;
        public boolean check;
        public boolean map;
        public boolean remap;
        public InputOptions remapping;
    }

    @VisibleForTesting
    static final class Executor implements OptionsExecutor<Options> {

        private final WorkspaceTool tool = new WorkspaceToolImpl();

        Executor() {
            ServiceLoader.load(ISaProcessingFactory.class).forEach(SaManager.instance::add);
        }

        @Override
        public void exec(Options params) throws Exception {
            try (FileWorkspace ws = FileWorkspace.open(params.file.toPath())) {
                if (params.tree) {
                    printTree(ws);
                } else if (params.check) {
                    checkContent(ws);
                } else if (params.map) {
                    mapMonikers(ws, params.output);
                } else if (params.remap) {
                    remapMonikers(ws, params.remapping);
                } else {
                    printInfo(ws);
                }
            }
        }

        private void printInfo(FileWorkspace ws) throws IOException {
            System.out.println("Index file: " + ws.getFile());
            System.out.println("Root folder: " + ws.getRootFolder());
            System.out.println("File format: " + ws.getFileFormat());
        }

        private void printTree(FileWorkspace ws) throws IOException {
            Id root = new LinearId(ws.getName());
            TreeOfIds tree = tool.getItemTree(ws);
            Trees.prettyPrint(root,
                    o -> Stream.of(o == root ? tree.roots() : tree.children((Id) o)),
                    Integer.MAX_VALUE,
                    o -> o == root ? root.tail() : o.tail(), System.out);
        }

        private void checkContent(FileWorkspace ws) throws IOException {
            List<WorkspaceTool.CheckResult> result = tool.checkContent(ws);
            if (result.isEmpty()) {
                System.out.println("Content is valid");
            } else {
                result.stream().forEach(o -> System.out.println(o.getItem().getLabel() + " > " + o.getReason()));
            }
        }

        private void mapMonikers(FileWorkspace ws, OutputOptions output) throws IOException {
            Set<TsMoniker> result = tool.mapMonikers(ws);
            output.write(XmlMonikerMap.class, XmlMonikerMap.of(result));
        }

        private void remapMonikers(FileWorkspace ws, InputOptions remapping) throws IOException {
            Map<TsMoniker, TsMoniker> map = remapping.read(XmlMonikerMap.class).toMap();
            tool.remapMonikers(ws, map);
        }
    }

    @XmlRootElement(name = "monikers")
    static final class XmlMonikerMap {

        public XmlMonikerEntry[] moniker;

        Map<TsMoniker, TsMoniker> toMap() {
            if (moniker == null) {
                return Collections.emptyMap();
            }
            return Stream.of(moniker)
                    .peek(o -> {
                        if (o.origin == null) {
                            throw new IllegalArgumentException("Origin must not be null");
                        }
                        if (o.destination == null) {
                            throw new IllegalArgumentException("Destination must not be null");
                        }
                    })
                    .collect(Collectors.toMap(o -> o.origin.toMoniker(), o -> o.destination.toMoniker()));
        }

        static XmlMonikerMap of(Set<TsMoniker> list) {
            XmlMonikerMap result = new XmlMonikerMap();
            result.moniker = list.stream()
                    .filter(o -> !o.isAnonymous())
                    .sorted()
                    .map(XmlMonikerEntry::of)
                    .toArray(XmlMonikerEntry[]::new);
            return result;
        }
    }

    static final class XmlMonikerEntry {

        public XmlMoniker origin;
        public XmlMoniker destination;

        static XmlMonikerEntry of(TsMoniker o) {
            XmlMonikerEntry result = new XmlMonikerEntry();
            result.origin = XmlMoniker.of(o);
            return result;
        }
    }

    static final class XmlMoniker {

        @XmlAttribute
        public String source;
        @XmlAttribute
        public String id;

        TsMoniker toMoniker() {
            return TsMoniker.create(source, id);
        }

        static XmlMoniker of(TsMoniker o) {
            XmlMoniker result = new XmlMoniker();
            result.source = o.getSource();
            result.id = o.getId();
            return result;
        }
    }

    @VisibleForTesting
    static final class Parser extends JOptSimpleParser<Options> {

        private final ComposedOptionSpec<StandardOptions> so = newStandardOptionsSpec(parser);
        private final ComposedOptionSpec<File> file = TsProviderOptionSpecs.newInputFileSpec(parser);
        private final ComposedOptionSpec<OutputOptions> output = newOutputOptionsSpec(parser);
        private final OptionSpec<Void> tree = parser.accepts("tree");
        private final OptionSpec<Void> check = parser.accepts("check");
        private final OptionSpec<Void> map = parser.accepts("map-monikers");
        private final OptionSpec<Void> remap = parser.accepts("remap-monikers");
        private final ComposedOptionSpec<InputOptions> remapping = newInputOptionsSpec(parser);

        @Override
        protected Options parse(OptionSet o) {
            Options result = new Options();
            result.so = so.value(o);
            result.file = file.value(o);
            result.output = output.value(o);
            result.tree = o.has(tree);
            result.check = o.has(check);
            result.map = o.has(map);
            result.remap = o.has(remap);
            result.remapping = remapping.value(o);
            return result;
        }
    }
}
