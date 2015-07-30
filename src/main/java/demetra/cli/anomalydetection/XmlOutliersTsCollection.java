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

import ec.tss.TsCollectionInformation;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Philippe Charles
 */
@XmlRootElement(name = "outliers_tscollection")
public final class XmlOutliersTsCollection {

    @XmlAttribute
    public String name;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String identifier;
    @XmlElement(name = "outliers_ts")
    @XmlElementWrapper(name = "data")
    public XmlOutliersTs[] items;

    public static XmlOutliersTsCollection create(TsCollectionInformation col, List<OutlierEstimation[]> data) {
        XmlOutliersTsCollection result = new XmlOutliersTsCollection();
        result.name = col.name;
        result.source = col.moniker.getSource();
        result.identifier = col.moniker.getId();
        result.items = new XmlOutliersTs[data.size()];
        for (int i = 0; i < result.items.length; i++) {
            result.items[i] = XmlOutliersTs.create(col.items.get(i), data.get(i));
        }
        return result;
    }
}
