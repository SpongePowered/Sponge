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
package org.spongepowered.common.data.util;

import static org.spongepowered.api.data.DataTransactionResult.successNoData;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.BlockItemData;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;


public final class ItemsHelper {

    private ItemsHelper() { // No sub-classing for you!
    }

    public static <T extends DataManipulator<T,?>> Optional<T> getClone(T DataManipulator, Class<T> clazz) {

        return Optional.empty();
    }

    public static Optional<Integer> getDamageValue(final ItemType type, final Set<DataManipulator<?, ?>> DataManipulatorSet) {
        if (type instanceof ItemBlock) {
            // If it's a block, well, we definitely should have some block state information we can use
            for (DataManipulator<?, ?> data : DataManipulatorSet) {
                if (data instanceof BlockItemData) {
                    BlockItemData blockData = (BlockItemData) data;
                    return Optional
                            .of(Block.getBlockFromItem((Item) type).damageDropped((BlockStateContainer.StateImplementation) blockData.state()));
                }
            }
        } else if (((Item) type).getHasSubtypes()) {
            // TODO we need a better way to represent identifiable damage values

        } else {

        }
        return Optional.empty();
    }

    public static DataTransactionResult validateData(ItemType type, DataManipulator<?, ?> data) {
        return successNoData(); // TODO actually implement
    }

    public static DataTransactionResult setData(ItemStack stack, DataManipulator<?, ?> data) {

        return successNoData(); // TODO
    }
}
