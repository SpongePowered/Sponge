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
package org.spongepowered.neoforge.mixin.core.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3d;

import java.util.function.Predicate;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin_Neo {

    @Unique
    @Nullable private Vector3d neo$preTeleportPosition;

    // Neo 26.2 replaced the vanilla hurtAndBreak call with its IItemStackExtension glide-damage hook.
    @Inject(
            method = "updateFallFlying",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;onGlideDamage(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
                    shift = At.Shift.AFTER
            )
    )
    protected void neo$onElytraUse(final CallbackInfo ci) {
    }

    // Neo PR #3119 (26.1.2.31-beta) split randomTeleport into a 4-arg forwarder and a 5-arg overload that holds the body.
    // As of 26.3 the body lives in the (Predicate, ItemStack) overload; the TagKey and Predicate-only overloads forward to it.
    // Inject into the 5-arg overload so the original RETURN+BY 2 anchor still resolves.
    @Inject(method = "randomTeleport(DDDZLjava/util/function/Predicate;Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"))
    private void neo$snapshotPositionBeforeVanillaTeleportLogic(final double x, final double y, final double z, final boolean changeState,
                                                                final Predicate<BlockState> isInvalidPosition, final ItemStack consumedStack, final CallbackInfoReturnable<Boolean> cir) {
        final Entity self = (Entity) (Object) this;
        this.neo$preTeleportPosition = new Vector3d(self.getX(), self.getY(), self.getZ());
    }

    @Inject(method = "randomTeleport(DDDZLjava/util/function/Predicate;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(value = "RETURN", ordinal = 0, shift = At.Shift.BY, by = 2), cancellable = true)
    private void neo$callMoveEntityEventForTeleport(final double x, final double y, final double z, final boolean changeState,
                                                    final Predicate<BlockState> isInvalidPosition, final ItemStack consumedStack, final CallbackInfoReturnable<Boolean> cir) {
        if (!ShouldFire.MOVE_ENTITY_EVENT) {
            return;
        }

        final Entity self = (Entity) (Object) this;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(this);

            // ENTITY_TELEPORT is our fallback context
            if (!frame.currentContext().containsKey(EventContextKeys.MOVEMENT_TYPE)) {
                frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.ENTITY_TELEPORT);
            }

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.currentCause(),
                    (org.spongepowered.api.entity.Entity) this, this.neo$preTeleportPosition,
                    new Vector3d(self.getX(), self.getY(), self.getZ()),
                    new Vector3d(x, y, z));

            if (SpongeCommon.post(event)) {
                self.teleportTo(this.neo$preTeleportPosition.x(), this.neo$preTeleportPosition.y(),
                        this.neo$preTeleportPosition.z());
                cir.setReturnValue(false);
                return;
            }

            self.teleportTo(event.destinationPosition().x(), event.destinationPosition().y(),
                    event.destinationPosition().z());
        }
    }
}
