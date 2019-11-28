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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

@Mixin(ItemFirework.class)
public class ItemFireworkMixin extends Item {

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
        method = "onItemRightClick",
        at = @At(
            value = "NEW",
            target = "net/minecraft/entity/item/EntityFireworkRocket"
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void spongeImpl$ThrowPreBeforeSpawning(World worldIn, PlayerEntity playerIn, Hand handIn,
        CallbackInfoReturnable<ActionResult<ItemStack>> cir, ItemStack stack) {
        if (spongeImpl$ThrowConstructPreEvent(worldIn, playerIn, stack)) {
            cir.setReturnValue(new ActionResult<>(ActionResultType.SUCCESS, stack));
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
     * @param player The player using the item
     * @param worldIn The world
     * @param pos The block position
     * @param hand The hand
     * @param facing The block face being clicked on
     * @param hitX The hit position
     * @param hitY The hit position
     * @param hitZ The hit position
     * @param cir The callback
     * @param stack The ItemStack used from the hand
     */
    @Inject(method = "onItemUse",
        at = @At(value = "NEW", target = "net/minecraft/entity/item/EntityFireworkRocket"),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void spongeImpl$ThrowPrimeEventsIfCancelled(PlayerEntity player, World worldIn, BlockPos pos, Hand hand, Direction facing,
        float hitX, float hitY, float hitZ, CallbackInfoReturnable<ActionResultType> cir, ItemStack stack) {
        if (spongeImpl$ThrowConstructPreEvent(worldIn, player, stack)) {
            cir.setReturnValue(ActionResultType.SUCCESS);
        }

    }

    /**
     * Private method for bridging the duplicate between
     * {@link #onItemRightClick(World, EntityPlayer, EnumHand)} and
     * {@link #onItemUse(EntityPlayer, World, BlockPos, EnumHand, EnumFacing, float, float, float)}
     * since both follow the same logic, but differ in how they are called.
     *
     * @param world The world
     * @param player The player
     * @param usedItem The used item
     * @return True if the event is cancelled and the callback needs to be cancelled
     */
    private boolean spongeImpl$ThrowConstructPreEvent(World world, PlayerEntity player, ItemStack usedItem) {
        if (ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE && !((WorldBridge) world).bridge$isFake()) {
            final Vector3d targetPosition = new Vector3d(player.posX, player.posY , player.posZ);
            final Transform<org.spongepowered.api.world.World> targetTransform = new Transform<>((org.spongepowered.api.world.World) world,
                targetPosition);
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(usedItem));
                frame.addContext(EventContextKeys.PROJECTILE_SOURCE, (ProjectileSource) player);
                frame.pushCause(player);
                ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Sponge.getCauseStackManager().getCurrentCause(),
                    EntityTypes.FIREWORK, targetTransform);
                return SpongeImpl.postEvent(event);
            }
        }
        return false;
    }

    @Inject(method = "onItemUse",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void spongeImpl$InjectPrimeEventAndCancel(PlayerEntity player, World worldIn, BlockPos pos, Hand hand, Direction facing, float hitX,
        float hitY, float hitZ, CallbackInfoReturnable<ActionResultType> cir, ItemStack usedItem, FireworkRocketEntity rocket) {
        if (spongeImpl$ThrowPrimeEventAndGetCancel(worldIn, player, rocket, usedItem)) {
            cir.setReturnValue(ActionResultType.SUCCESS);
        }
    }

    @Inject(method = "onItemRightClick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
        locals = LocalCapture.CAPTURE_FAILSOFT,
        cancellable = true
    )
    private void spongeImpl$InjectPrimeEventAndCancel(World worldIn, PlayerEntity player, Hand handIn,
        CallbackInfoReturnable<ActionResult<ItemStack>> cir, ItemStack usedItem, FireworkRocketEntity rocket) {
        if (spongeImpl$ThrowPrimeEventAndGetCancel(worldIn, player, rocket, usedItem)) {
            // We have to still return success because the server/client can get out of sync otherwise.
            cir.setReturnValue(new ActionResult<>(ActionResultType.SUCCESS, usedItem));
        }
    }

    /**
     * Private method for throwing the prime events on the firework. If
     * the prime is cancelled, then the firework will not be spawned.
     * This is to bridge the same logic between
     * {@link #onItemUse(EntityPlayer, World, BlockPos, EnumHand, EnumFacing, float, float, float)}
     * {@link #onItemRightClick(World, EntityPlayer, EnumHand)}.
     *
     * @param world The world
     * @param player The player using the item
     * @param rocket The rocket
     * @param usedItem The used item
     * @return True if the event is cancelled and the rocket should not be spawned
     */
    private boolean spongeImpl$ThrowPrimeEventAndGetCancel(World world, PlayerEntity player, FireworkRocketEntity rocket, ItemStack usedItem) {
        if (((WorldBridge) world).bridge$isFake() ) {
            return false;
        }
        ((Firework) rocket).setShooter((Player) player);
        if (ShouldFire.PRIME_EXPLOSIVE_EVENT_PRE) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
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
