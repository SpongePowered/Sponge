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

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.tileentity.CampfireTileEntityBridge;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.util.MinecraftBlockDamageSource;

import java.util.Optional;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin extends BlockMixin {


    @Redirect(method = "entityInside",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;hurt(Lnet/minecraft/util/DamageSource;F)Z"
            )
    )
    private boolean impl$spongeRedirectForFireDamage(final Entity self, final DamageSource source, final float damage,
            final BlockState blockState, final World world, final BlockPos blockPos, final Entity entity) {
        if (self.level.isClientSide) { // Short Circuit
            return self.hurt(source, damage);
        }
        try {
            final ServerLocation location = ServerLocation.of((ServerWorld) world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
            final MinecraftBlockDamageSource fire = new MinecraftBlockDamageSource("inFire", location);
            ((DamageSourceBridge) (Object) fire).bridge$setFireSource();
            return self.hurt(DamageSource.IN_FIRE, damage);
        } finally {
            // Since "source" is already the DamageSource.IN_FIRE object, we can re-use it to re-assign.
            ((DamageSourceBridge) source).bridge$setFireSource();
        }
    }

    @Inject(method = "use", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/CampfireTileEntity;placeFood(Lnet/minecraft/item/ItemStack;I)Z"))
    public void impl$placeFood(BlockState p_225533_1_, World p_225533_2_, BlockPos p_225533_3_, PlayerEntity p_225533_4_, Hand p_225533_5_,
            BlockRayTraceResult p_225533_6_, CallbackInfoReturnable<ActionResultType> cir,
            TileEntity tileEntity, CampfireTileEntity campfire, ItemStack itemStack, Optional<CampfireCookingRecipe> optional) {
        ((CampfireTileEntityBridge) campfire).bridge$placeRecipe(optional.get());
    }

}
