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
package org.spongepowered.common.item;

import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.Component;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.component.SingleValueComponent;
import org.spongepowered.api.data.component.item.BlockItemComponent;
import org.spongepowered.api.data.component.item.DurabilityComponent;
import org.spongepowered.api.item.ItemBlock;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Collection;
import java.util.Set;


public final class ItemsHelper {

    public static NBTTagCompound getTagCompound(net.minecraft.item.ItemStack itemStack) {
        NBTTagCompound compound = itemStack.getTagCompound();
        if (compound == null) {
            compound = new NBTTagCompound();
            itemStack.setTagCompound(compound);
        }
        return compound;
    }


    public static final DataTransactionResult SUCCESS_NO_REPLACEMENTS = new DataTransactionResult() {
        @Override
        public Type getType() {
            return Type.SUCCESS;
        }

        @Override
        public Optional<Collection<Component<?>>> getRejectedData() {
            return Optional.absent();
        }

        @Override
        public Optional<Collection<Component<?>>> getReplacedData() {
            return Optional.absent();
        }
    };

    private ItemsHelper() { // No subclassing for you!
    }

    public static <T extends Component<T>> Optional<T> getClone(T Component, Class<T> clazz) {

        return Optional.absent();
    }

    public static Optional<Integer> getDamageValue(final ItemType type, final Set<Component<?>> ComponentSet) {
        if (type instanceof ItemBlock) {
            // If it's a block, well, we definitely should have some block state information we can use
            for (Component<?> data : ComponentSet) {
                if (data instanceof BlockItemComponent) {
                    BlockItemComponent blockData = (BlockItemComponent) data;
                    return Optional.of(Block.getBlockFromItem((Item) type).damageDropped((BlockState.StateImplementation) blockData.getValue()));
                }
            }
        } else if (((Item) type).getHasSubtypes()) {
            // TODO we need a better way to represent identifiable damage values

        } else {
            for (Component<?> data : ComponentSet) {
                // Otherwise, it's a durability number
                if (data instanceof DurabilityComponent) {
                    return Optional.of(((DurabilityComponent) data).getValue());
                } else if (data instanceof SingleValueComponent<?, ?>) {
                    // We really need to figure this one out.
                }
            }
        }
        return Optional.absent();
    }

    public static DataTransactionResult validateData(ItemType type, Component<?> data) {
        return SUCCESS_NO_REPLACEMENTS; // TODO actually implement
    }

    public static DataTransactionResult setData(ItemStack stack, Component<?> data) {

        return SUCCESS_NO_REPLACEMENTS; // TODO
    }
}
