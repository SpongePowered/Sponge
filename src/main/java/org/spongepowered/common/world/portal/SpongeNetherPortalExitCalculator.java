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

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;


public final class SpongeNetherPortalExitCalculator implements PortalLogic.PortalExitCalculator {

    private final ResourceKey<Level> origin;
    private final ResourceKey<Level> target;
    @Nullable private final Double scale;

    public SpongeNetherPortalExitCalculator(final org.spongepowered.api.ResourceKey origin, final org.spongepowered.api.ResourceKey target, @Nullable final Double scale) {
        this.origin = SpongeWorldManager.createRegistryKey(origin);
        this.target = SpongeWorldManager.createRegistryKey(target);
        this.scale = scale;
    }

    @Override
    public Optional<ServerLocation> calculatePortalExit(final ServerWorld from, final Vector3i fromPos, final org.spongepowered.api.entity.Entity entity) {
        final var fromLevel = (ServerLevel) from.world();
        if (!fromLevel.dimension().equals(this.origin)) {
            return Optional.empty(); // configured Portals go only in one direction
        }
        final var toLevel = fromLevel.getServer().getLevel(this.target);
        if (toLevel == null) {
            return Optional.empty();
        }
        final var scale = this.calculateScale(fromLevel, toLevel);
        final var exitPosition = toLevel.getWorldBorder().clampToBounds(fromPos.x() * scale, fromPos.y(), fromPos.z() * scale);
        return Optional.of(ServerLocation.of((ServerWorld) toLevel, VecHelper.toVector3i(exitPosition)));
    }

    private double calculateScale(final ServerLevel fromLevel, final ServerLevel toLevel) {
        if (this.scale != null) {
            return this.scale;
        }
        return DimensionType.getTeleportationScale(fromLevel.dimensionType(), toLevel.dimensionType());
    }
}
