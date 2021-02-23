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

import static com.google.common.base.Preconditions.checkNotNull;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.dedicated.DedicatedServer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.manager.SpongeCommandManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class ConsoleCommandCompleter implements Completer {

    private final DedicatedServer server;

    ConsoleCommandCompleter(DedicatedServer server) {
        this.server = checkNotNull(server, "server");
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        /* String buffer = line.line();
        boolean prefix;
        if (buffer.isEmpty() || buffer.charAt(0) != '/') {
            buffer = '/' + buffer;
            prefix = false;
        } else {
            prefix = true;
        }*/

        final String input = line.line();
        final CommandDispatcher<CommandSourceStack> dispatcher = SpongeCommandManager.get(this.server).getDispatcher();
        final CompletableFuture<Suggestions> suggestions = dispatcher.getCompletionSuggestions(
                dispatcher.parse(input, this.server.createCommandSourceStack()),
                line.cursor()
            );

        try {
            final Suggestions result = suggestions.get();
            for (final Suggestion completion : result.getList()) {
                if (completion.getText().isEmpty()) {
                    continue;
                }

                candidates.add(ConsoleCommandCompleter.candidateFromSuggestion(input, completion));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            SpongeCommon.getLogger().error("Failed to tab complete", e);
        }
    }

    private static Candidate candidateFromSuggestion(final String input, final Suggestion suggestion) {
        final Message tooltip = suggestion.getTooltip();
        return new Candidate(suggestion.getText(), suggestion.getText(), null, tooltip == null ? null : tooltip.getString(), null, null, true);
    }

}
