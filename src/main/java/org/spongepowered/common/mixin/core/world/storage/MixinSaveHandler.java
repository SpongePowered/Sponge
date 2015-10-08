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
package org.spongepowered.common.mixin.core.world.storage;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinSaveHandler;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.world.DimensionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@NonnullByDefault
@Mixin(SaveHandler.class)
public abstract class MixinSaveHandler implements IMixinSaveHandler {

    @Shadow private File worldDirectory;
    @Shadow private long initializationTime;

    @ModifyArg(method = "checkSessionLock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V"
            , ordinal = 0, remap = false))
    public String modifyMinecraftExceptionOutputIfNotInitializationTime(String message) {
        return "The save folder for world " + this.worldDirectory + " is being accessed from another location, aborting";
    }

    @ModifyArg(method = "checkSessionLock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MinecraftException;<init>(Ljava/lang/String;)V"
            , ordinal = 1, remap = false))
    public String modifyMinecraftExceptionOutputIfIOException(String message) {
        return "Failed to check session lock for world " + this.worldDirectory + ", aborting";
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;setTag(Ljava/lang/String;"
            + "Lnet/minecraft/nbt/NBTBase;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onSaveWorldInfoWithPlayerAfterTagSet(WorldInfo worldInformation, NBTTagCompound tagCompound, CallbackInfo ci, NBTTagCompound nbttagcompound1, NBTTagCompound nbttagcompound2) {
        saveDimensionAndOtherData((SaveHandler) (Object) this, worldInformation, nbttagcompound2);
    }

    @Inject(method = "saveWorldInfoWithPlayer", at = @At("RETURN"))
    public void onSaveWorldInfoWithPlayerEnd(WorldInfo worldInformation, NBTTagCompound tagCompound, CallbackInfo ci) {
        saveSpongeDatData(worldInformation);
    }

    @Inject(method = "saveWorldInfo", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;setTag(Ljava/lang/String;"
            + "Lnet/minecraft/nbt/NBTBase;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onSaveWorldInfoAfterTagSet(WorldInfo worldInformation, CallbackInfo ci, NBTTagCompound nbttagcompound, NBTTagCompound nbttagcompound1) {
        saveDimensionAndOtherData((SaveHandler) (Object) this, worldInformation, nbttagcompound1);
    }

    @Inject(method = "saveWorldInfo", at = @At("RETURN"))
    public void onSaveWorldInfoEnd(WorldInfo worldInformation, CallbackInfo ci) {
        saveSpongeDatData(worldInformation);
    }

    @Override
    public void loadSpongeDatData(WorldInfo info) throws IOException {
        final File spongeFile = new File(this.worldDirectory, "level_sponge.dat");
        final File spongeOldFile = new File(this.worldDirectory, "level_sponge.dat_old");

        if (spongeFile.exists() || spongeOldFile.exists()) {
            final NBTTagCompound compound = CompressedStreamTools.readCompressed(new FileInputStream(spongeFile.exists() ? spongeFile :
                    spongeOldFile));
            ((IMixinWorldInfo) info).setSpongeRootLevelNBT(compound);
            if (compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
                ((IMixinWorldInfo) info).readSpongeNbt(compound.getCompoundTag(NbtDataUtil.SPONGE_DATA));
            }
        }
    }

    private void saveSpongeDatData(WorldInfo info) {
        try {
            final File spongeFile1 = new File(this.worldDirectory, "level_sponge.dat_new");
            final File spongeFile2 = new File(this.worldDirectory, "level_sponge.dat_old");
            final File spongeFile3 = new File(this.worldDirectory, "level_sponge.dat");
            CompressedStreamTools.writeCompressed(((IMixinWorldInfo) info).getSpongeRootLevelNbt(), new FileOutputStream(spongeFile1));

            if (spongeFile2.exists()) {
                spongeFile2.delete();
            }

            spongeFile3.renameTo(spongeFile2);

            if (spongeFile3.exists()) {
                spongeFile3.delete();
            }

            spongeFile1.renameTo(spongeFile3);

            if (spongeFile1.exists()) {
                spongeFile1.delete();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void loadDimensionAndOtherData(SaveHandler handler, WorldInfo info, NBTTagCompound compound) {
        // Preserve dimension data from Sponge
        final NBTTagCompound customWorldDataCompound = compound.getCompoundTag("Forge");
        if (customWorldDataCompound.hasKey("DimensionData")) {
            DimensionManager.loadDimensionDataMap(customWorldDataCompound.getCompoundTag("DimensionData"));
        }
    }

    private void saveDimensionAndOtherData(SaveHandler handler, WorldInfo info, NBTTagCompound compound) {
        // Only save dimension data to root world
        if (this.worldDirectory.getParentFile() == null || (Sponge.getGame().getPlatform().getType().isClient() && this.worldDirectory.getParentFile().equals(
                Sponge.getGame().getSavesDirectory()))) {
            final NBTTagCompound customWorldDataCompound = new NBTTagCompound();
            final NBTTagCompound customDimensionDataCompound = DimensionManager.saveDimensionDataMap();
            customWorldDataCompound.setTag("DimensionData", customDimensionDataCompound);
            // Share data back to Sponge
            compound.setTag("Forge", customWorldDataCompound);
        }
    }
}
