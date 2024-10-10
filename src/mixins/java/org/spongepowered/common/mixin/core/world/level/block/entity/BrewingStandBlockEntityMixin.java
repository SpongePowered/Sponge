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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.carrier.BrewingStand;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.BrewingEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.accessor.world.level.block.entity.BrewingStandBlockEntityAccessor;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.slots.SlotAdapter;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    // @formatter:off
    @Shadow int brewTime;
    @Shadow int fuel;
    @Shadow private NonNullList<ItemStack> items;
    @Shadow private Item ingredient;
    // @formatter:on

    private ItemStack[] impl$originalSlots = new ItemStack[4];

    @Inject(method = "serverTick",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable(Lnet/minecraft/world/item/alchemy/PotionBrewing;Lnet/minecraft/core/NonNullList;)Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;setChanged(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private static void impl$onConsumeFuel(final Level param0, final BlockPos param1, final BlockState param2, final BrewingStandBlockEntity param3,
                                           final CallbackInfo ci, final ItemStack fuelStack) {
        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        fuelStack.grow(1);
        final ItemStackSnapshot originalStack = ItemStackUtil.snapshotOf(fuelStack);
        fuelStack.shrink(1);
        final Lens lens = ((InventoryAdapter) param3).inventoryAdapter$getRootLens();
        final SlotTransaction fuelTransaction = new SlotTransaction(
            (Slot) lens.getLens(4).getAdapter(((InventoryAdapter) param3).inventoryAdapter$getFabric(), ((BrewingStand) param3).inventory()),
            originalStack,
            ItemStackUtil.snapshotOf(fuelStack)
        );
        final ItemStackSnapshot ingredientStack = ItemStackUtil.snapshotOf(((BrewingStandBlockEntityMixin) (Object) param3).items.get(3));
        final BrewingEvent.ConsumeFuel
                event = SpongeEventFactory.createBrewingEventConsumeFuel(currentCause, (BrewingStand) param3, ingredientStack, Collections.singletonList(fuelTransaction));
        if (Sponge.eventManager().post(event)) {
            fuelStack.grow(1);
            ((BrewingStandBlockEntityMixin) (Object) param3).fuel = 0;
        } else if (fuelTransaction.custom().isPresent()) {
            final ItemStackSnapshot finalFuel = fuelTransaction.finalReplacement();
            ((BrewingStandBlockEntityMixin) (Object) param3).items.set(4, ItemStackUtil.fromSnapshotToNative(finalFuel));
        }
    }

    @Inject(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;doBrew(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;)V"))
    private static void impl$captureOriginalItems(final Level $$0, final BlockPos $$1, final BlockState $$2, final BrewingStandBlockEntity $$3, final CallbackInfo ci) {
        for (int i = 0; i < 3; i++) {
            ((BrewingStandBlockEntityMixin) (Object) $$3).impl$originalSlots[i] = ((BrewingStandBlockEntityMixin) (Object) $$3).items.get(i);
        }
        ((BrewingStandBlockEntityMixin) (Object) $$3).impl$originalSlots[3] = ((BrewingStandBlockEntityMixin) (Object) $$3).items.get(3).copy();
    }

    @Inject(method = "serverTick",
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable(Lnet/minecraft/world/item/alchemy/PotionBrewing;Lnet/minecraft/core/NonNullList;)Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;" +
                    "setChanged(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private static void impl$callBrewEvents(final Level level, final BlockPos pos, final BlockState state, final BrewingStandBlockEntity entity, final CallbackInfo ci) {
        final NonNullList<ItemStack> items = ((BrewingStandBlockEntityAccessor) entity).accessor$items();
        final boolean isBrewable = BrewingStandBlockEntityAccessor.invoker$isBrewable(level.potionBrewing(), items);
        final boolean isBrewing = ((BrewingStandBlockEntityAccessor) entity).accessor$brewTime() > 0;
        final ItemStack ingredientStack = items.get(3);

        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        final BrewingStandBlockEntityMixin mixinSelf = (BrewingStandBlockEntityMixin) (Object) entity;
        if (isBrewing) {
            if (mixinSelf.brewTime == 0 && isBrewable) {
                final List<SlotTransaction> transactions = new ArrayList<>();
                final Lens lens = ((InventoryAdapter) entity).inventoryAdapter$getRootLens();
                for (int i = 0; i < 4; ++i) {
                    final ItemStack original = mixinSelf.impl$originalSlots[i];
                    final ItemStack replace = mixinSelf.items.get(i);
                    mixinSelf.impl$originalSlots[i] = null;
                    if (original == replace) {
                        continue;
                    }
                    transactions.add(new SlotTransaction(
                        (Slot) lens.getLens(i).getAdapter(((InventoryAdapter) entity).inventoryAdapter$getFabric(), ((BrewingStand) entity).inventory()),
                        ItemStackUtil.snapshotOf(original),
                        ItemStackUtil.snapshotOf(replace)
                    ));
                }
                final BrewingEvent.Finish event = SpongeEventFactory.createBrewingEventFinish(currentCause, (BrewingStand) entity, ItemStackUtil.snapshotOf(ingredientStack), Collections.unmodifiableList(transactions));
                Sponge.eventManager().post(event);
                for (final SlotTransaction transaction : transactions) {
                    transaction.custom().ifPresent(item ->
                        mixinSelf.items.set(((SlotAdapter) transaction.slot()).getOrdinal(), ItemStackUtil.fromSnapshotToNative(item)));
                }
            } else if (!isBrewable || mixinSelf.ingredient != ingredientStack.getItem()) {
                final BrewingEvent.Interrupt event = SpongeEventFactory.createBrewingEventInterrupt(currentCause, (BrewingStand) entity, ItemStackUtil.snapshotOf(ingredientStack));
                Sponge.eventManager().post(event);
            }
        } else if (isBrewable && mixinSelf.fuel > 0) {
            final BrewingEvent.Start event = SpongeEventFactory.createBrewingEventStart(currentCause, (BrewingStand) entity, ItemStackUtil.snapshotOf(ingredientStack));
            if (Sponge.eventManager().post(event)) {
                mixinSelf.brewTime = 0;
                mixinSelf.ingredient = Items.AIR;
                mixinSelf.fuel++;
            }
        }
    }

    @Inject(method = "serverTick", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;brewTime:I", ordinal = 1))
    private static void impl$onTick(
            final Level param0, final BlockPos param1, final BlockState param2, final BrewingStandBlockEntity param3, final CallbackInfo ci,
            final ItemStack fuelStack, final boolean isBrewable, final boolean isBrewing, final ItemStack ingredientStack) {
        if (((BrewingStandBlockEntityMixin) (Object) param3).brewTime != 0 && isBrewable &&
                ((BrewingStandBlockEntityMixin) (Object) param3).ingredient == ingredientStack.getItem()) {
            final Cause currentCause = Sponge.server().causeStackManager().currentCause();
            final BrewingEvent.Tick event = SpongeEventFactory.createBrewingEventTick(currentCause, (BrewingStand) param3, ItemStackUtil.snapshotOf(ingredientStack));
            if (Sponge.eventManager().post(event)) {
                ((BrewingStandBlockEntityMixin) (Object) param3).brewTime++;
            }
        }
    }

}
