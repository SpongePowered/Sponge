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

import com.google.common.collect.ImmutableMap;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.processor.common.AbstractItemDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.Map;
import java.util.Optional;

public class ShieldBannerDataProcessor extends AbstractItemDataProcessor<BannerData, ImmutableBannerData> {

    public ShieldBannerDataProcessor() {
        super(input -> input.getItem() == Items.SHIELD);
    }

    @Override
    public boolean doesDataExist(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean set(ItemStack itemStack, Map<Key<?>, Object> keyValues) {
        if (itemStack.getTagCompound() == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        final NBTTagCompound tagCompound = ItemStackUtil.getTagCompound(itemStack);
        final NBTTagCompound blockEntity = new NBTTagCompound();
        final DyeColor baseColor = (DyeColor) keyValues.get(Keys.BANNER_BASE_COLOR);
        final PatternListValue patternLayers = (PatternListValue) keyValues.get(Keys.BANNER_PATTERNS);
        if (!patternLayers.isEmpty()) {
            final NBTTagList patterns = new NBTTagList();

            for (PatternLayer layer : patternLayers) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setString(NbtDataUtil.BANNER_PATTERN_ID, ((BannerPattern) (Object) layer.getShape()).getHashname());
                compound.setInteger(NbtDataUtil.BANNER_PATTERN_COLOR, ((EnumDyeColor) (Object) layer.getColor()).getDyeDamage());
                patterns.appendTag(compound);
            }
            blockEntity.setTag(NbtDataUtil.BANNER_PATTERNS, patterns);
        }
        blockEntity.setInteger(NbtDataUtil.BANNER_BASE, ((EnumDyeColor) (Object) baseColor).getDyeDamage());
        tagCompound.setTag(NbtDataUtil.BLOCK_ENTITY_TAG, blockEntity);
        return true;
    }

    @Override
    public Map<Key<?>, ?> getValues(ItemStack itemStack) {
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey(NbtDataUtil.ITEM_UNBREAKABLE)) {
            return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getItemDamage(),
                    Keys.UNBREAKABLE, itemStack.getTagCompound().getBoolean(NbtDataUtil.ITEM_UNBREAKABLE));
        }
        return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getItemDamage(), Keys.UNBREAKABLE, false);
    }

    @Override
    public BannerData createManipulator() {
        return new SpongeBannerData();
    }

    @Override
    public Optional<BannerData> fill(DataContainer container, BannerData durabilityData) {
        final Optional<Integer> durability = container.getInt(Keys.ITEM_DURABILITY.getQuery());
        final Optional<Boolean> unbreakable = container.getBoolean(Keys.UNBREAKABLE.getQuery());
        if (durability.isPresent() && unbreakable.isPresent()) {
            durabilityData.set(Keys.ITEM_DURABILITY, durability.get());
            durabilityData.set(Keys.UNBREAKABLE, unbreakable.get());
            return Optional.of(durabilityData);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
