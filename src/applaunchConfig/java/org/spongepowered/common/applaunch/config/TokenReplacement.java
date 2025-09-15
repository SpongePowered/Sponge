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
package org.spongepowered.common.applaunch.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TokenReplacement {
    private static final Pattern TOKEN_MATCH = Pattern.compile("\\$\\{([^}]+)}");

    private final Map<String, String> tokens = new HashMap<>();

    public void register(final String token, final Path replacement) {
        this.register(token, replacement.toAbsolutePath().toString().replace("\\", "\\\\"));
    }

    public void register(final String token, final String replacement) {
        this.tokens.put(token.toLowerCase(Locale.ROOT), replacement);
    }

    /**
     * Parse an environment variable-style placeholder syntax.
     *
     * <p>Variable names are case-insensitive, and detected with the
     * {@link #TOKEN_MATCH} expression.</p>
     *
     * @param input input string
     * @return string with placeholders replaced
     */
    public String replace(final String input) {
        final Matcher matcher = TokenReplacement.TOKEN_MATCH.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        final StringBuilder result = new StringBuilder();
        do {
            final String token = matcher.group(1).toLowerCase(Locale.ROOT);
            final String replacement = this.tokens.get(token);
            if (replacement == null) {
                throw new IllegalArgumentException("Unknown token: " + token);
            }
            matcher.appendReplacement(result, replacement);
        } while (matcher.find());
        matcher.appendTail(result);
        return result.toString();
    }
}
