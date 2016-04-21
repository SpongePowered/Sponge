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
package org.spongepowered.common.data.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFlyingData;
import org.spongepowered.api.data.manipulator.mutable.entity.FlyingData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFlyingData;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.Optional;

public class IsFlyingNbtProcessor implements NbtDataProcessor<FlyingData, ImmutableFlyingData> {

    @Override
    public NbtDataType getTargetType() {
        return null;
    }

    @Override
    public Optional<FlyingData> readFromCompound(NBTTagCompound compound) {
        final NBTBase tag = compound.getTag(NbtDataUtil.Minecraft.IS_FLYING);
        if (tag != null) {
            return Optional.of(new SpongeFlyingData(((NBTTagByte) tag).getByte() != 0));
        }
        return Optional.empty();
    }

    @Override
    public NBTTagCompound storeToCompound(NBTTagCompound compound, FlyingData manipulator) {
        compound.setBoolean(NbtDataUtil.Minecraft.IS_FLYING, manipulator.flying().get());
        return compound;
    }
}
