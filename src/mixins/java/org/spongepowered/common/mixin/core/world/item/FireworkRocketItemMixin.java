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

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.FireworkRocket;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.math.vector.Vector3d;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemMixin {

    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
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
     * @param stack The ItemStack used from the hand
     */
    @Inject(
        method = "use",
        at = @At(
            value = "NEW",
            target = "net/minecraft/world/entity/projectile/FireworkRocketEntity"
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void impl$throwPreBeforeSpawning(final Level worldIn, final net.minecraft.world.entity.player.Player playerIn, final InteractionHand handIn,
        final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, final ItemStack stack) {
        if (this.impl$throwConstructPreEvent(worldIn, playerIn, stack)) {
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.SUCCESS, stack));
        }
    }


    /**
     * @author gabizou - June 10th, 2019 - 1.12.2
     * @reason We can throw a construct pre event here before the
     * entity is actually constructed, and if the event is cancelled,
     * we can still return the correct itemstack. If the event is
     * cancelled, we end up not shrinking the itemstack, but we will
     * make sure to notify the player at the end of the packet being
     * processed.
     *
     * @param context The player using the item
     * @param cir The callback
     */
    @Inject(method = "useOn",
        at = @At(value = "NEW", target = "net/minecraft/world/entity/projectile/FireworkRocketEntity"),
        cancellable = true)
    private void impl$throwPrimeEventsIfCancelled(final UseOnContext context, final CallbackInfoReturnable<InteractionResult> cir) {
        if (this.impl$throwConstructPreEvent(context.getLevel(), context.getPlayer(), context.getItemInHand())) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    /**
     * Private method for bridging the duplicate between
     * {@link #spongeImpl$ThrowPreBeforeSpawning(World, PlayerEntity, Hand, CallbackInfoReturnable, ItemStack)} and
     * {@link #spongeImpl$ThrowPrimeEventsIfCancelled(ItemUseContext, CallbackInfoReturnable)}
     * since both follow the same logic, but differ in how they are called.
     *
     * @param world The world
     * @param player The player
     * @param usedItem The used item
     * @return True if the event is cancelled and the callback needs to be cancelled
     */
    private boolean impl$throwConstructPreEvent(final Level world, final net.minecraft.world.entity.player.Player player, final ItemStack usedItem) {
        if (ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE && !((WorldBridge) world).bridge$isFake()) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(usedItem));
                frame.addContext(EventContextKeys.PROJECTILE_SOURCE, (ProjectileSource) player);
                frame.pushCause(player);
                final ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(frame.currentCause(),
                        ServerLocation.of((ServerWorld) world, player.getX(), player.getY(), player.getZ()), new Vector3d(0, 0, 0),
                        EntityTypes.FIREWORK_ROCKET.get());
                return SpongeCommon.post(event);
            }
        }
        return false;
    }

    @Inject(method = "useOn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void impl$injectPrimeEventAndCancel(final UseOnContext context, final CallbackInfoReturnable<InteractionResult> cir, final Level world,
        final ItemStack usedItem, final net.minecraft.world.phys.Vec3 vec3d, final Direction direction, final FireworkRocketEntity rocket) {
        if (this.impl$throwPrimeEventAndGetCancel(context.getLevel(), context.getPlayer(), rocket, usedItem)) {
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    private FireworkRocketEntity impl$capturedRocket;

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean impl$captureFireworkRocket(Level world, Entity p_217376_1_) {
        this.impl$capturedRocket = (FireworkRocketEntity) p_217376_1_;
        return true;
    }

    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER),
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true
    )
    private void impl$injectPrimeEventAndCancel(final Level worldIn, final net.minecraft.world.entity.player.Player player, final InteractionHand handIn,
        final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, final ItemStack usedItem) {
        if (this.impl$throwPrimeEventAndGetCancel(worldIn, player, this.impl$capturedRocket, usedItem)) {
            this.impl$capturedRocket = null;
            // We have to still return success because the server/client can get out of sync otherwise.
            cir.setReturnValue(new InteractionResultHolder<>(InteractionResult.SUCCESS, usedItem));
        }

        worldIn.addFreshEntity(this.impl$capturedRocket);
        this.impl$capturedRocket = null;
    }

    /**
     * Private method for throwing the prime events on the firework. If
     * the prime is cancelled, then the firework will not be spawned.
     * This is to bridge the same logic between
     *
     * {@link #impl$injectPrimeEventAndCancel(UseOnContext, CallbackInfoReturnable, Level, ItemStack, net.minecraft.world.phys.Vec3, Direction, FireworkRocketEntity)}
     * {@link #impl$injectPrimeEventAndCancel(Level, net.minecraft.world.entity.player.Player, InteractionHand, CallbackInfoReturnable, ItemStack)}
     *
     * @param world The world
     * @param player The player using the item
     * @param rocket The rocket
     * @param usedItem The used item
     * @return True if the event is cancelled and the rocket should not be spawned
     */
    private boolean impl$throwPrimeEventAndGetCancel(final Level world, final net.minecraft.world.entity.player.Player player, final FireworkRocketEntity rocket,
            final ItemStack usedItem) {
        if (((WorldBridge) world).bridge$isFake() ) {
            return false;
        }
        ((FireworkRocket) rocket).offer(Keys.SHOOTER, (Player) player);
        if (ShouldFire.PRIME_EXPLOSIVE_EVENT_PRE) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(usedItem));
                frame.addContext(EventContextKeys.PROJECTILE_SOURCE, (ProjectileSource) player);
                frame.pushCause(player);
                if (!((FusedExplosiveBridge) rocket).bridge$shouldPrime()) {
                    return true;
                }
            }
        }
        return false;
    }

}
