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

import net.minecraft.block.BlockPlanks;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class TreeTypeValueProcessor extends AbstractCatalogDataValueProcessor<TreeType, Value<TreeType>> {

    public TreeTypeValueProcessor() {
        super(Keys.TREE_TYPE);
    }

    @Override
    protected TreeType getFromMeta(int meta) {
        return (TreeType) (Object) BlockPlanks.EnumType.byMetadata(meta);
    }

    @Override
    protected int setToMeta(TreeType type) {
        return ((BlockPlanks.EnumType) (Object) type).getMetadata();
    }

    @Override
    protected boolean set(ItemStack stack, TreeType value) {
        if (stack.getItem() == ItemTypes.LOG || stack.getItem() == ItemTypes.LEAVES) {
            if (value.equals(TreeTypes.OAK) || value.equals(TreeTypes.BIRCH) ||
                    value.equals(TreeTypes.SPRUCE) || value.equals(TreeTypes.JUNGLE)) {
                stack.setItemDamage(this.setToMeta(value));
                return true;
            } else {
                // converting block so we can set new types to the log/leave
                if (stack.getItem() == ItemTypes.LOG) {
                    stack.setItem(Item.getItemFromBlock(Blocks.log2));
                    stack.setItemDamage(this.setToMeta(value) - 4);
                    return true;
                } else {
                    stack.setItem(Item.getItemFromBlock(Blocks.leaves2));
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
                    stack.setItem(Item.getItemFromBlock(Blocks.log));
                } else {
                    stack.setItem(Item.getItemFromBlock(Blocks.leaves));
                }
                stack.setItemDamage(this.setToMeta(value));
                return true;
            }
        } else {
            stack.setItemDamage(this.setToMeta(value));
            return true;
        }
    }

    @Override
    protected Optional<TreeType> getVal(ItemStack stack) {
        if (stack.getItem() == ItemTypes.LEAVES2 || stack.getItem() == ItemTypes.LOG2) {
            return Optional.of(getFromMeta(stack.getItemDamage() + 4));
        } else {
            return Optional.of(getFromMeta(stack.getItemDamage()));
        }
    }

    protected boolean supports(ItemStack container) {
        return container.getItem() == ItemTypes.PLANKS || container.getItem() == ItemTypes.LEAVES
                || container.getItem() == ItemTypes.LEAVES2 || container.getItem() == ItemTypes.LOG
                || container.getItem() == ItemTypes.LOG2 || container.getItem() == ItemTypes.SAPLING
                || container.getItem() == ItemTypes.WOODEN_SLAB;
    }

    @Override
    protected Value<TreeType> constructValue(TreeType defaultValue) {
        return new SpongeValue<>(Keys.TREE_TYPE, TreeTypes.OAK, defaultValue);
    }
}
