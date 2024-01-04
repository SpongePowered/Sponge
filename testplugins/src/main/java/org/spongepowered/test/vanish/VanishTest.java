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
package org.spongepowered.test.vanish;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.VanishState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Collection;

@Plugin("vanish_test")
public final class VanishTest {

    private final PluginContainer plugin;

    @Inject
    public VanishTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Entity> entityKey = Parameter.entity()
            .key("entity")
            .usage(key -> "any entity")
            .optional()
            .build();
        final Flag collisionsFlag = Flag.builder()
            .setParameter(entityKey)
            .alias("c")
            .build();
        final Flag targetingFlag = Flag.builder()
            .setParameter(entityKey)
            .alias("t")
            .build();
        final Flag soundFlag = Flag.builder()
            .setParameter(entityKey)
            .alias("s")
            .build();
        final Flag particlesFlag = Flag.builder()
            .setParameter(entityKey)
            .alias("p")
            .build();
        event.register(
            this.plugin,
            Command.builder()
                .addParameter(entityKey)
                .addFlags(soundFlag, particlesFlag, collisionsFlag, targetingFlag)
                .extendedDescription(Component.text("Vanishes the entities by the selector. Based on the flags -c and -t will enable collisions or targeting by entities. No entities selected will target the sender."))
                .executor(ctx -> {
                    final Collection<? extends Entity> allEntities = ctx.all(entityKey);
                    final VanishState vanishState = VanishState.vanished()
                        .untargetable(!ctx.hasFlag(targetingFlag))
                        .ignoreCollisions(!ctx.hasFlag(collisionsFlag))
                        .createParticles(ctx.hasFlag(particlesFlag))
                        .createSounds(ctx.hasFlag(soundFlag));
                    allEntities.forEach(entity -> entity.offer(Keys.VANISH_STATE, vanishState));
                    if (allEntities.isEmpty()) {
                        if (ctx.cause().root() instanceof DataHolder.Mutable) {
                            ((DataHolder.Mutable) ctx.cause().root()).offer(Keys.VANISH_STATE, vanishState
                            );
                            ctx.sendMessage(Identity.nil(), Component.text("Vanished!", NamedTextColor.DARK_AQUA));
                        } else {
                            ctx.sendMessage(Identity.nil(), Component.text("Nothing to Vanish!", NamedTextColor.YELLOW));
                        }
                    } else {
                        ctx.sendMessage(Identity.nil(), Component.text("Vanished!", NamedTextColor.DARK_AQUA));
                    }
                    return CommandResult.success();
                })
                .build(),
            "vanish",
            "v"
        );
        event.register(
            this.plugin,
            Command.builder()
                .addParameter(entityKey)
                .executor(ctx -> {
                    final Collection<? extends Entity> allEntities = ctx.all(entityKey);
                    allEntities.forEach(entity -> entity.offer(Keys.VANISH_STATE, VanishState.unvanished()));
                    if (allEntities.isEmpty()) {
                        if (ctx.cause().root() instanceof DataHolder.Mutable) {
                            ((DataHolder.Mutable) ctx.cause().root()).offer(Keys.VANISH_STATE, VanishState.unvanished());
                            ctx.sendMessage(Identity.nil(), Component.text("Unvanished! Huzzah!", NamedTextColor.AQUA));
                        } else {
                            ctx.sendMessage(Identity.nil(), Component.text("Nothing to Reveal!", NamedTextColor.YELLOW));
                        }
                    } else {
                        ctx.sendMessage(Identity.nil(), Component.text("Unvanished! Huzzah!", NamedTextColor.AQUA));
                    }
                    return CommandResult.success();
                })
                .build(),
            "unvanish",
            "uv"
        );
    }

}
