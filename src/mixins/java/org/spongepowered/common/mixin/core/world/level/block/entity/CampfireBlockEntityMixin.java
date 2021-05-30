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
package org.spongepowered.common.mixin.core.world.level.block.entity;

import org.spongepowered.api.block.entity.carrier.Campfire;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.CookingEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.block.entity.CampfireBlockEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collections;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements CampfireBlockEntityBridge {

    // @Formatter:off
    @Shadow @Final private NonNullList<ItemStack> items;
    @Shadow @Final private int[] cookingProgress;
    // @Formatter:on

    private final CampfireCookingRecipe[] impl$cookingRecipe = new CampfireCookingRecipe[4];

    // Tick up
    @Inject(method = "cook", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;cookingProgress:[I", ordinal = 1))
    private void impl$canCook(final CallbackInfo ci, final int i, final ItemStack itemStack) {
        final boolean isEmpty = itemStack.isEmpty();
        if (!isEmpty) {
            final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
            final ItemStackSnapshot stack = ItemStackUtil.snapshotOf(this.items.get(i));
            final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (Campfire) this, stack, Optional.empty(),
                    Optional.ofNullable((CookingRecipe) impl$cookingRecipe[i]));
            SpongeCommon.postEvent(event);
            if (event.isCancelled()) {
                this.cookingProgress[i]--;
            }
        }

    }

    @Inject(method = "cook", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private void impl$assembleCampfireResult(final CallbackInfo ci, final int i, final ItemStack itemStack, final int j,
            final Container iInventory, final ItemStack itemStack1, final BlockPos blockPos) {
        this.impl$assembleCampfireResult(ci, i, itemStack, iInventory, itemStack1, blockPos);
    }

    @Surrogate
    private void impl$assembleCampfireResult(final CallbackInfo ci, final int i, final ItemStack itemStack,
            final Container iInventory, final ItemStack itemStack1, final BlockPos blockPos) {
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(itemStack1);
        final CookingEvent.Finish event = SpongeEventFactory.createCookingEventFinish(cause, (Campfire) this,
                Collections.singletonList(snapshot), Optional.empty(), Optional.ofNullable((CookingRecipe) this.impl$cookingRecipe[i]));
        SpongeCommon.postEvent(event);
        this.impl$cookingRecipe[i] = null;
    }

    @Override
    public void bridge$placeRecipe(final CampfireCookingRecipe recipe) {
        for(int i = 0; i < this.items.size(); ++i) {
            ItemStack itemstack = this.items.get(i);
            if (itemstack.isEmpty()) {
                this.impl$cookingRecipe[i] = recipe;
                return;
            }
        }
    }
}
