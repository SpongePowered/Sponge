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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.BlockMagma;
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
import org.spongepowered.common.util.VecHelper;

@Mixin(BlockMagma.class)
public abstract class BlockMagmaMixin extends BlockMixin {

    @SuppressWarnings("ConstantConditions")
    @Redirect(
        method = "onEntityWalk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean impl$SwapDamageSourceForMagma(final Entity entity, final DamageSource source, final float damage, final World world,
            final BlockPos pos, final Entity original) {
        if (!world.field_72995_K) {
            try {
                final Vector3i blockPosition = VecHelper.toVector3i(pos);
                final Location<org.spongepowered.api.world.World> location = new Location<>((org.spongepowered.api.world.World) world, blockPosition);
                final MinecraftBlockDamageSource hotFloor = new MinecraftBlockDamageSource("hotFloor", location);
                hotFloor.impl$setFireDamage();
                ((DamageSourceBridge) hotFloor).bridge$setHotFloorSource();
                return entity.func_70097_a(DamageSource.field_190095_e, damage);
            } finally {
                ((DamageSourceBridge) source).bridge$setHotFloorSource();
            }
        }
        return entity.func_70097_a(source, damage);
    }

}
