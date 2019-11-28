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
package org.spongepowered.common.data.processor.data.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockPlanks;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.type.WoodType;
import org.spongepowered.api.data.type.WoodTypes;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Map;
import java.util.Optional;

public class TreeDataProcessor extends AbstractCatalogDataProcessor<WoodType, Mutable<WoodType>, TreeData, ImmutableTreeData> {

    private static final Map<ItemType, WoodType> boatMapping = ImmutableMap.<ItemType, WoodType>builder()
            .put(ItemTypes.BOAT, WoodTypes.OAK)
            .put(ItemTypes.ACACIA_BOAT, WoodTypes.ACACIA)
            .put(ItemTypes.BIRCH_BOAT, WoodTypes.BIRCH)
            .put(ItemTypes.DARK_OAK_BOAT, WoodTypes.DARK_OAK)
            .put(ItemTypes.JUNGLE_BOAT, WoodTypes.JUNGLE)
            .put(ItemTypes.SPRUCE_BOAT, WoodTypes.SPRUCE)
            .build();

    public TreeDataProcessor() {
        super(Keys.TREE_TYPE, input -> input.getItem() == ItemTypes.PLANKS || input.getItem() == ItemTypes.LEAVES
                || input.getItem() == ItemTypes.LEAVES2 || input.getItem() == ItemTypes.LOG
                || input.getItem() == ItemTypes.LOG2 || input.getItem() == ItemTypes.SAPLING
                || input.getItem() == ItemTypes.WOODEN_SLAB || boatMapping.containsKey((ItemType) input.getItem()));
    }

    @Override
    protected int setToMeta(WoodType value) {
        return ((BlockPlanks.EnumType) (Object) value).getMetadata();
    }

    @Override
    protected WoodType getFromMeta(int meta) {
        return (WoodType) (Object) BlockPlanks.EnumType.byMetadata(meta);
    }

    @Override
    protected Optional<WoodType> getVal(ItemStack stack) {
        if (stack.getItem() == ItemTypes.LEAVES2 || stack.getItem() == ItemTypes.LOG2) {
            return Optional.of(getFromMeta(stack.getDamage() + 4));
        } else if (boatMapping.containsKey((ItemType) stack.getItem())) {
            return Optional.of(boatMapping.get(stack.getItem()));
        } else {
            return Optional.of(getFromMeta(stack.getDamage()));
        }
    }

    @Override
    public TreeData createManipulator() {
        return new SpongeTreeData();
    }

    @Override
    protected boolean set(ItemStack stack, WoodType value) {
        // TODO - the API needs to be changed, as its no longer possible to change an ItemStack's type

        if (stack.getItem() == ItemTypes.LOG || stack.getItem() == ItemTypes.LEAVES) {
            if (value == WoodTypes.ACACIA || value == WoodTypes.DARK_OAK) {
                return false; // TODO
            }
            stack.setItemDamage(this.setToMeta(value));
            return true;
        }
        else if (stack.getItem() == ItemTypes.LOG2 || stack.getItem() == ItemTypes.LEAVES2) {
            if (value == WoodTypes.OAK || value == WoodTypes.SPRUCE || value == WoodTypes.BIRCH || value == WoodTypes.JUNGLE) {
                return false; // TODO
            }
            stack.setItemDamage(this.setToMeta(value) - 4);
            return true;
        }
        else {
            stack.setItemDamage(this.setToMeta(value));
            return true;
        }
    }

    @Override
    protected WoodType getDefaultValue() {
        return WoodTypes.OAK;
    }

    @Override
    protected Mutable<WoodType> constructValue(WoodType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
