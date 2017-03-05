/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.text.serializer.xml;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextParseException;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshallerHandler;

/**
 * TextXML format serializer for Text instances.
 */
public class TextXmlTextSerializer implements TextSerializer {

    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(Element.class);
        } catch (JAXBException e) {
            ExceptionInInitializerError err = new ExceptionInInitializerError("Error creating JAXB context: " + e);
            err.initCause(e);
            throw err;
        }
    }

    @Override
    public String getId() {
        return "sponge:text_xml";
    }

    @Override
    public String getName() {
        return "TextXML";
    }

    @Override
    public String serialize(Text text) {
        final StringWriter writer = new StringWriter();
        try {
            Marshaller marshaller = CONTEXT.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(Element.fromText(text), writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return writer.getBuffer().toString();
    }


    /**
     * Also courtesy of http://jazzjuice.blogspot.de/2009/06/jaxb-xmlmixed-and-white-space-anomalies.html
     */
    @SuppressWarnings("unchecked")
    private static <T> T unmarshal(JAXBContext ctx, String strData, boolean flgWhitespaceAware) throws Exception {
        UnmarshallerHandler uh = ctx.createUnmarshaller().getUnmarshallerHandler();
        XMLReader xr = XMLReaderFactory.createXMLReader();
        xr.setContentHandler(flgWhitespaceAware ? new WhitespaceAwareUnmarshallerHandler(uh) : uh);
        xr.setErrorHandler(new DefaultHandler());
        xr.parse(new InputSource(new StringReader(strData)));
        return (T) uh.getResult();
    }

    @Override
    public Text deserialize(String input) throws TextParseException {
        try {
            input = "<span>" + input + "</span>";
            final Element element = unmarshal(CONTEXT, input, true);
            return element.toText().build();
        } catch (Exception e) {
            throw new TextParseException("Error parsing TextXML message", e);
        }
    }

}
