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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class PacketState<P extends PacketContext<P>> implements IPhaseState<P> {


    private BiConsumer<CauseStackManager.StackFrame, P> BASIC_PACKET_MODIFIER = IPhaseState.super.getFrameModifier().andThen((frame, ctx) -> {
        if (ctx.packetPlayer != null) {
            frame.pushCause(ctx.packetPlayer);
        }
    });

    protected PacketState() {

    }


    protected static void processSpawnedEntities(EntityPlayerMP player, SpawnEntityEvent event) {
        List<Entity> entities = event.getEntities();
        processEntities(player, entities);
    }

    protected static void processEntities(EntityPlayerMP player, Collection<Entity> entities) {
        for (Entity entity : entities) {
            EntityUtil.processEntitySpawn(entity, () -> Optional.of(player.getUniqueID()));
        }
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, P> getFrameModifier() {
        return this.BASIC_PACKET_MODIFIER;
    }

    @Override
    public final TrackingPhase getPhase() {
        return TrackingPhases.PACKET;
    }

    @Override
    public void unwind(P phaseContext) {
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(phaseContext.getCapturedBlockSupplier(), this, phaseContext);
    }

    public boolean matches(int packetState) {
        return false;
    }

    @Override
    public void appendNotifierToBlockEvent(P context, IMixinWorldServer mixinWorldServer, BlockPos pos, IMixinBlockEventData blockEvent) {

    }

    @Override
    public void associateNeighborStateNotifier(P unwindingContext, BlockPos sourcePos, Block block, BlockPos notifyPos, WorldServer minecraftWorld,
        PlayerTracker.Type notifier) {
        final Player player = unwindingContext.getSpongePlayer();
        Chunk chunk = minecraftWorld.getChunk(notifyPos);
        ((IMixinChunk) chunk).setBlockNotifier(notifyPos, player.getUniqueId());
    }

    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, P context) {

    }

    public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
        return false;
    }

    @Override
    public boolean ignoresItemPreMerging() {
        return false;
    }

    @Override
    public boolean doesCaptureEntityDrops(P context) {
        return false;
    }

    @Override
    public boolean doesBulkBlockCapture(P context) {
        return true;
    }

    /**
     * A defaulted method to handle entities that are spawned due to packet placement during post processing.
     * Examples can include a player placing a redstone block priming a TNT explosive.
     * @param context The phase context
     * @param entities The list of entities to spawn
     */
    @Override
    public void postProcessSpawns(P context, ArrayList<Entity> entities) {
        final Player player = context.getSpongePlayer();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            frame.pushCause(player);
            SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    @Override
    public boolean spawnEntityOrCapture(P context, Entity entity, int chunkX, int chunkZ) {
        return this.shouldCaptureEntity()
        ? context.getCapturedEntities().add(entity)
        : this.spawnEntity(context, entity, chunkX, chunkZ);
    }

    public boolean shouldCaptureEntity() {
        return false;
    }


    @Override
    public boolean doesCaptureEntitySpawns() {
        return shouldCaptureEntity();
    }


    /**
     * Defaulted method for packet phase states to spawn an entity directly.
     * This should be overridden by all packet phase states that are handling spawns
     * customarily with contexts and such. Captured entities are handled in
     * their respective {@link PacketState#unwind(PhaseContext)}s.
     *
     * @param context
     * @param entity
     * @param chunkX
     * @param chunkZ
     * @return True if the entity was spawned
     */
    public boolean spawnEntity(P context, Entity entity, int chunkX, int chunkZ) {
        final Player player = context.getSource(Player.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a player", context));
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            frame.addContext(EventContextKeys.NOTIFIER, player);
            frame.addContext(EventContextKeys.OWNER, player);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }


    private final String className = this.getClass().getSimpleName();

    @Override
    public String toString() {
        return this.getPhase() + "{" + this.className + "}";
    }

}
