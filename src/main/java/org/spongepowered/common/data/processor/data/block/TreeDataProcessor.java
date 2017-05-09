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

import net.minecraft.block.BlockPlanks;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableTreeData;
import org.spongepowered.api.data.manipulator.mutable.block.TreeData;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeTreeData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class TreeDataProcessor extends AbstractCatalogDataProcessor<TreeType, Value<TreeType>, TreeData, ImmutableTreeData> {

    public TreeDataProcessor() {
        super(Keys.TREE_TYPE, input -> input.getItem() == ItemTypes.PLANKS || input.getItem() == ItemTypes.LEAVES
                || input.getItem() == ItemTypes.LEAVES2 || input.getItem() == ItemTypes.LOG
                || input.getItem() == ItemTypes.LOG2 || input.getItem() == ItemTypes.SAPLING
                || input.getItem() == ItemTypes.WOODEN_SLAB);
    }

    @Override
    protected int setToMeta(TreeType value) {
        return ((BlockPlanks.EnumType) (Object) value).getMetadata();
    }

    @Override
    protected TreeType getFromMeta(int meta) {
        return (TreeType) (Object) BlockPlanks.EnumType.byMetadata(meta);
    }

    @Override
    protected Optional<TreeType> getVal(ItemStack stack) {
        if (stack.getItem() == ItemTypes.LEAVES2 || stack.getItem() == ItemTypes.LOG2) {
            return Optional.of(getFromMeta(stack.getItemDamage() + 4));
        } else {
            return Optional.of(getFromMeta(stack.getItemDamage()));
        }
    }

    @Override
    public TreeData createManipulator() {
        return new SpongeTreeData();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected boolean set(ItemStack stack, TreeType value) {
        // TODO - the API needs to be changed, as its no longer possible to change an ItemStack's type
        return false;
        /*
        if (stack.getItem() == ItemTypes.LOG || stack.getItem() == ItemTypes.LEAVES) {
            if (value.equals(TreeTypes.OAK) || value.equals(TreeTypes.BIRCH) ||
                    value.equals(TreeTypes.SPRUCE) || value.equals(TreeTypes.JUNGLE)) {
                stack.setItemDamage(this.setToMeta(value));
                return true;
            } else {
                // converting block so we can set new types to the log/leave
                if (stack.getItem() == ItemTypes.LOG) {
                    stack.setItem(Item.getItemFromBlock(Blocks.LOG2));
                    stack.setItemDamage(this.setToMeta(value) - 4);
                    return true;
                } else {
                    stack.setItem(Item.getItemFromBlock(Blocks.LEAVES2));
                    stack.setItemDamage(this.setToMeta(value) - 4);
                    return true;
                }
            }
        } else if (stack.getItem() == ItemTypes.LOG2 || stack.getItem() == ItemTypes.LEAVES2) {
            if (value.equals(TreeTypes.ACACIA) || value.equals(TreeTypes.DARK_OAK)) {
                stack.setItemDamage(this.setToMeta(value) - 4);
                return true;
            } else {
                // converting block so we can set old types to the log/leave
                if (stack.getItem() == ItemTypes.LOG2) {
                    stack.setItem(Item.getItemFromBlock(Blocks.LOG));
                } else {
                    stack.setItem(Item.getItemFromBlock(Blocks.LEAVES));
                }
                stack.setItemDamage(this.setToMeta(value));
                return true;
            }
        } else {
            stack.setItemDamage(this.setToMeta(value));
            return true;
        }*/
    }

    @Override
    protected TreeType getDefaultValue() {
        return TreeTypes.OAK;
    }

    @Override
    protected Value<TreeType> constructValue(TreeType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
