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
package demetra.cli.workspace;

import ec.demetra.workspace.WorkspaceFamily;
import ec.demetra.workspace.WorkspaceItem;
import ec.demetra.workspace.file.FileWorkspace;
import ec.tss.DynamicTsVariable;
import ec.tss.Ts;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tss.documents.TsDocument;
import ec.tss.modelling.documents.RegArimaDocument;
import ec.tss.modelling.documents.TramoDocument;
import ec.tss.sa.SaItem;
import ec.tss.sa.SaProcessing;
import ec.tss.sa.documents.TramoSeatsDocument;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.LinearId;
import ec.tstoolkit.utilities.TreeOfIds;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
final class WorkspaceToolImpl implements WorkspaceTool {

    @Override
    public TreeOfIds getItemTree(FileWorkspace ws) throws IOException {
        return new TreeOfIds(ws.getItems().stream()
                .map(o -> LinearId.of(o.getFamily()).extend(o.getLabel()))
                .collect(Collectors.toList()));
    }

    @Override
    public List<CheckResult> checkContent(FileWorkspace ws) throws IOException {
        return ws.getItems().stream()
                .map(o -> checkContent(ws, o))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Set<TsMoniker> mapMonikers(FileWorkspace ws) throws IOException {
        Set<TsMoniker> result = new HashSet<>();
        for (WorkspaceItem item : ws.getItems()) {
            mapMonikers(ws, item).forEach(result::add);
        }
        return result;
    }

    @Override
    public void remapMonikers(FileWorkspace ws, Map<TsMoniker, TsMoniker> remapping) throws IOException {
        for (WorkspaceItem item : ws.getItems()) {
            remapMonikers(ws, item, remapping);
        }
    }

    private static Stream<TsMoniker> mapMonikers(FileWorkspace ws, WorkspaceItem item) throws IOException {
        WorkspaceFamily family = item.getFamily();
        if (family.equals(WorkspaceFamily.UTIL_VAR)) {
            TsVariables value = (TsVariables) ws.load(item);
            return Stream.of(value.getNames())
                    .map(value::get)
                    .filter(DynamicTsVariable.class::isInstance)
                    .map(o -> ((DynamicTsVariable) o).getMoniker());
        }
        if (family.equals(WorkspaceFamily.SA_MULTI)) {
            SaProcessing value = (SaProcessing) ws.load(item);
            return value.stream()
                    .map(o -> getUnfreezedMoniker(o.getTs()));
        }
        if (family.equals(WorkspaceFamily.SA_DOC_TRAMOSEATS)) {
            TramoSeatsDocument value = (TramoSeatsDocument) ws.load(item);
            return Stream.of(getMoniker(value));
        }
        if (family.equals(WorkspaceFamily.SA_DOC_X13)) {
            X13Document value = (X13Document) ws.load(item);
            return Stream.of(getMoniker(value));
        }
        if (family.equals(WorkspaceFamily.MOD_DOC_REGARIMA)) {
            RegArimaDocument value = (RegArimaDocument) ws.load(item);
            return Stream.of(getMoniker(value));
        }
        if (family.equals(WorkspaceFamily.MOD_DOC_TRAMO)) {
            TramoDocument value = (TramoDocument) ws.load(item);
            return Stream.of(getMoniker(value));
        }
        return Stream.empty();
    }

    private static TsMoniker getMoniker(TsDocument<?, ?> doc) {
        return getUnfreezedMoniker(doc.getInput());
    }

    private static TsMoniker getUnfreezedMoniker(Ts ts) {
        TsMoniker m_moniker = ts.getMoniker();
        MetaData m_metadata = ts.getMetaData();
        if (!m_moniker.isAnonymous() || m_metadata == null) {
            return m_moniker;
        }
        String source = m_metadata.get(MetaData.SOURCE);
        if (source == null) {
            source = m_metadata.get(Ts.SOURCE_OLD);
        }
        String id = m_metadata.get(MetaData.ID);
        if (id == null) {
            id = m_metadata.get(Ts.ID_OLD);
        }
        if (source == null || id == null) {
            return m_moniker;
        } else {
            return new TsMoniker(source, id);
        }
    }

    private static void remapMonikers(FileWorkspace ws, WorkspaceItem item, Map<TsMoniker, TsMoniker> remapping) throws IOException {
        WorkspaceFamily family = item.getFamily();
        if (family.equals(WorkspaceFamily.UTIL_VAR)) {
            TsVariables value = (TsVariables) ws.load(item);
            remapMonikers(value, remapping);
            ws.store(item, value);
        }
        if (family.equals(WorkspaceFamily.SA_MULTI)) {
            SaProcessing value = (SaProcessing) ws.load(item);
            remapMonikers(value, remapping);
            ws.store(item, value);
        }
        if (family.equals(WorkspaceFamily.SA_DOC_TRAMOSEATS)) {
            TramoSeatsDocument value = (TramoSeatsDocument) ws.load(item);
            remapMoniker(value, remapping);
            ws.store(item, value);
        }
        if (family.equals(WorkspaceFamily.SA_DOC_X13)) {
            X13Document value = (X13Document) ws.load(item);
            remapMoniker(value, remapping);
            ws.store(item, value);
        }
        if (family.equals(WorkspaceFamily.MOD_DOC_REGARIMA)) {
            RegArimaDocument value = (RegArimaDocument) ws.load(item);
            remapMoniker(value, remapping);
            ws.store(item, value);
        }
        if (family.equals(WorkspaceFamily.MOD_DOC_TRAMO)) {
            TramoDocument value = (TramoDocument) ws.load(item);
            remapMoniker(value, remapping);
            ws.store(item, value);
        }
    }

    private static void remapMonikers(TsVariables variables, Map<TsMoniker, TsMoniker> remapping) {
        for (String name : variables.getNames()) {
            ITsVariable var = variables.get(name);
            if (variables.get(name) instanceof DynamicTsVariable) {
                TsMoniker origin = ((DynamicTsVariable) var).getMoniker();
                TsMoniker destination = remapping.get(origin);
                if (destination != null && !origin.equals(destination)) {
                    variables.remove(name);
                    variables.set(name, new DynamicTsVariable(var.getDescription(), destination));
                }
            }
        }
    }

    private static void remapMonikers(SaProcessing sa, Map<TsMoniker, TsMoniker> remapping) {
        for (SaItem o : sa.toArray()) {
            TsMoniker origin = getUnfreezedMoniker(o.getTs());
            TsMoniker destination = remapping.get(origin);
            if (destination != null && !origin.equals(destination)) {
                sa.replace(o, withMoniker(o, destination));
            }
        }
    }

    private static void remapMoniker(TsDocument<?, ?> doc, Map<TsMoniker, TsMoniker> remapping) {
        TsMoniker origin = getMoniker(doc);
        TsMoniker destination = remapping.get(origin);
        if (destination != null && !origin.equals(destination)) {
            doc.setInput(withMoniker(doc.getInput(), destination));
        }
    }

    private static SaItem withMoniker(SaItem o, TsMoniker destination) {
        SaItem result = new SaItem(o.getDomainSpecification(), o.getEstimationPolicy(), o.getDomainSpecification().equals(o.getEstimationSpecification()) ? null : o.getEstimationSpecification(), withMoniker(o.getTs(), destination));
        result.setPointSpecification(o.getPointSpecification());
        result.setLocked(o.isLocked());
        result.setPriority(o.getPriority());
        result.setQuality(o.getQuality());
        result.setStatus(o.getStatus());
        result.setMetaData(o.getMetaData());
        result.setName(o.getRawName());
        return result;
    }

    private static Ts withMoniker(Ts o, TsMoniker destination) {
        return TsFactory.instance.createTs(o.getRawName(), destination, TsInformationType.None);
    }

    private static CheckResult checkContent(FileWorkspace ws, WorkspaceItem item) {
        try {
            Path file = ws.getFile(item);
            if (!Files.exists(file)) {
                return new CheckResult(item, "File '" + file + "' is missing");
            }
            if (Files.isDirectory(file)) {
                return new CheckResult(item, "File '" + file + "' is a directory");
            }
            if (!Files.isReadable(file)) {
                return new CheckResult(item, "File '" + file + "' is not readable");
            }
            try {
                ws.load(item);
            } catch (IOException ex) {
                return new CheckResult(item, "File '" + file + "' not loadable");
            }
            return null;
        } catch (IOException ex) {
            return new CheckResult(item, "Cannot resolve file");
        }
    }
}
