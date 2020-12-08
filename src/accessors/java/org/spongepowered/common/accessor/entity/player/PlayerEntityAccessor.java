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
package org.spongepowered.common.accessor.entity.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {

    @Accessor("DATA_PLAYER_ABSORPTION_ID") static DataParameter<Float> accessor$getDATA_PLAYER_ABSORPTION_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_SCORE_ID") static DataParameter<Integer> accessor$getDATA_SCORE_ID() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_PLAYER_MODE_CUSTOMISATION") static DataParameter<Byte> accessor$getDATA_PLAYER_MODE_CUSTOMISATION() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_PLAYER_MAIN_HAND") static DataParameter<Byte> accessor$getDATA_PLAYER_MAIN_HAND() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_SHOULDER_LEFT") static DataParameter<CompoundNBT> accessor$getDATA_SHOULDER_LEFT() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("DATA_SHOULDER_RIGHT") static DataParameter<CompoundNBT> accessor$getDATA_SHOULDER_RIGHT() {
        throw new IllegalStateException("Untransformed Accessor!");
    }

    @Accessor("sleepCounter") void accessor$setSleepCounter(int sleepCounter);
}
