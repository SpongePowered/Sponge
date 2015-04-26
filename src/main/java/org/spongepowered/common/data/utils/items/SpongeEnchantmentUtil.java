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
package org.spongepowered.common.data.utils.items;

import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.items.EnchantmentData;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.items.SpongeEnchantmentItemData;

import java.util.Map;

public class SpongeEnchantmentUtil implements SpongeDataUtil<EnchantmentData> {

    @Override
    public Optional<EnchantmentData> fillData(DataHolder holder, EnchantmentData manipulator, DataPriority priority) {
        if (holder instanceof ItemStack && ((ItemStack) holder).isItemEnchanted()) {
            NBTTagList compound = ((ItemStack) holder).getEnchantmentTagList();
            Map<Enchantment, Integer> enchantmentIntegerMap = Maps.newHashMap();

            for (int i = 0; i < compound.tagCount(); i++) {
                int enchantment = compound.getCompoundTagAt(i).getShort("id");
                int level = compound.getCompoundTagAt(i).getShort("lvl");

                if (net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantment) != null) {
                    enchantmentIntegerMap.put((Enchantment) (net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantment)), level);
                }
            }
            manipulator.setUnsafe(enchantmentIntegerMap);
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, EnchantmentData manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).isItemEnchanted()) {
            NBTTagList compound = ((ItemStack) dataHolder).getEnchantmentTagList();
            Map<Enchantment, Integer> enchantmentIntegerMap = Maps.newHashMap();

            for (int i = 0; i < compound.tagCount(); i++) {
                int enchantment = compound.getCompoundTagAt(i).getShort("id");
                int level = compound.getCompoundTagAt(i).getShort("lvl");

                if (net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantment) != null) {
                    enchantmentIntegerMap.put((Enchantment) (net.minecraft.enchantment.Enchantment.getEnchantmentById(enchantment)), level);
                }
            }
            manipulator.setUnsafe(enchantmentIntegerMap);
            SpongeEnchantmentItemData old = new SpongeEnchantmentItemData();
            old.setUnsafe(enchantmentIntegerMap);

            NBTTagList newList = new NBTTagList();
            for (Map.Entry<Enchantment, Integer> entry : manipulator.asMap().entrySet()) {
                NBTTagCompound enchantmentCompound = new NBTTagCompound();
                enchantmentCompound.setShort("id", (short) ((net.minecraft.enchantment.Enchantment) entry.getKey()).effectId);
                enchantmentCompound.setShort("lvl", entry.getValue().shortValue());
                newList.appendTag(enchantmentCompound);
            }
            ((ItemStack) dataHolder).setTagInfo("ench", newList);
            return builder().replace(old).result(DataTransactionResult.Type.SUCCESS).build();
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<EnchantmentData> build(DataView container) throws InvalidDataException {
        return null;
    }

    @Override
    public EnchantmentData create() {
        return new SpongeEnchantmentItemData();
    }

    @Override
    public Optional<EnchantmentData> createFrom(DataHolder dataHolder) {
        return null;
    }
}
