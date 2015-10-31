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
package org.spongepowered.common.data.processor.value.block;

import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.api.data.type.SlabTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SlabTypeValueProcessor extends AbstractCatalogDataValueProcessor<SlabType, Value<SlabType>> {

    public SlabTypeValueProcessor() {
        super(Keys.SLAB_TYPE);
    }

    protected boolean supports(ItemStack container) {
        return container.getItem() == ItemTypes.STONE_SLAB || container.getItem() == ItemTypes.STONE_SLAB2;
    }

    @Override
    protected SlabType getFromMeta(int meta) {
        return (SlabType) (Object) BlockStoneSlab.EnumType.byMetadata(meta);
    }

    @Override
    protected int setToMeta(SlabType type) {
        return -1; // not used
    }

    @Override
    protected boolean set(ItemStack container, SlabType value) {
        if (container.getItem() == ItemTypes.STONE_SLAB) {
            if (!value.equals(SlabTypes.RED_SAND)) {
                container.setItemDamage(((BlockStoneSlab.EnumType) (Object) value).getMetadata());
            } else {
                container.setItem(Item.getItemFromBlock(Blocks.stone_slab2));
                container.setItemDamage(((BlockStoneSlabNew.EnumType) (Object) value).getMetadata());
            }
        } else if (container.getItem() == ItemTypes.STONE_SLAB2) {
            if (container.getItem() == ItemTypes.STONE_SLAB2) {
                if (value.equals(SlabTypes.RED_SAND)) {
                    container.setItemDamage(((BlockStoneSlabNew.EnumType) (Object) value).getMetadata());
                } else {
                    container.setItem(Item.getItemFromBlock(Blocks.stone_slab));
                    container.setItemDamage(((BlockStoneSlab.EnumType) (Object) value).getMetadata());
                }
            }
        }
        return true;
    }

    @Override
    protected Value<SlabType> constructValue(SlabType defaultValue) {
        return new SpongeValue<>(Keys.SLAB_TYPE, SlabTypes.COBBLESTONE, defaultValue);
    }
}
