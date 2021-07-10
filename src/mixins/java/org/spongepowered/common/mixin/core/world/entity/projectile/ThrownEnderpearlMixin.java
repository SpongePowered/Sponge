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
package org.spongepowered.common.mixin.core.world.entity.projectile;

import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.PlatformTeleporter;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.HitResult;

@Mixin(ThrownEnderpearl.class)
public abstract class ThrownEnderpearlMixin extends ThrowableProjectileMixin {

    // TODO Key not implemented
    private double impl$damageAmount;

    @ModifyArg(method = "onHit",
            at = @At(value = "INVOKE",
                target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private float impl$onAttackEntityFromWithDamage(final float damage) {
        return (float) this.impl$damageAmount;
    }

    @Inject(
            method = "onHit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportTo(DDD)V"),
            cancellable = true
    )
    private void impl$callMoveEntityEventForThrower(final HitResult result, final CallbackInfo ci) {
        if (this.shadow$getCommandSenderWorld().isClientSide || !ShouldFire.MOVE_ENTITY_EVENT) {
            return;
        }
        final Entity entity = this.shadow$getOwner();

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(entity);
            frame.pushCause(this);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.ENDER_PEARL);

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.currentCause(),
                    (org.spongepowered.api.entity.Entity) entity, VecHelper.toVector3d(entity.position()),
                    VecHelper.toVector3d(this.shadow$position()), VecHelper.toVector3d(this.shadow$position()));
            if (SpongeCommon.post(event)) {
                // Eventhough the event is made, the pearl was still created so remove it anyways
                this.shadow$remove();
                return;
            }

            // This seems odd but we move the pearl so that the pearl's logic will move the living entity later in the impact method
            final Vector3d destinationPosition = event.destinationPosition();
            this.shadow$setPos(destinationPosition.x(), destinationPosition.y(), destinationPosition.z());
        }
    }

    @Override
    @Nullable
    public Entity bridge$changeDimension(final ServerLevel dimensionIn, final PlatformTeleporter teleporter) {
        final Entity entity = super.bridge$changeDimension(dimensionIn, teleporter);

        if (entity instanceof ThrownEnderpearl) {
            // We actually teleported so...
            this.shadow$setOwner(null);
        }

        return entity;
    }

}
