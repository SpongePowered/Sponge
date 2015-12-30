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
package org.spongepowered.common.mixin.core.entity.passive;

import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.SittingData;
import org.spongepowered.api.entity.living.animal.Ocelot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.TameEntityEvent;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.List;
import java.util.Random;

@Mixin(EntityOcelot.class)
public abstract class MixinEntityOcelot extends MixinEntityTameable implements Ocelot {

    private ItemStack currentItemStack;

    @Inject(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/EntityOcelot;isTamed()Z", shift = At.Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
    public void afterGetCurrentItem(EntityPlayer player, CallbackInfoReturnable<Boolean> cir, ItemStack currentItemStack) {
        if (currentItemStack != null) {
            this.currentItemStack = currentItemStack.copy();
        } else {
            this.currentItemStack = null;
        }
    }

    @Surrogate
    public void afterGetCurrentItem(EntityPlayer player, CallbackInfoReturnable<Boolean> cir) {
        this.currentItemStack = player.inventory.getCurrentItem().copy();
    }

    @Redirect(method = "interact", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 0, remap = false))
    public int onTame(Random rand, int bound, EntityPlayer player) {
        int random = rand.nextInt(bound);
        if (random == 0 && !SpongeImpl
            .postEvent(SpongeEventFactory.createTameEntityEvent(Cause.of(NamedCause.source(player),
                NamedCause.of(TameEntityEvent.USED_ITEM, ((org.spongepowered.api.item.inventory.ItemStack) this.currentItemStack).createSnapshot())),
                this))) {
            this.currentItemStack = null;
            return random;
        }
        this.currentItemStack = null;
        return 1;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        manipulators.add(get(SittingData.class).get());
        manipulators.add(getOcelotData());
    }

    @Override
    public Translation getTranslation() {
        if (shadow$isTamed()) {
            return new SpongeTranslation("entity.Cat.name");
        } else {
            return super.getTranslation();
        }
    }

}
