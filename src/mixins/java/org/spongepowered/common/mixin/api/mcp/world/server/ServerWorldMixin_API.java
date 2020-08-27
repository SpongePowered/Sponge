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
package org.spongepowered.common.mixin.api.mcp.world.server;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerTickList;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.SessionLockException;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.raid.RaidManagerAccessor;
import org.spongepowered.common.accessor.world.storage.SaveHandlerAccessor;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.mixin.api.mcp.world.WorldMixin_API;
import org.spongepowered.common.util.ChunkUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin_API extends WorldMixin_API<org.spongepowered.api.world.server.ServerWorld> implements org.spongepowered.api.world.server.ServerWorld {

    @Shadow @Final private ServerTickList<Block> pendingBlockTicks;
    @Shadow @Final private ServerTickList<Fluid> pendingFluidTicks;
    @Shadow @Final private Int2ObjectMap<Entity> entitiesById;
    @Shadow private boolean insideTick;

    @Shadow public abstract void shadow$save(@Nullable IProgressUpdate p_217445_1_, boolean p_217445_2_, boolean p_217445_3_) throws SessionLockException;
    @Shadow public abstract boolean shadow$addEntity(Entity p_217376_1_);
    @Shadow public abstract void shadow$onChunkUnloading(Chunk p_217466_1_);
    @Shadow public abstract void shadow$playSound(@Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract ServerChunkProvider shadow$getChunkProvider();
    @Nonnull @Shadow public abstract MinecraftServer shadow$getServer();
    @Nullable @Shadow public abstract Entity shadow$getEntityByUuid(UUID p_217461_1_);
    @Shadow public abstract SaveHandler shadow$getSaveHandler();
    @Shadow public abstract List<ServerPlayerEntity> shadow$getPlayers();
    @Shadow public abstract RaidManager shadow$getRaids();
    @Nullable @Shadow public abstract Raid shadow$findRaid(BlockPos p_217475_1_);

    // World
    @Override
    public boolean isLoaded() {
        if (((WorldBridge) this).bridge$isFake()) {
            return false;
        }

        final ServerWorld world = ((SpongeWorldManager) ((Server) this.shadow$getServer()).getWorldManager()).getWorld(this.shadow$getDimension().getType());
        if (world == null) {
            return false;
        }

        return world == (Object) this;
    }

    // LocationCreator

    @Override
    public ServerLocation getLocation(final Vector3i position) {
        return ServerLocation.of(this, position);
    }

    @Override
    public ServerLocation getLocation(final Vector3d position) {
        return ServerLocation.of(this, position);
    }

    // ServerWorld

    @Override
    public WorldProperties getProperties() {
        return (WorldProperties) this.shadow$getWorldInfo();
    }

    @Override
    public Server getEngine() {
        return (Server) this.shadow$getServer();
    }

    @Override
    public Optional<org.spongepowered.api.world.chunk.Chunk> regenerateChunk(
        final int cx, final int cy, final int cz, final ChunkRegenerateFlag flag) {
        return ChunkUtil.regenerateChunk(this, cx, cy, cz, flag);
    }

    @Override
    public Path getDirectory() {
        final File worldDirectory = this.shadow$getSaveHandler().getWorldDirectory();
        if (worldDirectory == null) {
            new PrettyPrinter(60).add("A Server World has a null save directory!").centre().hr()
                    .add("%s : %s", "World Name", ((SaveHandlerAccessor) this.shadow$getSaveHandler()).accessor$getWorldId())
                    .add("%s : %s", "Dimension", this.getProperties().getDimensionType())
                    .add("Please report this to sponge developers so they may potentially fix this")
                    .trace(System.err, SpongeCommon.getLogger(), Level.ERROR);
            return null;
        }
        return worldDirectory.toPath();
    }

    @Override
    public WorldStorage getWorldStorage() {
        return (WorldStorage) this.shadow$getChunkProvider();
    }

    @Override
    public boolean save() throws IOException {
        try {
            this.shadow$save((IProgressUpdate) null, false, false);
        } catch (final SessionLockException e) {
            throw new IOException(e);
        }
        return true;
    }

    @Override
    public boolean unloadChunk(final org.spongepowered.api.world.chunk.Chunk chunk) {
        this.shadow$onChunkUnloading((Chunk) chunk);
        return true;
    }

    // TODO move to bridge?
    private boolean impl$processingExplosion;

    @Override
    public void triggerExplosion(org.spongepowered.api.world.explosion.Explosion explosion) {
        checkNotNull(explosion, "explosion");
        // Sponge start
        this.impl$processingExplosion = true;
        // Set up the pre event
        final ExplosionEvent.Pre
                event =
                SpongeEventFactory.createExplosionEventPre(PhaseTracker.getCauseStackManager().getCurrentCause(),
                        explosion, this);
        if (SpongeCommon.postEvent(event)) {
            this.impl$processingExplosion = false;
            return;
        }
        explosion = event.getExplosion();
        final Explosion mcExplosion;
        try {
            // Since we already have the API created implementation Explosion, let's use it.
            mcExplosion = (Explosion) explosion;
        } catch (final Exception e) {
            new PrettyPrinter(60).add("Explosion not compatible with this implementation").centre().hr()
                    .add("An explosion that was expected to be used for this implementation does not")
                    .add("originate from this implementation.")
                    .add(e)
                    .trace();
            return;
        }

        try (final PhaseContext<?> ignored = GeneralPhase.State.EXPLOSION.createPhaseContext(PhaseTracker.SERVER)
                .explosion((Explosion) explosion)
                .source(explosion.getSourceExplosive().isPresent() ? explosion.getSourceExplosive() : this)) {
            ignored.buildAndSwitch();
            final boolean damagesTerrain = explosion.shouldBreakBlocks();
            // Sponge End

            mcExplosion.doExplosionA();
            mcExplosion.doExplosionB(false);

            if (!damagesTerrain) {
                mcExplosion.clearAffectedBlockPositions();
            }

            // Sponge Start - end processing
            this.impl$processingExplosion = false;
        }
        // Sponge End
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ServerPlayer> getPlayers() {
        return ImmutableList.copyOf((Collection<ServerPlayer>) (Collection<?>) this.shadow$getPlayers());
    }

    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities() {
        return (Collection< org.spongepowered.api.entity.Entity>) (Object) Collections.unmodifiableCollection(this.entitiesById.values());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<org.spongepowered.api.raid.Raid> getRaids() {
        final RaidManagerAccessor raidManager = (RaidManagerAccessor) this.shadow$getRaids();
        return (Collection<org.spongepowered.api.raid.Raid>) (Collection) raidManager.accessor$getById().values();
    }

    @Override
    public Optional<org.spongepowered.api.raid.Raid> getRaidAt(final Vector3i blockPosition) {
        final org.spongepowered.api.raid.Raid raid = (org.spongepowered.api.raid.Raid) this.shadow$findRaid(VecHelper.toBlockPos(blockPosition));
        return Optional.ofNullable(raid);
    }

    // ReadableEntityVolume

    @Override
    public Optional<org.spongepowered.api.entity.Entity> getEntity(final UUID uuid) {
        return Optional.ofNullable((org.spongepowered.api.entity.Entity) this.shadow$getEntityByUuid(uuid));
    }

    // UpdateableVolume

    @Override
    @SuppressWarnings("unchecked")
    public ScheduledUpdateList<BlockType> getScheduledBlockUpdates() {
        return (ScheduledUpdateList<BlockType>) this.pendingBlockTicks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ScheduledUpdateList<FluidType> getScheduledFluidUpdates() {
        return (ScheduledUpdateList<FluidType>) this.pendingFluidTicks;
    }

}
