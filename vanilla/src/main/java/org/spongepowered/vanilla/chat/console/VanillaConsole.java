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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.launch.Launch;

import java.util.function.Supplier;

public final class VanillaConsole extends SimpleTerminalConsole {

    private final DedicatedServer server;

    public VanillaConsole(DedicatedServer server) {
        this.server = server;
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        final Supplier<@Nullable CommandDispatcher<CommandSourceStack>> dispatcherProvider = () -> {
            final SpongeCommandManager manager = SpongeCommandManager.get(this.server);
            return manager == null ? null : manager.getDispatcher();
        };
        final Supplier<CommandSourceStack> commandSourceProvider = this.server::createCommandSourceStack;

        return super.buildReader(builder
            .appName(Launch.instance().platformPlugin().metadata().name().get())
            .completer(new BrigadierJLineCompleter<>(dispatcherProvider, commandSourceProvider))
            .highlighter(new BrigadierHighlighter<>(dispatcherProvider, commandSourceProvider))
            .option(LineReader.Option.COMPLETE_IN_WORD, true)); // Seems to fix trying to complete at the beginning of a word
    }

    @Override
    protected boolean isRunning() {
        return !this.server.isStopped() && this.server.isRunning();
    }

    @Override
    protected void runCommand(String command) {
        this.server.handleConsoleInput(command, this.server.createCommandSourceStack());
    }

    @Override
    protected void shutdown() {
        this.server.halt(true);
    }

}
