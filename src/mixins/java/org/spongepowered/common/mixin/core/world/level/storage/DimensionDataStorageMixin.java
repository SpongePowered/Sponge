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
package org.spongepowered.common.mixin.core.world.level.storage;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.world.level.storage.DimensionDataStorageBridge;
import org.spongepowered.common.data.DataUtil;

import java.util.Optional;

@Mixin(DimensionDataStorage.class)
public abstract class DimensionDataStorageMixin implements DimensionDataStorageBridge {

    // @formatter:off
    private @Nullable ResourceLocation impl$dimensionKey;
    // @formatter:on

    @WrapOperation(method = "readSavedData", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElse(Ljava/lang/Object;)Ljava/lang/Object;"))
    public <T> T readSpongeMapData(
        final Optional<T> instance, final T other, final Operation<T> original,
        final @Local CompoundTag rootTag
    ) {
        final var result = original.call(instance, other);
        if (result instanceof DataCompoundHolder dch) {
            dch.data$setCompound(rootTag);
            DataUtil.syncTagToData(dch);
            dch.data$setCompound(null);
        }
        return result;
    }

    @Override
    public void bridge$dimensionKey(final @Nullable ResourceLocation dimensionKey) {
        this.impl$dimensionKey = dimensionKey;
    }

    @Override
    public @Nullable ResourceLocation bridge$dimensionKey() {
        return this.impl$dimensionKey;
    }
}
