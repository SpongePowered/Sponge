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
package org.spongepowered.common.data.provider.block.entity;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.tileentity.AbstractFurnaceTileEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class AbstractFurnaceData {

    private AbstractFurnaceData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AbstractFurnaceTileEntityAccessor.class)
                    .create(Keys.FUEL)
                        .get(AbstractFurnaceTileEntityAccessor::accessor$getBurnTime)
                        .setAnd((h, v) -> {
                            if (v < 0) {
                                return false;
                            }
                            h.accessor$setBurnTime(v);
                            return true;
                        })
                    .create(Keys.MAX_BURN_TIME)
                        .get(AbstractFurnaceTileEntityAccessor::accessor$getRecipesUsed)
                        .set(AbstractFurnaceTileEntityAccessor::accessor$setRecipesUsed)
                    .create(Keys.MAX_COOK_TIME)
                        .get(AbstractFurnaceTileEntityAccessor::accessor$getCookTimeTotal)
                        .set(AbstractFurnaceTileEntityAccessor::accessor$setCookTimeTotal)
                    .create(Keys.PASSED_COOK_TIME)
                        .get(AbstractFurnaceTileEntityAccessor::accessor$getCookTime)
                        .set((h, v) -> {
                            if (v < h.accessor$getCookTimeTotal()) {
                                h.accessor$setCookTime(v);
                            }
                        });
    }
    // @formatter:on
}
