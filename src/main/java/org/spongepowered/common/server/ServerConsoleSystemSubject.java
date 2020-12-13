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
package org.spongepowered.common.server;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.Cause;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.command.ICommandSourceBridge;
import org.spongepowered.common.service.server.permission.SpongeSystemSubject;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.command.CommandSourceProviderBridge;

public final class ServerConsoleSystemSubject extends SpongeSystemSubject implements CommandSourceProviderBridge, ICommandSource, ICommandSourceBridge {

    @Override
    public String getIdentifier() {
        return "console";
    }

    @Override
    public void sendMessage(final @NonNull Identity identity, final @NonNull Component message, final @NonNull MessageType type) {
        SpongeCommon.getServer().sendMessage(SpongeAdventure.asVanilla(message));
    }

    @Override
    public CommandSource bridge$getCommandSource(final Cause cause) {
        return new CommandSource(this,
                Vec3d.ZERO,
                Vec2f.ZERO,
                SpongeCommon.getServer().getWorld(DimensionType.OVERWORLD),
                4,
                "System Subject",
                new StringTextComponent("System Subject"),
                SpongeCommon.getServer(),
                null);
    }

    @Override
    public void sendMessage(final ITextComponent component) {
        SpongeCommon.getLogger().info(component.getString());
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return true;
    }

    @Override
    public boolean shouldReceiveErrors() {
        return true;
    }

    @Override
    public boolean allowLogging() {
        return true;
    }

}
