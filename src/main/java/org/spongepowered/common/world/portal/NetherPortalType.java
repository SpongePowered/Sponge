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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.entity.player.ServerPlayerEntityAccessor;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportContext;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.util.VecHelper;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public final class NetherPortalType extends VanillaPortalType {

    public NetherPortalType(final ResourceKey key) {
        super(key);
    }

    @Override
    public void generatePortal(final ServerLocation location) {
        Objects.requireNonNull(location);
        PortalHelper.generateNetherPortal((ServerWorld) location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), true);
    }

    @Override
    public Optional<Portal> findPortal(final ServerLocation location) {
        Objects.requireNonNull(location);

        return Optional.empty();
    }

    @Override
    public boolean teleport(final Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(destination);

        final net.minecraft.entity.Entity mEntity = (net.minecraft.entity.Entity) entity;

        // Nether Portal Block Collision Rules
        if (mEntity.isPassenger() || mEntity.isBeingRidden() || !mEntity.isNonBoss()) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(SpongeCommon.getActivePlugin());
            frame.pushCause(this);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PORTAL);

            final ServerLocation previousLocation = entity.getServerLocation();
            ServerLocation actualDestination = destination;
            boolean worldChange = !previousLocation.getWorldKey().equals(actualDestination.getWorldKey());

            if (worldChange) {
                // Call platform event hook before changing dimensions
                final ChangeEntityWorldEvent.Pre event = PlatformHooks.getInstance().getEventHooks().callChangeEntityWorldEventPre((net.minecraft.entity.Entity)
                        entity, (ServerWorld) destination.getWorld());
                if (event == null || event.isCancelled() || ((WorldBridge) event.getDestinationWorld()).bridge$isFake()) {
                    return false;
                }

                actualDestination = ServerLocation.of(event.getDestinationWorld(), entity.getPosition());
            }

            final Function<Boolean, net.minecraft.entity.Entity> portalLogic;
            if (entity instanceof ServerPlayerEntity) {
                portalLogic = PortalHelper.createVanillaPlayerPortalLogic((ServerPlayerEntity) entity,
                        VecHelper.toVec3d(actualDestination.getPosition()), (ServerWorld) previousLocation.getWorld(),
                        (ServerWorld) actualDestination.getWorld(), this);
            } else {
                portalLogic = PortalHelper.createVanillaEntityPortalLogic((net.minecraft.entity.Entity) entity,
                        VecHelper.toVec3d(actualDestination.getPosition()), (ServerWorld) previousLocation.getWorld(),
                        (ServerWorld) actualDestination.getWorld(), this);
            }

            ((net.minecraft.entity.Entity) entity).setPortal(VecHelper.toBlockPos(previousLocation.getPosition()));

            if (entity instanceof ServerPlayerEntity) {
                ((ServerPlayerEntityAccessor) entity).accessor$setInvulnerableDimensionChange(true);
            }

            final net.minecraft.entity.Entity result = portalLogic.apply(generateDestinationPortal);

            ((EntityAccessor) entity).accessor$setInPortal(false);

            if (result == null) {
                return false;
            } else {
                final ServerLocation currentLocation = ((Entity) mEntity).getServerLocation();

                // We use actualDestination for the world as the world change does not happen yet
                if (previousLocation.getWorld() == actualDestination.getWorld() && previousLocation.getBlockPosition().equals(currentLocation.getBlockPosition())) {
                    return false;
                }

                actualDestination = ServerLocation.of(actualDestination.getWorld(), currentLocation.getPosition());
            }

            if (!worldChange) {
                ((EntityAccessor) entity).accessor$setLastPortalPos(new BlockPos(mEntity.getPosX(), mEntity.getPosY(), mEntity.getPosZ()));
                ((EntityAccessor) entity).accessor$setTimeUntilPortal(Integer.MAX_VALUE);

                return true;
            }

            // Players are special fickle things
            if (entity instanceof ServerPlayerEntity) {
                // Hacks
                ((EntityAccessor) entity).accessor$setTimeUntilPortal(Integer.MAX_VALUE);

                EntityUtil.performPostChangePlayerWorldLogic((ServerPlayerEntity) mEntity, (ServerWorld) previousLocation.getWorld(),
                        (ServerWorld) destination.getWorld(), (ServerWorld) actualDestination.getWorld(), false);
            } else {
                // The portal logic handles re-creating the entity in the other dimension, this is simply cleanup
                ((net.minecraft.entity.Entity) entity).detach();
                ((PlatformEntityBridge) entity).bridge$remove(false);
                ((ServerWorld) previousLocation.getWorld()).getProfiler().endSection();
                ((ServerWorld) previousLocation.getWorld()).resetUpdateEntityTick();
                ((ServerWorld) result.getEntityWorld()).resetUpdateEntityTick();
                ((ServerWorld) previousLocation.getWorld()).getProfiler().endSection();
            }

            return true;
        }
    }
}
