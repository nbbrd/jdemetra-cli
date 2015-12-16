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
package demetra.cli.hackathon;

import ec.tss.TsInformation;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Philippe Charles
 */
@ServiceProvider(service = MyCommandTool.class)
public final class MyCommandToolImpl implements MyCommandTool {

    @Override
    public MyCommandResults create(TsInformation info, Options options) {
        MyCommandResults result = new MyCommandResults();
        result.setName(info.name);
        try {
            // TO DO

        } catch (Exception err) {
            result.setInvalidDataCause(err.getMessage());
        }
        return result;
    }

    //</editor-fold>
}
