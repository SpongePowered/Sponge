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

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.damage.MinecraftFallingBlockDamageSource;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.mixin.core.entity.EntityMixin;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends EntityMixin {

    @Shadow private BlockState fallTile;

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "fall",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean spongeAttackFallingOrAnvil(final Entity entity, final DamageSource source, final float damage) {
        if (entity.world.isRemote) {
            return entity.attackEntityFrom(source, damage);
        }
        final boolean isAnvil = this.fallTile.getBlock() == Blocks.ANVIL;
        try {
            if (isAnvil) {
                final MinecraftFallingBlockDamageSource anvil = new MinecraftFallingBlockDamageSource("anvil", (FallingBlockEntity) (Object) this);
                ((DamageSourceBridge) anvil).bridge$setAnvilSource();

                return entity.attackEntityFrom(DamageSource.ANVIL, damage);
            } else {
                final MinecraftFallingBlockDamageSource
                    fallingblock =
                    new MinecraftFallingBlockDamageSource("fallingblock", (FallingBlockEntity) (Object) this);
                ((DamageSourceBridge) fallingblock).bridge$setFallingBlockSource();
                return entity.attackEntityFrom(DamageSource.FALLING_BLOCK, damage);
            }
        } finally {
            if (isAnvil) {
                ((DamageSourceBridge) source).bridge$setAnvilSource();
            } else {
                ((DamageSourceBridge) source).bridge$setFallingBlockSource();
            }
        }
    }
}
