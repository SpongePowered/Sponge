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
package org.spongepowered.common.data.builder.item;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.DataUtil.getData;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.persistence.SerializedDataTransaction;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;

import java.util.List;
import java.util.Optional;

public class SpongeItemStackDataBuilder extends AbstractDataBuilder<ItemStack> implements DataBuilder<ItemStack> {

    public SpongeItemStackDataBuilder() {
        super(ItemStack.class, 1);
    }

    @Override
    protected Optional<ItemStack> buildContent(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(DataQueries.ITEM_TYPE) || !container.contains(DataQueries.ITEM_COUNT)
                || !container.contains(DataQueries.ITEM_DAMAGE_VALUE)) {
            return Optional.empty();
        }
        final String itemTypeId = getData(container, DataQueries.ITEM_TYPE, String.class);
        final int count = getData(container, DataQueries.ITEM_COUNT, Integer.class);
        final ItemType itemType = SpongeImpl.getRegistry().getType(ItemType.class, itemTypeId).get();
        final int damage = getData(container, DataQueries.ITEM_DAMAGE_VALUE, Integer.class);
        final net.minecraft.item.ItemStack itemStack = new net.minecraft.item.ItemStack((Item) itemType, count, damage);
        if (container.contains(DataQueries.UNSAFE_NBT)) {
            final NBTTagCompound compound = NbtTranslator.getInstance().translateData(container.getView(DataQueries.UNSAFE_NBT).get());
            itemStack.setTagCompound(compound);
        }
        if (container.contains(DataQueries.DATA_MANIPULATORS)) {
            final List<DataView> views = container.getViewList(DataQueries.DATA_MANIPULATORS).get();
            final SerializedDataTransaction transaction = DataUtil.deserializeManipulatorList(views);
            final List<DataManipulator<?, ?>> manipulators = transaction.deserializedManipulators;
            for (DataManipulator<?, ?> manipulator : manipulators) {
                ((IMixinCustomDataHolder) itemStack).offerCustom(manipulator, MergeFunction.IGNORE_ALL);
            }
            if (!transaction.failedData.isEmpty()) {
                ((IMixinCustomDataHolder) itemStack).addFailedData(transaction.failedData);
            }
        }
        return Optional.of((ItemStack) itemStack);
    }

}
