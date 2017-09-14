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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class BasicPacketState<P extends PacketContext<P>> implements IPhaseState<P> {

    private final Map<Class<? extends Packet<?>>, PacketFunction> packetUnwindMap = new IdentityHashMap<>();

    BasicPacketState() {

    }

    @Override
    public void unwind(P phaseContext) {
        if (this == PacketPhase.General.INVALID) { // Invalid doesn't capture any packets.
            return;
        }
        final Packet<?> packetIn = phaseContext.getRequiredExtra(InternalNamedCauses.Packet.CAPTURED_PACKET, Packet.class);
        final EntityPlayerMP player = phaseContext.getSource(EntityPlayerMP.class).get();
        final Class<? extends Packet<?>> packetInClass = (Class<? extends Packet<?>>) packetIn.getClass();

        final PacketFunction unwindFunction = this.packetUnwindMap.get(packetInClass);
        checkArgument(true, "PhaseState passed in is not an instance of IPacketState! Got %s", this);
        if (unwindFunction != null) {
            unwindFunction.unwind(packetIn, this, player, phaseContext);
        } else {
            PacketFunction.UNKNOWN_PACKET.unwind(packetIn, this, player, phaseContext);
        }
    }

    @Override
    public final TrackingPhase getPhase() {
        return TrackingPhases.PACKET;
    }

    @Override
    public P start() {
        return new BasicPacketContext(this);
    }

    public boolean matches(int packetState) {
        return false;
    }

    public void associateBlockEventNotifier(PhaseContext<?> context, IMixinWorldServer mixinWorldServer, BlockPos pos, IMixinBlockEventData blockEvent) {

    }

    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, PhaseContext<?> context) {

    }

    public boolean isPacketIgnored(Packet<?> packetIn, EntityPlayerMP packetPlayer) {
        return false;
    }

    public boolean ignoresItemPreMerges() {
        return false;
    }

    public boolean doesCaptureEntityDrops() {
        return false;
    }

    public boolean doBlockCapturing() {
        return true;
    }

    /**
     * A defaulted method to handle entities that are spawned due to packet placement during post processing.
     * Examples can include a player placing a redstone block priming a TNT explosive.
     *  @param phaseContext The phase context
     * @param entities The list of entities to spawn
     */
    public void postSpawnEntities(PhaseContext<?> phaseContext, ArrayList<Entity> entities) {
        final Player player =
                phaseContext.getSource(Player.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a player packet, but didn't get anything",
                                phaseContext));
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            Sponge.getCauseStackManager().pushCause(player);
            final SpawnEntityEvent event =
                    SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity entity : event.getEntities()) {
                    EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                    EntityUtil.getMixinWorld(entity).forceSpawnEntity(entity);
                }
            }
        }
    }

    public boolean shouldCaptureEntity() {
        return false;
    }

    /**
     * Defaulted method for packet phase states to spawn an entity directly.
     * This should be overridden by all packet phase states that are handling spawns
     * customarily with contexts and such. Captured entities are handled in
     * their respective {@link PacketFunction}s.
     *
     * @param context
     * @param entity
     * @param chunkX
     * @param chunkZ
     * @return True if the entity was spawned
     */
    public boolean spawnEntity(PhaseContext<?> context, Entity entity, int chunkX, int chunkZ) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final WorldServer minecraftWorld = (WorldServer) minecraftEntity.world;
        final Player player = context.getSource(Player.class)
                        .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a player", context));
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);
            Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, player);
            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getCauseStackManager().getCurrentCause(), entities);
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity newEntity : event.getEntities()) {
                    EntityUtil.toMixin(newEntity).setCreator(player.getUniqueId());
                    ((IMixinWorldServer) minecraftWorld).forceSpawnEntity(newEntity);
                }

                return true;
            }
        }
        return false;
    }
}
