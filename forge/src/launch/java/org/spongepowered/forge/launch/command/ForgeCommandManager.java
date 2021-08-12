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
package org.spongepowered.forge.launch.command;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.registrar.CommandRegistrar;
import org.spongepowered.common.command.brigadier.SpongeStringReader;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.command.registrar.BrigadierBasedRegistrar;
import org.spongepowered.common.command.sponge.SpongeCommand;

import java.util.Collections;

public final class ForgeCommandManager extends SpongeCommandManager {

    @Inject
    public ForgeCommandManager(final Game game, final Provider<SpongeCommand> spongeCommand) {
        super(game, spongeCommand);
    }

    @Override
    protected CommandResult processCommand(final CommandCause cause, final CommandMapping mapping,
            final String original, final String command, final String args)
            throws Throwable {
        final CommandRegistrar<?> registrar = mapping.registrar();
        final boolean isBrig = registrar instanceof BrigadierBasedRegistrar;
        final ParseResults<CommandSourceStack> parseResults;
        if (isBrig) {
            parseResults = this.getDispatcher().parse(original, (CommandSourceStack) cause);
        } else {
            // We have a non Brig registrar, so we just create a dummy result for mods to inspect.
            final CommandContextBuilder<CommandSourceStack> contextBuilder = new CommandContextBuilder<>(
                    this.getDispatcher(),
                    (CommandSourceStack) cause,
                    this.getDispatcher().getRoot(),
                    0);
            contextBuilder.withCommand(ctx -> 1);
            if (!args.isEmpty()) {
                contextBuilder.withArgument("parsed", new ParsedArgument<>(command.length(), original.length(), args));
            }
            parseResults = new ParseResults<>(contextBuilder, new SpongeStringReader(original), Collections.emptyMap());
        }

        // Relocated from Commands (injection short circuits it there)
        final CommandEvent event = new CommandEvent(parseResults);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            if (event.getException() != null) {
                Throwables.throwIfUnchecked(event.getException());
            }
            // As per Forge, we just treat it as a zero success, and do nothing with it.
            return CommandResult.builder().result(0).build();
        }

        if (isBrig) {
            return CommandResult.builder().result(this.getDispatcher().execute(parseResults)).build();
        } else {
            return mapping.registrar().process(cause, mapping, command, args);
        }
    }

}
