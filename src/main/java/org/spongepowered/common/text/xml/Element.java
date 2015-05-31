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

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.common.interfaces.text.IMixinClickEvent;
import org.spongepowered.common.interfaces.text.IMixinHoverEvent;
import org.spongepowered.common.text.SpongeTexts;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({
        A.class,
        B.class,
        Color.class,
        I.class,
        Obfuscated.class,
        Strikethrough.class,
        Span.class,
        Tr.class,
        U.class
    })
public abstract class Element {

    @XmlAttribute
    private String onClick = null;

    @XmlAttribute
    private String onShiftClick = null;

    @XmlAttribute
    private String onHover = null;

    @XmlElementRef(type = Element.class)
    @XmlMixed
    protected List<Object> mixedContent = new ArrayList<Object>();

    protected abstract void modifyBuilder(TextBuilder builder);

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("^([^(]+)\\('(.*)'\\)$");

    protected void applyTextActions(TextBuilder builder) throws Exception {
        if (this.onClick != null) {
            final Matcher matcher = FUNCTION_PATTERN.matcher(this.onClick);
            if (!matcher.matches()) {
                throw new RuntimeException("Invalid click handler");
            }

            final String eventType = matcher.group(1);
            final String eventString = matcher.group(2);

            final ClickEvent.Action enumClickAction = ClickEvent.Action.getValueByCanonicalName(eventType.toLowerCase());
            if (enumClickAction == null) {
                throw new RuntimeException("Unknown click action " + eventType);
            }

            builder.onClick(((IMixinClickEvent) new ClickEvent(enumClickAction, eventString)).getHandle());
        }

        if (this.onShiftClick != null) {
            final Matcher matcher = FUNCTION_PATTERN.matcher(this.onShiftClick);
            if (!matcher.matches()) {
                throw new RuntimeException("Invalid click handler");
            }

            final String eventType = matcher.group(1);
            final String eventString = matcher.group(2);

            if (!eventType.equalsIgnoreCase("insert_text")) {
                throw new RuntimeException("Unknown click action " + eventType);
            }

            builder.onShiftClick(TextActions.insertText(eventString));
        }

        if (this.onHover != null) {
            final Matcher matcher = FUNCTION_PATTERN.matcher(this.onHover);
            if (!matcher.matches()) {
                throw new RuntimeException("Invalid hover handler");
            }

            final String eventType = matcher.group(1);
            final String eventString = matcher.group(2);
            final HoverEvent.Action enumClickAction = HoverEvent.Action.getValueByCanonicalName(eventType.toLowerCase());
            if (enumClickAction == null) {
                throw new RuntimeException("Unknown click action " + eventType);
            }

            if (enumClickAction == HoverEvent.Action.SHOW_TEXT) {
                builder.onHover(TextActions.showText(TextXmlRepresentation.INSTANCE.from(eventString)));
            } else {
                builder.onHover(((IMixinHoverEvent) new HoverEvent(enumClickAction,
                        SpongeTexts.toComponent(TextXmlRepresentation.INSTANCE.from(eventString)))).getHandle());
            }
        }

    }

    private TextBuilder builderFromString(String input) {
        return Texts.builder(input.replace('\u000B', ' '));
    }

    public TextBuilder toText() throws Exception {
        TextBuilder builder;
        if (this.mixedContent.size() == 0) {
            builder = Texts.builder();
        } else if (this.mixedContent.size() == 1) { // then we are a thin wrapper around the child
            Object child = this.mixedContent.get(0);
            if (child instanceof String) {
                builder = builderFromString(String.valueOf(child));
            } else if (child instanceof Element) {
                builder = ((Element) child).toText();
            } else {
                throw new IllegalArgumentException("What is this even? " + child);
            }
        } else {
            if (this.mixedContent.get(0) instanceof String) {
                builder = builderFromString((String) this.mixedContent.get(0));
                this.mixedContent.remove(0);
            } else {
                builder = Texts.builder();
            }
            for (Object child : this.mixedContent) {
                if (child instanceof String) {
                    builder.append(builderFromString((String) child).build());
                } else if (child instanceof Element) {
                    builder.append(((Element) child).toText().build());
                } else {
                    throw new IllegalArgumentException("What is this even? " + child);
                }
            }
        }

        modifyBuilder(builder);
        applyTextActions(builder);

        return builder;
    }
}
