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
package demetra.xml;

import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlTsPeriod;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import ec.tstoolkit.timeseries.regression.OutlierType;

/**
 *
 * @author Philippe Charles
 */
public final class XmlOutlierEstimation implements IXmlConverter<OutlierEstimation> {

    public XmlTsPeriod position;
    public String code;
    public double stdev;
    public double value;

    @Override
    public OutlierEstimation create() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void copy(OutlierEstimation t) {
        position = new XmlTsPeriod();
        position.copy(t.getPosition());
        code = t.getCode();
        stdev = t.getStdev();
        value = t.getValue();
    }
}
