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
package org.spongepowered.common.event.tracking.phase.packet;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.bridge.world.level.chunk.LevelChunkBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public abstract class PacketState<P extends PacketContext<P>> extends PooledPhaseState<P> implements IPhaseState<P> {

    private final BiConsumer<CauseStackManager.StackFrame, P> BASIC_PACKET_MODIFIER = super.getFrameModifier().andThen((frame, ctx) -> {
        if (ctx.packetPlayer != null) {
            frame.pushCause(ctx.packetPlayer);
        }
    });

    protected PacketState() {

    }


    protected static void processSpawnedEntities(final net.minecraft.server.level.ServerPlayer player, final SpawnEntityEvent event) {
        final List<Entity> entities = event.entities();
        PacketState.processEntities(player, entities);
    }

    protected static void processEntities(final net.minecraft.server.level.ServerPlayer player, final Collection<Entity> entities) {
        for (final Entity entity : entities) {
            EntityUtil.processEntitySpawn(entity, () -> Optional.of(((ServerPlayer)player).user()), e -> e.level.addFreshEntity(e));
        }
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, P> getFrameModifier() {
        return this.BASIC_PACKET_MODIFIER;
    }

    @Override
    public void unwind(final P phaseContext) {
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    public boolean matches(final int packetState) {
        return false;
    }

    @Override
    public void associateNeighborStateNotifier(
        final P unwindingContext, final BlockPos sourcePos, final Block block, final BlockPos notifyPos, final ServerLevel minecraftWorld,
        final PlayerTracker.Type notifier) {
        final Player player = unwindingContext.getSpongePlayer();
        final LevelChunk chunk = minecraftWorld.getChunkAt(notifyPos);
        ((LevelChunkBridge) chunk).bridge$setBlockNotifier(notifyPos, player.uniqueId());
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final P context, final net.minecraft.world.entity.Entity entityToSpawn
    ) {
        return SpawnTypes.PLACEMENT;
    }

    @Override
    public void associateScheduledTickUpdate(
        final P asContext, final ServerLevel level, final TickNextTickData<?> entry
    ) {
        asContext.getTransactor().logScheduledUpdate(level, entry);
    }

    public void populateContext(final net.minecraft.server.level.ServerPlayer playerMP, final Packet<?> packet, final P context) {

    }

    public boolean isPacketIgnored(final Packet<?> packetIn, final net.minecraft.server.level.ServerPlayer packetPlayer) {
        return false;
    }


    private final String desc = TrackingUtil.phaseStateToString("Packet", this);

    @Override
    public String toString() {
        return this.desc;
    }

}
