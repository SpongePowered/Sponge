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
package org.spongepowered.common.mixin.core.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.health.HealingTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.registry.type.effect.PotionEffectTypeRegistryModule;

@Mixin(Potion.class)
public abstract class PotionMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "registerPotions",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/RegistryNamespaced;register(ILjava/lang/Object;Ljava/lang/Object;)V"))
    private static void impl$registerForSponge(final RegistryNamespaced registry, final int id, final Object location, final Object potion) {
        final ResourceLocation resource = (ResourceLocation) location;
        final Potion mcPotion = (Potion) potion;
        PotionEffectTypeRegistryModule.getInstance().registerFromGameData(resource.toString(), (PotionEffectType) mcPotion);
        registry.register(id, location, potion);
    }

    @Redirect(method = "performEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;heal(F)V"))
    private void impl$addHealingContext(final EntityLivingBase entityLivingBase, final float healAmount) {
        if (!ShouldFire.REGAIN_HEALTH_EVENT || entityLivingBase.world.isRemote || !SpongeImplHooks.isMainThread()) {
            entityLivingBase.heal(healAmount);
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.HEALING_TYPE, HealingTypes.POTION);
            entityLivingBase.heal(healAmount);
        }
    }

    @Redirect(method = "affectEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;heal(F)V"))
    private void impl$addHealingContextForAffects(final EntityLivingBase entityLivingBase, final float healAmount) {
        if (entityLivingBase.world.isRemote || !ShouldFire.REGAIN_HEALTH_EVENT || !SpongeImplHooks.isMainThread()) {
            entityLivingBase.heal(healAmount);
            return;
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            if (entityLivingBase.isEntityUndead()) {
                frame.addContext(EventContextKeys.HEALING_TYPE, HealingTypes.UNDEAD);
            } else {
                frame.addContext(EventContextKeys.HEALING_TYPE, HealingTypes.POTION);
            }
            entityLivingBase.heal(healAmount);
        }
    }

}
