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
package demetra.cli.dstats;

import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Philippe Charles
 */
public final class XmlDStatsTs {

    @XmlAttribute
    public String name;
    @XmlAttribute
    public String source;
    @XmlAttribute
    public String identifier;
    public Double average;
    public Integer dataCount;
    public Double kurtosis;
    public Double max;
    public Double median;
    public Double min;
    public Integer missingValuesCount;
    public Integer observationsCount;
    public Double rmse;
    public Double skewness;
    public Double stdev;
    public Double sum;
    public Double sumSquare;
    public Double var;

}
