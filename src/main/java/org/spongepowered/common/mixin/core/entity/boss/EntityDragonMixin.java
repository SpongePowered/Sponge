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
package org.spongepowered.common.mixin.core.entity.boss;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseHover;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.health.HealingTypes;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.mixin.core.entity.EntityLivingMixin;

import javax.annotation.Nullable;

@Mixin(EntityDragon.class)
public abstract class EntityDragonMixin extends EntityLivingMixin {

    /**
     * @author gabizou - April 13th, 2018
     * @reason Forge changes the gamerule method calls, so the old injection/redirect
     * would fail in forge environments. This changes the injection to a predictable
     * place where we still can forcibly call things but still cancel as needed.
     */
    @Redirect(
        method = "destroyBlocksInAABB",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/block/material/Material;FIRE:Lnet/minecraft/block/material/Material;",
                opcode = Opcodes.GETSTATIC
            )
        ),
        require = 0 // Forge rewrites the material request to block.isAir
    )
    private Block impl$onCanGrief(final IBlockState state) {
        return ((GrieferBridge) this).bridge$CanGrief() ? state.getBlock() : Blocks.AIR;
    }

    /**
     * Fixes a hidden divide-by-zero error when {@link PhaseHover} returns the
     * current location as the target location.
     *
     * @author JBYoshi
     */
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/phase/IPhase;getTargetLocation()Lnet/minecraft/util/math/Vec3d;"))
    @Nullable
    private Vec3d impl$getTargetLocationOrNull(final IPhase iPhase) {
        final Vec3d target = iPhase.getTargetLocation();
        if (target != null && target.x == this.posX && target.z == this.posZ) {
            return null; // Skips the movement code
        }
        return target;
    }

    @Redirect(method = "updateDragonEnderCrystal",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/EntityDragon;setHealth(F)V"),
        slice = @Slice(
            from = @At("HEAD"),
            to = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", remap = false)
        )
    )
    private void impl$addBossHealingContext(final EntityDragon entityDragon, final float health) {
        if (this.world.isRemote || !ShouldFire.REGAIN_HEALTH_EVENT || !SpongeImplHooks.isMainThread()) {
            this.setHealth(health);
        }
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.HEALING_TYPE, HealingTypes.BOSS);
            this.heal(1f);
        }
    }

}
