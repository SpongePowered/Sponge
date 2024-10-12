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
package org.spongepowered.common.event.tracking.context.transaction.inventory;

import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Optional;

public class SelectTradeTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    private int tradeItem;

    public SelectTradeTransaction(final ServerPlayer player, final int tradeItem) {
        super(player.containerMenu);
        this.player = player;
        this.tradeItem = tradeItem;
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final List<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {

        final ItemStackSnapshot cursorItem = ItemStackUtil.snapshotOf(this.player.containerMenu.getCarried());
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(cursorItem, cursorItem);
        if (this.menu instanceof MerchantMenu) {
            final MerchantOffer offer = ((MerchantMenu) this.menu).getOffers().get(this.tradeItem);
            final ClickContainerEvent.SelectTrade event = SpongeEventFactory.createClickContainerEventSelectTrade(cause, (Container) this.menu,
                    cursorTransaction, Optional.empty(), (TradeOffer) offer, slotTransactions, this.tradeItem);
            return Optional.of(event);
        }
        SpongeCommon.logger().warn("SelectTradeTransaction without MerchantMenu");
        return Optional.empty();
    }

    @Override
    public void restore(final PhaseContext<@NonNull ?> context, final ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    public void postProcessEvent(final PhaseContext<@NonNull ?> context, final ClickContainerEvent event) {
        this.handleEventResults(this.player, event);
    }

    @Override
    boolean isContainerEventAllowed(final PhaseContext<@NonNull ?> context) {
        if (!(context instanceof InventoryPacketContext)) {
            return false;
        }
        final int containerId = ((InventoryPacketContext) context).<ServerboundPlaceRecipePacket>getPacket().containerId();
        return containerId != this.player.containerMenu.containerId;
    }
}
