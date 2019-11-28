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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
        super(input -> input.getItem() == Items.SHIELD);
    }

    @Override
    public boolean doesDataExist(final ItemStack itemStack) {
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean set(final ItemStack itemStack, final Map<Key<?>, Object> keyValues) {
        if (itemStack.getTag() == null) {
            itemStack.setTag(new CompoundNBT());
        }
        final CompoundNBT blockEntity = itemStack.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        final DyeColor baseColor = (DyeColor) keyValues.get(Keys.BANNER_BASE_COLOR);
        final PatternListValue patternLayers = (PatternListValue) keyValues.get(Keys.BANNER_PATTERNS);
        if (!patternLayers.isEmpty()) {
            final ListNBT patterns = new ListNBT();

            for (final PatternLayer layer : patternLayers) {
                final CompoundNBT compound = new CompoundNBT();
                compound.putString(Constants.TileEntity.Banner.BANNER_PATTERN_ID, ((BannerPattern) (Object) layer.getShape()).getHashname());
                compound.putInt(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR, ((net.minecraft.item.DyeColor) (Object) layer.getColor()).getDyeDamage());
                patterns.appendTag(compound);
            }
            blockEntity.setTag(Constants.TileEntity.Banner.BANNER_PATTERNS, patterns);
        }
        blockEntity.putInt(Constants.TileEntity.Banner.BANNER_BASE, ((net.minecraft.item.DyeColor) (Object) baseColor).getDyeDamage());
        return true;
    }

    @Override
    public Map<Key<?>, ?> getValues(final ItemStack itemStack) {
        if (itemStack.hasTag() && itemStack.getTag().contains(Constants.Item.ITEM_UNBREAKABLE)) {
            return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getDamage(),
                    Keys.UNBREAKABLE, itemStack.getTag().getBoolean(Constants.Item.ITEM_UNBREAKABLE));
        }
        return ImmutableMap.of(Keys.ITEM_DURABILITY, itemStack.getMaxDamage() - itemStack.getDamage(), Keys.UNBREAKABLE, false);
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
