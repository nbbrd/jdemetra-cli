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

import ec.demetra.workspace.WorkspaceItem;
import ec.demetra.workspace.file.FileWorkspace;
import ec.tss.TsMoniker;
import ec.tstoolkit.utilities.TreeOfIds;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Philippe Charles
 */
public interface WorkspaceTool {

    TreeOfIds getItemTree(FileWorkspace ws) throws IOException;

    List<CheckResult> checkContent(FileWorkspace ws) throws IOException;

    Set<TsMoniker> getMonikers(FileWorkspace ws) throws IOException;

    @lombok.Value
    static class CheckResult {

        WorkspaceItem item;
        String reason;
    }
}
