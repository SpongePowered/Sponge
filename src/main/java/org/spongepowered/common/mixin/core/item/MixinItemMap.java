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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;

import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(ItemMap.class)
public class MixinItemMap extends ItemMapBase {

    public MixinItemMap(Properties p_i48514_1_) {
        super(p_i48514_1_);
    }

    @Redirect(method = "createMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;func_212410_a(Lnet/minecraft/world/dimension/DimensionType;Ljava/lang/String;)I"))
    private static int onGetMapId(World world, DimensionType dimensionType, String key) {
        if (world.isRemote) {
            return world.func_212410_a(dimensionType, key);
        }
        return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).get().func_212410_a(dimensionType, key);
    }


    @Nullable
    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "loadData(Ljava/lang/Class;Ljava/lang/String;)Lnet/minecraft/world/storage/WorldSavedData;"))
    private static WorldSavedData loadOverworldMapData(IWorld world, Function<String, ?> constructor, String dataId) {
        if (((World) world).isRemote) {
            return world.load((Function) constructor, dataId);
        }
        return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).get().loadData((Function) constructor, dataId);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int getOverworldUniqueDataId(World worldIn, String key) {
        // The caller already has remote check
        return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).get().getUniqueDataId(key);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void setOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        // The caller already has remote check
        return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).setData(dataId, data);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onScaleMapGetOverworldUniqueDataId(World worldIn, String key) {
        if (worldIn.isRemote) {
            return worldIn.getUniqueDataId(key);
        }
        return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).get().getUniqueDataId(key);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onScaleMapSetOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        if (worldIn.isRemote) {
            worldIn.setData(dataId, data);
        } else {
            return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).setData(dataId, data);
        }
    }

    @Redirect(method = "createMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onEnableMapTrackingGetOverworldUniqueDataId(World worldIn, String key) {
        if (worldIn.isRemote) {
            return worldIn.getUniqueDataId(key);
        }
        return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).get().getUniqueDataId(key);
    }

    @Redirect(method = "createMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onEnableMapTrackingSetOverworldMapData(World worldIn, String dataId, WorldSavedData data) {
        if (worldIn.isRemote) {
            worldIn.setData(dataId, data);
        } else {
            return ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(DimensionType.OVERWORLD).get().setData(dataId, data);
        }
    }
}
