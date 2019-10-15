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
package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.DamageSource;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseMixin;

import java.util.ArrayList;

@Mixin(EntityArmorStand.class)
public abstract class EntityArmorStandMixin extends EntityLivingBaseMixin {

    @Shadow protected abstract void damageArmorStand(float damage);

    /**
     * The return value is set to false if the entity should not be completely
     * destroyed.
     */
    private void fireDestroyDamageEvent(final DamageSource source, final CallbackInfoReturnable<Boolean> cir) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            DamageEventHandler.generateCauseFor(source, frame);
            final DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), new ArrayList<>(),
                (Entity) this, Math.max(1000, this.getHealth()));
            if (SpongeImpl.postEvent(event)) {
                cir.setReturnValue(false);
            }
            if (event.getFinalDamage() < this.getHealth()) {
                this.damageArmorStand((float) event.getFinalDamage());
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "attackEntityFrom",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/util/DamageSource;OUT_OF_WORLD:Lnet/minecraft/util/DamageSource;")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;setDead()V", ordinal = 0),
            cancellable = true)
    private void fireDamageEventOutOfWorld(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    @Inject(method = "attackEntityFrom",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;isExplosion()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;dropContents()V"),
            cancellable = true)
    private void fireDamageEventExplosion(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    @Redirect(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;damageArmorStand(F)V"))
    private void fireDamageEventDamage(final EntityArmorStand self, final float effectiveAmount, final DamageSource source, final float originalAmount) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            DamageEventHandler.generateCauseFor(source, frame);
            final DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(frame.getCurrentCause(), new ArrayList<>(),
                (Entity) this, effectiveAmount);
            if (!SpongeImpl.postEvent(event)) {
                this.damageArmorStand((float) event.getFinalDamage());
            }
        }
    }

    @Inject(method = "attackEntityFrom", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;isCreativePlayer()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;playBrokenSound()V"), cancellable = true)
    private void fireDamageEventCreativePunch(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    @Inject(method = "attackEntityFrom",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityArmorStand;punchCooldown:J", opcode = Opcodes.GETFIELD)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setEntityState(Lnet/minecraft/entity/Entity;B)V"),
            cancellable = true)
    private void fireDamageEventFirstPunch(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        // While this doesn't technically "damage" the armor stand, it feels
        // like damage in other respects, so fire an event.
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            DamageEventHandler.generateCauseFor(source, frame);
            final DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(frame.getCurrentCause(), new ArrayList<>(),
                (Entity) this, 0);
            if (SpongeImpl.postEvent(event)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;dropBlock()V"),
            cancellable = true)
    private void fireDamageEventSecondPunch(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    /**
     * @author JBYoshi
     * @reason EntityArmorStand "simplifies" this method to simply call {@link
     * #setDead()}. However, this ignores our custom event. Instead, delegate
     * to the superclass and use {@link
     * EntityArmorStand#attackEntityFrom(DamageSource, float)}.
     */
    @Overwrite
    @Override
    public void onKillCommand() {
        super.onKillCommand();
    }
}
