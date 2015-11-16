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
package demetra.cli.sa;

import ec.tss.TsInformation;
import ec.tss.xml.XmlTsData;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Philippe Charles
 */
public final class XmlSaTs {

    @XmlAttribute
    public String name;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String identifier;
    @XmlAttribute
    public String algorithm;
    @XmlAttribute
    public String spec;
    @XmlElement(name = "sa")
    public XmlTsData[] values;

    public static XmlSaTs create(TsInformation ts, Map<String, TsData> data, SaOptions saOptions) {
        XmlSaTs result = new XmlSaTs();
        result.name = ts.name;
        result.source = ts.moniker.getSource();
        result.identifier = ts.moniker.getId();
        result.algorithm = saOptions.getAlgorithm();
        result.spec = saOptions.getSpec();
        if (ts.hasData() && !ts.data.isEmpty()) {
            List<XmlTsData> tmp = new ArrayList<>();
            for (Entry<String, TsData> o : data.entrySet()) {
                XmlTsData item = new XmlTsData();
                item.copy(o.getValue());
                item.name = o.getKey();
                tmp.add(item);
            }
            result.values = tmp.toArray(new XmlTsData[tmp.size()]);
        }
        return result;
    }
}
