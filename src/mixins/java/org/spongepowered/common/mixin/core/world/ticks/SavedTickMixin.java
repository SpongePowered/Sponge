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
package org.spongepowered.common.mixin.core.world.ticks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ticks.SavedTick;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;

import java.util.Optional;
import java.util.function.Function;

@Mixin(SavedTick.class)
public abstract class SavedTickMixin<T> implements SpongeMutableDataHolder, DataCompoundHolder, CreatorTrackedBridge {

    private @Nullable CompoundTag impl$compound;

    @Override
    public CompoundTag data$getCompound() {
        return this.impl$compound;
    }

    @Override
    public void data$setCompound(final CompoundTag nbt) {
        this.impl$compound = nbt;
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void impl$onSaveTick(final CallbackInfoReturnable<CompoundTag> cir) {
        if (DataUtil.syncDataToTag(this)) {
            cir.getReturnValue().merge(this.data$getCompound());
        }
    }

    @Inject(method = "loadTick", at = @At("RETURN"))
    private static <T> void impl$onLoad(final CompoundTag $$0, final Function<String, Optional<T>> $$1, final CallbackInfoReturnable<Optional<SavedTick<T>>> cir) {
        cir.getReturnValue().ifPresent(tick -> {
            ((DataCompoundHolder) (Object) tick).data$setCompound($$0);
            DataUtil.syncTagToData(tick);
            ((DataCompoundHolder) (Object) tick).data$setCompound(null);
        });
    }
}
