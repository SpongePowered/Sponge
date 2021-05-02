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

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.entity.animal.TurtleAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.VecHelper;

import net.minecraft.world.entity.animal.Turtle;

public final class TurtleData {

    private TurtleData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Turtle.class)
                    .create(Keys.HAS_EGG)
                        .get(Turtle::hasEgg)
                        .set((h, v) -> ((TurtleAccessor) h).invoker$setHasEgg(v))
                    .create(Keys.HOME_POSITION)
                        .get(h -> VecHelper.toVector3i(((TurtleAccessor) h).invoker$getHomePos()))
                        .set((h, v) -> h.setHomePos(VecHelper.toBlockPos(v)))
                    .create(Keys.IS_LAYING_EGG)
                        .get(Turtle::isLayingEgg)
                        .set((h, v) -> ((TurtleAccessor) h).invoker$setLayingEgg(v))
                .asMutable(TurtleAccessor.class)
                    .create(Keys.IS_GOING_HOME)
                        .get(TurtleAccessor::invoker$isGoingHome)
                        .set(TurtleAccessor::invoker$setGoingHome)
                    .create(Keys.IS_TRAVELING)
                        .get(TurtleAccessor::invoker$isTravelling)
                        .set(TurtleAccessor::invoker$setTravelling)
                    .create(Keys.TARGET_POSITION)
                        .get(h -> VecHelper.toVector3i(h.invoker$getTravelPos()))
                        .set((h, v) -> h.invoker$setTravelPos(VecHelper.toBlockPos(v)));
    }
    // @formatter:on
}
