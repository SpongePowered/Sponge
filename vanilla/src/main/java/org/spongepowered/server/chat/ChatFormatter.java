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
package org.spongepowered.server.chat;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public final class ChatFormatter {

    private static final String DEFAULT_SCHEME = "http://";

    private static final String SCHEME = "https?://";
    private static final String IP_ADDRESS = "(?:\\d{1,3}\\.){3}\\d{1,3}";
    private static final String DOMAIN = "(?:[a-z\\d](?:[a-z\\d-]*[a-z\\d])?\\.)+[a-z](?:[a-z\\d-]*[a-z\\d])?";
    private static final String PORT = "\\d{1,5}";
    private static final String PATH = ".*?";

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(?:" + SCHEME + ")?(?:" + IP_ADDRESS + '|' + DOMAIN + ")(?::" + PORT + ")?" + PATH + "(?=[!?,;:\"']?(?:[§\\s]|$))",
            Pattern.CASE_INSENSITIVE);

    private ChatFormatter() {
    }

    public static void formatChatComponent(TranslationTextComponent component) {
        String message = (String) component.getFormatArgs()[1];
        ITextComponent formatted = format(message);
        if (formatted == null) {
            return;
        }

        formatted.getStyle().setParentStyle(component.getStyle());
        component.getFormatArgs()[1] = formatted;
    }

    @Nullable
    public static ITextComponent format(String s) {
        Matcher matcher = URL_PATTERN.matcher(s);
        if (!matcher.find()) {
            return null;
        }

        ITextComponent result = null;

        int pos = 0;
        do {
            int start = matcher.start();
            int end = matcher.end();

            String displayUrl = s.substring(start, end);
            String url = displayUrl;

            try {
                if (new URI(url).getScheme() == null) {
                    url = DEFAULT_SCHEME + url;
                }
            } catch (URISyntaxException e) {
                continue; // Invalid URL so just ignore it
            }

            if (pos < start) {
                if (result == null) {
                    result = new StringTextComponent(s.substring(pos, start));
                } else {
                    result.appendText(s.substring(pos, start));
                }
            }

            pos = end;

            ITextComponent link = new StringTextComponent(displayUrl);
            link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

            if (result == null) {
                result = new StringTextComponent("");
            }

            result.appendSibling(link);

        } while (matcher.find());

        // If there is something left, append the rest
        if (pos < s.length()) {
            if (result == null) {
                result = new StringTextComponent(s.substring(pos));
            } else {
                result.appendText(s.substring(pos));
            }
        }

        return result;
    }

}
