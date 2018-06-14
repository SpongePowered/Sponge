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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.data.util.DirectionResolver;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntityHanging;
import org.spongepowered.common.interfaces.world.IMixinWorld;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

@Mixin(EntityHanging.class)
public abstract class MixinEntityHanging extends MixinEntity implements Hanging, IMixinEntityHanging {

    @Shadow @Nullable public EnumFacing facingDirection;
    @Shadow private int tickCounter1;

    @Shadow public abstract boolean onValidSurface();
    @Shadow public abstract void onBroken(Entity entity);
    @Shadow public abstract void updateFacingWithBoundingBox(EnumFacing facingDirectionIn);

    private boolean ignorePhysics = false;

    /**
     * Called to update the entity's position/logic.
     */
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;onValidSurface()Z"))
    private boolean checkIfOnValidSurfaceAndIgnoresPhysics(EntityHanging entityHanging) {
        return this.onValidSurface() && !this.ignorePhysics;
    }

    @Override
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setBoolean("ignorePhysics", this.ignorePhysics);
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
        if (compound.hasKey("ignorePhysics")) {
            this.ignorePhysics = compound.getBoolean("ignorePhysics");
        }
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityHanging;setDead()V"), cancellable = true)
    private void onAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.getCurrentCause(), new ArrayList<>(), this, 0, amount);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * @author gabizou - April 19th, 2018
     * @reason Redirect the flow of logic to sponge for events and captures. Forge's compatibility is built in
     * to the implementation.
     */
    @Override
    @Overwrite
    public EntityItem entityDropItem(ItemStack stack, float offsetY) {
        // Sponge Start - Check for client worlds,, don't care about them really. If it's server world, then we care.
        final double xOffset = ((float) this.facingDirection.getFrontOffsetX() * 0.15F);
        final double zOffset = ((float) this.facingDirection.getFrontOffsetZ() * 0.15F);
        if (((IMixinWorld) this.world).isFake()) {
            // Sponge End
            EntityItem entityitem = new EntityItem(this.world, this.posX + xOffset, this.posY + (double) offsetY, this.posZ + zOffset, stack);
            entityitem.setDefaultPickupDelay();
            this.world.spawnEntity(entityitem);
            return entityitem;
        }
        // Sponge - redirect server sided logic to sponge to handle cause stacks and phase states
        return EntityUtil.entityOnDropItem((EntityHanging) (Object) this, stack, offsetY, this.posX + xOffset, this.posZ + zOffset);
    }

    // Data delegated methods

    @Override
    public Direction getDirection() {
        return this.facingDirection == null ? Direction.NONE : DirectionResolver.getFor(this.facingDirection);
    }

    @Override
    public void setDirection(Direction direction) {
        updateFacingWithBoundingBox(DirectionResolver.getFor(direction));
    }

    @Override
    public DirectionalData getDirectionalData() {
        return new SpongeDirectionalData(getDirection());
    }

    @Override
    public Value<Direction> direction() {
        return new SpongeValue<>(Keys.DIRECTION, getDirection());
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(getDirectionalData());
    }

}
