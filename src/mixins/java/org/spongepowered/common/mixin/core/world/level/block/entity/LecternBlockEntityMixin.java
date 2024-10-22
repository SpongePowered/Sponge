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
package org.spongepowered.common.mixin.core.world.level.block.entity;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;

@Mixin(LecternBlockEntity.class)
public abstract class LecternBlockEntityMixin implements CommandSourceProviderBridge {

    @Shadow
    private CommandSourceStack createCommandSourceStack(@Nullable final Player playerEntity, final ServerLevel $$1) {
        throw new AssertionError("This method should be a shadow.");
    }

    @Override
    public CommandSourceStack bridge$getCommandSource(final Cause cause) {
        // We assume that if a player is in the Cause, they're the one reading the book.
        return this.createCommandSourceStack(cause.first(Player.class).orElse(null), cause.first(ServerLevel.class).orElse(null));
    }

}
