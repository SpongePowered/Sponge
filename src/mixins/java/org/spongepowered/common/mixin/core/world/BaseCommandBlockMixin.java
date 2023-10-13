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
package org.spongepowered.common.mixin.core.world;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.BaseCommandBlock;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;

@Mixin(BaseCommandBlock.class)
public abstract class BaseCommandBlockMixin implements CommandSourceProviderBridge, Audience {

    // @formatter:off
    @Shadow public abstract CommandSourceStack shadow$createCommandSourceStack();
    @Shadow public abstract void shadow$sendSystemMessage(net.minecraft.network.chat.Component $$0);
    // @formatter:on

    @Override
    public CommandSourceStack bridge$getCommandSource(final Cause cause) {
        return this.shadow$createCommandSourceStack();
    }

    @Override
    public void sendMessage(final @NonNull Identity identity, final @NonNull Component message, final @NonNull MessageType type) {
        this.shadow$sendSystemMessage(SpongeAdventure.asVanilla(message));
    }
}
