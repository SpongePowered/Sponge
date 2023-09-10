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
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.block.entity.AbstractFurnaceBlockEntityAccessor;
import org.spongepowered.common.bridge.block.entity.AbstractFurnaceBlockEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin extends BaseContainerBlockEntityMixin implements AbstractFurnaceBlockEntityBridge {

    // @Formatter:off
    @Shadow protected NonNullList<ItemStack> items;
    @Shadow int cookingProgress;
    @Shadow int cookingTotalTime;

    // @Formatter:on

    @Shadow @Final private RecipeManager.CachedCheck<Container, ? extends AbstractCookingRecipe> quickCheck;

    // Shrink Fuel
    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void impl$throwFuelEventIfOrShrink(final ItemStack itemStack, final int quantity, final Level var0, final BlockPos var1, final BlockState var2, final AbstractFurnaceBlockEntity entity) {
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();

        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(itemStack);
        final ItemStackSnapshot shrinkedFuel = ItemStackUtil.snapshotOf(ItemStackUtil.cloneDefensive(itemStack, itemStack.getCount() - 1));

        final Transaction<ItemStackSnapshot> transaction = new Transaction<>(fuel, shrinkedFuel);
        final AbstractCookingRecipe recipe = ((AbstractFurnaceBlockEntityMixin) (Object) entity).impl$getCurrentRecipe();
        final CookingEvent.ConsumeFuel event = SpongeEventFactory.createCookingEventConsumeFuel(cause, (FurnaceBlockEntity) entity, Optional.of(fuel),
                Optional.of((CookingRecipe) recipe), Collections.singletonList(transaction));
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            ((AbstractFurnaceBlockEntityMixin) (Object) entity).cookingTotalTime = 0;
            return;
        }

        if (!transaction.isValid()) {
            return;
        }

        if (transaction.custom().isPresent()) {
            ((AbstractFurnaceBlockEntityMixin) (Object) entity).items.set(1, ItemStackUtil.fromSnapshotToNative(transaction.finalReplacement()));
        } else { // vanilla
            itemStack.shrink(quantity);
        }
    }

    @Override
    public AbstractCookingRecipe impl$getCurrentRecipe() {
        return this.quickCheck.getRecipeFor((AbstractFurnaceBlockEntity) (Object) this, this.level).orElse(null);
    }

    // Interrupt-Active - e.g. a player removing the currently smelting item
    @Inject(
        method = "setItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;getTotalCookTime(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I"
        )
    )
    private void impl$interruptSmelt(final CallbackInfo ci) {
        this.impl$callInteruptSmeltEvent();
    }

    // Interrupt-Passive - if the currently smelting item was removed in some other way
    @Inject(method = "serverTick",
        at = @At(
            shift = At.Shift.BEFORE,
            value = "FIELD",
            opcode = Opcodes.PUTFIELD,
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;cookingProgress:I"
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z"
            ),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/util/Mth;clamp(III)I"
            )
        )
    )
    private static void impl$onResetCookTimePassive(final Level level, final BlockPos pos, final BlockState state,
        @Coerce final AbstractFurnaceBlockEntityMixin entity, final CallbackInfo ci) {
        entity.impl$callInteruptSmeltEvent();
    }

    private void impl$callInteruptSmeltEvent() {
        if (this.cookingProgress > 0) {
            final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(this.items.get(1));
            final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
            final AbstractCookingRecipe recipe = this.impl$getCurrentRecipe();
            final CookingEvent.Interrupt event = SpongeEventFactory.createCookingEventInterrupt(cause, (FurnaceBlockEntity) this, Optional.of(fuel),
                                                                                            Optional.ofNullable((CookingRecipe) recipe));
            SpongeCommon.post(event);
        }
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
