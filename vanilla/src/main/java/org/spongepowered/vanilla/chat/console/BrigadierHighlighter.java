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
package org.spongepowered.vanilla.chat.console;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.spongepowered.common.SpongeCommon;

import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class BrigadierHighlighter<S> implements Highlighter {
    // Colours sourced from Vanilla's CommandSuggestions
    private static final AttributedStyle ERROR_STYLE = AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
    private static final AttributedStyle LITERAL_STYLE = AttributedStyle.DEFAULT;
    private static final AttributedStyle[] ARGUMENT_STYLES = IntStream.of(
        AttributedStyle.CYAN,
        AttributedStyle.YELLOW,
        AttributedStyle.GREEN,
        AttributedStyle.MAGENTA,
        AttributedStyle.BLUE /* GOLD in vanilla */)
        .mapToObj(AttributedStyle.DEFAULT::foreground)
        .toArray(AttributedStyle[]::new);

    private final Supplier<@Nullable CommandDispatcher<S>> dispatcherProvider;
    private final Supplier<S> commandSourceProvider;

    public BrigadierHighlighter(final Supplier<@Nullable CommandDispatcher<S>> dispatcherProvider, final Supplier<S> commandSourceProvider) {
        this.dispatcherProvider = dispatcherProvider;
        this.commandSourceProvider = commandSourceProvider;
    }

    @Override
    public AttributedString highlight(final LineReader lineReader, final String buffer) {
        final CommandDispatcher<S> dispatcher = this.dispatcherProvider.get();
        if (dispatcher == null) {
            return new AttributedString(buffer);
        }

        try {
            final ParseResults<S> results = dispatcher.parse(buffer, this.commandSourceProvider.get());
            final ImmutableStringReader reader = results.getReader();
            final AttributedStringBuilder builder = new AttributedStringBuilder();

            int lastPos = 0;
            int argColorIdx = 0;
            for (final ParsedCommandNode<S> node : results.getContext().getLastChild().getNodes()) {
                // Sometimes Brigadier will spit out ranges that are invalid for the current input string????
                final int start = Math.min(node.getRange().getStart(), reader.getTotalLength());
                final int end = Math.min(node.getRange().getEnd(), reader.getTotalLength());
                if (lastPos < start) {
                    builder.append(reader.getString(), lastPos, start);
                }
                builder.append(reader.getString().substring(start, end), BrigadierHighlighter.ARGUMENT_STYLES[argColorIdx]);

                argColorIdx = (argColorIdx + 1) % BrigadierHighlighter.ARGUMENT_STYLES.length;
                lastPos = end;
            }

            if (lastPos < reader.getTotalLength()) {
                builder.append(
                    reader.getString().substring(lastPos),
                    results.getExceptions().isEmpty() ? BrigadierHighlighter.LITERAL_STYLE : BrigadierHighlighter.ERROR_STYLE
                );
            }

            return builder.toAttributedString();
        } catch (final Exception ex) {
            SpongeCommon.logger().warn("Error while highlighting console command line", ex);
            return new AttributedString(buffer);
        }
    }

    // TODO(zml): not sure what these methods are used for, but seems fine to have them unimplemented?

    @Override
    public void setErrorPattern(final Pattern errorPattern) {
    }

    @Override
    public void setErrorIndex(final int errorIndex) {
    }

}
