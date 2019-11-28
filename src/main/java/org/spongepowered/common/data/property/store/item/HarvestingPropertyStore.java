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
package org.spongepowered.common.data.property.store.item;

import com.google.common.collect.ImmutableSet;  
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.property.item.HarvestingProperty;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.property.store.common.AbstractItemStackPropertyStore;
import org.spongepowered.common.mixin.core.item.ItemToolAccessor;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class HarvestingPropertyStore extends AbstractItemStackPropertyStore<HarvestingProperty> {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    protected Optional<HarvestingProperty> getFor(ItemStack itemStack) {
        final Item item = itemStack.func_77973_b();
        if (item instanceof ItemToolAccessor && !(item instanceof ItemPickaxe)) {
            final ImmutableSet<BlockType> blocks = ImmutableSet.copyOf((Set) ((ItemToolAccessor) item).accessor$getEffectiveBlocks());
            return Optional.of(new HarvestingProperty(blocks));
        }
        final Collection<BlockType> blockTypes = SpongeImpl.getRegistry().getAllOf(BlockType.class);
        final ImmutableSet.Builder<BlockType> builder = ImmutableSet.builder();
        blockTypes.stream().filter(blockType -> item.func_150897_b((IBlockState) blockType.getDefaultState())).forEach(builder::add);
        final ImmutableSet<BlockType> blocks = builder.build();
        if (blocks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new HarvestingProperty(blocks));
    }
}
