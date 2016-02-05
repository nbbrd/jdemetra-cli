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

import be.nbb.demetra.toolset.SaTool;
import ec.tss.TsMoniker;
import ec.tss.xml.IXmlConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Philippe Charles
 */
@XmlRootElement(name = "sa_tscollection")
public final class XmlSaTsCollection implements IXmlConverter<SaTool.SaTsCollection> {

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

    @Override
    public SaTool.SaTsCollection create() {
        SaTool.SaTsCollection result = new SaTool.SaTsCollection();
        result.setName(name);
        result.setMoniker(TsMoniker.createDynamicMoniker());
        if (items != null) {
            result.setItems(Arrays.asList(items).stream().map(XmlSaTs::create).collect(Collectors.toList()));
        } else {
            result.setItems(Collections.emptyList());
        }
        return result;
    }

    @Override
    public void copy(SaTool.SaTsCollection t) {
        name = t.getName();
        source = t.getMoniker().getSource();
        identifier = t.getMoniker().getId();
        algorithm = t.getAlgorithm();
        spec = t.getSpec();
        items = t.getItems().stream().map(o -> convert(o)).toArray(o -> new XmlSaTs[o]);
    }

    private static XmlSaTs convert(SaTool.SaTs o) {
        XmlSaTs result = new XmlSaTs();
        result.copy(o);
        return result;
    }
}
