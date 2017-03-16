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

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class MediaTypeTest {

    @Test
    @SuppressWarnings("null")
    public void testParse() {
        assertThat(MediaType.parse("text/plain; charset=utf-8")).hasToString("text/plain; charset=utf-8");
        assertThat(MediaType.parse("TEXT/PLAIN; CHARSET=utf-8")).hasToString("text/plain; charset=utf-8");
        assertThat(MediaType.parse("text/plain")).hasToString("text/plain");

        assertThatThrownBy(() -> MediaType.parse(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> MediaType.parse("text/plain ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse(" text/plain")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("text/plain;")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("/plain")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MediaType.parse("text/")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testIsCompatible() {
        assertThat(MediaType.PLAIN_TEXT_UTF_8).satisfies(o -> {
            assertThat(o.isCompatible(MediaType.PLAIN_TEXT_UTF_8)).isTrue();
            assertThat(o.isCompatible(MediaType.HTML_UTF_8)).isFalse();
            assertThat(o.isCompatible(MediaType.ANY_TYPE)).isTrue();
            assertThat(o.isCompatible(MediaType.ANY_TEXT_TYPE)).isTrue();
            assertThat(o.isCompatible(MediaType.ANY_IMAGE_TYPE)).isFalse();
            assertThat(o.isCompatible(MediaType.ANY_TEXT_TYPE.withCharset(UTF_8))).isTrue();
            assertThat(o.withoutParameters().isCompatible(MediaType.ANY_TEXT_TYPE.withCharset(UTF_8))).isFalse();
            assertThat(o.isCompatible(MediaType.ANY_TEXT_TYPE.withCharset(UTF_16))).isFalse();
        });
    }

    @Test
    public void testWithoutParameters() {
        assertThat(MediaType.SVG_UTF_8.withoutParameters()).hasToString("image/svg+xml");
    }

    @Test
    public void testToString() {
        assertThat(MediaType.ANY_TYPE).hasToString("*/*");
        assertThat(MediaType.ANY_TEXT_TYPE).hasToString("text/*");
        assertThat(MediaType.ANY_IMAGE_TYPE).hasToString("image/*");
        assertThat(MediaType.PLAIN_TEXT_UTF_8).hasToString("text/plain; charset=utf-8");
        assertThat(MediaType.HTML_UTF_8).hasToString("text/html; charset=utf-8");

        assertThat(MediaType.XML_UTF_8).hasToString("text/xml; charset=utf-8");
        assertThat(MediaType.JSON_UTF_8).hasToString("application/json; charset=utf-8");
        assertThat(MediaType.JPEG).hasToString("image/jpeg");
        assertThat(MediaType.PNG).hasToString("image/png");
        assertThat(MediaType.SVG_UTF_8).hasToString("image/svg+xml; charset=utf-8");
    }

    @Test
    public void testCompatibilityWithGuava() {
        assertThat(MediaType.ANY_TYPE).hasToString(com.google.common.net.MediaType.ANY_TYPE.toString());
        assertThat(MediaType.ANY_TEXT_TYPE).hasToString(com.google.common.net.MediaType.ANY_TEXT_TYPE.toString());
        assertThat(MediaType.ANY_IMAGE_TYPE).hasToString(com.google.common.net.MediaType.ANY_IMAGE_TYPE.toString());
        assertThat(MediaType.PLAIN_TEXT_UTF_8).hasToString(com.google.common.net.MediaType.PLAIN_TEXT_UTF_8.toString());
        assertThat(MediaType.HTML_UTF_8).hasToString(com.google.common.net.MediaType.HTML_UTF_8.toString());

        assertThat(MediaType.XML_UTF_8).hasToString(com.google.common.net.MediaType.XML_UTF_8.toString());
        assertThat(MediaType.JSON_UTF_8).hasToString(com.google.common.net.MediaType.JSON_UTF_8.toString());
        assertThat(MediaType.JPEG).hasToString(com.google.common.net.MediaType.JPEG.toString());
        assertThat(MediaType.PNG).hasToString(com.google.common.net.MediaType.PNG.toString());
        assertThat(MediaType.SVG_UTF_8).hasToString(com.google.common.net.MediaType.SVG_UTF_8.toString());
    }
}
