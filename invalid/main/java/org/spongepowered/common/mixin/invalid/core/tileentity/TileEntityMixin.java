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
package org.spongepowered.common.mixin.invalid.core.tileentity;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(TileEntity.class)
public class TileEntityMixin {


    @SuppressWarnings({"rawtypes"})
    @Inject(method = "register(Ljava/lang/String;Ljava/lang/Class;)V", at = @At(value = "RETURN"))
    private static void impl$registerTileEntityClassWithSpongeRegistry(final String name, @Nullable final Class clazz, final CallbackInfo callbackInfo) {
        if (clazz != null) {
            TileEntityTypeRegistryModule.getInstance().doTileEntityRegistration(clazz, name);
        }
    }

    @Inject(method = "invalidate", at = @At("RETURN"))
    private void impl$RemoveActiveChunkOnInvalidate(final CallbackInfo ci) {
        ((ActiveChunkReferantBridge) this).bridge$setActiveChunk(null);
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #bridge$writeToSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override writeToNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"))
    private void impl$WriteSpongeDataToCompound(final CompoundNBT compound, final CallbackInfoReturnable<CompoundNBT> ci) {
        if (!((CustomDataHolderBridge) this).bridge$getCustomManipulators().isEmpty()) {
            this.bridge$writeToSpongeCompound(this.data$getSpongeDataCompound());
        }
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #bridge$readFromSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override readSpongeNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(final CompoundNBT compound, final CallbackInfo ci) {
        if (this.data$hasSpongeCompound()) {
            this.bridge$readFromSpongeCompound(this.data$getSpongeDataCompound());
        }
    }
}
