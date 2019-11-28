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

import net.minecraft.tileentity.TileEntityBanner;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.bridge.tileentity.TileEntityBannerBridge;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class TileBannerBaseColorValueProcessor extends AbstractSpongeValueProcessor<TileEntityBanner, DyeColor, Value<DyeColor>> {

    public TileBannerBaseColorValueProcessor() {
        super(TileEntityBanner.class, Keys.BANNER_BASE_COLOR);
    }

    @Override
    protected Value<DyeColor> constructValue(DyeColor actualValue) {
        return new SpongeValue<>(Keys.BANNER_BASE_COLOR, Constants.Catalog.DEFAULT_BANNER_BASE, actualValue);
    }

    @Override
    protected boolean set(TileEntityBanner container, DyeColor value) {
        if (!container.func_145831_w().field_72995_K) {
            ((TileEntityBannerBridge) container).bridge$setBaseColor(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<DyeColor> getVal(TileEntityBanner container) {
        return Optional.of(((TileEntityBannerBridge) container).bridge$getBaseColor());
    }

    @Override
    protected ImmutableValue<DyeColor> constructImmutableValue(DyeColor value) {
        return ImmutableSpongeValue.cachedOf(Keys.BANNER_BASE_COLOR, Constants.Catalog.DEFAULT_BANNER_BASE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
