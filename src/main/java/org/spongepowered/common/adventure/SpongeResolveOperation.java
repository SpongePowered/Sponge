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
package org.spongepowered.common.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.api.adventure.ResolveOperation;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.common.bridge.adventure.ComponentBridge;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ComponentUtils;

import java.util.Locale;

public abstract class SpongeResolveOperation implements ResolveOperation {

    private static final Logger LOGGER = LogManager.getLogger();

    public abstract Component resolve(final Component input, final CommandCause senderContext, final @Nullable Entity viewer);

    public static SpongeResolveOperation newCustomTranslations() {
        return new SpongeResolveOperation() {
            @Override
            public Component resolve(final Component input, final CommandCause senderContext, final @Nullable Entity viewer) {
                final Locale targetLocale = viewer instanceof ServerPlayer ? ((ServerPlayer) viewer).locale() : Locale.getDefault();
                return GlobalTranslator.render(input, targetLocale);
            }
        };
    }

    public static SpongeResolveOperation newContextualComponents() {
        return new SpongeResolveOperation() {
            @Override
            public Component resolve(final Component input, final CommandCause senderContext, final @Nullable Entity viewer) {
                try {
                    // TODO: Have a pure-Adventure implementation of this
                    return SpongeAdventure.asAdventure(ComponentUtils.updateForEntity(
                        (CommandSourceStack) senderContext,
                        ((ComponentBridge) input).bridge$asVanillaComponent(),
                        (net.minecraft.world.entity.Entity) viewer,
                        /* depth = */ 0
                    ));
                } catch (final CommandSyntaxException ex) {
                    SpongeResolveOperation.LOGGER.error("Failed to resolve component {} due to a parsing error", input, ex);
                    return input;
                }
            }
        };
    }

}
