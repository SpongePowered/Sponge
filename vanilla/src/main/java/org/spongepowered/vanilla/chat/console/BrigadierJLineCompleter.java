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

import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ComponentUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

final class BrigadierJLineCompleter<S> implements Completer {

    private final Supplier<@Nullable SpongeCommandDispatcher> dispatcherProvider;
    private final Supplier<CommandSourceStack> commandSourceProvider;

    public BrigadierJLineCompleter(final Supplier<@Nullable SpongeCommandDispatcher> dispatcherProvider, final Supplier<CommandSourceStack> commandSourceProvider) {
        this.dispatcherProvider = dispatcherProvider;
        this.commandSourceProvider = commandSourceProvider;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        final SpongeCommandDispatcher dispatcher = this.dispatcherProvider.get();
        if (dispatcher == null) {
            return;
        }

        final String input = line.line();
        final ParseResults<CommandSourceStack> parseResult = dispatcher.parse(input, this.commandSourceProvider.get(), true);
        final CompletableFuture<Suggestions> suggestions = dispatcher.getCompletionSuggestions(
            parseResult,
            line.cursor()
        );

        try {
            final Suggestions result = suggestions.get();
            for (final Suggestion completion : result.getList()) {
                if (completion.getText().isEmpty()) {
                    continue;
                }

                candidates.add(BrigadierJLineCompleter.candidateFromSuggestion(parseResult, completion));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            SpongeCommon.logger().error("Failed to tab complete", e);
        }
    }

    private static Candidate candidateFromSuggestion(final ParseResults<?> result, final Suggestion suggestion) {
        final Message tooltip = suggestion.getTooltip();
        final Component componentTooltip = tooltip == null
                ? null
                : GlobalTranslator.render(
                        SpongeAdventure.asAdventure(ComponentUtils.fromMessage(tooltip)),
                        Locale.getDefault());
        return new Candidate(
            suggestion.getText(),
            suggestion.getText(),
            null,
            ANSIComponentSerializer.ansi().serializeOrNull(componentTooltip),
            null,
            null,
            result.getExceptions().isEmpty()
        );
    }

}
