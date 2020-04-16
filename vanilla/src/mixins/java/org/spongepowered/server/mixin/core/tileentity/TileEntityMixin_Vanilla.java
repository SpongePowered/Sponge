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
package org.spongepowered.server.mixin.core.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

@Mixin(value = TileEntity.class, priority = 999)
public abstract class TileEntityMixin_Vanilla implements DataCompoundHolder {

    @Nullable private NBTTagCompound vanilla$customTileData;

    @Override
    public boolean data$hasRootCompound() {
        return this.vanilla$customTileData != null;
    }

    @Override
    public NBTTagCompound data$getRootCompound() {
        if (this.vanilla$customTileData == null) {
            this.vanilla$customTileData = new NBTTagCompound();
        }
        return this.vanilla$customTileData;
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void vanilla$GetForgeDataFromCompound(NBTTagCompound tagCompound, CallbackInfo ci) {
        if (tagCompound.hasKey(Constants.Forge.FORGE_DATA)) {
            this.vanilla$customTileData = tagCompound.getCompoundTag(Constants.Forge.FORGE_DATA);
        }
    }

    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("RETURN"))
    private void vanilla$writeForgeDataToCompound(NBTTagCompound tagCompound, CallbackInfoReturnable<NBTTagCompound> ci) {
        if (this.vanilla$customTileData != null) {
            tagCompound.setTag(Constants.Forge.FORGE_DATA, this.vanilla$customTileData);
        }
    }

}
