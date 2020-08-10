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

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalType;
import org.spongepowered.common.bridge.world.PlatformITeleporterBridge;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

public final class WrappedITeleporterPortalType implements PortalType {

    private final ResourceKey key = ResourceKey.sponge("unknown");
    private final PlatformITeleporterBridge teleporter;
    private final PortalType logicType;

    public WrappedITeleporterPortalType(final PlatformITeleporterBridge teleporter, @Nullable final PortalType logicType) {
        this.teleporter = teleporter;
        this.logicType = logicType;
    }

    @Override
    public ResourceKey getKey() {
        return this.key;
    }

    @Override
    public void generatePortal(ServerLocation location) {
        if (this.logicType != null) {
            this.logicType.generatePortal(location);
        } else if (this.teleporter instanceof Teleporter) {
            final Teleporter mTeleporter = (Teleporter) this.teleporter;


        }
    }

    @Override
    public Optional<Portal> findPortal(ServerLocation location) {
        if (this.logicType != null) {
            return this.logicType.findPortal(location);
        }

        // TODO If the teleporter is a subclass of the Vanilla implementation, we can possibly find it
        return Optional.empty();
    }

    @Override
    public boolean teleport(Entity entity, ServerLocation destination,  boolean generateDestinationPortal) {
        final ServerLocation currentLocation = entity.getServerLocation();

        net.minecraft.entity.Entity result;

        if (this.logicType != null) {
            final Function<Boolean, net.minecraft.entity.Entity> portalLogic;

            if (entity instanceof ServerPlayer) {
                portalLogic = PortalHelper.createVanillaPlayerPortalLogic((ServerPlayerEntity) entity,
                    VecHelper.toVec3d(destination.getPosition()), (ServerWorld) entity.getServerLocation().getWorld(),
                    (ServerWorld) destination.getWorld(), this);
            } else {
                portalLogic = PortalHelper.createVanillaEntityPortalLogic((net.minecraft.entity.Entity) entity,
                        VecHelper.toVec3d(destination.getPosition()), (ServerWorld) entity.getServerLocation().getWorld(),
                        (ServerWorld) destination.getWorld(), this);
            }

            result = portalLogic.apply(generateDestinationPortal);
        } else {
            if (entity instanceof ServerPlayer) {
                result = this.teleporter.bridge$placeEntity((ServerPlayerEntity) entity, (ServerWorld) entity.getWorld(),
                    (ServerWorld) destination.getWorld(), 0, PortalHelper.createVanillaPlayerPortalLogic((ServerPlayerEntity) entity,
                    VecHelper.toVec3d(destination.getPosition()), (ServerWorld) entity.getWorld(), (ServerWorld) destination.getWorld(), this));
            } else {
                result = this.teleporter.bridge$placeEntity((net.minecraft.entity.Entity) entity, (ServerWorld) entity.getWorld(),
                    (ServerWorld) destination.getWorld(), 0, PortalHelper.createVanillaEntityPortalLogic((net.minecraft.entity.Entity) entity,
                        VecHelper.toVec3d(destination.getPosition()), (ServerWorld) entity.getWorld(), (ServerWorld) destination.getWorld(), this));
            }
        }

        if (result == null) {
            return false;
        } else if (result instanceof ServerPlayerEntity) {
            return !currentLocation.equals(((Entity) result).getServerLocation());
        }

        return true;
    }

    public PortalType getLogicType() {
        return this.logicType;
    }

    public PlatformITeleporterBridge getTeleporter() {
        return this.teleporter;
    }
}
