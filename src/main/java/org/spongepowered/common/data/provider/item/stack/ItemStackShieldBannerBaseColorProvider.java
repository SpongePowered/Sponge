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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class ItemStackShieldBannerBaseColorProvider extends GenericMutableDataProvider<ItemStack, DyeColor> {

    public ItemStackShieldBannerBaseColorProvider() {
        super(Keys.BANNER_BASE_COLOR);
    }

    @Override
    protected boolean supports(ItemStack dataHolder) {
        return dataHolder.getItem() == Items.SHIELD;
    }

    @Override
    protected Optional<DyeColor> getFrom(ItemStack dataHolder) {
        final CompoundNBT tag = dataHolder.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        if (tag == null || tag.contains(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_LIST)) {
            return Optional.of(DyeColors.WHITE);
        }
        final int id = tag.getInt(Constants.TileEntity.Banner.BANNER_BASE);
        return Optional.of((DyeColor) (Object) net.minecraft.item.DyeColor.byId(id));
    }

    @Override
    protected boolean set(ItemStack dataHolder, DyeColor value) {
        final CompoundNBT tag = dataHolder.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        tag.putInt(Constants.TileEntity.Banner.BANNER_BASE, ((net.minecraft.item.DyeColor) (Object) value).getId());
        return true;
    }
}
