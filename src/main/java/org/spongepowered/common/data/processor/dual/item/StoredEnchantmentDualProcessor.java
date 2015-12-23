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
package org.spongepowered.common.data.processor.dual.item;

import com.google.common.collect.Lists;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableStoredEnchantmentData;
import org.spongepowered.api.data.manipulator.mutable.item.StoredEnchantmentData;
import org.spongepowered.api.data.meta.ItemEnchantment;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeStoredEnchantmentData;
import org.spongepowered.common.data.processor.dual.common.AbstractSingleTargetDualProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.List;
import java.util.Optional;

public class StoredEnchantmentDualProcessor extends
        AbstractSingleTargetDualProcessor<ItemStack, List<ItemEnchantment>, ListValue<ItemEnchantment>, StoredEnchantmentData,
                ImmutableStoredEnchantmentData> {

    public StoredEnchantmentDualProcessor() {
        super(ItemStack.class, Keys.STORED_ENCHANTMENTS);
    }

    public static Enchantment getEnchantmentById(int id) {
        switch (id) {
            case 1:
                return Enchantments.FIRE_PROTECTION;
            case 2:
                return Enchantments.FEATHER_FALLING;
            case 3:
                return Enchantments.BLAST_PROTECTION;
            case 4:
                return Enchantments.PROJECTILE_PROTECTION;
            case 5:
                return Enchantments.RESPIRATION;
            case 6:
                return Enchantments.AQUA_AFFINITY;
            case 7:
                return Enchantments.THORNS;
            case 8:
                return Enchantments.DEPTH_STRIDER;
            case 34:
                return Enchantments.UNBREAKING;
            case 16:
                return Enchantments.SHARPNESS;
            case 17:
                return Enchantments.SMITE;
            case 18:
                return Enchantments.BANE_OF_ARTHROPODS;
            case 19:
                return Enchantments.KNOCKBACK;
            case 20:
                return Enchantments.FIRE_ASPECT;
            case 21:
                return Enchantments.LOOTING;
            case 32:
                return Enchantments.EFFICIENCY;
            case 33:
                return Enchantments.SILK_TOUCH;
            case 35:
                return Enchantments.FORTUNE;
            case 48:
                return Enchantments.POWER;
            case 49:
                return Enchantments.PUNCH;
            case 50:
                return Enchantments.FLAME;
            case 51:
                return Enchantments.INFINITY;
            case 61:
                return Enchantments.LUCK_OF_THE_SEA;
            case 62:
                return Enchantments.LURE;
            default:
            case 0:
                return Enchantments.PROTECTION;
        }
    }

    public static int getIdByEnchantment(Enchantment enchantment) {
        if (enchantment.equals(Enchantments.FIRE_PROTECTION)) {
            return 1;
        } else if (enchantment.equals(Enchantments.FEATHER_FALLING)) {
            return 2;
        } else if (enchantment.equals(Enchantments.BLAST_PROTECTION)) {
            return 3;
        } else if (enchantment.equals(Enchantments.PROJECTILE_PROTECTION)) {
            return 4;
        } else if (enchantment.equals(Enchantments.RESPIRATION)) {
            return 5;
        } else if (enchantment.equals(Enchantments.AQUA_AFFINITY)) {
            return 6;
        } else if (enchantment.equals(Enchantments.THORNS)) {
            return 7;
        } else if (enchantment.equals(Enchantments.DEPTH_STRIDER)) {
            return 8;
        } else if (enchantment.equals(Enchantments.UNBREAKING)) {
            return 34;
        } else if (enchantment.equals(Enchantments.SHARPNESS)) {
            return 16;
        } else if (enchantment.equals(Enchantments.SMITE)) {
            return 17;
        } else if (enchantment.equals(Enchantments.BANE_OF_ARTHROPODS)) {
            return 18;
        } else if (enchantment.equals(Enchantments.KNOCKBACK)) {
            return 19;
        } else if (enchantment.equals(Enchantments.FIRE_ASPECT)) {
            return 20;
        } else if (enchantment.equals(Enchantments.LOOTING)) {
            return 21;
        } else if (enchantment.equals(Enchantments.EFFICIENCY)) {
            return 32;
        } else if (enchantment.equals(Enchantments.SILK_TOUCH)) {
            return 33;
        } else if (enchantment.equals(Enchantments.FORTUNE)) {
            return 35;
        } else if (enchantment.equals(Enchantments.POWER)) {
            return 48;
        } else if (enchantment.equals(Enchantments.PUNCH)) {
            return 49;
        } else if (enchantment.equals(Enchantments.FLAME)) {
            return 50;
        } else if (enchantment.equals(Enchantments.INFINITY)) {
            return 51;
        } else if (enchantment.equals(Enchantments.LUCK_OF_THE_SEA)) {
            return 61;
        } else if (enchantment.equals(Enchantments.LURE)) {
            return 62;
        } else {
            return 0;
        }
    }

    @Override
    protected boolean supports(ItemStack entity) {
        return entity.getItem().equals(Items.enchanted_book);
    }

    @Override
    protected ListValue<ItemEnchantment> constructValue(List<ItemEnchantment> actualValue) {
        return SpongeValueFactory.getInstance().createListValue(Keys.STORED_ENCHANTMENTS, actualValue, Lists.newArrayList());
    }

    @Override
    protected boolean set(ItemStack entity, List<ItemEnchantment> value) {
        if (!entity.hasTagCompound()) {
            entity.setTagCompound(new NBTTagCompound());
        }
        NBTTagList list = new NBTTagList();
        for (ItemEnchantment enchantment : value) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setShort(NbtDataUtil.ITEM_ENCHANTMENT_ID, (short) getIdByEnchantment(enchantment.getEnchantment()));
            tag.setShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL, (short) enchantment.getLevel());
            list.appendTag(tag);
        }
        entity.getTagCompound().setTag(NbtDataUtil.ITEM_STORED_ENCHANTMENTS_LIST, list);
        return true;
    }

    @Override
    protected Optional<List<ItemEnchantment>> getVal(ItemStack entity) {
        if (!entity.hasTagCompound() || !entity.getTagCompound().hasKey(NbtDataUtil.ITEM_STORED_ENCHANTMENTS_LIST, NbtDataUtil.TAG_LIST)) {
            return Optional.empty();
        }
        List<ItemEnchantment> list = Lists.newArrayList();
        NBTTagList tags = entity.getTagCompound().getTagList(NbtDataUtil.ITEM_STORED_ENCHANTMENTS_LIST, NbtDataUtil.TAG_COMPOUND);
        for (int i = 0; i < tags.tagCount(); i++) {
            NBTTagCompound tag = tags.getCompoundTagAt(i);
            list.add(new ItemEnchantment(getEnchantmentById(tag.getShort(NbtDataUtil.ITEM_ENCHANTMENT_ID)),
                    tag.getShort(NbtDataUtil.ITEM_ENCHANTMENT_LEVEL)));
        }
        return Optional.of(list);
    }

    @Override
    protected ImmutableValue<List<ItemEnchantment>> constructImmutableValue(List<ItemEnchantment> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected StoredEnchantmentData createManipulator() {
        return new SpongeStoredEnchantmentData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            ItemStack stack = (ItemStack) container;
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NbtDataUtil.ITEM_STORED_ENCHANTMENTS_LIST, NbtDataUtil.TAG_COMPOUND)) {
                stack.getTagCompound().removeTag(NbtDataUtil.ITEM_STORED_ENCHANTMENTS_LIST);
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }
}
