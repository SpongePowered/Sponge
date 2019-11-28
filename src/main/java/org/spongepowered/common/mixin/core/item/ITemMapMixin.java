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

import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemMapBase;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.WorldManager;

import javax.annotation.Nullable;

@Mixin(ItemMap.class)
public class ITemMapMixin extends ItemMapBase {


    @Redirect(method = "setupNewMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onCreateMap(World worldIn, String key) {
        if (worldIn.field_72995_K) {
            return worldIn.func_72841_b(key);
        }
        return WorldManager.getWorldByDimensionId(0).get().func_72841_b(key);
    }

    @Redirect(method = "setupNewMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onSetupNewMapSetOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        if (worldIn.field_72995_K) {
            worldIn.func_72823_a(dataId, data);
        } else {
            WorldManager.getWorldByDimensionId(0).get().func_72823_a(dataId, data);
        }
    }

    @Nullable
    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "loadData(Ljava/lang/Class;Ljava/lang/String;)Lnet/minecraft/world/storage/WorldSavedData;"))
    private WorldSavedData loadOverworldMapData(World worldIn, Class<? extends WorldSavedData> clazz, String dataId) {
        if (worldIn.field_72995_K) {
            return worldIn.func_72943_a(clazz, dataId);
        }
        return WorldManager.getWorldByDimensionId(0).get().func_72943_a(clazz, dataId);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private int getOverworldUniqueDataId(World worldIn, String key) {
        // The caller already has remote check
        return WorldManager.getWorldByDimensionId(0).get().func_72841_b(key);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private void setOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        // The caller already has remote check
        WorldManager.getWorldByDimensionId(0).get().func_72823_a(dataId, data);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onScaleMapGetOverworldUniqueDataId(World worldIn, String key) {
        if (worldIn.field_72995_K) {
            return worldIn.func_72841_b(key);
        }
        return WorldManager.getWorldByDimensionId(0).get().func_72841_b(key);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onScaleMapSetOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        if (worldIn.field_72995_K) {
            worldIn.func_72823_a(dataId, data);
        } else {
            WorldManager.getWorldByDimensionId(0).get().func_72823_a(dataId, data);
        }
    }

    @Redirect(method = "enableMapTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onEnableMapTrackingGetOverworldUniqueDataId(World worldIn, String key) {
        if (worldIn.field_72995_K) {
            return worldIn.func_72841_b(key);
        }
        return WorldManager.getWorldByDimensionId(0).get().func_72841_b(key);
    }

    @Redirect(method = "enableMapTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onEnableMapTrackingSetOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        if (worldIn.field_72995_K) {
            worldIn.func_72823_a(dataId, data);
        } else {
            WorldManager.getWorldByDimensionId(0).get().func_72823_a(dataId, data);
        }
    }
}
