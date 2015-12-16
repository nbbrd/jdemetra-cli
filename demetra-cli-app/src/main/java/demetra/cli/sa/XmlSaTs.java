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

import ec.tss.TsMoniker;
import ec.tss.xml.IXmlConverter;
import ec.tss.xml.XmlTsData;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Philippe Charles
 */
public final class XmlSaTs implements IXmlConverter<SaTool.SaTs> {

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
    @XmlElement
    public String invalidDataCause;

    @Override
    public SaTool.SaTs create() {
        SaTool.SaTs result = new SaTool.SaTs();
        result.setName(name);
        result.setMoniker(TsMoniker.createDynamicMoniker());
        result.setAlgorithm(algorithm);
        result.setSpec(spec);
        if (invalidDataCause == null && values != null) {
            result.setData(Arrays.asList(values).stream().filter(o-> o.data != null ).collect(Collectors.toMap(o -> o.name, o -> o.create())));
            result.setInvalidDataCause(null);
        } else {
            result.setData(null);
            result.setInvalidDataCause(invalidDataCause);
        }
        return result;
    }

    @Override
    public void copy(SaTool.SaTs t) {
        name = t.getName();
        source = t.getMoniker().getSource();
        identifier = t.getMoniker().getId();
        algorithm = t.getAlgorithm();
        spec = t.getSpec();
        if (t.getInvalidDataCause() == null) {
            values = t.getData().entrySet().stream().map(o -> convert(o)).toArray(o -> new XmlTsData[o]);
        } else {
            invalidDataCause = t.getInvalidDataCause();
        }
    }

    private static XmlTsData convert(Entry<String, TsData> o) {
        XmlTsData result = new XmlTsData();
        result.copy(o.getValue());
        result.name = o.getKey();
        return result;
    }
}
