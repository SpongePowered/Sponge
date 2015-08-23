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
package org.spongepowered.common.mixin.core.entity.living;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.target.entity.LeashEntityEvent;
import org.spongepowered.api.event.target.entity.UnleashEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.Sponge;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(EntityLiving.class)
public abstract class MixinEntityLiving extends MixinEntityLivingBase implements Agent {

    @Shadow private boolean canPickUpLoot;
    @Shadow public abstract boolean isAIDisabled();
    @Shadow protected abstract void setNoAI(boolean p_94061_1_);
    @Shadow public abstract net.minecraft.entity.Entity getLeashedToEntity();
    @Shadow public abstract void setLeashedToEntity(net.minecraft.entity.Entity entityIn, boolean sendAttachNotification);

    public boolean isAiEnabled() {
        return !isAIDisabled();
    }

    public void setAiEnabled(boolean aiEnabled) {
        setNoAI(!aiEnabled);
    }

    public boolean isLeashed() {
        return getLeashedToEntity() != null;
    }

    public void setLeashed(boolean leashed) {
        throw new UnsupportedOperationException(); // TODO
    }

    public Optional<Entity> getLeashHolder() {
        return Optional.fromNullable((Entity) getLeashedToEntity());
    }

    public void setLeashHolder(@Nullable Entity entity) {
        setLeashedToEntity((net.minecraft.entity.Entity) entity, true);
    }

    public boolean getCanPickupItems() {
        return this.canPickUpLoot;
    }

    public void setCanPickupItems(boolean canPickupItems) {
        this.canPickUpLoot = canPickupItems;
    }

    @Inject(method = "interactFirst", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLiving;setLeashedToEntity(Lnet/minecraft/entity/Entity;Z)V"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callLeashEvent(EntityPlayer playerIn, CallbackInfoReturnable<Boolean> ci, ItemStack itemstack) {
        if (!playerIn.worldObj.isRemote) {
            final LeashEntityEvent event = SpongeEventFactory.createEntityLeash(Sponge.getGame(), this, (Player)playerIn);
            Sponge.getGame().getEventManager().post(event);
            if(event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "clearLeashed", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLiving;isLeashed:Z", opcode = Opcodes.PUTFIELD), cancellable = true)
    public void callUnleashEvent(boolean sendPacket, boolean dropLead, CallbackInfo ci) {
        net.minecraft.entity.Entity entity = getLeashedToEntity();
        if (!entity.worldObj.isRemote) {
            final UnleashEntityEvent event = SpongeEventFactory.createEntityUnleash(Sponge.getGame(), this, (Entity)entity);
            Sponge.getGame().getEventManager().post(event);
            if(event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        container.set(of("AiEnabled"), !this.isAIDisabled());
        return container;
    }
}
