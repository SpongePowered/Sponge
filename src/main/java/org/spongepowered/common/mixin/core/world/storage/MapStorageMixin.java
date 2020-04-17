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

import com.flowpowered.math.vector.Vector2i;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.item.MapItemData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeMapItemData;
import org.spongepowered.common.world.WorldManager;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

@Mixin(MapStorage.class)
public abstract class MapStorageMixin implements org.spongepowered.api.world.map.MapStorage {

    @Shadow @Final private ISaveHandler saveHandler;

    @Shadow
    @Final
    private Map<String, Short> idCounts;

    @Shadow
    @Nullable
    public abstract WorldSavedData getOrLoadData(Class<? extends WorldSavedData> clazz, String dataIdentifier);

    @Override
    public Optional<Integer> getHighestMapId() {
        Number num = idCounts.get("map");
        if (num == null) {
            return Optional.empty();
        }
        return Optional.of(num.intValue());
    }

    @Override
    public Optional<MapItemData> getMapData(int id) {
        MapData mapData = (MapData)getOrLoadData(MapData.class, "map_" + id);
        if (mapData == null) {
            return Optional.empty();
        }
        return Optional.of(new SpongeMapItemData(
                        new Vector2i(mapData.xCenter, mapData.zCenter),
                        (World) WorldManager.getWorldByDimensionId(mapData.dimension).get(),
                        mapData.trackingPosition,
                        mapData.unlimitedTracking,
                        mapData.scale
        ));
    }

    // When we have a null saveHandler, we're not in the 'real' Overworld
    // MapStorage

    @Inject(method = "getOrLoadData", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false))
    private void impl$ValidateCallingOnMainThread(CallbackInfoReturnable<WorldSavedData> ci) {
        if (this.saveHandler != null && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            SpongeImpl.getLogger().error("Attempted to call MapStorage#getOrLoadData from off the main thread!", new Exception("Dummy exception"));
        }
    }

    @Inject(method = "setData", at = @At(value = "HEAD"))
    private void impl$ValidateCallingOnMainThread(CallbackInfo ci) {
        if (this.saveHandler != null && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
            SpongeImpl.getLogger().error("Attempted to call MapStorage#setData from off the main thread!", new Exception("Dummy exception"));
        }
    }
}
