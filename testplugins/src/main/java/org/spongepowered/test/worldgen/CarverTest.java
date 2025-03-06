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
package org.spongepowered.test.worldgen;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.generation.carver.Carver;
import org.spongepowered.api.world.generation.carver.CarverTypes;
import org.spongepowered.api.world.generation.carver.Carvers;


public class CarverTest {

    private CommandResult carvers(final CommandContext ctx) {
        ctx.sendMessage(Identity.nil(), Component.text("Carver Types", NamedTextColor.DARK_AQUA));
        CarverTypes.registry().streamEntries().forEach(e -> {
            ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY));
        });
        ctx.sendMessage(Identity.nil(), Component.text("Carver", NamedTextColor.DARK_AQUA));
        Carvers.registry().streamEntries().forEach(e -> {
            ctx.sendMessage(Identity.nil(), Component.text(" - " + e.key(), NamedTextColor.GRAY));
        });

        return CommandResult.success();
    }

    public void register(final RegisterRegistryValueEvent event) {
        event.registry(RegistryTypes.CARVER, (h, s) ->
            s.register(ResourceKey.of("carvertest", "custom_carver"), Carver.builder().from(Carvers.CAVE.get(h)).build()));
    }


    Command.Parameterized carverCmd() {
        return Command.builder()
                .addChild(Command.builder().executor(this::carvers).build(), "list")
                .build();
    }

}
