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

import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.common.data.provider.item.ItemStackDataProvider;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class ItemStackShieldBannerPatternsProvider extends ItemStackDataProvider<List<BannerPatternLayer>> {

    private static final Map<String, BannerPatternShape> SHAPE_BY_HASHNAME = new HashMap<>();

    static {
        for (final BannerPattern pattern : BannerPattern.values()) {
            SHAPE_BY_HASHNAME.put(pattern.getHashname(), (BannerPatternShape) (Object) pattern);
        }
    }

    public ItemStackShieldBannerPatternsProvider() {
        super(Keys.BANNER_PATTERN_LAYERS);
    }

    @Override
    protected boolean supports(Item item) {
        return item == Items.SHIELD;
    }

    @Override
    protected Optional<List<BannerPatternLayer>> getFrom(ItemStack dataHolder) {
        final CompoundNBT tag = dataHolder.getTag();
        if (tag == null || tag.contains(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_LIST)) {
            return Optional.of(new ArrayList<>());
        }
        final ListNBT layersList = tag.getList(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_COMPOUND);
        return Optional.of(layersList.stream()
                .map(layer -> layerFromNbt((CompoundNBT) layer))
                .collect(Collectors.toList()));
    }

    @Override
    protected boolean set(ItemStack dataHolder, List<BannerPatternLayer> value) {
        final ListNBT layersTag = value.stream()
                .map(ItemStackShieldBannerPatternsProvider::layerToNbt)
                .collect(NbtCollectors.toTagList());

        final CompoundNBT blockEntity = dataHolder.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
        blockEntity.put(Constants.TileEntity.Banner.BANNER_PATTERNS, layersTag);
        return true;
    }

    private static BannerPatternLayer layerFromNbt(CompoundNBT layerCompound) {
        final BannerPatternShape shape = SHAPE_BY_HASHNAME.get(
                layerCompound.getString(Constants.TileEntity.Banner.BANNER_PATTERN_ID));
        final DyeColor dyeColor = DyeColor.byId(
                layerCompound.getInt(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR));
        return BannerPatternLayer.of(shape, (org.spongepowered.api.data.type.DyeColor) (Object) dyeColor);
    }

    private static CompoundNBT layerToNbt(BannerPatternLayer layer) {
        final CompoundNBT layerCompound = new CompoundNBT();
        layerCompound.putString(Constants.TileEntity.Banner.BANNER_PATTERN_ID,
                ((BannerPattern) (Object) layer.getShape()).getHashname());
        layerCompound.putInt(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR,
                ((DyeColor) (Object) layer.getColor()).getId());
        return layerCompound;
    }
}
