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

import com.google.common.collect.Maps;
import net.minecraft.block.BlockJukebox;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JukeboxDataProcessor extends AbstractTileEntityDataProcessor<BlockJukebox.TileEntityJukebox, RepresentedItemData, ImmutableRepresentedItemData> {

    public JukeboxDataProcessor() {
        super(BlockJukebox.TileEntityJukebox.class);
    }

    @Override
    protected boolean doesDataExist(BlockJukebox.TileEntityJukebox entity) {
        return true;
    }

    @Override
    protected boolean set(BlockJukebox.TileEntityJukebox entity, Map<Key<?>, Object> keyValues) {
        entity.setRecord((ItemStack) ((ItemStackSnapshot)keyValues.get(Keys.REPRESENTED_ITEM)).createStack());
        entity.getWorld().setBlockState(entity.getPos(), entity.getWorld().getBlockState(entity.getPos()).withProperty(BlockJukebox.HAS_RECORD, true), 2);
        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(BlockJukebox.TileEntityJukebox entity) {
        HashMap<Key<?>, ItemStackSnapshot> values = Maps.newHashMapWithExpectedSize(1);
        if(entity.getRecord() != null) {
            values.put(Keys.REPRESENTED_ITEM, ((org.spongepowered.api.item.inventory.ItemStack) entity.getRecord()).createSnapshot());
        }
        return values;
    }

    @Override
    public Optional<RepresentedItemData> fill(DataContainer container, RepresentedItemData representedItemData) {
        representedItemData.set(Keys.REPRESENTED_ITEM, (ItemStackSnapshot)container.get(Keys.REPRESENTED_ITEM.getQuery()).get());
        return Optional.of(representedItemData);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        final DataTransactionBuilder builder = DataTransactionBuilder.builder();
        if(dataHolder instanceof BlockJukebox.TileEntityJukebox) {
            Optional<ItemStackSnapshot> itemStackSnapshot = dataHolder.get(Keys.REPRESENTED_ITEM);
            if (itemStackSnapshot.isPresent()) {
                try {
                    BlockJukebox.TileEntityJukebox jukebox = (BlockJukebox.TileEntityJukebox) dataHolder;
                    ((BlockJukebox) Blocks.jukebox).dropRecord(jukebox.getWorld(), jukebox.getPos(), null);
                    jukebox.getWorld().setBlockState(jukebox.getPos(),
                            jukebox.getWorld().getBlockState(jukebox.getPos()).withProperty(BlockJukebox.HAS_RECORD, false), 2);
                    return builder.replace(itemStackSnapshot.get().getValues()).result(DataTransactionResult.Type.SUCCESS).build();
                } catch (Exception e) {
                    Sponge.getLogger().error("There was an issue removing the repesented item from an Jukebox!", e);
                    return builder.result(DataTransactionResult.Type.ERROR).build();
                }
            } else {
                return DataTransactionBuilder.successNoData();
            }
        }

        return builder.failNoData();
    }

    @Override
    protected RepresentedItemData createManipulator() {
        return new SpongeRepresentedItemData();
    }
}
