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

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.carrier.BrewingStand;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.entity.BrewingEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

    // @Formatter:off
    @Shadow private int brewTime;
    @Shadow private int fuel;
    @Shadow private NonNullList<ItemStack> items;
    @Shadow private Item ingredient;
    // @Formatter:on

    @Inject(method = "tick",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;setChanged()V"))
    private void impl$onConsumeFuel(CallbackInfo ci, ItemStack fuelStack) {
        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        fuelStack.grow(1);
        final ItemStackSnapshot originalStack = ItemStackUtil.snapshotOf(fuelStack);
        fuelStack.shrink(1);
        final Transaction<ItemStackSnapshot> fuelTransaction = new Transaction<>(originalStack, ItemStackUtil.snapshotOf(fuelStack));
        final ItemStackSnapshot ingredientStack = ItemStackUtil.snapshotOf(this.items.get(3));
        final BrewingEvent.ConsumeFuel
                event = SpongeEventFactory.createBrewingEventConsumeFuel(currentCause, (BrewingStand) this, fuelTransaction, ingredientStack);
        if (Sponge.eventManager().post(event)) {
            fuelStack.grow(1);
            this.fuel = 0;
        } else if (event.fuel().custom().isPresent()) {
            final ItemStackSnapshot finalFuel = event.fuel().finalReplacement();
            this.items.set(4, ItemStackUtil.fromSnapshotToNative(finalFuel));
        }
    }

    @Inject(method = "tick",
            locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;isBrewable()Z")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;setChanged()V"))
    private void impl$callBrewEvents(CallbackInfo ci, ItemStack fuelStack, boolean isBrewable, boolean isBrewing, ItemStack ingredientStack) {
        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        if (isBrewing) {
            if (this.brewTime == 0 && isBrewable) {
                List<ItemStackSnapshot> stacks = new ArrayList<>();
                for(int i = 0; i < 3; ++i) {
                    stacks.add(ItemStackUtil.snapshotOf(this.items.get(i)));
                }
                final BrewingEvent.Finish event = SpongeEventFactory.createBrewingEventFinish(currentCause, Collections.unmodifiableList(stacks), (BrewingStand) this, ItemStackUtil.snapshotOf(ingredientStack));
                Sponge.eventManager().post(event);
            } else if (!isBrewable || this.ingredient != ingredientStack.getItem()) {
                final BrewingEvent.Interrupt event = SpongeEventFactory.createBrewingEventInterrupt(currentCause, (BrewingStand) this, ItemStackUtil.snapshotOf(ingredientStack));
                Sponge.eventManager().post(event);
            }
        } else if (isBrewable && this.fuel > 0) {
            final BrewingEvent.Start event = SpongeEventFactory.createBrewingEventStart(currentCause, (BrewingStand) this, ItemStackUtil.snapshotOf(ingredientStack));
            if (Sponge.eventManager().post(event)) {
                this.brewTime = 0;
                this.ingredient = Items.AIR;
                this.fuel++;
            }
        }
    }

    @Inject(method = "tick", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;brewTime:I", ordinal = 1))
    private void impl$onTick(CallbackInfo ci, ItemStack fuelStack, boolean isBrewable, boolean isBrewing, ItemStack ingredientStack) {
        if (this.brewTime != 0 && isBrewable && this.ingredient == ingredientStack.getItem()) {
            final Cause currentCause = Sponge.server().causeStackManager().currentCause();
            final BrewingEvent.Tick event = SpongeEventFactory.createBrewingEventTick(currentCause, (BrewingStand) this, ItemStackUtil.snapshotOf(ingredientStack));
            if (Sponge.eventManager().post(event)) {
                this.brewTime++;
            }
        }
    }

}
