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
package org.spongepowered.common.mixin.core.world.item;

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.projectile.EyeOfEnder;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

@Mixin(EnderEyeItem.class)
public abstract class EnderEyeItemMixin extends ItemMixin {

    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
     * @author i509VCB - February 23rd, 2020 - 1.14.4
     * @reason We can throw a construct pre event here before the
     * entity is actually constructed, and if the event is cancelled,
     * we can still return the correct itemstack. If the event is
     * cancelled, we end up not shrinking the itemstack, but we will
     * make sure to notify the player at the end of the packet being
     * processed.
     *
     * @param worldIn The world
     * @param playerIn The player using the item
     * @param handIn The hand
     * @param cir The callback
     * @param used The ItemStack used from the hand
     * @param rayTraceResult The raytrace validated using the item
     * @param targetPos The target position of the dungeon
     */
    @SuppressWarnings("Duplicates")
    @Inject(
        method = "use",
        at = @At(
            value = "NEW",
            target = "net/minecraft/world/entity/projectile/EyeOfEnder"
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        require = 1,
        cancellable = true
    )
    private void impl$ThrowForPreEvent(final Level worldIn, final Player playerIn, final InteractionHand handIn,
        final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, final ItemStack used, final HitResult rayTraceResult, @Nullable final BlockPos targetPos) {
        if (targetPos != null && !((WorldBridge) worldIn).bridge$isFake() && ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            final ConstructEntityEvent.Pre event =
                    SpongeEventFactory.createConstructEntityEventPre(PhaseTracker.getCauseStackManager().currentCause(),
                            ServerLocation.of((ServerWorld) worldIn, playerIn.getX(), playerIn.getY() + (double) (playerIn.getDimensions(playerIn
                                    .getPose()).height / 2.0F), playerIn.getZ()), new Vector3d(0, 0, 0), EntityTypes.EYE_OF_ENDER.get());
            if (SpongeCommon.post(event)) {
                cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.SUCCESS, used));
            }
        }
    }

    /**
     * In production, the RayTraceResult is lost.
     */
    @SuppressWarnings("Duplicates")
    @Surrogate
    private void impl$ThrowForPreEvent(final Level worldIn, final Player playerIn, final InteractionHand handIn,
        final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, final ItemStack used, @Nullable final BlockPos targetPos) {
        if (targetPos != null && !((WorldBridge) worldIn).bridge$isFake() && ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            final ConstructEntityEvent.Pre event =
                    SpongeEventFactory.createConstructEntityEventPre(PhaseTracker.getCauseStackManager().currentCause(),
                            ServerLocation.of((ServerWorld) worldIn, playerIn.getX(), playerIn.getY() + (double) (playerIn.getDimensions(playerIn
                                    .getPose()).height / 2.0F), playerIn.getZ()), new Vector3d(0, 0, 0), EntityTypes.EYE_OF_ENDER.get());
            if (SpongeCommon.post(event)) {
                cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.SUCCESS, used));
            }
        }
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean impl$setShooter(final Level world, final Entity entity, final Level p_77659_1_, final Player p_77659_2_) {
        if (((WorldBridge) world).bridge$isFake()) {
            return world.addFreshEntity(entity);
        }

        ((EyeOfEnder) entity).offer(Keys.SHOOTER, (ProjectileSource) p_77659_2_);
        return world.addFreshEntity(entity);
    }
}
