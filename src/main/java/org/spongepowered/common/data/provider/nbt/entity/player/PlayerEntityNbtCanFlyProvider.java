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
package org.spongepowered.common.data.provider.nbt.entity.player;

import static org.spongepowered.common.data.util.NbtHelper.getNullableCompound;
import static org.spongepowered.common.data.util.NbtHelper.getOrCreateCompound;

import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.nbt.NbtDataProvider;
import org.spongepowered.common.data.provider.nbt.NbtDataTypes;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class PlayerEntityNbtCanFlyProvider extends NbtDataProvider<Boolean> {

    public PlayerEntityNbtCanFlyProvider() {
        super(Keys.CAN_FLY, NbtDataTypes.ENTITY);
    }

    @Override
    protected boolean supports(CompoundNBT dataHolder) {
        return dataHolder.contains(Constants.Entity.Player.ABILITIES);
    }

    @Override
    protected Optional<Boolean> getFrom(CompoundNBT dataHolder) {
        @Nullable final CompoundNBT abilities = getNullableCompound(dataHolder, Constants.Entity.Player.ABILITIES);
        if (abilities == null) {
            return Optional.empty();
        }
        return Optional.of(abilities.getBoolean(Constants.Entity.Player.Abilities.CAN_FLY));
    }

    @Override
    protected boolean set(CompoundNBT dataHolder, Boolean value) {
        getOrCreateCompound(dataHolder, Constants.Entity.Player.ABILITIES)
                .putBoolean(Constants.Entity.Player.Abilities.CAN_FLY, value);
        return true;
    }

    @Override
    protected boolean delete(CompoundNBT dataHolder) {
        return this.set(dataHolder, false);
    }
}
