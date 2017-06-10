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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.Explosion;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.registry.provider.DamageSourceToTypeProvider;

import java.util.Optional;
import java.util.UUID;

@Mixin(value = net.minecraft.util.DamageSource.class, priority = 990)
public abstract class MixinDamageSource implements DamageSource {

    @Shadow public String damageType;

    @Shadow public abstract boolean isProjectile();
    @Shadow public abstract boolean isUnblockable();
    @Shadow public abstract boolean canHarmInCreative();
    @Shadow public abstract boolean isDamageAbsolute();
    @Shadow public abstract boolean isMagicDamage();
    @Shadow public abstract float getHungerDamage();
    @Shadow public abstract boolean isDifficultyScaled();
    @Shadow public abstract boolean isExplosion();

    private DamageType apiDamageType;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(String damageTypeIn, CallbackInfo ci) {
        this.apiDamageType = DamageSourceToTypeProvider.getInstance().getOrCustom(damageTypeIn);
    }

    @Inject(method = "getDeathMessage(Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/util/text/ITextComponent;", cancellable = true,
            at = @At(value = "RETURN"))
    @SuppressWarnings("deprecation")
    public void beforeGetDeathMessageReturn(EntityLivingBase entityLivingBaseIn, CallbackInfoReturnable<ITextComponent> cir) {
        // This prevents untranslated keys from appearing in death messages, switching out those that are untranslated with the generic message.
        if (cir.getReturnValue().getUnformattedText().equals("death.attack." + this.damageType)) {
            cir.setReturnValue(new TextComponentTranslation("death.attack.generic", entityLivingBaseIn.getDisplayName()));
        }
    }

    @Inject(method = "causeExplosionDamage(Lnet/minecraft/world/Explosion;)Lnet/minecraft/util/DamageSource;", at = @At("HEAD"), cancellable = true)
    private static void onSetExplosionSource(Explosion explosionIn, CallbackInfoReturnable<net.minecraft.util.DamageSource> cir) {
        if (explosionIn != null && explosionIn.exploder != null && explosionIn.world != null) {
            if (explosionIn.getExplosivePlacedBy() == null) {
                // check creator
                Entity spongeEntity = (Entity) explosionIn.exploder;
                Optional<UUID> creatorUuid = spongeEntity.getCreator();
                if (creatorUuid.isPresent()) {
                    EntityPlayer player = explosionIn.world.getPlayerEntityByUUID(creatorUuid.get());
                    if (player != null) {
                        EntityDamageSourceIndirect damageSource =
                                new EntityDamageSourceIndirect("explosion.player",
                                        explosionIn.exploder, player);
                        damageSource.setDifficultyScaled().setExplosion();
                        cir.setReturnValue(damageSource);
                    }
                }
            }
        }
    }

    @Override
    public boolean isExplosive() {
        return isExplosion();
    }

    @Override
    public boolean isMagic() {
        return isMagicDamage();
    }

    @Override
    public boolean doesAffectCreative() {
        return canHarmInCreative();
    }

    @Override
    public boolean isAbsolute() {
        return isDamageAbsolute();
    }

    @Override
    public boolean isBypassingArmor() {
        return isUnblockable();
    }

    @Override
    public boolean isScaledByDifficulty() {
        return isDifficultyScaled();
    }

    @Override
    public DamageType getType() {
        return this.apiDamageType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("DamageSource")
                .add("Name", this.damageType)
                .add("Type", this.apiDamageType.getId())
                .toString();
    }
}
