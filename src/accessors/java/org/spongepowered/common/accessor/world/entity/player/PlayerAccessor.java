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
package org.spongepowered.common.accessor.world.entity.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.common.UntransformedAccessorError;

@Mixin(Player.class)
public interface PlayerAccessor {

    @Accessor("DATA_PLAYER_ABSORPTION_ID") static EntityDataAccessor<Float> accessor$DATA_PLAYER_ABSORPTION_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_SCORE_ID") static EntityDataAccessor<Integer> accessor$DATA_SCORE_ID() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION") static EntityDataAccessor<Byte> accessor$DATA_PLAYER_MODE_CUSTOMISATION() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_PLAYER_MAIN_HAND") static EntityDataAccessor<Byte> accessor$DATA_PLAYER_MAIN_HAND() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_SHOULDER_LEFT") static EntityDataAccessor<CompoundTag> accessor$DATA_SHOULDER_LEFT() {
        throw new UntransformedAccessorError();
    }

    @Accessor("DATA_SHOULDER_RIGHT") static EntityDataAccessor<CompoundTag> accessor$DATA_SHOULDER_RIGHT() {
        throw new UntransformedAccessorError();
    }
    @Invoker("getPermissionLevel") int invoker$getPermissionLevel();

    @Accessor("sleepCounter") void accessor$sleepCounter(final int sleepCounter);

}
