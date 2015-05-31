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
package org.spongepowered.common.text.xml;

import org.spongepowered.api.text.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.StringReader;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;

public class TextXmlParser {
    private static final JAXBContext CONTEXT;

    static {
        try {
            CONTEXT = JAXBContext.newInstance(Element.class);
        } catch (JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Courtesy of http://jazzjuice.blogspot.de/2009/06/jaxb-xmlmixed-and-white-space-anomalies.html
     */
    static class WhitespaceAwareUnmarshallerHandler implements ContentHandler {

        private final UnmarshallerHandler uh;

        public WhitespaceAwareUnmarshallerHandler(UnmarshallerHandler uh) {
            this.uh = uh;
        }

        /**
         * Replace all-whitespace character blocks with the character '\u000B',
         * which satisfies the following properties:
         * <br\ >
         * 1. "\u000B".matches( "\\s" ) == true
         * 2. when parsing XmlMixed content, JAXB does not suppress the whitespace
         **/
        public void characters(
                char[] ch, int start, int length
        ) throws SAXException {
            for (int i = start + length - 1; i >= start; --i) {
                if (!Character.isWhitespace(ch[i])) {
                    this.uh.characters(ch, start, length);
                    return;
                }
            }
            Arrays.fill(ch, start, start + length, '\u000B');
            this.uh.characters(ch, start, length);
        }

        /* what follows is just blind delegation monkey code */
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            this.uh.characters(ch, start, length);
        }

        public void endDocument() throws SAXException {
            this.uh.endDocument();
        }

        public void endElement(String uri, String localName, String name) throws SAXException {
            this.uh.endElement(uri, localName, name);
        }

        public void endPrefixMapping(String prefix) throws SAXException {
            this.uh.endPrefixMapping(prefix);
        }

        public void processingInstruction(String target, String data) throws SAXException {
            this.uh.processingInstruction(target, data);
        }

        public void setDocumentLocator(Locator locator) {
            this.uh.setDocumentLocator(locator);
        }

        public void skippedEntity(String name) throws SAXException {
            this.uh.skippedEntity(name);
        }

        public void startDocument() throws SAXException {
            this.uh.startDocument();
        }

        public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
            this.uh.startElement(uri, localName, name, atts);
        }

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            this.uh.startPrefixMapping(prefix, uri);
        }
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

    public static Text parse(String xmlSource) throws Exception {
        xmlSource = "<span>" + xmlSource + "</span>";
        final Element element = unmarshal(CONTEXT, xmlSource, true);
        return element.toText().build();
    }
}
