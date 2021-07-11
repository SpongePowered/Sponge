package org.spongepowered.common.event.tracking.context.transaction;

import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.event.tracking.phase.packet.inventory.InventoryPacketContext;
import org.spongepowered.common.item.util.ItemStackUtil;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;

public class ClickMenuTransaction extends ContainerBasedTransaction {

    private final ServerPlayer player;
    private final int slotNum;
    private final int buttonNum;
    private final ClickType clickType;
    private final @Nullable Slot slot;
    private final ItemStackSnapshot cursor;

    public ClickMenuTransaction(
        final Player player, final AbstractContainerMenu menu, final int slotNum, final int buttonNum,
        final ClickType clickType,
        final @Nullable Slot slot,
        final ItemStackSnapshot cursor
    ) {
        super(((ServerWorld) player.level).key(), menu);
        this.player = (ServerPlayer) player;
        this.slotNum = slotNum;
        this.buttonNum = buttonNum;
        this.clickType = clickType;
        this.slot = slot;
        this.cursor = cursor;
    }

    @Override
    Optional<ClickContainerEvent> createInventoryEvent(
        final List<SlotTransaction> slotTransactions, final ImmutableList<Entity> entities,
        final PhaseContext<@NonNull ?> context,
        final Cause cause
    ) {
        if (slotTransactions.isEmpty()  && this.slotNum >= 0 && this.slot != null && entities.isEmpty()) {
            // No SlotTransaction was captured. So we add the clicked slot as a transaction
            final ItemStackSnapshot item = this.slot.peek().createSnapshot();
            slotTransactions.add(new SlotTransaction(this.slot, item, item));
        }
        final ItemStackSnapshot resultingCursor = ItemStackUtil.snapshotOf(this.player.inventory.getCarried());
        final Transaction<ItemStackSnapshot> cursorTransaction = new Transaction<>(this.cursor, resultingCursor);
        final @Nullable ClickContainerEvent event = context.createInventoryEvent(cause, this.player, (Container) this.menu,
            cursorTransaction, slotTransactions, entities, this.buttonNum, this.slot);

        // The client sends several packets all at once for drag events we only care about the last one.
        // Therefore, we never add any 'fake' transactions, as the final packet has everything we want.
        // TODO - investigate whether this is still needed:
        if (event != null && this.slot != null && !(event instanceof ClickContainerEvent.Drag)) {
            final ItemStackSnapshot item = this.slot.peek().createSnapshot();
            slotTransactions.add(new SlotTransaction(this.slot, item, item));
        }
        
        return Optional.ofNullable(event);
    }

    @Override
    public void restore(PhaseContext<@NonNull ?> context, ClickContainerEvent event) {
        if (!event.cursorTransaction().isValid()) {
            PacketPhaseUtil.handleCustomCursor(this.player, event.cursorTransaction().original());
        } else if (event.cursorTransaction().custom().isPresent()) {
            PacketPhaseUtil.handleCustomCursor(this.player, event.cursorTransaction().finalReplacement());
        }
    }

    @Override
    boolean isContainerEventAllowed(
        final PhaseContext<@Nullable ?> context
    ) {
        if (!(context instanceof InventoryPacketContext)) {
            return false;
        }
        final int containerId = ((InventoryPacketContext) context).<ServerboundContainerClickPacket>getPacket().getContainerId();
        return containerId != this.player.containerMenu.containerId;
    }

    @Override
    Optional<SlotTransaction> getSlotTransaction() {
        return Optional.empty();
    }

    @Override
    List<Entity> getEntitiesSpawned() {
        return this.sideEffects != null ? this.sideEffects.stream().flatMap(e -> e.stream());
    }

}
