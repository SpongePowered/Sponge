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

import net.minecraft.block.MagmaBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;
import org.spongepowered.common.mixin.accessor.util.DamageSourceAccessor;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

@Mixin(MagmaBlock.class)
public abstract class MagmaBlockMixin extends BlockMixin {

    @Redirect(
        method = "onEntityWalk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean impl$swapDamageSourceForMagma(Entity entity, DamageSource source, float damage, World world, BlockPos pos, Entity original) {
        if (!world.isRemote) {
            try {
                final Vector3i blockPosition = VecHelper.toVector3i(pos);
                final Location location = Location.of((org.spongepowered.api.world.World) world, blockPosition);
                final MinecraftBlockDamageSource hotFloor = new MinecraftBlockDamageSource("hotFloor", location);
                ((DamageSourceAccessor) (Object) hotFloor).accessor$setFireDamage();
                ((DamageSourceBridge) (Object) hotFloor).bridge$setHotFloorSource();
                return entity.attackEntityFrom(DamageSource.HOT_FLOOR, damage);
            } finally {
                ((DamageSourceBridge) source).bridge$setHotFloorSource();
            }
        }
        return entity.attackEntityFrom(source, damage);
    }
}
