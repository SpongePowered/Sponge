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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapStorage;
import org.spongepowered.api.map.MapSettings;
import org.spongepowered.api.map.MapView;
import org.spongepowered.api.map.MapViewStorage;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.map.SpongeMapScale;
import org.spongepowered.common.scheduler.SpongeScheduler;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkNotNull;

@NonnullByDefault
@Mixin(MapStorage.class)
public abstract class MixinMapStorage implements MapViewStorage {

    @Shadow private ISaveHandler saveHandler;
    @Shadow private List<WorldSavedData> loadedDataList;
    @Shadow public abstract WorldSavedData getOrLoadData(Class<? extends WorldSavedData> clazz, String dataIdentifier);
    @Shadow public abstract int getUniqueDataId(String key);

    @Override
    public MapView createMap(MapSettings settings) {
        checkNotNull(settings, "settings");
        String mapId = "map_" + getUniqueDataId("map");
        MapData map = new MapData(mapId);
        map.scale = ((SpongeMapScale) settings.getScale()).getRawScale();
        map.xCenter = settings.getCenter().getX();
        map.zCenter = settings.getCenter().getY();
        // TODO: add remaining properties using interface IMixinMapData
        return (MapView) map;
    }

    @Override
    public Optional<MapView> getMap(String mapId) {
        checkNotNull(mapId, "mapId");
        MapView possibleMapView = (MapView) getOrLoadData(MapData.class, mapId);
        return Optional.ofNullable(possibleMapView);
    }

    @Override
    public CompletableFuture<Collection<String>> getStoredMaps() {
        // XXX: This isn't quite the best way, but afaik it's the only way to do this
        // due to no common index of maps
        // TODO: Evaluate effectiveness of creating an index file for map data
        return SpongeImpl.getScheduler().submitAsyncTask(() -> {
            File mapDataDir = new File(saveHandler.getWorldDirectory(), "data");
            String[] filenameListing = mapDataDir.list((dir, name) -> name.startsWith("map_") && name.endsWith(".dat"));
            for (int i = 0; i < filenameListing.length; i++) {
                filenameListing[i] = filenameListing[i].substring(0, filenameListing[i].length() - 4);
            }
            return Arrays.asList(filenameListing);
        });
    }

    @Override
    public ImmutableCollection<MapView> getLoadedMaps() {
        ImmutableList.Builder<MapView> builder = ImmutableList.builder();
        loadedDataList.forEach(entry -> {
            if (entry instanceof MapView) {
                builder.add((MapView) entry);
            }
        });
        return builder.build();
    }

    @Override
    public boolean deleteMap(String mapId) {
        // TODO: implement
        return false;
    }
}
