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

import net.minecraft.world.level.block.Blocks;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class SpongePortalLogicFactory implements PortalLogic.Factory {

    public static final PortalLogic.PortalFinder NO_OP_PORTALFINDER = (at, range) -> Optional.of(new SpongePortal(at));

    @Override
    public PortalLogic endPortal() {
        return (PortalLogic) Blocks.END_PORTAL;
    }

    @Override
    public PortalLogic endGateway() {
        return (PortalLogic) Blocks.END_GATEWAY;
    }

    @Override
    public PortalLogic netherPortal() {
        return (PortalLogic) Blocks.NETHER_PORTAL;
    }

    @Override
    public PortalLogic.PortalExitCalculator netherPortalExitCalculator(final ResourceKey origin, final ResourceKey target) {
        return new SpongeNetherPortalExitCalculator(origin, target, null);
    }

    @Override
    public PortalLogic.PortalExitCalculator netherPortalExitCalculator(final ResourceKey origin, final ResourceKey target, final double scale) {
        return new SpongeNetherPortalExitCalculator(origin, target, scale);
    }

    @Override
    public PortalLogic.PortalExitCalculator targetCalculator(final ResourceKey origin, final ResourceKey target, final Vector3d targetPos) {
        return new SpongeTargetPortalFinder(origin, target, targetPos);
    }

    @Override
    public PortalLogic.PortalExitCalculator spawnCalculator(final ResourceKey origin, final ResourceKey target) {
        return new SpongeSpawnPortalFinder(origin, target);
    }

    @Override
    public PortalLogic.PortalFinder netherPortalFinder() {
        return this.netherPortal().finder().get();
    }

    @Override
    public PortalLogic.PortalGenerator netherPortalGenerator() {
        return this.netherPortal().generator().get();
    }

    @Override
    public PortalLogic.PortalGenerator endPlatformGenerator() {
        return SpongeEndPlatformGenerator.INSTANCE;
    }

    @Override
    public PortalLogic.PortalFinder noOpFinder() {
        return NO_OP_PORTALFINDER;
    }

    @Override
    public Portal portalOf(final PortalLogic logic, final ServerLocation position) {
        return new SpongePortal(position, logic);
    }

    @Override
    public Portal portalOf(final PortalLogic logic, final ServerLocation position, final AABB aabb) {
        return new SpongePortal(position, logic, aabb);
    }


}
