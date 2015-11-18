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
@XmlRootElement(name = "outliers_tscollection")
public final class XmlOutliersTsCollection implements IXmlConverter<OutliersTsCollection> {

    @XmlAttribute
    public String name;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String identifier;
    @XmlElement(name = "outliers_ts")
    @XmlElementWrapper(name = "data")
    public XmlOutliersTs[] items;

    @Override
    public OutliersTsCollection create() {
        OutliersTsCollection result = new OutliersTsCollection();
        result.setName(name);
        result.setMoniker(new TsMoniker(source, identifier));
        if (items != null) {
            result.setItems(Arrays.asList(items).stream().map(XmlOutliersTs::create).collect(Collectors.toList()));
        } else {
            result.setItems(Collections.emptyList());
        }
        return result;
    }

    @Override
    public void copy(OutliersTsCollection t) {
        name = t.getName();
        source = t.getMoniker().getSource();
        identifier = t.getMoniker().getId();
        items = t.getItems().stream().map(XmlOutliersTsCollection::convert).toArray(o -> new XmlOutliersTs[o]);
    }

    private static XmlOutliersTs convert(OutliersTs o) {
        XmlOutliersTs result = new XmlOutliersTs();
        result.copy(o);
        return result;
    }
}
