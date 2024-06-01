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

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.entity.carrier.Campfire;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.CookingEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.MixinTargetHelper;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.block.entity.CampfireBlockEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.Optional;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin implements CampfireBlockEntityBridge {

    // @Formatter:off
    @Shadow @Final private NonNullList<ItemStack> items;
    @Shadow @Final private int[] cookingProgress;
    @Shadow @Final private int[] cookingTime;
    // @Formatter:on

    private final RecipeHolder[] impl$cookingRecipe = new RecipeHolder[4];
    private int impl$currentIndex;
    private SlotTransaction impl$pendingSlotTransaction;

    // Tick up
    @Inject(method = "cookTick", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;cookingProgress:[I", ordinal = 1))
    private static void impl$canCook(final Level level, final BlockPos pos, final BlockState state, final CampfireBlockEntity self,
        final CallbackInfo ci, final boolean hasChanged, final int i, final ItemStack itemStack) {
        ((CampfireBlockEntityMixin) (Object) self).impl$currentIndex = i;
        final boolean isEmpty = itemStack.isEmpty();
        if (!isEmpty) {
            final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
            final CampfireBlockEntityMixin mixinSelf = MixinTargetHelper.cast(self);
            final ItemStackSnapshot stack = ItemStackUtil.snapshotOf(mixinSelf.items.get(i));
            final RecipeHolder<?> recipe = mixinSelf.impl$cookingRecipe[i];
            final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (Campfire) self, stack, Optional.empty(),
                    Optional.ofNullable(recipe).map(r -> (CookingRecipe) r.value()), Optional.ofNullable(recipe).map(r -> (ResourceKey) (Object) r.id()));
            SpongeCommon.post(event);
            if (event.isCancelled()) {
                ((CampfireBlockEntityMixin) (Object) self).cookingProgress[i]--;
            }
        }

    }

    @Inject(method = "cookTick", locals = LocalCapture.CAPTURE_FAILEXCEPTION, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private static void impl$assembleCampfireResult(final Level level, final BlockPos pos, final BlockState state,
        final CampfireBlockEntity self, final CallbackInfo ci, final boolean hasChanged, final int i,
        final ItemStack itemStack, final SingleRecipeInput recipeInput, final ItemStack itemStack1) {
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final CampfireBlockEntityMixin mixinSelf = MixinTargetHelper.cast(self);
        final SlotTransaction transaction = new SlotTransaction(((Campfire) self).inventory().slot(i).get(), ItemStackUtil.snapshotOf(itemStack1), ItemStackSnapshot.empty());
        final RecipeHolder<?> recipe = mixinSelf.impl$cookingRecipe[i];
        final CookingEvent.Finish event = SpongeEventFactory.createCookingEventFinish(cause, (Campfire) self,
            Optional.empty(), Optional.ofNullable(recipe).map(r -> (CookingRecipe) r.value()), Optional.ofNullable(recipe).map(r -> (ResourceKey) (Object) r.id()),
            Collections.singletonList(transaction));
        SpongeCommon.post(event);
        mixinSelf.impl$cookingRecipe[i] = null;
        mixinSelf.impl$pendingSlotTransaction = transaction;
    }

    @Inject(method = "cookTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;sendBlockUpdated(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;I)V"))
    private static void impl$setCookingEventFinishResult(final Level $$0, final BlockPos $$1, final BlockState $$2, final CampfireBlockEntity $$3, final CallbackInfo ci) {
        final CampfireBlockEntityMixin mixinSelf = MixinTargetHelper.cast($$3);
        mixinSelf.impl$pendingSlotTransaction.custom().ifPresent(item ->
            mixinSelf.items.set(((SlotAdapter) mixinSelf.impl$pendingSlotTransaction.slot()).getOrdinal(), ItemStackUtil.fromSnapshotToNative(item)));
        mixinSelf.impl$pendingSlotTransaction = null;
    }

    @Override
    public void bridge$placeRecipe(final RecipeHolder<CampfireCookingRecipe> recipe) {
        for(int i = 0; i < this.items.size(); ++i) {
            final ItemStack itemstack = this.items.get(i);
            if (itemstack.isEmpty()) {
                this.impl$cookingRecipe[i] = recipe;
                return;
            }
        }
    }

    @Redirect(method = "cookTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"))
    private static boolean impl$checkInfiniteCookingTime(final ItemStack instance, final FeatureFlagSet featureFlagSet, final Level level, final BlockPos blockPos,
                                  final BlockState state, final CampfireBlockEntity self) {
        final int cookingTime =
                ((CampfireBlockEntityMixin) (Object) self).cookingTime[((CampfireBlockEntityMixin) (Object) self).impl$currentIndex];
        if (cookingTime != Constants.TickConversions.INFINITE_TICKS) {
            return instance.isItemEnabled(featureFlagSet);
        }
        return false;
    }
}
