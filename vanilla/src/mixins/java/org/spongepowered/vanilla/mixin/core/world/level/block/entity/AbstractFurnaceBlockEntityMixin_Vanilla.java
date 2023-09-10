/*
 * This file is part of ${name}, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://spongepowered.org>
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
 *
 */
package org.spongepowered.vanilla.mixin.core.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.block.entity.carrier.furnace.FurnaceBlockEntity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.CookingEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.recipe.cooking.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.UntransformedInvokerError;
import org.spongepowered.common.bridge.block.entity.AbstractFurnaceBlockEntityBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin_Vanilla implements AbstractFurnaceBlockEntityBridge {

    // @formatter:off
    @Shadow protected NonNullList<ItemStack> items;
    @Shadow int cookingProgress;
    @Shadow private static boolean shadow$canBurn(final RegistryAccess registryAccess, @Nullable final Recipe<?> var0, final NonNullList<ItemStack> var1, final int var2) {
        throw new UntransformedInvokerError();
    }
    // @formatter:on


    // Tick up and Start
    @Redirect(method = "serverTick",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z",
            ordinal = 1))
    private static boolean vanillaImpl$checkIfCanSmelt(RegistryAccess registryAccess, @Nullable final Recipe<?> recipe, final NonNullList<ItemStack> slots, final int maxStackSize, final Level level, final BlockPos entityPos, final BlockState state, final AbstractFurnaceBlockEntity entity) {
        if (!AbstractFurnaceBlockEntityMixin_Vanilla.shadow$canBurn(registryAccess, recipe, slots, maxStackSize)) {
            return false;
        }

        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf(slots.get(1));

        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final var $this = (AbstractFurnaceBlockEntityMixin_Vanilla) (Object) (entity);
        if ($this.cookingProgress == 0) { // Start
            final CookingEvent.Start event = SpongeEventFactory.createCookingEventStart(cause, (FurnaceBlockEntity) entity, Optional.of(fuel),
                Optional.of((CookingRecipe) recipe));
            SpongeCommon.post(event);
            return !event.isCancelled();
        } else { // Tick up
            final ItemStackSnapshot cooking = ItemStackUtil.snapshotOf($this.items.get(0));
            final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) entity, cooking, Optional.of(fuel),
                Optional.of((CookingRecipe) recipe));
            SpongeCommon.post(event);
            return !event.isCancelled();
        }
    }

    // Tick down
    @Redirect(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private static int vanillaImpl$resetCookTimeIfCancelled(final int newCookTime, final int zero, final int totalCookTime,
                                                            final Level level, final BlockPos entityPos, final BlockState state, final AbstractFurnaceBlockEntity entity) {
        final int clampedCookTime = Mth.clamp(newCookTime, zero, totalCookTime);
        final var $this = (AbstractFurnaceBlockEntityMixin_Vanilla) (Object) entity;
        final ItemStackSnapshot fuel = ItemStackUtil.snapshotOf($this.items.get(1));
        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
        final AbstractCookingRecipe recipe = $this.impl$getCurrentRecipe();
        final ItemStackSnapshot cooking = ItemStackUtil.snapshotOf($this.items.get(0));
        final CookingEvent.Tick event = SpongeEventFactory.createCookingEventTick(cause, (FurnaceBlockEntity) entity, cooking, Optional.of(fuel),
            Optional.of((CookingRecipe) recipe));
        SpongeCommon.post(event);
        if (event.isCancelled()) {
            return $this.cookingProgress; // dont tick down
        }

        return clampedCookTime;
    }

    // Finish
    @Inject(
        method = "burn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private static void vanillaImpl$afterSmeltItem(
        final RegistryAccess registryAccess, final Recipe<?> recipe,
        final NonNullList<ItemStack> slots, final int var2, final CallbackInfoReturnable<Boolean> cir
    ) {
        final var fuel = ItemStackUtil.snapshotOf(slots.get(1));
        final var cause = PhaseTracker.getCauseStackManager().currentCause();
        final var entity = cause.first(FurnaceBlockEntity.class)
            .orElseThrow(() -> new IllegalStateException("Expected to have a FurnaceBlockEntity in the Cause"));
        final var snapshot = ItemStackUtil.snapshotOf(recipe.getResultItem(registryAccess));
        final var event = SpongeEventFactory.createCookingEventFinish(cause, entity,
            Collections.singletonList(snapshot), Optional.of(fuel), Optional.ofNullable((CookingRecipe) recipe));
        SpongeCommon.post(event);
    }
}
