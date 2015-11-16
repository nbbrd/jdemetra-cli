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

import ec.tss.TsCollectionInformation;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Philippe Charles
 */
@XmlRootElement(name = "sa_tscollection")
public final class XmlSaTsCollection {

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
    @XmlElement(name = "sa_ts")
    @XmlElementWrapper(name = "data")
    public XmlSaTs[] items;

    public static XmlSaTsCollection create(TsCollectionInformation col, List<Map<String, TsData>> data, SaOptions saOptions) {
        XmlSaTsCollection result = new XmlSaTsCollection();
        result.name = col.name;
        result.source = col.moniker.getSource();
        result.identifier = col.moniker.getId();
        result.algorithm = saOptions.getAlgorithm();
        result.spec = saOptions.getSpec();
        result.items = new XmlSaTs[data.size()];
        for (int i = 0; i < result.items.length; i++) {
            result.items[i] = XmlSaTs.create(col.items.get(i), data.get(i), saOptions);
        }
        return result;
    }
}
