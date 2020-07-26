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
package org.spongepowered.common.mixin.core.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.data.SpongeDataManager;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(net.minecraft.item.ItemStack.class)
public abstract class ItemStackMixin implements CustomDataHolderBridge, DataCompoundHolder {

    @Shadow public abstract void shadow$setTag(@Nullable CompoundNBT nbt);
    @Shadow public abstract @Nullable CompoundNBT shadow$getTag();

    @Override
    public <E> DataTransactionResult bridge$offerCustom(Key<? extends Value<E>> key, E value) {
        return CustomDataHolderBridge.super.bridge$offerCustom(key, value);
    }

    @Override
    public <E> Optional<E> bridge$getCustom(Key<? extends Value<E>> key) {
        return CustomDataHolderBridge.super.bridge$getCustom(key);
    }

    @Override
    public <E> DataTransactionResult bridge$removeCustom(Key<? extends Value<E>> key) {
        return CustomDataHolderBridge.super.bridge$removeCustom(key);
    }

    @Override
    public CompoundNBT data$getCompound() {
        return this.shadow$getTag();
    }

    @Override
    public void data$setCompound(CompoundNBT nbt) {
        this.shadow$setTag(nbt);
    }

    // Add our manipulators when creating copies from this ItemStack:
    @Inject(method = "copy", at = @At("RETURN"))
    private void onCopy(CallbackInfoReturnable<ItemStack> info) {
        ((CustomDataHolderBridge) (Object) info.getReturnValue()).bridge$getManipulator().copyFrom(this.bridge$getManipulator());
    }

    @Inject(method = "split", at = @At("RETURN"))
    private void onSplit(int amount, CallbackInfoReturnable<net.minecraft.item.ItemStack> info) {
        ((CustomDataHolderBridge) (Object) info.getReturnValue()).bridge$getManipulator().copyFrom(this.bridge$getManipulator());
    }

    // Read custom data from nbt
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundNBT;)V", at = @At("RETURN"))
    private void onRead(CompoundNBT compound, CallbackInfo info) {
        if (this.data$hasSpongeData()) {
            SpongeDataManager.getInstance().deserializeCustomData(this.data$getSpongeData(), this);
            SpongeDataManager.getInstance().syncCustomToTag(this);
        }
    }

    @Inject(method = "setTag", at = @At("RETURN"))
    private void onSet(CompoundNBT compound, CallbackInfo callbackInfo) {
        if (this.shadow$getTag() != compound) {
            this.bridge$clearCustomData();
        }
        if (this.data$hasSpongeData()) {
            SpongeDataManager.getInstance().deserializeCustomData(this.data$getSpongeData(), this);
            SpongeDataManager.getInstance().syncCustomToTag(this);
        }
    }

}
