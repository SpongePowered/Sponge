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
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongePatternListValue;
import org.spongepowered.common.data.value.mutable.SpongePatternListValue;
import org.spongepowered.common.bridge.tileentity.TileEntityBannerBridge;

import java.util.List;
import java.util.Optional;

public class TileBannerPatternLayersValueProcessor extends AbstractSpongeValueProcessor<TileEntityBanner, List<PatternLayer>, PatternListValue> {

    public TileBannerPatternLayersValueProcessor() {
        super(TileEntityBanner.class, Keys.BANNER_PATTERNS);
    }

    @Override
    protected PatternListValue constructValue(List<PatternLayer> actualValue) {
        return new SpongePatternListValue(Keys.BANNER_PATTERNS, actualValue);
    }

    @Override
    protected boolean set(TileEntityBanner container, List<PatternLayer> value) {
        if (!container.func_145831_w().field_72995_K) { // This avoids a client crash because clientside.
            ((TileEntityBannerBridge) container).bridge$setLayers(value);
            return true;
        }
        return false;
    }

    @Override
    protected Optional<List<PatternLayer>> getVal(TileEntityBanner container) {
        return Optional.of(((TileEntityBannerBridge) container).bridge$getLayers());
    }

    @Override
    protected ImmutableValue<List<PatternLayer>> constructImmutableValue(List<PatternLayer> value) {
        return new ImmutableSpongePatternListValue(Keys.BANNER_PATTERNS, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
