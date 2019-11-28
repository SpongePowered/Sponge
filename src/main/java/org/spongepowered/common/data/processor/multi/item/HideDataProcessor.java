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
package org.spongepowered.common.data.processor.multi.item;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableHideData;
import org.spongepowered.api.data.manipulator.mutable.item.HideData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeHideData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Optional;

public class HideDataProcessor extends AbstractMultiDataSingleTargetProcessor<ItemStack, HideData, ImmutableHideData> {

    public HideDataProcessor() {
        super(ItemStack.class);
    }

    @Override
    protected boolean doesDataExist(ItemStack dataHolder) {
        return dataHolder.func_77942_o();
    }

    @Override
    protected boolean set(ItemStack dataHolder, Map<Key<?>, Object> keyValues) {
        if (!dataHolder.func_77942_o()) {
            dataHolder.func_77982_d(new NBTTagCompound());
        }
        int flag = 0;
        if ((boolean) keyValues.get(Keys.HIDE_ENCHANTMENTS)) {
            flag |= Constants.Item.HIDE_ENCHANTMENTS_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_ATTRIBUTES)) {
            flag |= Constants.Item.HIDE_ATTRIBUTES_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_UNBREAKABLE)) {
            flag |= Constants.Item.HIDE_UNBREAKABLE_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_CAN_DESTROY)) {
            flag |= Constants.Item.HIDE_CAN_DESTROY_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_CAN_PLACE)) {
            flag |= Constants.Item.HIDE_CAN_PLACE_FLAG;
        }
        if ((boolean) keyValues.get(Keys.HIDE_MISCELLANEOUS)) {
            flag |= Constants.Item.HIDE_MISCELLANEOUS_FLAG;
        }
        dataHolder.func_77978_p().func_74768_a(Constants.Item.ITEM_HIDE_FLAGS, flag);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(ItemStack dataHolder) {
        if (!dataHolder.func_77942_o()) {
            return Maps.newHashMap();
        }
        Map<Key<?>, Boolean> map = Maps.newHashMap();
        int flag = dataHolder.func_77978_p().func_74762_e(Constants.Item.ITEM_HIDE_FLAGS);

        map.put(Keys.HIDE_MISCELLANEOUS, (flag & Constants.Item.HIDE_MISCELLANEOUS_FLAG) != 0);
        map.put(Keys.HIDE_CAN_PLACE, (flag & Constants.Item.HIDE_CAN_PLACE_FLAG) != 0);
        map.put(Keys.HIDE_CAN_DESTROY, (flag & Constants.Item.HIDE_CAN_DESTROY_FLAG) != 0);
        map.put(Keys.HIDE_UNBREAKABLE, (flag & Constants.Item.HIDE_UNBREAKABLE_FLAG) != 0);
        map.put(Keys.HIDE_ATTRIBUTES, (flag & Constants.Item.HIDE_ATTRIBUTES_FLAG) != 0);
        map.put(Keys.HIDE_ENCHANTMENTS, (flag & Constants.Item.HIDE_ENCHANTMENTS_FLAG) != 0);

        return map;
    }

    @Override
    protected HideData createManipulator() {
        return new SpongeHideData();
    }

    @Override
    public Optional<HideData> fill(DataContainer container, HideData hideData) {
        Optional<Boolean> enchantments = container.getBoolean(Keys.HIDE_ENCHANTMENTS.getQuery());
        Optional<Boolean> attributes = container.getBoolean(Keys.HIDE_ATTRIBUTES.getQuery());
        Optional<Boolean> unbreakable = container.getBoolean(Keys.HIDE_UNBREAKABLE.getQuery());
        Optional<Boolean> canDestroy = container.getBoolean(Keys.HIDE_CAN_DESTROY.getQuery());
        Optional<Boolean> canPlace = container.getBoolean(Keys.HIDE_CAN_PLACE.getQuery());
        Optional<Boolean> miscellaneous = container.getBoolean(Keys.HIDE_MISCELLANEOUS.getQuery());
        if (enchantments.isPresent() && attributes.isPresent() && unbreakable.isPresent() && canDestroy.isPresent() && canPlace.isPresent()
                && miscellaneous.isPresent()) {
            return Optional.of(new SpongeHideData(enchantments.get(), attributes.get(), unbreakable.get(), canDestroy.get(), canPlace.get(),
                    miscellaneous.get()));
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (supports(dataHolder)) {
            ItemStack data = (ItemStack) dataHolder;
            if (data.func_77942_o() && data.func_77978_p().func_150297_b(Constants.Item.ITEM_HIDE_FLAGS, Constants.NBT.TAG_INT)) {
                data.func_77978_p().func_82580_o(Constants.Item.ITEM_HIDE_FLAGS);
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }
}
