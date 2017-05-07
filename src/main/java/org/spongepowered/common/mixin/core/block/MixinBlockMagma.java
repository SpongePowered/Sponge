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
package org.spongepowered.common.mixin.core.block;

import net.minecraft.block.BlockMagma;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;
import org.spongepowered.common.util.VecHelper;

@Mixin(BlockMagma.class)
public abstract class MixinBlockMagma extends MixinBlock {

    private static final String ATTACK_ENTITY_FROM = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z";

    @Redirect(method = "onEntityWalk", at = @At(value = "INVOKE", target = ATTACK_ENTITY_FROM))
    private boolean onEntityWalkRedirectForMagma(Entity entity, DamageSource originalDamageSource, float damage, net.minecraft.world.World world,
            BlockPos pos, Entity original) {
        if (!world.isRemote) {
            DamageSource.HOT_FLOOR =
                    new MinecraftBlockDamageSource("hotFloor", new Location<>((World) world, VecHelper.toVector3i(pos))).setFireDamage();
            boolean result = entity.attackEntityFrom(DamageSource.HOT_FLOOR, damage);
            DamageSource.HOT_FLOOR = originalDamageSource;
            return result;
        }
        return entity.attackEntityFrom(originalDamageSource, damage);
    }

}
