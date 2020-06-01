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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.util.Constants;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@Mixin(MapStorage.class)
public abstract class MapStorageMixin_API implements org.spongepowered.api.world.map.MapStorage {

    @Final
    @Shadow
    private Map<String, Short> idCounts;

    @Shadow
    public abstract WorldSavedData getOrLoadData(Class<? extends WorldSavedData> clazz, String dataIdentifier);

    @Override
    public Set<MapInfo> getAllMapInfos() {
        int highestId = idCounts.get(Constants.Map.ID_COUNTS_KEY).intValue(); // Prefer intValue() as it means max map ids can be changed to int/long without breaking this
        return Stream.iterate(0, i -> i + 1)
                .limit(highestId + 1) // limit is < but we want <=
                .map(id -> Constants.Map.MAP_PREFIX + id)
                .map(s -> getOrLoadData(MapData.class, s))
                .filter(Objects::nonNull) // if we have missing map between 0 and highest map .getOrLoadData() returns null so filter out
                .map(worldSavedData -> (MapInfo)worldSavedData)
                .collect(Collectors.toSet());
    }

    @Override
    public MapInfo createNewMapInfo() {
        return MapUtil.fireCreateMapEvent(new SpongeMapInfoData(), SpongeImpl.getCauseStackManager().getCurrentCause())
                .orElseThrow(() -> new IllegalStateException("Map creation was cancelled!"));
    }
}
