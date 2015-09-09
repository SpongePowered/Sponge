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
package org.spongepowered.common.data.builder.manipulator.mutable.item;

import com.google.common.collect.Sets;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableBreakableData;
import org.spongepowered.api.data.manipulator.mutable.item.BreakableData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeBreakableData;
import org.spongepowered.common.data.util.NbtDataUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BreakableDataBuilder implements DataManipulatorBuilder<BreakableData, ImmutableBreakableData> {

    @Override
    public BreakableData create() {
        return new SpongeBreakableData();
    }

    @Override
    public Optional<BreakableData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            NBTTagCompound tag = ((ItemStack) dataHolder).getTagCompound();
            if (tag == null) {
                return Optional.<BreakableData>of(new SpongeBreakableData());
            }
            NBTTagList blockIds = tag.getTagList(NbtDataUtil.ITEM_BREAKABLE_BLOCKS, NbtDataUtil.TAG_STRING);
            if (blockIds.hasNoTags()) {
                return Optional.<BreakableData>of(new SpongeBreakableData());
            }
            Set<BlockType> blockTypes = Sets.newHashSetWithExpectedSize(blockIds.tagCount());
            for (int i = 0; i < blockIds.tagCount(); i++) {
                Optional<BlockType> blockType = Sponge.getGame().getRegistry()
                        .getType(BlockType.class, blockIds.getStringTagAt(i));
                if (blockType.isPresent()) {
                    blockTypes.add(blockType.get());
                }
            }
            return Optional.<BreakableData>of(new SpongeBreakableData(blockTypes));
        }
        return Optional.empty();
    }

    @Override
    public Optional<BreakableData> build(DataView container) throws InvalidDataException {
        if (container.contains(Keys.BREAKABLE_BLOCK_TYPES.getQuery())) {
            List<String> blockIds = container.getStringList(Keys.BREAKABLE_BLOCK_TYPES.getQuery()).get();
            Set<BlockType> blockTypes = Sets.newHashSetWithExpectedSize(blockIds.size());
            for (String blockId : blockIds) {
                blockTypes.add(Sponge.getGame().getRegistry().getType(BlockType.class, blockId).get());
            }
            return Optional.<BreakableData>of(new SpongeBreakableData(blockTypes));
        }
        return Optional.empty();
    }
}
