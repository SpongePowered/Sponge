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

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemMapBase;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.world.WorldManager;

import javax.annotation.Nullable;

@Mixin(ItemMap.class)
public class ItemMapMixin extends ItemMapBase {

    @Inject(method = "updateMapData", at = @At(value = "HEAD"), cancellable = true)
    public void impl$preventUpdateIfLocked(final World worldIn, final Entity viewer, final MapData data, final CallbackInfo ci) {
        if (((MapDataBridge) data).bridge$isLocked()) {
            ci.cancel();
        }
    }

    @Redirect(method = "setupNewMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int impl$onCreateMap(final World worldIn, final String key) {
        if (worldIn.isRemote) {
            return worldIn.getUniqueDataId(key);
        }
        return WorldManager.getWorldByDimensionId(0).get().getUniqueDataId(key);
    }

    @Redirect(method = "setupNewMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void impl$onSetupNewMapSetOverworldMapData(final World worldIn, final String dataId, final WorldSavedData data) {
        if (worldIn.isRemote) {
            worldIn.setData(dataId, data);
        } else {
            WorldManager.getWorldByDimensionId(0).get().setData(dataId, data);
        }
    }

    @Nullable
    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "loadData(Ljava/lang/Class;Ljava/lang/String;)Lnet/minecraft/world/storage/WorldSavedData;"))
    private WorldSavedData impl$loadOverworldMapData(final World worldIn, final Class<? extends WorldSavedData> clazz, final String dataId) {
        if (worldIn.isRemote) {
            return worldIn.loadData(clazz, dataId);
        }
        return WorldManager.getWorldByDimensionId(0).get().loadData(clazz, dataId);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private int impl$getOverworldUniqueDataId(final World worldIn, final String key) {
        // The caller already has remote check
        return WorldManager.getWorldByDimensionId(0).get().getUniqueDataId(key);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private void impl$setOverworldMapData(final World worldIn, final String dataId, final WorldSavedData data) {
        // The caller already has remote check
        WorldManager.getWorldByDimensionId(0).get().setData(dataId, data);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int impl$onScaleMapGetOverworldUniqueDataId(final World worldIn, final String key) {
        if (worldIn.isRemote) {
            return worldIn.getUniqueDataId(key);
        }
        return WorldManager.getWorldByDimensionId(0).get().getUniqueDataId(key);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void impl$onScaleMapSetOverworldMapData(final World worldIn, final String dataId, final WorldSavedData data) {
        if (worldIn.isRemote) {
            worldIn.setData(dataId, data);
        } else {
            WorldManager.getWorldByDimensionId(0).get().setData(dataId, data);
        }
    }

    @Redirect(method = "enableMapTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int impl$onEnableMapTrackingGetOverworldUniqueDataId(final World worldIn, final String key) {
        if (worldIn.isRemote) {
            return worldIn.getUniqueDataId(key);
        }
        return WorldManager.getWorldByDimensionId(0).get().getUniqueDataId(key);
    }

    @Redirect(method = "enableMapTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void impl$onEnableMapTrackingSetOverworldMapData(final World worldIn, final String dataId, final WorldSavedData data) {
        if (worldIn.isRemote) {
            worldIn.setData(dataId, data);
        } else {
            WorldManager.getWorldByDimensionId(0).get().setData(dataId, data);
        }
    }
}
