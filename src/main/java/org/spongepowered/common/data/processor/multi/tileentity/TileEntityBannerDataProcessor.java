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
package org.spongepowered.common.data.processor.multi.tileentity;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.processor.common.AbstractTileEntityDataProcessor;
import org.spongepowered.common.bridge.tileentity.TileEntityBannerBridge;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.tileentity.BannerTileEntity;

public class TileEntityBannerDataProcessor extends AbstractTileEntityDataProcessor<BannerTileEntity, BannerData, ImmutableBannerData> {

    public TileEntityBannerDataProcessor() {
        super(BannerTileEntity.class);
    }

    @Override
    protected boolean doesDataExist(BannerTileEntity entity) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected boolean set(BannerTileEntity entity, Map<Key<?>, Object> keyValues) {
        if (!entity.getWorld().isRemote) {
            List<PatternLayer> layers = (List<PatternLayer>) keyValues.get(Keys.BANNER_PATTERNS);
            DyeColor baseColor = (DyeColor) keyValues.get(Keys.BANNER_BASE_COLOR);
            ((TileEntityBannerBridge) entity).bridge$setLayers(layers);
            ((TileEntityBannerBridge) entity).bridge$setBaseColor(baseColor);
            return true;
        }
        return false;
    }

    @Override
    protected Map<Key<?>, ?> getValues(BannerTileEntity entity) {
        List<PatternLayer> layers = ((TileEntityBannerBridge) entity).bridge$getLayers();
        DyeColor color = ((TileEntityBannerBridge) entity).bridge$getBaseColor();
        return ImmutableMap.of(Keys.BANNER_BASE_COLOR, color, Keys.BANNER_PATTERNS, layers);
    }

    @Override
    protected BannerData createManipulator() {
        return new SpongeBannerData();
    }

    @Override
    public Optional<BannerData> fill(DataContainer container, BannerData bannerData) {
        if (container.contains(Keys.BANNER_PATTERNS.getQuery()) || container.contains(Keys.BANNER_BASE_COLOR.getQuery())) {
            List<PatternLayer> layers = container.getSerializableList(Keys.BANNER_PATTERNS.getQuery(), PatternLayer.class).get();
            String colorId = container.getString(Keys.BANNER_BASE_COLOR.getQuery()).get();
            DyeColor color = Sponge.getRegistry().getType(DyeColor.class, colorId).get();
            bannerData.set(Keys.BANNER_BASE_COLOR, color);
            bannerData.set(Keys.BANNER_PATTERNS, layers);
            return Optional.of(bannerData);
        }
        return Optional.empty();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
