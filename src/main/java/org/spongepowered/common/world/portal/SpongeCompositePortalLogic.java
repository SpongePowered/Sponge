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
package org.spongepowered.common.world.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is a custom portal type.
 */
public final class SpongeCompositePortalLogic implements net.minecraft.world.level.block.Portal, PortalLogic {

    private final List<net.minecraft.world.level.block.Portal> rules;

    public SpongeCompositePortalLogic(final List<net.minecraft.world.level.block.Portal> rules) {
        this.rules = rules;
    }

    @Nullable @Override
    public DimensionTransition getPortalDestination(final ServerLevel sourceLevel, final Entity entity, final BlockPos portalPos) {
        return this.rules.stream()
                .map(p -> p.getPortalDestination(sourceLevel, entity, portalPos))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    @Override
    public Optional<PortalExitCalculator> exitCalculator() {
        return Optional.of((from, fromPos, entity) -> this.rules.stream().map(PortalLogic.class::cast)
                .map(PortalLogic::exitCalculator).flatMap(Optional::stream)
                .flatMap(c -> c.calculatePortalExit(from, fromPos, entity).stream())
                .findFirst());
    }

    @Override
    public Optional<PortalFinder> finder() {
        return Optional.of((at, range) -> this.rules.stream().map(PortalLogic.class::cast)
                .map(PortalLogic::finder).flatMap(Optional::stream)
                .flatMap(c -> c.findPortal(at, range).stream())
                .findFirst());
    }

    @Override
    public Optional<PortalGenerator> generator() {
        return Optional.of((at, axis) -> this.rules.stream().map(PortalLogic.class::cast)
                .map(PortalLogic::generator).flatMap(Optional::stream)
                .flatMap(c -> c.generatePortal(at, axis).stream())
                .findFirst());
    }

    @Override
    public boolean teleport(final org.spongepowered.api.entity.Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        final var searchRange = 1;
        final var axis = Axis.X;

        var foundPortal = this.finder().flatMap(finder -> finder.findPortal(destination, searchRange));
        if (foundPortal.isPresent()) {
            return foundPortal.map(Portal::position).map(entity::setLocation).orElse(false);
        }
        if (generateDestinationPortal) {
            var generatedPortal = this.generator().flatMap(generator -> generator.generatePortal(destination, axis));
            return generatedPortal.map(Portal::position).map(entity::setLocation).orElse(false);
        }
        return false;
    }
}
