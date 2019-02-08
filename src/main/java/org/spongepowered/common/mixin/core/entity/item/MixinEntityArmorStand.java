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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Rotations;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.ArmorStandData;
import org.spongepowered.api.data.manipulator.mutable.BodyPartRotationalData;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArmorStandData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeBodyPartRotationalData;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(EntityArmorStand.class)
@Implements(@Interface(iface = ArmorStand.class, prefix = "armor$"))
public abstract class MixinEntityArmorStand extends MixinEntityLivingBase implements ArmorStand {

    @Shadow public Rotations leftArmRotation;
    @Shadow public Rotations rightArmRotation;
    @Shadow public Rotations leftLegRotation;
    @Shadow public Rotations rightLegRotation;

    @Shadow public abstract boolean getShowArms(); // getShowArms
    @Shadow public abstract boolean hasNoBasePlate(); // hasNoBasePlate
    @Shadow public abstract boolean hasMarker();
    @Shadow public abstract boolean shadow$isSmall();
    @Shadow public abstract Rotations shadow$getHeadRotation();
    @Shadow public abstract Rotations getBodyRotation();

    @Shadow protected abstract void damageArmorStand(float damage);

    @Override
    public Value.Mutable<Boolean> marker() {
        return new SpongeMutableValue<>(Keys.ARMOR_STAND_MARKER, this.hasMarker());
    }

    @Override
    public Value.Mutable<Boolean> small() {
        return new SpongeMutableValue<>(Keys.ARMOR_STAND_IS_SMALL, this.shadow$isSmall());
    }

    @Override
    public Value.Mutable<Boolean> basePlate() {
        return new SpongeMutableValue<>(Keys.ARMOR_STAND_HAS_BASE_PLATE, !this.hasNoBasePlate());
    }

    @Override
    public Value.Mutable<Boolean> arms() {
        return new SpongeMutableValue<>(Keys.ARMOR_STAND_HAS_ARMS, this.getShowArms());
    }

    @Override
    public ArmorStandData getArmorStandData() {
        return new SpongeArmorStandData(this.hasMarker(), this.shadow$isSmall(), this.getShowArms(), !this.hasNoBasePlate());
    }

    @Override
    public BodyPartRotationalData getBodyPartRotationalData() {
        Map<BodyPart, Vector3d> rotations = Maps.newHashMapWithExpectedSize(6);
        rotations.put(BodyParts.HEAD, VecHelper.toVector3d(this.shadow$getHeadRotation()));
        rotations.put(BodyParts.CHEST, VecHelper.toVector3d(this.getBodyRotation()));
        rotations.put(BodyParts.LEFT_ARM, VecHelper.toVector3d(this.leftArmRotation));
        rotations.put(BodyParts.RIGHT_ARM, VecHelper.toVector3d(this.rightArmRotation));
        rotations.put(BodyParts.LEFT_LEG, VecHelper.toVector3d(this.leftLegRotation));
        rotations.put(BodyParts.RIGHT_LEG, VecHelper.toVector3d(this.rightLegRotation));
        return new SpongeBodyPartRotationalData(rotations);
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getBodyPartRotationalData());
        manipulators.add(getArmorStandData());
    }

    /**
     * The return value is set to false if the entity should not be completely
     * destroyed.
     */
    private void fireDestroyDamageEvent(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            DamageEventHandler.generateCauseFor(source, frame);
            DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), this, new ArrayList<>(),
                    Math.max(1000, this.getHealth()));
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;remove()V", ordinal = 0),
            cancellable = true)
    private void fireDamageEventOutOfWorld(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    @Inject(method = "attackEntityFrom",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;isExplosion()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;dropContents()V"),
            cancellable = true)
    private void fireDamageEventExplosion(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    @Redirect(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;damageArmorStand(F)V"))
    private void fireDamageEventDamage(EntityArmorStand self, float effectiveAmount, DamageSource source, float originalAmount) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            DamageEventHandler.generateCauseFor(source, frame);
            DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(frame.getCurrentCause(), this, new ArrayList<>(),
                     effectiveAmount);
            if (!SpongeImpl.postEvent(event)) {
                this.damageArmorStand((float) event.getFinalDamage());
            }
        }
    }

    @Inject(method = "attackEntityFrom", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/DamageSource;isCreativePlayer()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;playBrokenSound()V"), cancellable = true)
    private void fireDamageEventCreativePunch(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    @Inject(method = "attackEntityFrom",
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/entity/item/EntityArmorStand;punchCooldown:J", opcode = Opcodes.GETFIELD)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setEntityState(Lnet/minecraft/entity/Entity;B)V"),
            cancellable = true)
    private void fireDamageEventFirstPunch(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        // While this doesn't technically "damage" the armor stand, it feels
        // like damage in other respects, so fire an event.
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            DamageEventHandler.generateCauseFor(source, frame);
            DamageEntityEvent event = SpongeEventFactory.createDamageEntityEvent(frame.getCurrentCause(), this, new ArrayList<>(),
                     0);
            if (SpongeImpl.postEvent(event)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;dropBlock()V"),
            cancellable = true)
    private void fireDamageEventSecondPunch(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        fireDestroyDamageEvent(source, cir);
    }

    /**
     * @author JBYoshi
     * @reason EntityArmorStand "simplifies" this method to simply call {@link
     * #remove()}. However, this ignores our custom event. Instead, delegate
     * to the superclass and use {@link
     * EntityArmorStand#attackEntityFrom(DamageSource, float)}.
     */
    @Overwrite
    @Override
    public void onKillCommand() {
        super.onKillCommand();
    }
}
