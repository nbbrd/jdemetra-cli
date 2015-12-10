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
import ec.tss.TsMoniker;
import ec.tss.xml.IXmlConverter;
import ec.tstoolkit.timeseries.regression.OutlierEstimation;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Philippe Charles
 */
public final class XmlOutliersTs implements IXmlConverter<AnomalyDetectionTool.OutliersTs> {

    @XmlAttribute
    public String name;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String identifier;
    @XmlElement(name = "outlier")
    public XmlOutlierEstimation[] outliers;
    @XmlElement
    public String invalidDataCause;

    @Override
    public AnomalyDetectionTool.OutliersTs create() {
        AnomalyDetectionTool.OutliersTs result = new AnomalyDetectionTool.OutliersTs();
        result.setName(name);
        result.setMoniker(new TsMoniker(source, identifier));
        if (invalidDataCause == null) {
            result.setOutliers(Arrays.asList(outliers).stream().map(XmlOutlierEstimation::create).collect(Collectors.toList()));
        } else {
            result.setInvalidDataCause(invalidDataCause);
        }
        return result;
    }

    @Override
    public void copy(AnomalyDetectionTool.OutliersTs t) {
        name = t.getName();
        source = t.getMoniker().getSource();
        identifier = t.getMoniker().getId();
        if (t.getInvalidDataCause() == null) {
            outliers = t.getOutliers().stream().map(o -> convert(o)).toArray(o -> new XmlOutlierEstimation[o]);
        } else {
            invalidDataCause = t.getInvalidDataCause();
        }
    }

    private static XmlOutlierEstimation convert(OutlierEstimation o) {
        XmlOutlierEstimation result = new XmlOutlierEstimation();
        result.copy(o);
        return result;
    }
}
