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
package org.spongepowered.common.mixin.api.mcp.map;

import com.google.common.collect.Sets;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Mixin(MapData.class)
public abstract class MapInfoMixin_API implements MapInfo {
    private MapInfoData getMapInfoData() {
        return new SpongeMapInfoData((MapData)(Object)this);
    }

    @Override
    public boolean isLinked(MapInfo other) {
        return ((MapDataBridge) this).bridge$getMapId() == ((MapDataBridge)other).bridge$getMapId();
    }

    @Override
    public boolean isLinked(ItemStack itemStack) {
        return itemStack.getType() == ItemTypes.FILLED_MAP
                && ((MapDataBridge) this).bridge$getMapId() == ((net.minecraft.item.ItemStack) itemStack).getMetadata();
    }

    @Override
    public boolean validateRawData(DataView container) {
        if (!container.getInt(Constants.Map.MAP_ID).isPresent()) {
            return false;
        }
        Optional<DataView> manipulator = container.getView(Constants.Map.MAP_DATA);
        return manipulator.filter(dataView -> new SpongeMapInfoData().from((DataContainer) dataView).isPresent()).isPresent();
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {
        Optional<DataView> manipulator = container.getView(Constants.Map.MAP_DATA);
        manipulator.flatMap(dataView -> new SpongeMapInfoData().from(dataView.copy()))
            .map(this::offer)
            .orElseThrow(() -> new InvalidDataException("Manipulator data given to MapInfo was invalid!"));
        ((MapDataBridge) this).bridge$setMapId(container.getInt(Constants.Map.MAP_ID).orElseThrow(() -> new InvalidDataException("Could not get the MapId from the container given to MapInfo")));
    }

    @Override
    public UUID getUniqueId() {
        return ((MapDataBridge) this).bridge$getUniqueId();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Map.MAP_ID, ((MapDataBridge) this).bridge$getMapId())
                .set(Constants.Map.MAP_DATA, this.getMapInfoData());
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        return Sets.newHashSet(this.getMapInfoData());
    }

}
