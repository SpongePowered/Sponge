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
package org.spongepowered.common.data.processor.value.tileentity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.Value.Immutable;
import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.bridge.tileentity.BannerTileEntityBridge;
import org.spongepowered.common.util.Constants;

import java.util.Optional;
import net.minecraft.tileentity.BannerTileEntity;

public class TileBannerBaseColorValueProcessor extends AbstractSpongeValueProcessor<BannerTileEntity, DyeColor, Mutable<DyeColor>> {

    public TileBannerBaseColorValueProcessor() {
        super(BannerTileEntity.class, Keys.BANNER_BASE_COLOR);
    }

    @Override
    protected Mutable<DyeColor> constructValue(DyeColor actualValue) {
        return new SpongeValue<>(Keys.BANNER_BASE_COLOR, Constants.Catalog.DEFAULT_BANNER_BASE, actualValue);
    }

    @Override
    protected boolean set(BannerTileEntity container, DyeColor value) {
        if (!container.getWorld().isRemote) {
            ((BannerTileEntityBridge) container).bridge$setBaseColor(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<DyeColor> getVal(BannerTileEntity container) {
        return Optional.of(((BannerTileEntityBridge) container).bridge$getBaseColor());
    }

    @Override
    protected Immutable<DyeColor> constructImmutableValue(DyeColor value) {
        return ImmutableSpongeValue.cachedOf(Keys.BANNER_BASE_COLOR, Constants.Catalog.DEFAULT_BANNER_BASE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
