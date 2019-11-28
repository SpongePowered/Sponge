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
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Optional;

public class ShieldBannerDataProcessor extends AbstractItemDataProcessor<BannerData, ImmutableBannerData> {

    public ShieldBannerDataProcessor() {
        super(input -> input.func_77973_b() == Items.field_185159_cQ);
    }

    @Override
    public boolean doesDataExist(final ItemStack itemStack) {
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean set(final ItemStack itemStack, final Map<Key<?>, Object> keyValues) {
        if (itemStack.func_77978_p() == null) {
            itemStack.func_77982_d(new NBTTagCompound());
        }
        final NBTTagCompound blockEntity = itemStack.func_190925_c(Constants.Item.BLOCK_ENTITY_TAG);
        final DyeColor baseColor = (DyeColor) keyValues.get(Keys.BANNER_BASE_COLOR);
        final PatternListValue patternLayers = (PatternListValue) keyValues.get(Keys.BANNER_PATTERNS);
        if (!patternLayers.isEmpty()) {
            final NBTTagList patterns = new NBTTagList();

            for (final PatternLayer layer : patternLayers) {
                final NBTTagCompound compound = new NBTTagCompound();
                compound.func_74778_a(Constants.TileEntity.Banner.BANNER_PATTERN_ID, ((BannerPattern) (Object) layer.getShape()).func_190993_b());
                compound.func_74768_a(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR, ((EnumDyeColor) (Object) layer.getColor()).func_176767_b());
                patterns.func_74742_a(compound);
            }
            blockEntity.func_74782_a(Constants.TileEntity.Banner.BANNER_PATTERNS, patterns);
        }
        blockEntity.func_74768_a(Constants.TileEntity.Banner.BANNER_BASE, ((EnumDyeColor) (Object) baseColor).func_176767_b());
        return true;
    }

    @Override
    public Map<Key<?>, ?> getValues(final ItemStack itemStack) {
        if (itemStack.func_77942_o() && itemStack.func_77978_p().func_74764_b(Constants.Item.ITEM_UNBREAKABLE)) {
            return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.func_77958_k() - itemStack.func_77952_i(),
                    Keys.UNBREAKABLE, itemStack.func_77978_p().func_74767_n(Constants.Item.ITEM_UNBREAKABLE));
        }
        return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.func_77958_k() - itemStack.func_77952_i(), Keys.UNBREAKABLE, false);
    }

    @Override
    public BannerData createManipulator() {
        return new SpongeBannerData();
    }

    @Override
    public Optional<BannerData> fill(final DataContainer container, final BannerData durabilityData) {
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
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
