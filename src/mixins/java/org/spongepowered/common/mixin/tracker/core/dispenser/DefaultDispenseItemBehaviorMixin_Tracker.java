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
package org.spongepowered.common.mixin.tracker.core.dispenser;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

@Mixin(DefaultDispenseItemBehavior.class)
public abstract class DefaultDispenseItemBehaviorMixin_Tracker {

    // Before the item is split off the stack and spawned, so a cancelled drop leaves the stack whole and creates no entity
    @Inject(method = "execute", at = @At("HEAD"), cancellable = true)
    private void tracker$throwDropItemPre(final BlockSource source, final ItemStack stack, final CallbackInfoReturnable<ItemStack> cir) {
        if (!ShouldFire.DROP_ITEM_EVENT) {
            return;
        }
        final ItemStackSnapshot dropped = ItemStackUtil.snapshotOf(stack.copyWithCount(1));
        final List<ItemStackSnapshot> items = new ArrayList<>();
        items.add(dropped);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(source.blockEntity());
            final DropItemEvent.Pre event = SpongeEventFactory.createDropItemEventPre(frame.currentCause(), ImmutableList.of(dropped), items);
            if (SpongeCommon.post(event) || event.droppedItems().isEmpty()) {
                cir.setReturnValue(stack);
            }
        }
    }
}
