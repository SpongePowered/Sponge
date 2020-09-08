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

import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.util.NbtCollectors;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class ShieldItemStackData {

    private static final Map<String, BannerPatternShape> SHAPE_BY_HASHNAME = new HashMap<>();

    static {
        for (final BannerPattern pattern : BannerPattern.values()) {
            SHAPE_BY_HASHNAME.put(pattern.getHashname(), (BannerPatternShape) (Object) pattern);
        }
    }

    private ShieldItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.DYE_COLOR)
                        .get(h -> {
                            final CompoundNBT tag = h.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
                            if (tag == null || tag.contains(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_LIST)) {
                                return DyeColors.WHITE.get();
                            }
                            final int id = tag.getInt(Constants.TileEntity.Banner.BANNER_BASE);
                            return (DyeColor) (Object) net.minecraft.item.DyeColor.byId(id);
                        })
                        .set((h, v) -> {
                            final CompoundNBT tag = h.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
                            tag.putInt(Constants.TileEntity.Banner.BANNER_BASE, ((net.minecraft.item.DyeColor) (Object) v).getId());
                        })
                        .supports(h -> h.getItem() instanceof ShieldItem || h.getItem() instanceof BannerItem)
                    .create(Keys.BANNER_PATTERN_LAYERS)
                        .get(h -> {
                            final CompoundNBT tag = h.getChildTag(Constants.Item.BLOCK_ENTITY_TAG);
                            if (tag == null || !tag.contains(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_LIST)) {
                                return new ArrayList<>();
                            }
                            final ListNBT layersList = tag.getList(Constants.TileEntity.Banner.BANNER_PATTERNS, Constants.NBT.TAG_COMPOUND);
                            return layersList.stream()
                                    .map(layer -> layerFromNbt((CompoundNBT) layer))
                                    .collect(Collectors.toList());
                        })
                        .set((h, v) -> {
                            final ListNBT layersTag = v.stream()
                                    .filter(layer -> layer.getShape() != BannerPatternShapes.BASE.get())
                                    .map(ShieldItemStackData::layerToNbt)
                                    .collect(NbtCollectors.toTagList());
                            final CompoundNBT blockEntity = h.getOrCreateChildTag(Constants.Item.BLOCK_ENTITY_TAG);
                            blockEntity.put(Constants.TileEntity.Banner.BANNER_PATTERNS, layersTag);
                            if (h.getItem() instanceof ShieldItem) {
                                // TODO reject BannerPatternShapes.BASE for BannerItem?
                                v.stream().filter(layer -> layer.getShape() == BannerPatternShapes.BASE.get()).forEach(layer -> {
                                    blockEntity.putInt(Constants.TileEntity.Banner.BANNER_BASE, ((net.minecraft.item.DyeColor) (Object) layer.getColor()).getId());
                                });
                            }
                        })
                        .supports(h -> h.getItem() instanceof ShieldItem || h.getItem() instanceof BannerItem);
    }
    // @formatter:on

    public static BannerPatternLayer layerFromNbt(final CompoundNBT nbt) {
        final BannerPatternShape shape = SHAPE_BY_HASHNAME.get(nbt.getString(Constants.TileEntity.Banner.BANNER_PATTERN_ID));
        final net.minecraft.item.DyeColor dyeColor = net.minecraft.item.DyeColor.byId(nbt.getInt(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR));
        return BannerPatternLayer.of(shape, (org.spongepowered.api.data.type.DyeColor) (Object) dyeColor);
    }

    public static CompoundNBT layerToNbt(final BannerPatternLayer layer) {
        final CompoundNBT nbt = new CompoundNBT();
        nbt.putString(Constants.TileEntity.Banner.BANNER_PATTERN_ID, ((BannerPattern) (Object) layer.getShape()).getHashname());
        nbt.putInt(Constants.TileEntity.Banner.BANNER_PATTERN_COLOR, ((net.minecraft.item.DyeColor) (Object) layer.getColor()).getId());
        return nbt;
    }
}
