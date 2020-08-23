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

import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class PacketState<P extends PacketContext<P>> extends PooledPhaseState<P> implements IPhaseState<P> {

    private final BiConsumer<CauseStackManager.StackFrame, P> BASIC_PACKET_MODIFIER = super.getFrameModifier().andThen((frame, ctx) -> {
        if (ctx.packetPlayer != null) {
            frame.pushCause(ctx.packetPlayer);
        }
    });

    protected PacketState() {

    }


    protected static void processSpawnedEntities(final ServerPlayerEntity player, final SpawnEntityEvent event) {
        final List<Entity> entities = event.getEntities();
        processEntities(player, entities);
    }

    protected static void processEntities(final ServerPlayerEntity player, final Collection<Entity> entities) {
        for (final Entity entity : entities) {
            EntityUtil.processEntitySpawn(entity, () -> Optional.of(((ServerPlayer)player).getUser()));
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
    public void appendNotifierToBlockEvent(final P context, final PhaseContext<?> currentContext,
                                           final ServerWorldBridge mixinWorldServer, final BlockPos pos, final BlockEventDataBridge blockEvent) {

    }

    @Override
    public void associateNeighborStateNotifier(
        final P unwindingContext, final BlockPos sourcePos, final Block block, final BlockPos notifyPos, final ServerWorld minecraftWorld,
        final PlayerTracker.Type notifier) {
        final Player player = unwindingContext.getSpongePlayer();
        final Chunk chunk = minecraftWorld.getChunkAt(notifyPos);
        ((ChunkBridge) chunk).bridge$setBlockNotifier(notifyPos, player.getUniqueId());
    }

    public void populateContext(final ServerPlayerEntity playerMP, final IPacket<?> packet, final P context) {

    }

    public boolean isPacketIgnored(final IPacket<?> packetIn, final ServerPlayerEntity packetPlayer) {
        return false;
    }

    @Override
    public boolean ignoresItemPreMerging() {
        return false;
    }

    @Override
    public boolean doesCaptureEntityDrops(final P context) {
        return false;
    }

    @Override
    public boolean doesBulkBlockCapture(final P context) {
        return true;
    }

    /**
     * A defaulted method to handle entities that are spawned due to packet placement during post processing.
     * Examples can include a player placing a redstone block priming a TNT explosive.
     * @param context The phase context
     * @param entities The list of entities to spawn
     */
    @Override
    public void postProcessSpawns(final P context, final ArrayList<Entity> entities) {
        final Player player = context.getSpongePlayer();
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            frame.pushCause(player);
            SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }


    public boolean shouldCaptureEntity() {
        return false;
    }


    /**
     * Defaulted method for packet phase states to spawn an entity directly.
     * This should be overridden by all packet phase states that are handling spawns
     * customarily with contexts and such. Captured entities are handled in
     * their respective {@link PacketState#unwind(PhaseContext)}s.
     *
     * @param context
     * @param entity
     * @return True if the entity was spawned
     */
    public boolean spawnEntity(final P context, final Entity entity) {
        final Player player = context.getSource(Player.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a player", context));
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.SPAWN_TYPE, this.getEntitySpawnType(context));
            frame.addContext(EventContextKeys.CREATOR, ((ServerPlayer)player).getUser());
            frame.addContext(EventContextKeys.NOTIFIER, ((ServerPlayer)player).getUser());
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    public SpawnType getEntitySpawnType(final P context) {
        return SpawnTypes.PLACEMENT.get();
    }

    private final String desc = TrackingUtil.phaseStateToString("Packet", this);

    @Override
    public String toString() {
        return this.desc;
    }

}
