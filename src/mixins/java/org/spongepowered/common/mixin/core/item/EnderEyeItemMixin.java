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
package org.spongepowered.common.mixin.core.item;

import net.minecraft.entity.item.EyeOfEnderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.projectile.EyeOfEnder;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;

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
        method = "onItemRightClick",
        at = @At(
            value = "NEW",
            target = "net/minecraft/entity/item/EyeOfEnderEntity"
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        require = 1,
        cancellable = true
    )
    private void impl$ThrowForPreEvent(final World worldIn, final PlayerEntity playerIn, final Hand handIn,
        final CallbackInfoReturnable<ActionResult<ItemStack>> cir, final ItemStack used, final RayTraceResult rayTraceResult, @Nullable final BlockPos targetPos) {
        if (targetPos != null && !((WorldBridge) worldIn).bridge$isFake() && ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            final ConstructEntityEvent.Pre event =
                    SpongeEventFactory.createConstructEntityEventPre(PhaseTracker.getCauseStackManager().getCurrentCause(),
                            ServerLocation.of((ServerWorld) worldIn, playerIn.getPosX(), playerIn.getPosY() + (double) (playerIn.getSize(playerIn
                                    .getPose()).height / 2.0F), playerIn.getPosZ()), new Vector3d(0, 0, 0), EntityTypes.EYE_OF_ENDER.get());
            if (SpongeCommon.postEvent(event)) {
                cir.setReturnValue(new ActionResult<>(ActionResultType.SUCCESS, used));
            }
        }
    }

    /**
     * In production, the RayTraceResult is lost.
     */
    @SuppressWarnings("Duplicates")
    @Surrogate
    private void impl$ThrowForPreEvent(final World worldIn, final PlayerEntity playerIn, final Hand handIn,
        final CallbackInfoReturnable<ActionResult<ItemStack>> cir, final ItemStack used, @Nullable final BlockPos targetPos) {
        if (targetPos != null && !((WorldBridge) worldIn).bridge$isFake() && ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            final ConstructEntityEvent.Pre event =
                    SpongeEventFactory.createConstructEntityEventPre(PhaseTracker.getCauseStackManager().getCurrentCause(),
                            ServerLocation.of((ServerWorld) worldIn, playerIn.getPosX(), playerIn.getPosY() + (double) (playerIn.getSize(playerIn
                                    .getPose()).height / 2.0F), playerIn.getPosZ()), new Vector3d(0, 0, 0), EntityTypes.EYE_OF_ENDER.get());
            if (SpongeCommon.postEvent(event)) {
                cir.setReturnValue(new ActionResult<>(ActionResultType.SUCCESS, used));
            }
        }
    }

    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
     * @reason Instead of redirecting the world.spawnEntity,
     * we'll inject with a local capture to have the ability to
     * add context to the entity being spawned. Normally, by this point,
     * the cause stack will have the information available, but,
     * there are cases where we need to add the context of the item
     * and stack if the interact is being called by another source.
     *
     * @param worldIn The world
     * @param playerIn The player using the item
     * @param handIn The hand
     * @param cir The callback
     * @param playerStack The ItemStack used from the hand
     * @param result The raytrace validated using the item
     * @param targetPos The target position of the dungeon
     * @param enderEye The ender eye being spawned
     */
    @Inject(method = "onItemRightClick(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;addEntity(Lnet/minecraft/entity/Entity;)Z"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/entity/item/EyeOfEnderEntity;moveTowards(Lnet/minecraft/util/math/BlockPos;)V"
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/advancements/CriteriaTriggers;USED_ENDER_EYE:Lnet/minecraft/advancements/criterion/UsedEnderEyeTrigger;",
                opcode = Opcodes.GETSTATIC
            )
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void impl$setShooter(final World worldIn, final PlayerEntity playerIn, final Hand handIn,
        final CallbackInfoReturnable<ActionResult<ItemStack>> cir, final ItemStack playerStack, final RayTraceResult result,
        final BlockPos targetPos, final EyeOfEnderEntity enderEye) {
        if (((WorldBridge) worldIn).bridge$isFake()) {
            return;
        }
        ((EyeOfEnder) enderEye).offer(Keys.SHOOTER, (ProjectileSource) playerIn);
    }

    /**
     * The RayTraceResult is lost, and somehow, production JVM will shove the CallbackInfoReturnable
     * into the LVT.... So.... Don't care which one is actually on the stack, it might be the one from
     * {@link #implThrowForPreEvent(World, EntityPlayer, EnumHand, CallbackInfoReturnable, ItemStack, BlockPos)}
     * or some other injection. Either way, this one works in production.
     */
    @Surrogate
    private void implSetShooter(final World worldIn, final PlayerEntity playerIn, final Hand handIn,
        final CallbackInfoReturnable<ActionResult<ItemStack>> cir, final ItemStack playerStack,
        final BlockPos targetPos, final EyeOfEnderEntity enderEye, final CallbackInfoReturnable<ActionResult<ItemStack>> preEventCir) {
        if (((WorldBridge) worldIn).bridge$isFake()) {
            return;
        }
        ((EyeOfEnder) enderEye).offer(Keys.SHOOTER, (ProjectileSource) playerIn);
    }

}
