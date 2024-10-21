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
package org.spongepowered.common.mixin.core.world.damagesource;

import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.accessor.world.level.ServerExplosionAccessor;
import org.spongepowered.common.bridge.CreatorTrackedBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;

@Mixin(DamageSources.class)
public abstract class DamageSourcesMixin {

    @Redirect(method = "explosion(Lnet/minecraft/world/level/Explosion;)Lnet/minecraft/world/damagesource/DamageSource;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Explosion;getIndirectSourceEntity()Lnet/minecraft/world/entity/LivingEntity;"))
    private LivingEntity onSetExplosionSource(final Explosion explosion) {
        // When indirect source is not set
        if (explosion.getIndirectSourceEntity() == null
                && explosion.getDirectSourceEntity() instanceof CreatorTrackedBridge creatorBridge
                && !((LevelBridge) ((ServerExplosionAccessor) explosion).accessor$level()).bridge$isFake()) {
            // check creator
            var indirectSource = creatorBridge.tracker$getCreatorUUID().flatMap(x -> Sponge.server().player(x));
            if (indirectSource.isPresent()) {
                return (LivingEntity) indirectSource.get();
            }
        }
        return explosion.getIndirectSourceEntity();
    }


}
