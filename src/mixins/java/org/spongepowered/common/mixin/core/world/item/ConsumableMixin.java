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
package org.spongepowered.common.mixin.core.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.data.VanishableBridge;

import java.util.Optional;

@Mixin(Consumable.class)
public abstract class ConsumableMixin {

    @Redirect(method = "emitParticlesAndSounds", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/LivingEntity;spawnItemParticles(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void impl$createCauseFrameForTeleport(final LivingEntity instance, final ItemStack $$0, final int $$1) {
        if (!(instance instanceof VanishableBridge bridge) || bridge.bridge$vanishState().createsParticles()) {
            instance.spawnItemParticles($$0, $$1);
        }
    }


    @WrapOperation(method = "emitParticlesAndSounds", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/component/Consumable;getConsumingSound(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;"))
    private Optional<Holder<SoundEvent>> impl$ignoreConsumeSoundsWhileVanished(final Consumable instance, final LivingEntity user,
        final ItemStack stack, final Operation<Optional<Holder<SoundEvent>>> original) {
        if (user instanceof VanishableBridge bridge && !bridge.bridge$vanishState().createsSounds()) {
            return Optional.empty();
        }
        return original.call(instance, user, stack);
    }

}
