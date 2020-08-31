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

import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.storage.MapStorageBridge;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;

@Mixin(net.minecraft.world.storage.MapStorage.class)
public abstract class MapStorageBridgeMixin implements MapStorageBridge {

    @Shadow
    @Final
    private Map<String, Short> idCounts;

    @Shadow public abstract WorldSavedData getOrLoadData(Class<? extends WorldSavedData> clazz, String dataIdentifier);

    @Nonnull
    @Override
    public Optional<MapData> bridge$getMinecraftMapData(int id) {
        return Optional.ofNullable((MapData)getOrLoadData(MapData.class, Constants.Map.MAP_PREFIX + id));
    }

    @Override
    public void bridge$setHighestMapId(short id) {
        idCounts.put("map", id);
    }

    @Nonnull
    @Override
    public Optional<Integer> bridge$getHighestMapId() {
        Number num = idCounts.get("map");
        if (num == null) {
            return Optional.empty();
        }
        return Optional.of(num.intValue());
    }
}
