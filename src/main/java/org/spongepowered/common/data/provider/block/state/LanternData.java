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

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.level.block.Lantern;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.bridge.data.DataContainerHolder.Immutable;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class LanternData {

    private LanternData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
            .asImmutable(BlockState.class)
                .create(Keys.HANGING)
                    .get(h -> h.getValue(Lantern.HANGING))
                    .set((h, v) -> h.setValue(Lantern.HANGING, v))
                    .supports(h -> h.getBlock() instanceof Lantern)
                .create(Keys.IS_ATTACHED)
                    .get(h -> h.getValue(Lantern.HANGING))
                    .set((h, v) -> h.setValue(Lantern.HANGING, v))
                    .supports(h -> h.getBlock() instanceof Lantern)
                .create(Keys.CONNECTED_DIRECTIONS)
                    .get(h -> h.getValue(Lantern.HANGING) ? ImmutableSet.of(Direction.UP) : ImmutableSet.of(Direction.DOWN))
                    .set((h, v) -> {
                        if (v.size() == 1) {
                            if (v.contains(Direction.UP)) {
                                return h.setValue(Lantern.HANGING, true);
                            } else if (v.contains(Direction.DOWN)) {
                                return h.setValue(Lantern.HANGING, false);
                            }
                        }
                        return null;
                    })
                    .supports(h -> h.getBlock() instanceof Lantern)
                .create(Keys.IS_CONNECTED_UP)
                    .get(h -> h.getValue(Lantern.HANGING))
                    .set((h, v) -> h.setValue(Lantern.HANGING, v))
                    .supports(h -> h.getBlock() instanceof Lantern);
    }
    // @formatter:on
}
