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
package org.spongepowered.common.data.provider.block.state;

import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.level.block.CropBlockAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.BoundedUtil;
import org.spongepowered.common.util.DataUtil;

import java.util.function.Function;

public final class GrowthData {

    private GrowthData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        register(registrator, BambooStalkBlock.class, state -> BambooStalkBlock.AGE);
        register(registrator, CactusBlock.class, state -> CactusBlock.AGE);
        register(registrator, CocoaBlock.class, state -> CocoaBlock.AGE);
        register(registrator, CropBlock.class, state -> ((CropBlockAccessor) state.getBlock()).invoker$getAgeProperty());
        register(registrator, NetherWartBlock.class, state -> NetherWartBlock.AGE);
        register(registrator, SaplingBlock.class, state -> SaplingBlock.STAGE);
        register(registrator, StemBlock.class, state -> StemBlock.AGE);
        register(registrator, SugarCaneBlock.class, state -> SugarCaneBlock.AGE);
    }

    private static void register(
            final DataProviderRegistrator registrator, final Class<?> cropClass, final Function<BlockState, IntegerProperty> propertyFunction
    ) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.GROWTH_STAGE)
                        .constructValue((h, v) -> BoundedUtil.constructImmutableValueInteger(v, Keys.GROWTH_STAGE, propertyFunction.apply(h)))
                        .get(h -> h.getValue(propertyFunction.apply(h)))
                        .set((h, v) -> BoundedUtil.setInteger(h, v, propertyFunction.apply(h)))
                        .supports(h -> cropClass.isInstance(h.getBlock()))
                    .create(Keys.MAX_GROWTH_STAGE)
                        .get(h -> DataUtil.maxi(propertyFunction.apply(h)))
                        .supports(h -> cropClass.isInstance(h.getBlock()));
    }
    // @formatter:on
}
