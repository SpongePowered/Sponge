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
package org.spongepowered.common.mixin.inventory.api.server.level;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.ContainerBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.mixin.inventory.api.world.entity.player.PlayerMixin_Inventory_API;
import java.util.Optional;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public abstract class ServerPlayerMixin_Inventory_API extends PlayerMixin_Inventory_API implements ServerPlayer {

    @Override
    public Optional<Container> openInventory() {
        return Optional.ofNullable((Container) this.containerMenu);
    }

    @Override
    public Optional<Container> openInventory(final Inventory inventory) throws IllegalArgumentException {
        return this.openInventory(inventory, null);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "rawtypes"})
    @Override
    public Optional<Container> openInventory(final Inventory inventory, final Component displayName) {
        final ContainerBridge openContainer = (ContainerBridge) this.containerMenu;
        if (openContainer.bridge$isInUse()) {
            final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
            SpongeCommon.logger().warn("This player is currently modifying an open container. This action will be delayed.");
            Task.builder().delay(Ticks.zero()).execute(() -> {
                try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    cause.all().forEach(frame::pushCause);
                    cause.context().asMap().forEach((key, value) -> frame.addContext(((EventContextKey) key), value));
                    this.closeInventory(); // Cause close event first. So cursor item is not lost.
                    this.openInventory(inventory); // Then open the inventory
                }
            }).plugin(Launch.instance().commonPlugin()).build();
            return this.openInventory();
        }
        return Optional.ofNullable((Container) InventoryEventFactory.displayContainer((net.minecraft.server.level.ServerPlayer) (Object) this, inventory, displayName));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public boolean closeInventory() throws IllegalArgumentException {
        final net.minecraft.world.inventory.AbstractContainerMenu openContainer = this.containerMenu;
        if (((ContainerBridge) openContainer).bridge$isInUse()) {
            final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
            SpongeCommon.logger().warn("This player is currently modifying an open container. This action will be delayed.");
            Task.builder().delay(Ticks.zero()).execute(() -> {
                try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                    cause.all().forEach(frame::pushCause);
                    cause.context().asMap().forEach((key, value) -> frame.addContext(((EventContextKey) key), value));
                    this.closeInventory();
                }
            }).plugin(Launch.instance().commonPlugin()).build();
            return false;
        }
        // Create Close_Window to capture item drops
        try (final PhaseContext<?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext(PhaseTracker.SERVER)
                .source(this)
                .packetPlayer(((net.minecraft.server.level.ServerPlayer)(Object) this))
                .openContainer(openContainer)
                // intentionally missing the lastCursor to not double throw close event
        ) {
            ctx.buildAndSwitch();
            final net.minecraft.world.entity.player.Inventory inventory = this.inventory;
            final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(inventory.getCarried());
            return !SpongeCommonEventFactory.callInteractInventoryCloseEvent(openContainer, (net.minecraft.server.level.ServerPlayer) (Object) this, cursor, cursor, false).isCancelled();
        }
    }

}
