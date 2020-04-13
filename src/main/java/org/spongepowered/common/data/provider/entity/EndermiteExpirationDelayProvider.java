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

import static com.google.common.base.Preconditions.checkArgument;

import net.minecraft.entity.monster.EndermiteEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.TemporalUnits;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.accessor.entity.monster.EndermiteEntityAccessor;

import java.time.Duration;
import java.util.Optional;

public class EndermiteExpirationDelayProvider extends GenericMutableDataProvider<EndermiteEntity, Integer> {

    public EndermiteExpirationDelayProvider() {
        super(Keys.DESPAWN_DELAY);
    }

    @Override
    protected Optional<Integer> getFrom(EndermiteEntity dataHolder) {
        if (dataHolder.isNoDespawnRequired()) {
            return Optional.empty();
        }
        int ticks = ((EndermiteEntityAccessor) dataHolder).accessor$getLifetime();
        return Optional.of(ticks);
    }

    @Override
    protected boolean set(EndermiteEntity dataHolder, Integer ticks) {
        if (dataHolder.isNoDespawnRequired()) {
            return false;
        }
        checkArgument(ticks >= 0);
        checkArgument(ticks <= 2400);
        ((EndermiteEntityAccessor) dataHolder).accessor$setLifetime(ticks);
        return true;
    }

}
