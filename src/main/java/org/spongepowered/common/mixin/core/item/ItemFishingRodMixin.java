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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;

import javax.annotation.Nullable;

@Mixin(ItemFishingRod.class)
public abstract class ItemFishingRodMixin extends Item {

    @Nullable private EntityFishHook fishHook;

    @Inject(method = "onItemRightClick", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/entity/projectile/EntityFishHook;handleHookRetraction()I"), cancellable = true)
    private void cancelHookRetraction(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        if (player.field_71104_cf != null) {
            // Event was cancelled
            cir.setReturnValue(new ActionResult<>(EnumActionResult.SUCCESS, player.func_184586_b(hand)));
        }
    }

    @Inject(method = "onItemRightClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound"
            + "(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", ordinal = 1),
            cancellable = true)
    private void onThrowEvent(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        if (world.field_72995_K) {
            // Only fire event on server-side to avoid crash on client
            return;
        }

        EntityFishHook fishHook = new EntityFishHook(world, player);
        Sponge.getCauseStackManager().pushCause(player);
        if (SpongeImpl.postEvent(SpongeEventFactory.createFishingEventStart(Sponge.getCauseStackManager().getCurrentCause(), (FishHook) fishHook))) {
            fishHook.func_70106_y(); // Bye
            cir.setReturnValue(new ActionResult<>(EnumActionResult.SUCCESS, player.func_184586_b(hand)));
        } else {
            this.fishHook = fishHook;
        }
        Sponge.getCauseStackManager().popCause();
    }

    @Redirect(method = "onItemRightClick", at = @At(value = "NEW", target = "net/minecraft/entity/projectile/EntityFishHook"))
    private EntityFishHook onNewEntityFishHook(World world, EntityPlayer player) {
        // Use the fish hook we created for the event
        EntityFishHook fishHook = this.fishHook;
        this.fishHook = null;
        return fishHook;
    }

}
