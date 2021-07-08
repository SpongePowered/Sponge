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
package org.spongepowered.common.event.tracking.phase.packet.player;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.transaction.GameTransaction;
import org.spongepowered.common.event.tracking.context.transaction.SpawnEntityTransaction;
import org.spongepowered.common.event.tracking.phase.packet.PacketState;
import org.spongepowered.common.world.BlockChange;

import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class PacketCommandState extends PacketState<PlayerCommandPhaseContext> {

    private final BiConsumer<CauseStackManager.StackFrame, PlayerCommandPhaseContext> COMMAND_MODIFIER = super.getFrameModifier()
        .andThen((frame, ctx) -> {
            ctx.getSource(Object.class).ifPresent(frame::pushCause);
            if (ctx.command != null && !ctx.command.isEmpty()) {
                frame.addContext(EventContextKeys.COMMAND, ctx.command);
            }
        });

    @Override
    public PlayerCommandPhaseContext createNewContext(final PhaseTracker tracker) {
        return new PlayerCommandPhaseContext(this, tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, PlayerCommandPhaseContext> getFrameModifier() {
        return this.COMMAND_MODIFIER;
    }

    @Override
    public void postBlockTransactionApplication(
        final PlayerCommandPhaseContext context, final BlockChange blockChange,
        final BlockTransactionReceipt transaction
    ) {
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
    public SpawnEntityEvent createSpawnEvent(
        final PlayerCommandPhaseContext context, final GameTransaction<@NonNull ?> parent,
        final ImmutableList<Tuple<Entity, SpawnEntityTransaction.DummySnapshot>> collect, final Cause currentCause
    ) {
        final Cause newCauseWithSpawnType = Cause.builder().from(currentCause).build(
            EventContext.builder().from(currentCause.context()).add(
                EventContextKeys.SPAWN_TYPE,
                SpawnTypes.PLACEMENT.get()
            ).build());
        return SpongeEventFactory.createSpawnEntityEvent(newCauseWithSpawnType,
            collect.stream()
                .map(t -> (org.spongepowered.api.entity.Entity) t.first())
                .collect(Collectors.toList())
        );
    }


}
