/*
 * Copyright 2017 National Bank of Belgium
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
package be.nbb.cli.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
public final class MediaType {

    @Nonnull
    public static MediaType parse(@Nonnull String input) throws IllegalArgumentException {
        if (isEmptyOrTrimable(input)) {
            throw new IllegalArgumentException();
        }

        int subtypeIndex = input.indexOf("/");
        if (subtypeIndex == -1) {
            throw new IllegalArgumentException();
        }
        String type = input.substring(0, subtypeIndex).toLowerCase();
        if (isEmptyOrTrimable(type)) {
            throw new IllegalArgumentException();
        }

        int paramsIndex = input.indexOf(";", subtypeIndex);
        String subType = input.substring(subtypeIndex + 1, paramsIndex != -1 ? paramsIndex : input.length()).toLowerCase();
        if (isEmptyOrTrimable(subType)) {
            throw new IllegalArgumentException();
        }

        if (paramsIndex == -1) {
            return new MediaType(type, subType);
        }
        Map<String, Collection<String>> parameters = new HashMap<>();
        for (String item : input.substring(paramsIndex + 1).trim().split(";", -1)) {
            String[] tmp = item.split("=", -1);
            if (tmp.length != 2) {
                throw new IllegalArgumentException();
            }
            parameters.computeIfAbsent(tmp[0].toLowerCase(), o -> new ArrayList()).add(tmp[1]);
        }
        return new MediaType(type, subType, parameters);
    }

    private static boolean isEmptyOrTrimable(String o) {
        return o.isEmpty() || !o.trim().equals(o);
    }

    private static final Map<String, Collection<String>> UTF_8 = singleton("charset", "utf-8");
    private static final String WILDCARD = "*";

    static final MediaType ANY_TYPE = new MediaType(WILDCARD, WILDCARD);
    static final MediaType ANY_TEXT_TYPE = new MediaType("text", WILDCARD);
    static final MediaType ANY_IMAGE_TYPE = new MediaType("image", WILDCARD);
    static final MediaType PLAIN_TEXT_UTF_8 = new MediaType("text", "plain", UTF_8);
    static final MediaType HTML_UTF_8 = new MediaType("text", "html", UTF_8);

    public static final MediaType XML_UTF_8 = new MediaType("text", "xml", UTF_8);
    public static final MediaType JSON_UTF_8 = new MediaType("application", "json", UTF_8);
    public static final MediaType JPEG = new MediaType("image", "jpeg");
    public static final MediaType PNG = new MediaType("image", "png");
    public static final MediaType SVG_UTF_8 = new MediaType("image", "svg+xml", UTF_8);

    private final String type;
    private final String subtype;
    private final Map<String, Collection<String>> parameters;

    private MediaType(String type, String subtype) {
        this(type, subtype, Collections.emptyMap());
    }

    private MediaType(String type, String subtype, Map<String, Collection<String>> parameters) {
        this.type = type;
        this.subtype = subtype;
        this.parameters = parameters;
    }

    public boolean isCompatible(@Nonnull MediaType other) {
        return (other.type.equals(WILDCARD) || other.type.equals(this.type))
                && (other.subtype.equals(WILDCARD) || other.subtype.equals(this.subtype))
                && containsAll(this.parameters, other.parameters);
    }

    @Nonnull
    MediaType withCharset(@Nonnull Charset charset) {
        return new MediaType(type, subtype, singleton("charset", charset.name().toLowerCase()));
    }

    @Nonnull
    public MediaType withoutParameters() {
        return parameters.isEmpty() ? this : new MediaType(type, subtype);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder().append(type).append("/").append(subtype);
        parameters.forEach((k, v) -> v.forEach(o -> result.append("; ").append(k).append("=").append(o)));
        return result.toString();
    }

    private static Map<String, Collection<String>> singleton(String key, String value) {
        return Collections.singletonMap(key, Collections.singletonList(value));
    }

    private static boolean containsAll(Map<String, Collection<String>> l, Map<String, Collection<String>> r) {
        for (Entry<String, Collection<String>> entry : r.entrySet()) {
            Collection<String> values = l.get(entry.getKey());
            if (values == null || !values.containsAll(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
