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
package org.spongepowered.common.mixin.core.world.entity.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.MinecraftFallingBlockDamageSource;

import java.util.Optional;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends EntityMixin {

    @SuppressWarnings("UnresolvedMixinReference")
    @Redirect(method = "lambda$causeFallDamage$0(Lnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        )
    )
    private static boolean impl$swapDamageSources(final Entity entity, final DamageSource source, final float damage) {
        if (entity.level.isClientSide) {
            return entity.hurt(source, damage);
        }
        final boolean isAnvil = source == DamageSource.ANVIL;
        final boolean isFallingBlock = source == DamageSource.FALLING_BLOCK;
        final boolean isStalactite = source == DamageSource.FALLING_STALACTITE;
        final Optional<FallingBlockEntity> fallingBlockEntity = PhaseTracker.getInstance().getPhaseContext().getSource(
            FallingBlockEntity.class);
        try {
            if (isAnvil && fallingBlockEntity.isPresent()) {
                final MinecraftFallingBlockDamageSource anvil = new MinecraftFallingBlockDamageSource("anvil", fallingBlockEntity.get());
                anvil.setDefaultFallingBlockDamage();
                ((DamageSourceBridge) (Object) anvil).bridge$setAnvilSource();

                return entity.hurt(DamageSource.ANVIL, damage);
            } else if (isFallingBlock && fallingBlockEntity.isPresent()) {
                final MinecraftFallingBlockDamageSource
                    fallingblock =
                    new MinecraftFallingBlockDamageSource("fallingblock", fallingBlockEntity.get());
                fallingblock.setDefaultFallingBlockDamage();
                ((DamageSourceBridge) (Object) fallingblock).bridge$setFallingBlockSource();
                return entity.hurt(DamageSource.FALLING_BLOCK, damage);
            } else if (isStalactite && fallingBlockEntity.isPresent()){
                final MinecraftFallingBlockDamageSource stalactite = new MinecraftFallingBlockDamageSource(
                    "fallingStalactite", fallingBlockEntity.get());
                stalactite.setDefaultFallingBlockDamage();
                ((DamageSourceBridge) (Object) stalactite).bridge$setFallingStalactite();

                return entity.hurt(DamageSource.FALLING_STALACTITE, damage);
            } else {
                return entity.hurt(source, damage);
            }
        } finally {
            if (isAnvil) {
                ((DamageSourceBridge) source).bridge$setAnvilSource();
            } else if (isFallingBlock) {
                ((DamageSourceBridge) source).bridge$setFallingBlockSource();
            } else if (isStalactite) {
                ((DamageSourceBridge) source).bridge$setFallingStalactite();
            }
        }
    }
}
