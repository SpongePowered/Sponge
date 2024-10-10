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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

/**
 * This is a custom portal type.
 */
public final class SpongeCustomPortalLogic implements net.minecraft.world.level.block.Portal, PortalLogic {

    private final PortalLogic.PortalExitCalculator exitCalculator;
    private final PortalLogic.PortalFinder finder;
    private final PortalLogic.PortalGenerator generator;

    private int searchRange = 16;
    private Axis axis = Axis.X;

    public SpongeCustomPortalLogic(final PortalLogic.PortalExitCalculator calulator,
            final PortalLogic.PortalFinder finder,
            final PortalLogic.PortalGenerator generator) {

        this.exitCalculator = calulator;
        this.finder = finder;
        this.generator = generator;
    }

    @Nullable @Override
    public DimensionTransition getPortalDestination(final ServerLevel fromLevel, final Entity entity, final BlockPos fromPos) {
        final var spongeEntity = (org.spongepowered.api.entity.Entity) entity;
        // Calculate desired portal location
        // Then find existing portal or generate if not found
        return this.exitCalculator.calculatePortalExit((ServerWorld) fromLevel, VecHelper.toVector3i(fromPos), spongeEntity)
                .flatMap(calcExit -> this.finder.findPortal(calcExit, this.searchRange).map(Portal::position).or(() -> this.generator.generatePortal(calcExit, this.axis).map(Portal::position))
                        .map(realExit -> SpongeCustomPortalLogic.generateTransition(entity, realExit))
                ).orElse(null);
    }

    private static DimensionTransition generateTransition(final Entity entity, final ServerLocation finalExit) {
        return new DimensionTransition(
                (ServerLevel) finalExit.world(),
                VecHelper.toVanillaVector3d(finalExit.position()),
                entity.getDeltaMovement(),
                entity.getYRot(),
                entity.getXRot(),
                DimensionTransition.PLACE_PORTAL_TICKET);
    }

    @Override
    public Optional<PortalExitCalculator> exitCalculator() {
        return Optional.of(this.exitCalculator);
    }

    @Override
    public Optional<PortalFinder> finder() {
        return Optional.of(this.finder);
    }

    @Override
    public Optional<PortalGenerator> generator() {
        return Optional.of(this.generator);
    }

    @Override
    public boolean teleport(final org.spongepowered.api.entity.Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        final var foundPortal = this.finder.findPortal(destination, this.searchRange);
        if (foundPortal.isPresent()) {
            return foundPortal.map(Portal::position).map(entity::setLocation).orElse(false);
        }
        if (generateDestinationPortal) {
            var generatedPortal = this.generator.generatePortal(destination, this.axis);
            return generatedPortal.map(Portal::position).map(entity::setLocation).orElse(false);
        }
        return false;
    }
}
