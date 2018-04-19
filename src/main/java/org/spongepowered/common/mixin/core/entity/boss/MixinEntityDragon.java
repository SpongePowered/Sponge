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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseHover;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.spongepowered.api.entity.EnderCrystal;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.complex.EnderDragonPart;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.mixin.core.entity.MixinEntityLiving;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(EntityDragon.class)
public abstract class MixinEntityDragon extends MixinEntityLiving implements EnderDragon {

    @Shadow public MultiPartEntityPart[] dragonPartArray;
    @Shadow public EntityEnderCrystal healingEnderCrystal;

    @Override
    public Set<EnderDragonPart> getParts() {
        Builder<EnderDragonPart> builder = ImmutableSet.builder();

        for (MultiPartEntityPart part : this.dragonPartArray) {
            builder.add((EnderDragonPart) part);
        }

        return builder.build();
    }

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
            target = "Lnet/minecraft/block/state/IBlockState;getMaterial()Lnet/minecraft/block/material/Material;"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;"
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/block/material/Material;AIR:Lnet/minecraft/block/material/Material;",
                opcode = Opcodes.GETSTATIC
            )
        )
    )
    private Material onCanGrief(IBlockState state) {
        return ((IMixinGriefer) this).canGrief() ? state.getMaterial() : Material.AIR;
    }
    
    @Override
    public Optional<EnderCrystal> getHealingCrystal() {
        return Optional.ofNullable((EnderCrystal) this.healingEnderCrystal);
    }

    /**
     * Fixes a hidden divide-by-zero error when {@link PhaseHover} returns the
     * current location as the target location.
     *
     * @author JBYoshi
     */
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/phase/IPhase;getTargetLocation()Lnet/minecraft/util/math/Vec3d;"))
    @Nullable
    private Vec3d getTargetLocationOrNull(IPhase iPhase) {
        Vec3d target = iPhase.getTargetLocation();
        if (target != null && target.x == this.posX && target.z == this.posZ) {
            return null; // Skips the movement code
        }
        return target;
    }
}
