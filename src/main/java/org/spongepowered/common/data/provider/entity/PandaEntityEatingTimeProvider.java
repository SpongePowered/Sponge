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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.passive.PandaEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.entity.passive.PandaEntityAccessor;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public final class PandaEntityEatingTimeProvider extends GenericMutableDataProvider<PandaEntity, Integer> {

    public PandaEntityEatingTimeProvider() {
        super(Keys.EATING_TIME);
    }

    @Override
    protected final Optional<Integer> getFrom(final PandaEntity dataHolder) {
        return Optional.of(((PandaEntityAccessor) dataHolder).accessor$getEatingTime());
    }

    @Override
    protected final boolean set(final PandaEntity dataHolder, final Integer value) {
        ((PandaEntityAccessor) dataHolder).accessor$setEatingTime(value);
        return true;
    }
}
