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
package org.spongepowered.common.event.tracking.phase.general;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhaseUtil;
import org.spongepowered.common.world.BlockChange;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

final class CommandState extends GeneralState<CommandPhaseContext> {

    private final BiConsumer<CauseStackManager.StackFrame, CommandPhaseContext> COMMAND_MODIFIER = super.getFrameModifier()
        .andThen((frame, ctx) -> {
            ctx.getSource(Object.class).ifPresent(frame::pushCause);
        });

    @Override
    public CommandPhaseContext createNewContext(final PhaseTracker tracker) {
        return new CommandPhaseContext(this, tracker)
            .addCaptures()
            .addEntityDropCaptures();
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, CommandPhaseContext> getFrameModifier() {
        return this.COMMAND_MODIFIER;
    }

    @Override
    public void postBlockTransactionApplication(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> transaction,
        final CommandPhaseContext context) {
        // We want to investigate if there is a user on the cause stack
        // and if possible, associate the notiifer/owner based on the change flag
        // We have to check if there is a player, because command blocks can be triggered
        // without player interaction.
        // Fixes https://github.com/SpongePowered/SpongeForge/issues/2442
        PhaseTracker.getCauseStackManager().currentCause().first(User.class).ifPresent(user -> {
            TrackingUtil.associateTrackerToTarget(blockChange, transaction, user);
        });
   }

    @Override
    public void associateNeighborStateNotifier(final CommandPhaseContext context, final @Nullable BlockPos sourcePos, final Block block,
        final BlockPos notifyPos, final ServerLevel minecraftWorld, final PlayerTracker.Type notifier) {
        context.getSource(Player.class)
            .ifPresent(player -> ((LevelChunkBridge) minecraftWorld.getChunkAt(notifyPos))
                .bridge$addTrackedBlockPosition(block, notifyPos, ((ServerPlayer) player).user(), PlayerTracker.Type.NOTIFIER));
    }

    @Override
    public void unwind(final CommandPhaseContext phaseContext) {
        final Optional<net.minecraft.world.entity.player.Player> playerSource = phaseContext.getSource(net.minecraft.world.entity.player.Player.class);
        final CauseStackManager csm = PhaseTracker.getCauseStackManager();
        if (playerSource.isPresent()) {
            // Post event for inventory changes
            ((TrackedInventoryBridge) playerSource.get().inventory).bridge$setCaptureInventory(false);
            final List<SlotTransaction> list = ((TrackedInventoryBridge) playerSource.get().inventory).bridge$getCapturedSlotTransactions();
            if (!list.isEmpty()) {
                final ChangeInventoryEvent event = SpongeEventFactory.createChangeInventoryEvent(csm.currentCause(),
                        ((Inventory) playerSource.get().inventory), list);
                SpongeCommon.post(event);
                PacketPhaseUtil.handleSlotRestore(playerSource.get(), null, list, event.isCancelled());
                list.clear();
            }
        }
        TrackingUtil.processBlockCaptures(phaseContext);
    }

}
