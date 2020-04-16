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
package org.spongepowered.server.console;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.server.dedicated.DedicatedServer;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import org.spongepowered.common.SpongeImpl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

final class ConsoleCommandCompleter implements Completer {

    private final DedicatedServer server;

    ConsoleCommandCompleter(DedicatedServer server) {
        this.server = checkNotNull(server, "server");
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String buffer = line.line();
        boolean prefix;
        if (buffer.isEmpty() || buffer.charAt(0) != '/') {
            buffer = '/' + buffer;
            prefix = false;
        } else {
            prefix = true;
        }

        final String input = buffer;
        Future<List<String>> tabComplete =
                this.server.callFromMainThread(() -> this.server.getTabCompletions(this.server, input, this.server.getPosition(), false));

        try {
            for (String completion : tabComplete.get()) {
                if (completion.isEmpty()) {
                    continue;
                }

                candidates.add(new Candidate(prefix || completion.charAt(0) != '/' ? completion : completion.substring(1)));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            SpongeImpl.getLogger().error("Failed to tab complete", e);
        }
    }

}
