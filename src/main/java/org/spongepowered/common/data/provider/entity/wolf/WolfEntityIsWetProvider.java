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
package org.spongepowered.common.data.provider.entity.wolf;

import net.minecraft.entity.passive.WolfEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.OptBool;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.accessor.entity.passive.WolfEntityAccessor;

import java.util.Optional;

public class WolfEntityIsWetProvider extends GenericMutableDataProvider<WolfEntity, Boolean> {

    public WolfEntityIsWetProvider() {
        super(Keys.IS_WET);
    }

    @Override
    protected Optional<Boolean> getFrom(WolfEntity dataHolder) {
        final WolfEntityAccessor accessor = (WolfEntityAccessor) dataHolder;
        return OptBool.of(accessor.accessor$getIsWet() || accessor.accessor$getIsShaking());
    }

    @Override
    protected boolean set(WolfEntity dataHolder, Boolean value) {
        final WolfEntityAccessor accessor = (WolfEntityAccessor) dataHolder;
        accessor.accessor$setIsWet(value);
        accessor.accessor$setIsShaking(value);
        accessor.accessor$setTimeWolfIsShaking(0f);
        accessor.accessor$setPrevTimeWolfIsShaking(0f);
        return true;
    }
}
