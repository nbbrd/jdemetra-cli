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
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Philippe Charles
 */
public abstract class QuickAdapter<X> extends XmlAdapter<IXmlConverter<X>, X> {

    protected abstract IXmlConverter<X> newObj();

    @Override
    public X unmarshal(IXmlConverter<X> v) throws Exception {
        return v.create();
    }

    @Override
    public IXmlConverter<X> marshal(X v) throws Exception {
        IXmlConverter<X> result = newObj();
        result.copy(v);
        return result;
    }
}
