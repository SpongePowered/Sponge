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
import net.minecraft.world.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.DimensionManager;

@Mixin(ItemMap.class)
public class MixinItemMap extends ItemMapBase {

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
            + "loadItemData(Ljava/lang/Class;Ljava/lang/String;)Lnet/minecraft/world/WorldSavedData;"))
    private WorldSavedData loadOverworldMapData(World worldIn, Class<? extends WorldSavedData> clazz, String dataId) {
        if (worldIn.isRemote) {
            return worldIn.loadItemData(clazz, dataId);
        }
        return DimensionManager.getWorldByDimensionId(0).get().loadItemData(clazz, dataId);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private int getOverworldUniqueDataId(World worldIn, String key) {
        return DimensionManager.getWorldByDimensionId(0).get().getUniqueDataId(key);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
            + "setItemData(Ljava/lang/String;Lnet/minecraft/world/WorldSavedData;)V"))
    private void setOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        DimensionManager.getWorldByDimensionId(0).get().setItemData(dataId, data);
    }

    @Redirect(method = "onCreated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private int onCreatedGetOverworldUniqueDataId(World worldIn, String key) {
        if (worldIn.isRemote) {
            return worldIn.getUniqueDataId(key);
        }
        return DimensionManager.getWorldByDimensionId(0).get().getUniqueDataId(key);

    }

    @Redirect(method = "enableMapTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onCreatedGetOverworldUniqueDataId2(World worldIn, String key) {
        return DimensionManager.getWorldByDimensionId(0).get().getUniqueDataId(key);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
            + "setItemData(Ljava/lang/String;Lnet/minecraft/world/WorldSavedData;)V"))
    private void onCreatedSetOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        if (worldIn.isRemote) {
            worldIn.setItemData(dataId, data);
        } else {
            DimensionManager.getWorldByDimensionId(0).get().setItemData(dataId, data);
        }
    }
}
