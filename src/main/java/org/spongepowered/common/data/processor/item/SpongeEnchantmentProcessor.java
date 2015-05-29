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
package org.spongepowered.common.data.processor.item;

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
import org.spongepowered.api.data.component.item.EnchantmentComponent;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.item.SpongeEnchantmentItemComponent;

import java.util.Map;

public class SpongeEnchantmentProcessor implements SpongeDataProcessor<EnchantmentComponent> {

    @Override
    public Optional<EnchantmentComponent> fillData(DataHolder dataHolder, EnchantmentComponent manipulator, DataPriority priority) {
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
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, EnchantmentComponent manipulator, DataPriority priority) {
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
            SpongeEnchantmentItemComponent old = new SpongeEnchantmentItemComponent();
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
        if (!(dataHolder instanceof ItemStack)) {
            return false;
        }
        if (!((ItemStack) dataHolder).isItemEnchanted()) {
            return false;
        }
        ((ItemStack) dataHolder).getTagCompound().removeTag("ench");
        return true;
    }

    @Override
    public Optional<EnchantmentComponent> build(DataView container) throws InvalidDataException {
        return null;
    }

    @Override
    public EnchantmentComponent create() {
        return new SpongeEnchantmentItemComponent();
    }

    @Override
    public Optional<EnchantmentComponent> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack)) {
            return Optional.absent();
        }
        final EnchantmentComponent data = create();
        return fillData(dataHolder, data, DataPriority.DATA_HOLDER);
    }

    @Override
    public Optional<EnchantmentComponent> getFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack)) {
            return Optional.absent();
        }
        if (!((ItemStack) dataHolder).isItemEnchanted()) {
            return Optional.absent();
        }
        return createFrom(dataHolder);
    }
}
