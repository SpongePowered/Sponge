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

import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockJukebox.TileEntityJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemRecord;
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

public class JukeboxDataProcessor extends
        AbstractTileEntitySingleDataProcessor<BlockJukebox.TileEntityJukebox, ItemStackSnapshot, Value<ItemStackSnapshot>, RepresentedItemData, ImmutableRepresentedItemData> {

    public JukeboxDataProcessor() {
        super(BlockJukebox.TileEntityJukebox.class, Keys.REPRESENTED_ITEM);
    }

    @Override
    protected boolean set(BlockJukebox.TileEntityJukebox jukebox, ItemStackSnapshot stackSnapshot) {
        IBlockState block = jukebox.func_145831_w().func_180495_p(jukebox.func_174877_v());
        if (stackSnapshot == ItemStackSnapshot.NONE) {
            if (jukebox.func_145856_a() == null) {
                return true;
            }
            return remove(jukebox);
        }
        if (!(stackSnapshot.getType() instanceof ItemRecord)) {
            return false;
        }
        ((Jukebox) jukebox).insertRecord(stackSnapshot.createStack());
        block = jukebox.func_145831_w().func_180495_p(jukebox.func_174877_v());
        return block.func_177230_c() instanceof BlockJukebox && block.func_177229_b(BlockJukebox.field_176432_a);
    }

    @Override
    protected Optional<ItemStackSnapshot> getVal(BlockJukebox.TileEntityJukebox jukebox) {
        if (jukebox.func_145856_a() == null) {
            return Optional.empty();
        }
        return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) jukebox.func_145856_a()).createSnapshot());
    }

    private boolean remove(BlockJukebox.TileEntityJukebox jukebox) {
        ((Jukebox) jukebox).ejectRecord();
        IBlockState block = jukebox.func_145831_w().func_180495_p(jukebox.func_174877_v());
        return block.func_177230_c() instanceof BlockJukebox && !block.func_177229_b(BlockJukebox.field_176432_a);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof BlockJukebox.TileEntityJukebox)) {
            return DataTransactionResult.failNoData();
        }
        BlockJukebox.TileEntityJukebox jukebox = (TileEntityJukebox) container;
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
