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

import net.minecraft.item.AbstractMapItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;

import javax.annotation.Nullable;

@Mixin(FilledMapItem.class)
public abstract class FilledMapItemMixin extends AbstractMapItem {

    public FilledMapItemMixin() {
        super(null);
    }

    /**
     * @author Unknown
     * @author i509VCB - March 25th, 2020 - 1.14.4
     * @reason Use default world in world manager for getting next map id
     */
    @Redirect(method = "createMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getNextMapId()I"))
    private static int impl$onCreateMap(World world) {
        if (world.isRemote()) {
            return world.getNextMapId();
        }
        return SpongeImpl.getWorldManager().getDefaultWorld().getNextMapId();
    }

    @Redirect(method = "setupNewMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onSetupNewMapSetOverworldMapData(World world, String dataId, WorldSavedData data) {
        if (world.isRemote()) {
            world.setData(dataId, data);
        } else {
            SpongeImpl.getWorldManager().getDefaultWorld().setData(dataId, data);
        }
    }

    @Nullable
    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;loadData(Ljava/lang/Class;Ljava/lang/String;)Lnet/minecraft/world/storage/WorldSavedData;"))
    private WorldSavedData impl$loadOverworldMapData(World world, String mapName) {
        if (world.isRemote()) {
            return world.getMapData(mapName);
        }
        return SpongeImpl.getWorldManager().getDefaultWorld().getMapData(mapName);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private int getOverworldUniqueDataId(World world, String key) {
        // The caller already has remote check
        return SpongeImpl.getWorldManager().getDefaultWorld().getUniqueDataId(key);
    }

    @Redirect(method = "getMapData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private void setOverworldMapData(World world, String dataId, WorldSavedData data) {
        // The caller already has remote check
        SpongeImpl.getWorldManager().getDefaultWorld().setData(dataId, data);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onScaleMapGetOverworldUniqueDataId(World world, String key) {
        if (world.isRemote()) {
            return world.getUniqueDataId(key);
        }
        return SpongeImpl.getWorldManager().getDefaultWorld().getUniqueDataId(key);
    }

    @Redirect(method = "scaleMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onScaleMapSetOverworldMapData(World world, String dataId, WorldSavedData data) {
        if (world.isRemote()) {
            world.setData(dataId, data);
        } else {
            SpongeImpl.getWorldManager().getDefaultWorld().setData(dataId, data);
        }
    }

    @Redirect(method = "enableMapTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getUniqueDataId(Ljava/lang/String;)I"))
    private static int onEnableMapTrackingGetOverworldUniqueDataId(World world, String key) {
        if (world.isRemote()) {
            return world.getUniqueDataId(key);
        }
        return SpongeImpl.getWorldManager().getDefaultWorld().getUniqueDataId(key);
    }

    @Redirect(method = "enableMapTracking", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;"
        + "setData(Ljava/lang/String;Lnet/minecraft/world/storage/WorldSavedData;)V"))
    private static void onEnableMapTrackingSetOverworldMapData(World world, String dataId, WorldSavedData data) {
        if (world.isRemote()) {
            world.setData(dataId, data);
        } else {
            SpongeImpl.getWorldManager().getDefaultWorld().setData(dataId, data);
        }
    }
}
