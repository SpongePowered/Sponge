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
package org.spongepowered.neoforge.mixin.core.server.console;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.command.manager.SpongeCommandManager;

/**
 * Routes the NeoForge dedicated-server console completer through Sponge's
 * suggestion-mode parse.
 *
 * <p>NeoForge's {@code ConsoleCommandCompleter} synthesises a leading
 * {@code /} and parses with the regular (non-suggestion) overload. On 12.0.3
 * that path rebuilds the reader and drops the trailing separator before the
 * completion call, so console completion after {@code "/time "} returns no
 * suggestions. Sponge's suggestion-mode parse keeps the original reader
 * (including the trailing separator) and does not fire
 * {@code ExecuteCommandEvent.Pre}, which is the behaviour the vanilla console
 * completer uses.</p>
 */
@Mixin(targets = "net.neoforged.neoforge.server.console.ConsoleCommandCompleter")
public abstract class ConsoleCommandCompleterMixin_Neo {

    @Redirect(
        method = "complete",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/CommandDispatcher;parse(Lcom/mojang/brigadier/StringReader;Ljava/lang/Object;)Lcom/mojang/brigadier/ParseResults;"
        )
    )
    private ParseResults<CommandSourceStack> spongeneo$parseAsSuggestion(
            final CommandDispatcher<CommandSourceStack> dispatcher,
            final StringReader reader,
            final Object source) {
        final CommandSourceStack commandSource = (CommandSourceStack) source;
        final String input = reader.getString();
        final String command = input.startsWith("/") ? input.substring(1) : input;
        return SpongeCommandManager.get(commandSource.getServer()).getDispatcher()
                .parse(command, commandSource, true);
    }
}
