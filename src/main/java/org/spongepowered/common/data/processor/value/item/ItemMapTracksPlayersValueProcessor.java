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
package org.spongepowered.common.data.processor.value.item;

import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableMapItemData;
import org.spongepowered.api.data.manipulator.mutable.item.MapItemData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.bridge.world.storage.MapStorageBridge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeMapItemData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemMapTracksPlayersValueProcessor extends AbstractItemSingleDataProcessor<Boolean, Value<Boolean>, MapItemData, ImmutableMapItemData> {

    public ItemMapTracksPlayersValueProcessor() {
        super(itemStack -> ((org.spongepowered.api.item.inventory.ItemStack) itemStack)
                .getType() == ItemTypes.FILLED_MAP, Keys.MAP_TRACKS_PLAYERS);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return new SpongeValue<>(Keys.MAP_TRACKS_PLAYERS,
                Constants.ItemStack.DEFAULT_TRACKS_PLAYERS, actualValue);
    }

    @Override
    protected boolean set(ItemStack dataHolder, Boolean value) {
        Optional<MapData> mapData = Sponge.getServer().getMapStorage()
                .flatMap(mapStorage -> ((MapStorageBridge)mapStorage).bridge$getMinecraftMapData(dataHolder.getMetadata()));
        if (!mapData.isPresent()) {
            return false;
        }
        mapData.get().trackingPosition = value;
        return true;
    }

    @Override
    protected Optional<Boolean> getVal(ItemStack dataHolder) {
        return Sponge.getServer().getMapStorage()
                .flatMap(mapStorage -> ((MapStorageBridge)mapStorage).bridge$getMinecraftMapData(dataHolder.getMetadata()))
                .map(mapData -> mapData.trackingPosition);
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MapItemData createManipulator() {
        return new SpongeMapItemData();
    }
}
