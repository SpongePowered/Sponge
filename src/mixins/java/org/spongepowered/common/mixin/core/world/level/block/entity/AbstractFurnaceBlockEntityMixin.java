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

import org.objectweb.asm.Opcodes;
import org.spongepowered.api.block.entity.carrier.furnace.FurnaceBlockEntity;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collections;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BaseContainerBlockEntityMixin {

    // @Formatter:off
    @Shadow protected NonNullList<ItemStack> items;
    @Shadow private int cookingProgress;
    @Shadow private int cookingTotalTime;
    @Shadow @Final protected RecipeType<? extends AbstractCookingRecipe> recipeType;

    @Shadow protected abstract boolean shadow$canBurn(final Recipe<?> recipe);
    // @Formatter:on

    // Shrink Fuel
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void impl$throwFuelEventIfOrShrink(final ItemStack itemStack, final int quantity) {
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();

        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(itemStack);
        final ItemStackSnapshot shrinkedFuel = ItemStackUtil.snapshotOf(ItemStackUtil.cloneDefensive(itemStack, itemStack.getCount() - 1));

        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(fuel, shrinkedFuel);
        final AbstractCookingRecipe recipe = this.impl$getCurrentRecipe();
        final CookingEvent.ConsumeFuel event = SpongeEventFactory.createCookingEventConsumeFuel(cause, (FurnaceBlockEntity) this, Optional.of(fuel),
                Optional.of((CookingRecipe) recipe), Collections.singletonList(transaction));
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            this.cookingTotalTime = 0;
            return;
        }

        if (!transaction.isValid()) {
            return;
        }

        if (transaction.custom().isPresent()) {
            this.items.set(1, ItemStackUtil.fromSnapshotToNative(transaction.finalReplacement()));
        } else { // vanilla
            itemStack.shrink(quantity);
        }
    }

    private AbstractCookingRecipe impl$getCurrentRecipe() {
        return this.level.getRecipeManager().getRecipeFor((RecipeType<AbstractCookingRecipe>) this.recipeType,
                (AbstractFurnaceBlockEntity) (Object) this, this.level).orElse(null);
    }

    // Tick up and Start
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/world/item/crafting/Recipe;)Z", ordinal = 1))
    private boolean impl$checkIfCanSmelt(final AbstractFurnaceBlockEntity furnace, final Recipe<?> recipe) {
        if (!this.shadow$canBurn(recipe)) {
            return false;
        }

        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));

        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        if (this.cookingProgress == 0) { // Start
            final CookingEvent.Start event = SpongeEventFactory.createCookingEventStart(cause, (FurnaceBlockEntity) this, Optional.of(fuel),
                    Optional.of((CookingRecipe) recipe));
            SpongeCommon.postEvent(event);
            return !event.isCancelled();
        } else { // Tick up
            final ItemStackSnapshot stack = ItemStackUtil.snapshotOf(this.items.get(0));
            final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) this, stack, Optional.of(fuel),
                    Optional.of((CookingRecipe) recipe));
            SpongeCommon.postEvent(event);
            return !event.isCancelled();
        }
    }

    // Tick down
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private int impl$resetCookTimeIfCancelled(final int newCookTime, final int zero, final int totalCookTime) {
        final int clampedCookTime = Mth.clamp(newCookTime, zero, totalCookTime);
        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final AbstractCookingRecipe recipe = this.impl$getCurrentRecipe();
        final ItemStackSnapshot stack = ItemStackUtil.snapshotOf(this.items.get(0));
        final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) this, stack, Optional.of(fuel),
                Optional.of((CookingRecipe) recipe));
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            return this.cookingProgress; // dont tick down
        }

        return clampedCookTime;
    }

    // Interrupt-Active - e.g. a player removing the currently smelting item
    @Inject(
        method = "setItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime()I"
        )
    )
    private void impl$interruptSmelt(final CallbackInfo ci) {
        this.impl$callInteruptSmeltEvent();
    }

    // Interrupt-Passive - if the currently smelting item was removed in some other way
    @Inject(method = "tick",
        at = @At(
            shift = At.Shift.BEFORE,
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingProgress:I"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/world/item/crafting/Recipe;)V"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/Mth;clamp(III)I"
            )
        )
    )
    private void impl$onResetCookTimePassive(final CallbackInfo ci) {
        this.impl$callInteruptSmeltEvent();
    }

    private void impl$callInteruptSmeltEvent() {
        if (this.cookingProgress > 0) {
            final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));
            final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
            final AbstractCookingRecipe recipe = this.impl$getCurrentRecipe();
            final CookingEvent.Interrupt event = SpongeEventFactory.createCookingEventInterrupt(cause, (FurnaceBlockEntity) this, Optional.of(fuel),
                                                                                            Optional.ofNullable((CookingRecipe) recipe));
            SpongeCommon.postEvent(event);
        }
    }

    // Finish
    @Inject(
        method = "burn",
        locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void impl$afterSmeltItem(final Recipe<?> recipe, final CallbackInfo ci) {
        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(recipe.getResultItem());
        final CookingEvent.Finish event = SpongeEventFactory.createCookingEventFinish(cause, (FurnaceBlockEntity) this,
                Collections.singletonList(snapshot), Optional.of(fuel), Optional.ofNullable((CookingRecipe) recipe));
        SpongeCommon.postEvent(event);
    }

    // Custom recipes
//    @Redirect(method = "smelt", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/IRecipe;getRecipeOutput()Lnet/minecraft/item/ItemStack;"))
//    public ItemStack impl$getSmeltRecipeOutput(IRecipe<?> recipe) {
//        return ((AbstractCookingRecipe) recipe).getCraftingResult((IInventory) this);
//    }
//
//    @Redirect(method = "canSmelt", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/crafting/IRecipe;getRecipeOutput()Lnet/minecraft/item/ItemStack;"))
//    public ItemStack impl$getCanSmeltRecipeOutput(IRecipe<?> recipe) {
//        return ((AbstractCookingRecipe) recipe).getCraftingResult((IInventory) this);
//    }

}
