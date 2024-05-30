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
package org.spongepowered.common.mixin.core.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.entity.PortalProcessorBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportContext;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

@Mixin(PortalProcessor.class)
public abstract class PortalProcessorMixin implements PortalProcessorBridge {
    // @formatter:off
    @Shadow private Portal portal;
    @Shadow private BlockPos entryPosition;
    // @formatter:on

    private Level impl$level;
    private Integer impl$customPortalTransitionTime;

    @Redirect(method = "processPortalTeleportation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Portal;getPortalTransitionTime(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)I"))
    public int impl$onGetPortalDestination(final Portal instance, final ServerLevel $$0, final Entity $$1) {
        if (this.impl$customPortalTransitionTime != null) {
            return this.impl$customPortalTransitionTime;
        }
        return instance.getPortalTransitionTime($$0, $$1);
    }


    @Redirect(method = "getPortalDestination",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Portal;getPortalDestination(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/portal/DimensionTransition;"))
    public DimensionTransition impl$onGetPortalDestination(final Portal instance, final ServerLevel serverLevel, final Entity entity, final BlockPos blockPos) {
        final var spongEntity = (org.spongepowered.api.entity.Entity) entity;

        var finalPortal = instance;
        final var contextToSwitchTo = EntityPhase.State.PORTAL_DIMENSION_CHANGE.createPhaseContext(PhaseTracker.getInstance()).worldChange();
        if (entity instanceof ServerPlayer) {
            contextToSwitchTo.player();
        }

        try (final TeleportContext context = contextToSwitchTo.buildAndSwitch();
                final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);
            var be = serverLevel.getBlockEntity(this.entryPosition);
            if (be != null) {
                frame.pushCause(be);
            }
            frame.pushCause(this.portal); // portal type

            var wrapperTransaction = context.getTransactor().logWrapper();

            var movementType = this.portal == Blocks.END_GATEWAY ? MovementTypes.END_GATEWAY : MovementTypes.PORTAL;
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, movementType);
            frame.addContext(EventContextKeys.PORTAL_LOGIC, (PortalLogic) portal);

            var preEvent = SpongeEventFactory.createInvokePortalEventPrepare(frame.currentCause(), spongEntity, (PortalLogic) this.portal);
            if (SpongeCommon.post(preEvent)) {
                return null;
            }
            finalPortal = (Portal) preEvent.portalLogic();

            final var transition = finalPortal.getPortalDestination(serverLevel, entity, blockPos);

            frame.popCause();

            if (transition == null) {
                return null;
            }

            frame.pushCause(finalPortal);
            frame.addContext(EventContextKeys.PORTAL, (org.spongepowered.api.world.portal.Portal) this);

            var preWorldChangeEvent = PlatformHooks.INSTANCE.getEventHooks().callChangeEntityWorldEventPre(entity, transition.newLevel());
            if (preWorldChangeEvent.isCancelled()) {
                wrapperTransaction.markCancelled();
                return null;
            }

            var tpEvent = SpongeEventFactory.createInvokePortalEventExecute(frame.currentCause(),
                    spongEntity,
                    (ServerWorld) entity.level(),
                    new Vector3d(transition.xRot(), transition.yRot(), 0),
                    spongEntity.position(),
                    spongEntity.rotation(),
                    (ServerWorld) transition.newLevel(),
                    VecHelper.toVector3d(transition.pos()),
                    VecHelper.toVector3d(transition.pos()),
                    preWorldChangeEvent.destinationWorld(), // with modified preWorldChangeEvent destination world
                    VecHelper.toVector3d(transition.speed()),
                    (PortalLogic) finalPortal);
            if (SpongeCommon.post(tpEvent)) {
                wrapperTransaction.markCancelled();
                return null;
            }

            // modify transition after event
            return new DimensionTransition((ServerLevel) tpEvent.destinationWorld(),
                    VecHelper.toVanillaVector3d(tpEvent.destinationPosition()),
                    VecHelper.toVanillaVector3d(tpEvent.exitSpeed()),
                    (float) tpEvent.toRotation().x(),
                    (float) tpEvent.toRotation().y(),
                    transition.missingRespawnBlock(),
                    transition.postDimensionTransition());
        }
    }

    @Override
    public void bridge$init(final Level level) {
        this.impl$level = level;
    }

    @Override
    public void bridge$setTransitionTime(final Integer customTime) {
        this.impl$customPortalTransitionTime = customTime;
    }

    @Override
    public Level bridge$level() {
        return this.impl$level;
    }
}
