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
package org.spongepowered.common.mixin.inventory.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.inventory.container.ContainerBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_API implements Player {

    @Override
    public Optional<Container> getOpenInventory() {
        return Optional.ofNullable((Container) ((PlayerEntity) (Object) this).openContainer);
    }

    @Override
    public Optional<Container> openInventory(final Inventory inventory) throws IllegalArgumentException {
        return this.openInventory(inventory, null);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    @Override
    public Optional<Container> openInventory(final Inventory inventory, final Text displayName) {
        ContainerBridge openContainer = (ContainerBridge) ((PlayerEntity) (Object) this).openContainer;
        if (openContainer.bridge$isInUse()) {
            final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
            SpongeImpl.getLogger().warn("This player is currently modifying an open container. This action will be delayed.");
            Task.builder().delayTicks(0).execute(() -> {
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    cause.all().forEach(frame::pushCause);
                    cause.getContext().asMap().forEach((key, value) -> frame.addContext(((EventContextKey) key), value));
                    this.closeInventory(); // Cause close event first. So cursor item is not lost.
                    this.openInventory(inventory); // Then open the inventory
                }
            }).plugin(SpongeImpl.getPlugin()).build();
            return this.getOpenInventory();
        }
        return Optional.ofNullable((Container) InventoryEventFactory.displayContainer((ServerPlayerEntity) (Object) this, inventory, displayName));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean closeInventory() throws IllegalArgumentException {
        net.minecraft.inventory.container.Container openContainer = ((PlayerEntity) (Object) this).openContainer;
        if (((ContainerBridge) openContainer).bridge$isInUse()) {
            final Cause cause = Sponge.getCauseStackManager().getCurrentCause();
            SpongeImpl.getLogger().warn("This player is currently modifying an open container. This action will be delayed.");
            Task.builder().delayTicks(0).delayTicks(0).execute(() -> {
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    cause.all().forEach(frame::pushCause);
                    cause.getContext().asMap().forEach((key, value) -> frame.addContext(((EventContextKey) key), value));
                    this.closeInventory();
                }
            }).plugin(SpongeImpl.getPlugin()).build();
            return false;
        }
        // Create Close_Window to capture item drops
        try (final PhaseContext<?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext(PhaseTracker.SERVER)
                .source(this)
                .packetPlayer(((ServerPlayerEntity)(Object) this))
                .openContainer(openContainer)
                // intentionally missing the lastCursor to not double throw close event
        ) {
            ctx.buildAndSwitch();
            PlayerInventory inventory = ((PlayerEntity) (Object) this).inventory;
            final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(inventory.getItemStack());
            return !SpongeCommonEventFactory.callInteractInventoryCloseEvent(openContainer, (ServerPlayerEntity) (Object) this, cursor, cursor, false).isCancelled();
        }
    }

    @Override
    public Inventory getEnderChestInventory() {
        return (Inventory) ((PlayerEntity) (Object) this).getInventoryEnderChest();
    }

}
