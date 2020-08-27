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
package org.spongepowered.common.mixin.core.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.block.entity.carrier.furnace.FurnaceBlockEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.SmeltEvent;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.smelting.SmeltingRecipe;
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

@Mixin(AbstractFurnaceTileEntity.class)
public abstract class AbstractFurnaceTileEntityMixin extends LockableTileEntityMixin {

    @Shadow protected NonNullList<ItemStack> items;
    @Shadow private int cookTime;
    @Shadow private int burnTime;

    @Shadow protected abstract boolean shadow$canSmelt(IRecipe<?> iRecipe);

    @Shadow @Final protected IRecipeType<? extends AbstractCookingRecipe> recipeType;

    // Shrink Fuel
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V"))
    private void impl$throwFuelEventIfOrShrink(final ItemStack itemStack, final int quantity) {
        final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();

        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(itemStack);
        final ItemStackSnapshot shrinkedFuel = ItemStackUtil.snapshotOf(ItemStackUtil.cloneDefensive(itemStack, itemStack.getCount() - 1));

        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(fuel, shrinkedFuel);
        final AbstractCookingRecipe recipe = this.impl$getCurrentRecipe();
        final SmeltEvent.ConsumeFuel event = SpongeEventFactory.createSmeltEventConsumeFuel(cause, fuel, (FurnaceBlockEntity) this,
                Optional.of((SmeltingRecipe) recipe), Collections.singletonList(transaction));
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            this.burnTime = 0;
            return;
        }

        if (!transaction.isValid()) {
            return;
        }

        if (transaction.getCustom().isPresent()) {
            this.items.set(1, ItemStackUtil.fromSnapshotToNative(transaction.getFinal()));
        } else { // vanilla
            itemStack.shrink(quantity);
        }
    }

    private AbstractCookingRecipe impl$getCurrentRecipe() {
        return this.world.getRecipeManager().getRecipe((IRecipeType<AbstractCookingRecipe>) this.recipeType,
                (AbstractFurnaceTileEntity) (Object) this, this.world).orElse(null);
    }

    // Tick up and Start
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/AbstractFurnaceTileEntity;canSmelt(Lnet/minecraft/item/crafting/IRecipe;)Z", ordinal = 1))
    private boolean impl$checkIfCanSmelt(final AbstractFurnaceTileEntity furnace, final IRecipe<?> recipe) {
        if (!this.shadow$canSmelt(recipe)) {
            return false;
        }

        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));

        final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();
        if (this.cookTime == 0) { // Start
            final SmeltEvent.Start event = SpongeEventFactory.createSmeltEventStart(cause, fuel, (FurnaceBlockEntity) this, Optional.of((SmeltingRecipe) recipe), Collections.emptyList());
            SpongeCommon.postEvent(event);
            return !event.isCancelled();

        } else { // Tick up
            final SmeltEvent.Tick event = SpongeEventFactory.createSmeltEventTick(cause, fuel, (FurnaceBlockEntity) this, Optional.of((SmeltingRecipe) recipe), Collections.emptyList());
            SpongeCommon.postEvent(event);
            return !event.isCancelled();
        }
    }

    // Tick down
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"))
    private int impl$resetCookTimeIfCancelled(final int newCookTime, final int zero, final int totalCookTime) {
        final int clampedCookTime = MathHelper.clamp(newCookTime, zero, totalCookTime);
        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));
        final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();
        final AbstractCookingRecipe recipe = this.impl$getCurrentRecipe();
        final SmeltEvent.Tick event = SpongeEventFactory.createSmeltEventTick(cause, fuel, (FurnaceBlockEntity) this, Optional.of((SmeltingRecipe) recipe), Collections.emptyList());
        SpongeCommon.postEvent(event);
        if (event.isCancelled()) {
            return this.cookTime; // dont tick down
        }

        return clampedCookTime;
    }

    // Interrupt-Active - e.g. a player removing the currently smelting item
    @Inject(
        method = "setInventorySlotContents",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/AbstractFurnaceTileEntity;getCookTime()I"
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
            target = "Lnet/minecraft/tileentity/AbstractFurnaceTileEntity;cookTime:I"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/tileentity/AbstractFurnaceTileEntity;smelt(Lnet/minecraft/item/crafting/IRecipe;)V"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"
            )
        )
    )
    private void impl$onResetCookTimePassive(final CallbackInfo ci) {
        this.impl$callInteruptSmeltEvent();
    }

    private void impl$callInteruptSmeltEvent() {
        if (this.cookTime > 0) {
            final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));
            final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();
            final AbstractCookingRecipe recipe = this.impl$getCurrentRecipe();
            final SmeltEvent.Interrupt event = SpongeEventFactory.createSmeltEventInterrupt(cause, fuel, (FurnaceBlockEntity) this, Optional.ofNullable(recipe));
            SpongeCommon.postEvent(event);
        }
    }

    // Finish
    @Inject(
        method = "smelt",
        locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;shrink(I)V"))
    private void impl$afterSmeltItem(final IRecipe<?> recipe, final CallbackInfo ci) {
        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));
        final Cause cause = PhaseTracker.getCauseStackManager().getCurrentCause();
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(recipe.getRecipeOutput());
        final SmeltEvent.Finish event = SpongeEventFactory.createSmeltEventFinish(cause, fuel, (FurnaceBlockEntity) this,
                Optional.ofNullable(recipe), Collections.singletonList(snapshot));
        SpongeCommon.postEvent(event);
    }

}
