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
package org.spongepowered.server.mixin.core.world.storage;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.storage.SaveHandlerBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.server.util.VanillaConstants;

import java.io.File;
import java.io.FileInputStream;

import javax.annotation.Nullable;

@Mixin(SaveHandler.class)
public abstract class SaveHandlerMixin_Vanilla implements SaveHandlerBridge {

    @Shadow @Final private File worldDirectory;

    @Inject(method = "saveWorldInfoWithPlayer",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/nbt/NBTTagCompound;setTag(Ljava/lang/String;Lnet/minecraft/nbt/NBTBase;)V",
            shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void vanilla$saveDimensionMapping(final WorldInfo worldInformation, final NBTTagCompound tagCompound, final CallbackInfo ci,
        final NBTTagCompound nbttagcompound1, final NBTTagCompound nbttagcompound2) {
        // Only save dimension data to root world
        if (this.worldDirectory.getParentFile() == null
            || (SpongeImpl.getGame().getPlatform().getType().isClient()
                && this.worldDirectory.getParentFile().toPath().equals(SpongeImpl.getGame().getSavesDirectory()))) {
            if (!nbttagcompound2.hasKey(VanillaConstants.Forge.FORGE_DIMENSION_DATA_TAG, Constants.NBT.TAG_COMPOUND)) {
                nbttagcompound2.setTag(VanillaConstants.Forge.FORGE_DIMENSION_DATA_TAG, new NBTTagCompound());
            }
            final NBTTagCompound forgeTag = nbttagcompound2.getCompoundTag(VanillaConstants.Forge.FORGE_DIMENSION_DATA_TAG);
            final NBTTagCompound customDimensionDataCompound = WorldManager.saveDimensionDataMap();
            forgeTag.setTag(VanillaConstants.Forge.FORGE_DIMENSION_ID_MAP, customDimensionDataCompound);
        }
    }

    @Redirect(method = "loadWorldInfo",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/storage/SaveFormatOld;getWorldData(Ljava/io/File;Lnet/minecraft/util/datafix/DataFixer;)Lnet/minecraft/world/storage/WorldInfo;"),
        require = 2)
    @Nullable
    private WorldInfo vanilla$loadDimensionDataAndSpongeDataFromLevelDat(File file, DataFixer fixer) {
        try {
            NBTTagCompound root = CompressedStreamTools.readCompressed(new FileInputStream(file));
            NBTTagCompound data = root.getCompoundTag(VanillaConstants.VANILLA_DIMENSION_DATA);
            WorldInfo info = new WorldInfo(fixer.process(FixTypes.LEVEL, data));

            if (root.hasKey(VanillaConstants.Forge.FORGE_DIMENSION_DATA_TAG, Constants.NBT.TAG_COMPOUND)) {
                final NBTTagCompound forgeData = root.getCompoundTag(VanillaConstants.Forge.FORGE_DIMENSION_DATA_TAG);
                WorldManager.loadDimensionDataMap(forgeData.getCompoundTag(VanillaConstants.Forge.FORGE_DIMENSION_ID_MAP));
            }
            try {
                this.bridge$loadSpongeDatData(info);
            } catch (Exception ex) {
                SpongeImpl.getLogger().error("Exception reading Sponge level data", ex);
                return null;
            }

            return info;
        } catch (Exception exception) {
            SpongeImpl.getLogger().error("Exception reading " + file, exception);
            return null;
        }
    }

}
