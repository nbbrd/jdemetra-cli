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
package demetra.cli.anomalydetection;

import demetra.xml.XmlOutlierEstimation;
import ec.tss.TsInformation;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Philippe Charles
 */
public final class XmlOutliersTs {

    @XmlAttribute
    public String name;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String identifier;
    @XmlElement(name = "outlier")
    public XmlOutlierEstimation[] outliers;

    public static XmlOutliersTs create(TsInformation ts, OutlierEstimation[] outliers) {
        XmlOutliersTs result = new XmlOutliersTs();
        result.name = ts.name;
        result.source = ts.moniker.getSource();
        result.identifier = ts.moniker.getId();
        if (ts.hasData() && !ts.data.isEmpty()) {
            if (outliers != null) {
                result.outliers = new XmlOutlierEstimation[outliers.length];
                for (int i = 0; i < outliers.length; i++) {
                    XmlOutlierEstimation xxx = new XmlOutlierEstimation();
                    xxx.copy(outliers[i]);
                    result.outliers[i] = xxx;
                }
            } else {
                System.err.println("BUG: " + MultiLineNameUtil.join(result.name, " \\n "));
            }
        }
        return result;
    }
}
