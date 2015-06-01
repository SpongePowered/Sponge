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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import org.apache.commons.lang3.StringEscapeUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.text.action.SpongeClickAction;
import org.spongepowered.common.text.action.SpongeHoverAction;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * Take a Text object and print it in the xml-like format.
 */
public class TextXmlPrinter {
    private static final List<Map.Entry<TextStyle, String>> FORMAT_ENTRIES = ImmutableList.of(
            Maps.<TextStyle, String>immutableEntry(TextStyles.BOLD, "b"),
            Maps.<TextStyle, String>immutableEntry(TextStyles.ITALIC, "i"),
            Maps.<TextStyle, String>immutableEntry(TextStyles.STRIKETHROUGH, "s"),
            Maps.<TextStyle, String>immutableEntry(TextStyles.OBFUSCATED, "obfuscated"),
            Maps.<TextStyle, String>immutableEntry(TextStyles.UNDERLINE, "u")
    );

    /**
     * Return the xml representation of the given input text.
     *
     * @param input The input text
     * @param locale The locale to output as
     * @return the xml representation
     */
    public static String toHtml(Text input, Locale locale) {
        final StringBuilder ret = new StringBuilder();
        handleSingle(input, ret, locale);
        return ret.toString();
    }

    private static void handleSingle(Text node, StringBuilder build, Locale locale) {
        if (node.getColor() != TextColors.NONE) {
            build.append("<color name=\"").append(node.getColor().getName()).append("\">");
        }

        for (Map.Entry<TextStyle, String> ent : FORMAT_ENTRIES) {
            if (node.getStyle().contains(ent.getKey())) {
                build.append("<").append(ent.getValue()).append(">");
            }
        }

        boolean hasLink = false;
        boolean hasAnyClick = false;
        if (node.getClickAction().isPresent()) {
            ClickAction<?> action = node.getClickAction().get();
            if (action instanceof ClickAction.OpenUrl) {
                hasLink = true;
                build.append("<a herf=\"").append(((ClickAction.OpenUrl) action).getResult().toString()).append("\">");
            }
        }

        if ((!hasLink && node.getClickAction().isPresent())
                || node.getHoverAction().isPresent()
                || node.getShiftClickAction().isPresent()) {
            build.append("<span");
            if (node.getClickAction().isPresent()) {
                build.append(" onClick=\"");
                ClickEvent nmsEvent = SpongeClickAction.getHandle(node.getClickAction().get());
                build.append(nmsEvent.getAction().getCanonicalName()).append("('").append(nmsEvent.getValue()).append("')\"");
            }

            if (node.getHoverAction().isPresent()) {
                build.append(" onHover=\"");
                HoverEvent nmsEvent = SpongeHoverAction.getHandle(node.getHoverAction().get(), locale);
                build.append(nmsEvent.getAction().getCanonicalName()).append("('").append(nmsEvent.getValue()).append("')\"");
            }

            if (node.getShiftClickAction().isPresent()) {
                build.append(" onShiftClick=\"insert_text('");
                ShiftClickAction<?> action = node.getShiftClickAction().get();
                if (!(action instanceof ShiftClickAction.InsertText)) {
                    throw new IllegalArgumentException("Shift-click action is not an insertion. Currently not supported!");
                }
                build.append(action.getResult()).append("')\"");
            }

            build.append(">");
            hasAnyClick = true;
        }

        if (node instanceof Text.Literal) {
            build.append(StringEscapeUtils.escapeXml11(((Text.Literal) node).getContent()));
        } else if (node instanceof Text.Translatable) {
            Translation transl = ((Text.Translatable) node).getTranslation();
            build.append("<tr key=\"");
            if (transl instanceof SpongeTranslation) {
                build.append(transl.getId());
            } else {
                build.append(StringEscapeUtils.escapeXml11(transl.get(locale)));
            }

            build.append("\">");
        } else {
            throw new IllegalArgumentException("Invalid text subclass " + node.toString());
        }

        for (Text child : node.getChildren()) {
            handleSingle(child, build, locale);
        }

        if (node instanceof Text.Translatable) {
            build.append("</tr>");
        }

        if (hasLink) {
            build.append("</a>");
        } else if (hasAnyClick) {
            build.append("</span>");
        }

        for (Map.Entry<TextStyle, String> ent : FORMAT_ENTRIES) {
            if (node.getStyle().contains(ent.getKey())) {
                build.append("</").append(ent.getValue()).append(">");
            }
        }

        if (node.getColor() != TextColors.NONE) {
            build.append("</color>");
        }
    }
}
