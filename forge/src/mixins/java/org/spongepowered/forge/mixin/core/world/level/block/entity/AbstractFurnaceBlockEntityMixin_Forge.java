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
package org.spongepowered.forge.mixin.core.world.level.block.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.entity.carrier.furnace.FurnaceBlockEntity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.CookingEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.block.entity.AbstractFurnaceBlockEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.general.RecipeContext;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.world.level.block.entity.BaseContainerBlockEntityMixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin_Forge extends BaseContainerBlockEntityMixin implements AbstractFurnaceBlockEntityBridge {

    // @formatter:off
    @Shadow protected NonNullList<ItemStack> items;
    @Shadow private int cookingTimer;
    // @formatter:on

    private static final ThreadLocal<RecipeContext> switched = new ThreadLocal<>();
    private boolean forge$filledWaterBucket;

    // Tick up and Start
    @WrapOperation(method = "serverTick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/core/NonNullList;ILnet/minecraft/world/item/ItemStack;)Z"
        )
    )
    private static boolean forge$checkIfCanSmelt(
        final AbstractFurnaceBlockEntity instance,
        final NonNullList<ItemStack> maxResultCount,
        final int items,
        final ItemStack maxStackSize,
        final Operation<Boolean> original,
        final ServerLevel level,
        final BlockPos pos,
        final BlockState state,
        final AbstractFurnaceBlockEntity entityIn,
        final @Local RecipeHolder<? extends AbstractCookingRecipe> recipe
    ) {
        if (!original.call(instance, maxResultCount, items, maxStackSize)) {
            return false;
        }

        final var cookingRecipe = recipe.value();

        final var entity = (AbstractFurnaceBlockEntityMixin_Forge) (Object) (entityIn);
        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(maxResultCount.get(1));
        final Cause cause = PhaseTracker.getInstance().currentCause();
        final var identifier = recipe.id().identifier();
        // Prepare a phase stack to capture the recipe in use
        final var context = GeneralPhase.State.RECIPE_CRAFTING.createPhaseContext(PhaseTracker.getInstance())
            .recipe(recipe.value())
            .recipeID(identifier)
            .furnace(entityIn)
            .provideItem(maxResultCount.get(1))
            .buildAndSwitch();
        switched.set(context);
        if (entity.cookingTimer == 0) { // Start
            final CookingEvent.Start event = SpongeEventFactory.createCookingEventStart(cause, (FurnaceBlockEntity) entityIn, Optional.of(fuel),
                Optional.of((CookingRecipe) cookingRecipe), Optional.of((ResourceKey) (Object) identifier));
            SpongeCommon.post(event);
            return !event.isCancelled();
        } else { // Tick up
            final ItemStackSnapshot cooking = ItemStackUtil.snapshotOf(entity.items.get(0));
            final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) entityIn, cooking, Optional.of(fuel),
                Optional.of((CookingRecipe) cookingRecipe), Optional.of((ResourceKey) (Object) identifier));
            SpongeCommon.post(event);
            return !event.isCancelled();
        }
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private static void forge$closeRecipeContext(
        final ServerLevel level,
        final BlockPos pos, final BlockState state,
        final AbstractFurnaceBlockEntity entity,
        final CallbackInfo ci) {
        if (switched.get() != null && PhaseTracker.getInstance().getPhaseContext() != switched.get()) {
            SpongeCommon.logger().warn("Closed a recipe context that wasn't open!");
        }
        final var closing = switched.get();
        switched.remove();
        if (closing != null) {
            closing.close();
        }
    }

    // Tick down
    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Ljava/lang/Math;clamp(JII)I"))
    private static int forge$resetCookTimeIfCancelled(
        final long newCookTime, final int zero, final int totalCookTime,
        final ServerLevel level, final BlockPos entityPos, final BlockState state,
        final AbstractFurnaceBlockEntity entityIn) {
        final int clampedCookTime = Math.clamp(newCookTime, zero, totalCookTime);
        final var entity = (AbstractFurnaceBlockEntityMixin_Forge) (Object) entityIn;
        if (PhaseTracker.getInstance().getPhaseContext() != switched.get()) {
            // we're in an invariant state
            return clampedCookTime;
        }
        final var fuel = switched.get().usedItem();
        final Cause cause = PhaseTracker.getInstance().currentCause();
        final var recipe = switched.get().recipe();
        final ItemStackSnapshot cooking = ItemStackUtil.snapshotOf(entity.items.get(0));
        final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) entityIn, cooking, Optional.of(fuel),
           Optional.of((CookingRecipe) recipe), Optional.of((ResourceKey) (Object) switched.get().recipeID()));
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            return entity.cookingTimer; // dont tick down
        }

        return clampedCookTime;
    }

    // Finish
    @Inject(method = "burn", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;WET_SPONGE:Lnet/minecraft/world/item/Item;", opcode = Opcodes.GETSTATIC)
        ))
    private void forge$captureBucketFill(
        final NonNullList<ItemStack> items, final ItemStack inputItemStack, final ItemStack result, final CallbackInfo ci
    ) {
        final Cause cause = PhaseTracker.getInstance().currentCause();
        final FurnaceBlockEntity entity = cause.first(FurnaceBlockEntity.class)
            .orElseThrow(() -> new IllegalStateException("Expected to have a FurnaceBlockEntity in the Cause"));
        ((AbstractFurnaceBlockEntityMixin_Forge) entity).forge$filledWaterBucket = true;
    }

    @Inject(method = "burn", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
        shift = At.Shift.AFTER
    ))
    private void forge$afterSmeltItem(
        final NonNullList<ItemStack> items, final ItemStack inputItemStack, final ItemStack result, final CallbackInfo ci
    ) {
        final ItemStack itemIn = items.get(0);
        final ItemStack itemOut = items.get(2);
        if (PhaseTracker.getInstance().getPhaseContext() != switched.get()) {
            return;
        }

        final Cause cause = PhaseTracker.getInstance().currentCause();
        final FurnaceBlockEntity entity = (FurnaceBlockEntity) switched.get().furnace();
        final var recipe = switched.get().recipe();
        final var recipeID = switched.get().recipeID();
        final List<SlotTransaction> transactions = new ArrayList<>();
        itemIn.grow(1);
        final ItemStackSnapshot originalSmeltItem = ItemStackUtil.snapshotOf(itemIn);
        itemIn.shrink(1);
        transactions.add(new SlotTransaction(entity.inventory().slot(0).get(), originalSmeltItem, ItemStackUtil.snapshotOf(itemIn)));

        final boolean hasFuel = !((AbstractFurnaceBlockEntityMixin_Forge) entity).forge$filledWaterBucket;
        if (((AbstractFurnaceBlockEntityMixin_Forge) entity).forge$filledWaterBucket) {
            transactions.add(new SlotTransaction(entity.inventory().slot(1).get(), ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(items.get(1))));
        }
        ((AbstractFurnaceBlockEntityMixin_Forge) entity).forge$filledWaterBucket = false;

        if (itemOut.isEmpty()) {
            transactions.add(new SlotTransaction(entity.inventory().slot(2).get(), ItemStackSnapshot.empty(), ItemStackUtil.snapshotOf(result)));
        } else if (ItemStack.isSameItemSameComponents(itemOut, result)) {
            itemOut.shrink(1);
            final ItemStackSnapshot originalResult = ItemStackUtil.snapshotOf(itemOut);
            itemOut.grow(1);
            transactions.add(new SlotTransaction(entity.inventory().slot(2).get(), originalResult, ItemStackUtil.snapshotOf(itemOut)));
        }
        final Optional<ItemStackSnapshot> fuel = hasFuel && !items.get(1).isEmpty() ? Optional.of(ItemStackUtil.snapshotOf(items.get(1))) : Optional.empty();
        final CookingEvent.Finish event = SpongeEventFactory.createCookingEventFinish(cause, entity,
            fuel, Optional.of((CookingRecipe) recipe), Optional.of((ResourceKey) (Object) recipeID), Collections.unmodifiableList(transactions));
        SpongeCommon.post(event);

        for (final SlotTransaction transaction : transactions) {
            transaction.custom().ifPresent(item -> items.set(((SlotAdapter) transaction.slot()).getOrdinal(), ItemStackUtil.fromSnapshotToNative(item)));
        }
    }

    // Interrupt-Active - e.g. a player removing the currently smelting item
    @Inject(
        method = "setItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I"
        )
    )
    private void forge$interruptSmelt(final CallbackInfo ci) {
        this.impl$callInterruptSmeltEvent();
    }
}
