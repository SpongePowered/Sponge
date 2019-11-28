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
package org.spongepowered.common.item.inventory.custom;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.common.item.inventory.util.ContainerUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class CustomInventoryListener implements EventListener<InteractContainerEvent> {

    private WeakReference<Inventory> inventory;
    List<Consumer<InteractContainerEvent>> consumers;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public CustomInventoryListener(Inventory inventory, List<Consumer<? extends InteractContainerEvent>> consumers) {
        this.inventory = new WeakReference<>(inventory);
        this.consumers = (List) ImmutableList.copyOf(consumers);
    }

    @Override
    public void handle(InteractContainerEvent event) throws Exception {
        net.minecraft.inventory.container.Container nativeContainer = ContainerUtil.toNative(event.getTargetInventory());
        Inventory inventory = this.inventory.get();
        if (inventory == null) {
            Sponge.getEventManager().unregisterListeners(this);
            return;
        }
        for (net.minecraft.inventory.container.Slot slot : nativeContainer.inventorySlots) {
            if (slot.inventory == inventory) {
                // This container does contain our inventory
                for (Consumer<InteractContainerEvent> consumer : this.consumers) {
                    consumer.accept(event);
                }
                break;
            }
        }
    }

    @Nullable
    public Inventory getInventory() {
        return this.inventory.get();
    }
}
