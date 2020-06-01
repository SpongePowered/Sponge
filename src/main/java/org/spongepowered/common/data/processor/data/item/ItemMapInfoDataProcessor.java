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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.item.ItemStack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableMapInfoItemData;
import org.spongepowered.api.data.manipulator.mutable.item.MapInfoItemData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.common.bridge.world.storage.MapStorageBridge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeMapInfoItemData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.map.SpongeMapInfo;

import java.util.Optional;

public class ItemMapInfoDataProcessor extends AbstractItemSingleDataProcessor<MapInfo, Value<MapInfo>, MapInfoItemData, ImmutableMapInfoItemData> {
    protected ItemMapInfoDataProcessor() {
        super(itemStack -> ((org.spongepowered.api.item.inventory.ItemStack)itemStack).getType() == ItemTypes.FILLED_MAP
                && Sponge.getServer().getMapStorage()
                    .map(mapStorage -> (MapStorageBridge)mapStorage)
                    .flatMap(bridge -> bridge.bridge$getMinecraftMapData(itemStack.getMetadata()))
                .isPresent(),
                Keys.MAP_INFO);
    }

    @Override
    protected boolean set(ItemStack dataHolder, MapInfo value) {
        if (((org.spongepowered.api.item.inventory.ItemStack)dataHolder).getType() != ItemTypes.FILLED_MAP) {
            return false;
        }
        dataHolder.setItemDamage(((SpongeMapInfo)value).getMapId());
        return true;
    }

    @Override
    protected Optional<MapInfo> getVal(ItemStack dataHolder) {
        if (((org.spongepowered.api.item.inventory.ItemStack)dataHolder).getType() != ItemTypes.FILLED_MAP) {
            return Optional.empty();
        }
        return Sponge.getServer().getMapStorage()
                .map(mapStorage -> (MapStorageBridge)mapStorage)
                .flatMap(mapStorageBridge -> mapStorageBridge.bridge$getMinecraftMapData(dataHolder.getMetadata()))
                .map(mapData -> (MapInfo)mapData);

    }

    @Override
    protected ImmutableValue<MapInfo> constructImmutableValue(MapInfo value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected Value<MapInfo> constructValue(MapInfo actualValue) {
        return new SpongeValue<>(Keys.MAP_INFO, SpongeMapInfoItemData.getDefaultMapInfo(), actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MapInfoItemData createManipulator() {
        return new SpongeMapInfoItemData();
    }
}
