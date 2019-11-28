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
package org.spongepowered.common.data.processor.data.tileentity;

import org.spongepowered.api.block.tileentity.Jukebox;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.registry.type.ItemTypeRegistryModule;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.JukeboxBlock.TileEntityJukebox;
import net.minecraft.item.MusicDiscItem;

public class JukeboxDataProcessor extends
        AbstractTileEntitySingleDataProcessor<JukeboxBlock.TileEntityJukebox, ItemStackSnapshot, Value<ItemStackSnapshot>, RepresentedItemData, ImmutableRepresentedItemData> {

    public JukeboxDataProcessor() {
        super(JukeboxBlock.TileEntityJukebox.class, Keys.REPRESENTED_ITEM);
    }

    @Override
    protected boolean set(JukeboxBlock.TileEntityJukebox jukebox, ItemStackSnapshot stackSnapshot) {
        BlockState block = jukebox.getWorld().getBlockState(jukebox.getPos());
        if (stackSnapshot == ItemStackSnapshot.NONE) {
            if (jukebox.getRecord() == null) {
                return true;
            }
            return remove(jukebox);
        }
        if (!(stackSnapshot.getType() instanceof MusicDiscItem)) {
            return false;
        }
        ((Jukebox) jukebox).insertRecord(stackSnapshot.createStack());
        block = jukebox.getWorld().getBlockState(jukebox.getPos());
        return block.getBlock() instanceof JukeboxBlock && block.get(JukeboxBlock.HAS_RECORD);
    }

    @Override
    protected Optional<ItemStackSnapshot> getVal(JukeboxBlock.TileEntityJukebox jukebox) {
        if (jukebox.getRecord() == null) {
            return Optional.empty();
        }
        return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) jukebox.getRecord()).createSnapshot());
    }

    private boolean remove(JukeboxBlock.TileEntityJukebox jukebox) {
        ((Jukebox) jukebox).ejectRecord();
        BlockState block = jukebox.getWorld().getBlockState(jukebox.getPos());
        return block.getBlock() instanceof JukeboxBlock && !block.get(JukeboxBlock.HAS_RECORD);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof JukeboxBlock.TileEntityJukebox)) {
            return DataTransactionResult.failNoData();
        }
        JukeboxBlock.TileEntityJukebox jukebox = (TileEntityJukebox) container;
        Optional<ItemStackSnapshot> old = getVal(jukebox);
        if (!old.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        if (remove(jukebox)) {
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.builder().result(DataTransactionResult.Type.ERROR).build();
    }

    @Override
    protected RepresentedItemData createManipulator() {
        return new SpongeRepresentedItemData();
    }

    @Override
    protected Value<ItemStackSnapshot> constructValue(ItemStackSnapshot value) {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, ItemTypeRegistryModule.getInstance().NONE_SNAPSHOT, value);
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> constructImmutableValue(ItemStackSnapshot value) {
        return new ImmutableSpongeValue<>(Keys.REPRESENTED_ITEM, ItemTypeRegistryModule.getInstance().NONE_SNAPSHOT, value);
    }

}
