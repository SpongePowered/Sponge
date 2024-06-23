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
package org.spongepowered.common.effect;

import org.spongepowered.api.effect.ForwardingViewer;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class SpongeCustomForwardingViewer implements SpongeForwardingViewer {

    private final Supplier<? extends Iterable<? extends Viewer>> viewersSupplier;

    private SpongeCustomForwardingViewer(final Supplier<? extends Iterable<? extends Viewer>> viewersSupplier) {
        this.viewersSupplier = viewersSupplier;
    }

    @Override
    public Iterable<? extends Viewer> audiences() {
        return this.viewersSupplier.get();
    }

    public static final class FactoryImpl implements ForwardingViewer.Factory {

        public FactoryImpl() {
        }

        @Override
        public ForwardingViewer of(final Supplier<? extends Iterable<? extends Viewer>> viewersSupplier) {
            Objects.requireNonNull(viewersSupplier, "viewersSupplier");
            return new SpongeCustomForwardingViewer(viewersSupplier);
        }

        @Override
        public ForwardingViewer of(final Collection<? extends Viewer> viewers) {
            Objects.requireNonNull(viewers, "viewers");
            final List<Viewer> list = List.copyOf(viewers);
            return new SpongeCustomForwardingViewer(() -> list);
        }

        @Override
        public ForwardingViewer of(final Viewer... viewers) {
            Objects.requireNonNull(viewers, "viewers");
            final List<Viewer> list = List.of(viewers);
            return new SpongeCustomForwardingViewer(() -> list);
        }

        @Override
        public ForwardingViewer allAround(final World<?, ?> world, final Vector3i position, final int radius) {
            Objects.requireNonNull(world, "world");
            Objects.requireNonNull(position, "position");
            if (radius <= 0) {
                throw new IllegalArgumentException("The radius has to be greater then zero!");
            }
            return new SpongeCustomForwardingViewer(() -> listAllAround(world, position, radius));
        }

        @Override
        public ForwardingViewer allAround(final Entity entity, final int radius) {
            Objects.requireNonNull(entity, "entity");
            if (radius <= 0) {
                throw new IllegalArgumentException("The radius has to be greater then zero!");
            }
            return new SpongeCustomForwardingViewer(() -> entity.isRemoved()
                    ? List.of()
                    : listAllAround(entity.world(), entity.blockPosition(), radius)
            );
        }

        private static List<? extends Player> listAllAround(final World<?, ?> world, final Vector3i position, final int radius) {
            final int radiusSquared = radius * radius;
            return world.players().stream()
                    .filter(player -> player.blockPosition().distanceSquared(position) <= radiusSquared)
                    .toList();
        }
    }
}
