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
package org.spongepowered.common.data.processor.value.tileentity;

import net.minecraft.block.BlockJukebox;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class JukeboxValueProcessor extends AbstractSpongeValueProcessor<BlockJukebox.TileEntityJukebox, ItemStackSnapshot, Value<ItemStackSnapshot>> {

    public JukeboxValueProcessor() {
        super(BlockJukebox.TileEntityJukebox.class, Keys.REPRESENTED_ITEM);
    }

    @Override
    protected Value<ItemStackSnapshot> constructValue(ItemStackSnapshot value) {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, value);
    }

    @Override
    protected boolean set(BlockJukebox.TileEntityJukebox container, ItemStackSnapshot value) {
        container.setRecord((ItemStack) value.createStack());
        container.getWorld().setBlockState(container.getPos(), container.getWorld().getBlockState(container.getPos()).withProperty(BlockJukebox.HAS_RECORD, true), 2);
        return true;
    }

    @Override
    protected Optional<ItemStackSnapshot> getVal(BlockJukebox.TileEntityJukebox container) {
        if(container.getRecord() != null) {
            return Optional.of(((org.spongepowered.api.item.inventory.ItemStack)container.getRecord()).createSnapshot());
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> constructImmutableValue(ItemStackSnapshot value) {
        return new ImmutableSpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        if(container instanceof BlockJukebox.TileEntityJukebox) {
            Optional<ItemStackSnapshot> itemStackSnapshot = container.get(Keys.REPRESENTED_ITEM);
            if (itemStackSnapshot.isPresent()) {
                try {
                    BlockJukebox.TileEntityJukebox jukebox = (BlockJukebox.TileEntityJukebox) container;
                    ((BlockJukebox) Blocks.jukebox).dropRecord(jukebox.getWorld(), jukebox.getPos(), null);
                    jukebox.getWorld().setBlockState(jukebox.getPos(),
                            jukebox.getWorld().getBlockState(jukebox.getPos()).withProperty(BlockJukebox.HAS_RECORD, false), 2);
                    return builder.replace(itemStackSnapshot.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    SpongeImpl.getLogger().error("There was an issue removing the repesented item from an Jukebox!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return DataTransactionResult.successNoData();
            }
        }

        return DataTransactionResult.failNoData();
    }
}
