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
package org.spongepowered.common.mixin.core.util;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.bridge.world.ExplosionBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.registry.provider.DamageSourceToTypeProvider;

import javax.annotation.Nullable;

@Mixin(value = net.minecraft.util.DamageSource.class)
public abstract class DamageSourceMixin implements DamageSourceBridge {

    @Shadow @Final @Mutable public static DamageSource LAVA;
    @Shadow @Final @Mutable public static DamageSource IN_FIRE;
    @Shadow @Final @Mutable public static DamageSource LIGHTNING_BOLT;
    @Shadow @Final @Mutable public static DamageSource HOT_FLOOR;
    @Shadow @Final @Mutable public static DamageSource FIREWORKS;
    @Shadow @Final @Mutable public static DamageSource ANVIL;
    @Shadow @Final @Mutable public static DamageSource FALLING_BLOCK;
    @Shadow @Final @Mutable public static DamageSource CACTUS;

    @Shadow public String damageType;

    @Shadow @Nullable public abstract Entity getTrueSource();

    DamageType impl$damageType;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void spongeSetDamageTypeFromConstructor(final String damageTypeIn, final CallbackInfo ci) {
        if (!damageTypeIn.contains(":")) {
            this.impl$damageType = DamageSourceToTypeProvider.getInstance().getOrCustom(damageTypeIn);
        } else {
            this.impl$damageType = Sponge.getRegistry().getType(DamageType.class, damageTypeIn).orElse(DamageTypes.CUSTOM);
        }
    }

    @Inject(method = "getDeathMessage(Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/util/text/ITextComponent;", cancellable = true,
            at = @At(value = "RETURN"))
    private void beforeGetDeathMessageReturn(final LivingEntity entityLivingBaseIn, final CallbackInfoReturnable<ITextComponent> cir) {
        // This prevents untranslated keys from appearing in death messages, switching out those that are untranslated with the generic message.
        if (cir.getReturnValue().func_150260_c().equals("death.attack." + this.damageType)) {
            cir.setReturnValue(new TranslationTextComponent("death.attack.generic", entityLivingBaseIn.func_145748_c_()));
        }
    }

    @Inject(method = "causeExplosionDamage(Lnet/minecraft/world/Explosion;)Lnet/minecraft/util/DamageSource;", at = @At("HEAD"), cancellable = true)
    private static void onSetExplosionSource(@Nullable final Explosion explosionIn, final CallbackInfoReturnable<net.minecraft.util.DamageSource> cir) {
        if (explosionIn != null) {
            final Entity entity = ((ExplosionBridge) explosionIn).bridge$getExploder();
            if (entity != null && !((WorldBridge) ((ExplosionBridge) explosionIn).bridge$getWorld()).bridge$isFake()) {
                if (explosionIn.func_94613_c() == null && entity instanceof OwnershipTrackedBridge) {
                    // check creator
                    final OwnershipTrackedBridge spongeEntity = (OwnershipTrackedBridge) entity;
                    spongeEntity.tracked$getOwnerReference()
                        .filter(user -> user instanceof PlayerEntity)
                        .map(user -> (PlayerEntity) user)
                        .ifPresent(player -> {
                            final IndirectEntityDamageSource damageSource = new IndirectEntityDamageSource("explosion.player", entity, player);
                            damageSource.func_76351_m().func_94540_d();
                            cir.setReturnValue(damageSource);
                        });
                }
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("DamageSource")
                .add("Name", this.damageType)
                .add("Type", this.impl$damageType.getId())
                .toString();
    }

    @Override
    public DamageType bridge$getDamageType() {
        return this.impl$damageType;
    }

    @Override
    public void bridge$resetDamageType() {
        if (!this.damageType.contains(":")) {
            this.impl$damageType = DamageSourceToTypeProvider.getInstance().getOrCustom(this.damageType);
        } else {
            this.impl$damageType = Sponge.getRegistry().getType(DamageType.class, this.damageType).orElse(DamageTypes.CUSTOM);
        }
    }

    @Override
    public void bridge$setLava() {
        LAVA = (DamageSource) (Object) this;
    }

    @Override
    public void bridge$setFireSource() {
        IN_FIRE = (DamageSource) (Object) this;
    }

    @Override
    public void bridge$setLightningSource() {
        LIGHTNING_BOLT = (DamageSource) (Object) this;
    }

    @Override
    public void bridge$setHotFloorSource() {
        HOT_FLOOR = (DamageSource) (Object) this;
    }

    @Override
    public void bridge$setFireworksSource() {
        FIREWORKS = (DamageSource) (Object) this;
    }

    @Override
    public void bridge$setFallingBlockSource() {
        FALLING_BLOCK = (DamageSource) (Object) this;
    }

    @Override
    public void bridge$setAnvilSource() {
        ANVIL = (DamageSource) (Object) this;
    }

    @Override
    public void bridge$setCactusSource() {
        CACTUS = (DamageSource) (Object) this;
    }
}
