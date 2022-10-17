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
package org.spongepowered.vanilla.chat;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
            "(?:" + ChatFormatter.SCHEME + ")?(?:" + ChatFormatter.IP_ADDRESS + '|' + ChatFormatter.DOMAIN + ")(?::" + ChatFormatter.PORT + ")?" + ChatFormatter.PATH + "(?=[!?,;:\"']?(?:[§\\s]|$))",
            Pattern.CASE_INSENSITIVE);

    private ChatFormatter() {
    }

    public static void formatChatComponent(final TranslatableComponent component) {
        final String message = (String) component.getArgs()[1];
        final Component formatted = ChatFormatter.format(message);
        if (formatted == null) {
            return;
        }

        component.getArgs()[1] = formatted;
    }

    @Nullable
    public static Component format(final String s) {
        final Matcher matcher = ChatFormatter.URL_PATTERN.matcher(s);
        if (!matcher.find()) {
            return null;
        }

        MutableComponent result = null;

        int pos = 0;
        do {
            final int start = matcher.start();
            final int end = matcher.end();

            final String displayUrl = s.substring(start, end);
            String url = displayUrl;

            try {
                if (new URI(url).getScheme() == null) {
                    url = ChatFormatter.DEFAULT_SCHEME + url;
                }
            } catch (final URISyntaxException e) {
                continue; // Invalid URL so just ignore it
            }

            if (pos < start) {
                if (result == null) {
                    result = new TextComponent(s.substring(pos, start));
                } else {
                    result.append(s.substring(pos, start));
                }
            }

            pos = end;

            final MutableComponent link = new TextComponent(displayUrl);
            link.setStyle(link.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));

            if (result == null) {
                result = new TextComponent("");
            }

            result.append(link);

        } while (matcher.find());

        // If there is something left, append the rest
        if (pos < s.length()) {
            if (result == null) {
                result = new TextComponent(s.substring(pos));
            } else {
                result.append(s.substring(pos));
            }
        }

        return result;
    }

}
