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
package org.spongepowered.common.mixin.api.mcp.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.server.management.PlayerChunkMapEntryBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.ServerWorldEventHandlerBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.mixin.core.world.chunk.ChunkAccessor;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeRecordType;
import org.spongepowered.common.effect.sound.SoundEffectHelper;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.mixin.core.server.management.PlayerChunkMapAccessor;
import org.spongepowered.common.mixin.core.server.management.PlayerChunkMapEntryAccessor;
import org.spongepowered.common.mixin.core.util.SoundEventsAccessor;
import org.spongepowered.common.mixin.core.world.chunk.ChunkProviderServerAccessor;
import org.spongepowered.common.util.NonNullArrayList;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin_API extends WorldMixin_API {

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow @Final private PlayerChunkMap playerChunkMap;
    @Shadow @Final @Mutable private Teleporter worldTeleporter;

    @Shadow @Nullable public abstract net.minecraft.entity.Entity getEntityFromUuid(UUID uuid);
    @Shadow public abstract PlayerChunkMap getPlayerChunkMap();
    @Shadow public abstract ChunkProviderServer getChunkProvider();
    @Shadow public abstract void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority);
    @Shadow protected abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);

    @Override
    public boolean isLoaded() {
        return WorldManager.isKnownWorld((WorldServer) (Object) this);
    }

    @Override
    public Path getDirectory() {
        final File worldDirectory = this.saveHandler.getWorldDirectory();
        if (worldDirectory == null) {
            new PrettyPrinter(60).add("A Server World has a null save directory!").centre().hr()
                .add("%s : %s", "World Name", this.getName())
                .add("%s : %s", "Dimension", this.getProperties().getDimensionType())
                .add("Please report this to sponge developers so they may potentially fix this")
                .trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
            return null;
        }
        return worldDirectory.toPath();
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        return ((WorldServerBridge) this).bridge$getSpongeGenerator();
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(final int x, final int y, final int z, final int priority, final int ticks) {
        final BlockPos pos = new BlockPos(x, y, z);
        this.updateBlockTick(pos, getBlockState(pos).getBlock(), ticks, priority);
        final ScheduledBlockUpdate sbu = ((WorldServerBridge) this).bridge$getScheduledBlockUpdate();
        ((WorldServerBridge) this).bridge$setScheduledBlockUpdate(null);
        return sbu;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void removeScheduledUpdate(final int x, final int y, final int z, final ScheduledBlockUpdate update) {
        // Note: Ignores position argument
        this.pendingTickListEntriesHashSet.remove(update);
        this.pendingTickListEntriesTreeSet.remove(update);
    }

    @Override
    public boolean save() throws IOException {
        if (!getChunkProvider().canSave()) {
            return false;
        }

        // TODO: Expose flush parameter in SpongeAPI?
        try {
            WorldManager.saveWorld((WorldServer) (Object) this, true);
        } catch (MinecraftException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(final int x, final int y, final int z) {
        final BlockPos position = new BlockPos(x, y, z);
        final ImmutableList.Builder<ScheduledBlockUpdate> builder = ImmutableList.builder();
        for (final NextTickListEntry sbu : this.pendingTickListEntriesTreeSet) {
            if (sbu.position.equals(position)) {
                builder.add((ScheduledBlockUpdate) sbu);
            }
        }
        return builder.build();
    }

    @Override
    public Optional<Entity> getEntity(final UUID uuid) {
        return Optional.ofNullable((Entity) this.getEntityFromUuid(uuid));
    }
    @SuppressWarnings("deprecation")
    @Override
    public Optional<org.spongepowered.api.world.Chunk> regenerateChunk(final int cx, final int cy, final int cz, final ChunkRegenerateFlag flag) {
        final List<EntityPlayerMP> playerList = new ArrayList<>();
        final List<net.minecraft.entity.Entity> entityList = new ArrayList<>();
        org.spongepowered.api.world.Chunk spongeChunk;
        try (final PhaseContext<?> context = GenerationPhase.State.CHUNK_REGENERATING_LOAD_EXISTING.createPhaseContext()
            .world((net.minecraft.world.World)(Object) this)) {
            context.buildAndSwitch();
            spongeChunk = this.loadChunk(cx, cy, cz, false).orElse(null);
        }

        if (spongeChunk == null) {
            if (!flag.create()) {
                return Optional.empty();
            }
            // This should generate a chunk so there won't be a need to regenerate one
            return this.loadChunk(cx, cy, cz, true);
        }

        final net.minecraft.world.chunk.Chunk chunk = (net.minecraft.world.chunk.Chunk) spongeChunk;
        final boolean keepEntities = flag.entities();
        try (final PhaseContext<?> context = GenerationPhase.State.CHUNK_REGENERATING.createPhaseContext()
            .chunk(chunk)) {
            context.buildAndSwitch();
            // If we reached this point, an existing chunk was found so we need to regen
            for (final ClassInheritanceMultiMap<net.minecraft.entity.Entity> multiEntityList : chunk.getEntityLists()) {
                for (final net.minecraft.entity.Entity entity : multiEntityList) {
                    if (entity instanceof EntityPlayerMP) {
                        playerList.add((EntityPlayerMP) entity);
                        entityList.add(entity);
                    } else if (keepEntities) {
                        entityList.add(entity);
                    }
                }
            }

            for (final net.minecraft.entity.Entity entity : entityList) {
                chunk.removeEntity(entity);
            }

            final ChunkProviderServer chunkProviderServer = (ChunkProviderServer) chunk.getWorld().getChunkProvider();
            ((ChunkProviderServerBridge) chunkProviderServer).bridge$unloadChunkAndSave(chunk);
            final net.minecraft.world.chunk.Chunk newChunk = ((ChunkProviderServerAccessor) chunkProviderServer).accessor$getChunkGenerator().generateChunk(cx, cz);
            final PlayerChunkMapEntry playerChunk = ((WorldServer) chunk.getWorld()).getPlayerChunkMap().getEntry(cx, cz);
            if (playerChunk != null) {
                ((PlayerChunkMapEntryBridge) playerChunk).bridge$setChunk(newChunk);
            }

            if (newChunk != null) {
                final WorldServer world = (WorldServer) newChunk.getWorld();
                ((ChunkProviderServerAccessor) world.getChunkProvider()).accessor$getLoadedChunks().put(ChunkPos.asLong(cx, cz), newChunk);
                newChunk.onLoad();
                ((ChunkAccessor) newChunk).accessor$populate(((ChunkProviderServerAccessor) world.getChunkProvider()).accessor$getChunkGenerator());
                for (final net.minecraft.entity.Entity entity: entityList) {
                    newChunk.addEntity(entity);
                }

                if (((ChunkProviderBridge) chunkProviderServer).bridge$getLoadedChunkWithoutMarkingActive(cx, cz) == null) {
                    return Optional.of((org.spongepowered.api.world.Chunk) newChunk);
                }

                final PlayerChunkMapEntry playerChunkMapEntry = ((WorldServer) newChunk.getWorld()).getPlayerChunkMap().getEntry(cx, cz);
                if (playerChunkMapEntry != null) {
                    final List<EntityPlayerMP> chunkPlayers = ((PlayerChunkMapEntryAccessor) playerChunkMapEntry).accessor$getPlayers();
                    // We deliberately send two packets, to avoid sending a 'fullChunk' packet
                    // (a changedSectionFilter of 65535). fullChunk packets cause the client to
                    // completely overwrite its current chunk with a new chunk instance. This causes
                    // weird issues, such as making any entities in that chunk invisible (until they leave it
                    // for a new chunk)
                    // - Aaron1011
                    for (final EntityPlayerMP playerMP: chunkPlayers) {
                        playerMP.connection.sendPacket(new SPacketChunkData(newChunk, 65534));
                        playerMP.connection.sendPacket(new SPacketChunkData(newChunk, 1));
                    }
                }
            }

            return Optional.of((org.spongepowered.api.world.Chunk) newChunk);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState blockState, final BlockChangeFlag flag) {
        checkBlockBounds(x, y, z);
        final IPhaseState<?> state = PhaseTracker.getInstance().getCurrentState();
        final boolean isWorldGen = state.isWorldGeneration();
        final boolean handlesOwnCompletion = state.handlesOwnStateCompletion();
        if (!isWorldGen) {
            checkArgument(flag != null, "BlockChangeFlag cannot be null!");
        }
        try (final PhaseContext<?> context = isWorldGen || handlesOwnCompletion
                ? null
                : PluginPhase.State.BLOCK_WORKER.createPhaseContext()) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return setBlockState(new BlockPos(x, y, z), (IBlockState) blockState, ((SpongeBlockChangeFlag) flag).getRawFlag());
        }
    }

    @Override
    public BlockSnapshot createSnapshot(final int x, final int y, final int z) {
        if (!containsBlock(x, y, z)) {
            return BlockSnapshot.NONE;
        }
        if (!isChunkLoaded(x >> 4, z >> 4, false)) {
            return BlockSnapshot.NONE;
        }
        final BlockPos pos = new BlockPos(x, y, z);
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.worldId(this.getUniqueId())
            .position(new Vector3i(x, y, z));
        final Chunk chunk = this.getChunk(pos);
        final IBlockState state = chunk.getBlockState(x, y, z);
        builder.blockState(state);
        try {
            builder.extendedState((BlockState) state.getActualState((WorldServer) (Object) this, pos));
        } catch (Throwable throwable) {
            // do nothing
        }
        final net.minecraft.tileentity.TileEntity tile = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK);
        if (tile != null) {
            TrackingUtil.addTileEntityToBuilder(tile, builder);
        }
        ((ChunkBridge) chunk).bridge$getBlockOwnerUUID(pos).ifPresent(builder::creator);
        ((ChunkBridge) chunk).bridge$getBlockNotifierUUID(pos).ifPresent(builder::notifier);

        builder.flag(BlockChangeFlags.NONE);


        return builder.build();
    }

    @Override
    public Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        final List<Entity> entitiesToSpawn = new NonNullArrayList<>();
        entities.forEach(entitiesToSpawn::add);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entitiesToSpawn);
        if (Sponge.getEventManager().post(event)) {
            return ImmutableList.of();
        }
        for (final Entity entity : event.getEntities()) {
            EntityUtil.processEntitySpawn(entity, Optional::empty);
        }

        final ImmutableList.Builder<Entity> builder = ImmutableList.builder();
        for (final Entity entity : event.getEntities()) {
            builder.add(entity);
        }
        return builder.build();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void triggerExplosion(org.spongepowered.api.world.explosion.Explosion explosion) {
        checkNotNull(explosion, "explosion");
        // Sponge start
        this.processingExplosion = true;
        // Set up the pre event
        final ExplosionEvent.Pre
            event =
            SpongeEventFactory.createExplosionEventPre(Sponge.getCauseStackManager().getCurrentCause(),
                explosion, this);
        if (SpongeImpl.postEvent(event)) {
            this.processingExplosion = false;
            return;
        }
        explosion = event.getExplosion();
        final Explosion mcExplosion;
        try {
            // Since we already have the API created implementation Explosion, let's use it.
            mcExplosion = (Explosion) explosion;
        } catch (Exception e) {
            new PrettyPrinter(60).add("Explosion not compatible with this implementation").centre().hr()
                .add("An explosion that was expected to be used for this implementation does not")
                .add("originate from this implementation.")
                .add(e)
                .trace();
            return;
        }

        try (final PhaseContext<?> ignored = GeneralPhase.State.EXPLOSION.createPhaseContext()
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
            this.processingExplosion = false;
        }
        // Sponge End
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public boolean spawnEntity(final Entity entity) {
        checkNotNull(entity, "The entity cannot be null!");
        if (PhaseTracker.isEntitySpawnInvalid(entity)) {
            return true;
        }
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final IPhaseState<?> state = phaseTracker.getCurrentState();
        if (!state.alreadyCapturingEntitySpawns()) {
            try (final BasicPluginContext context = PluginPhase.State.CUSTOM_SPAWN.createPhaseContext()) {
                context.buildAndSwitch();
                phaseTracker.spawnEntityWithCause(this, entity);
                return true;
            }
        }
        return phaseTracker.spawnEntityWithCause(this, entity);
    }


    @Override
    public WorldStorage getWorldStorage() {
        return (WorldStorage) ((WorldServer) (Object) this).getChunkProvider();
    }

    @Override
    public PortalAgent getPortalAgent() {
        return (PortalAgent) this.worldTeleporter;
    }

    @Override
    public void playSound(final SoundType sound, final SoundCategory category, final Vector3d position, final double volume) {
        this.playSound(sound, category, position, volume, 1);
    }

    @Override
    public void playSound(final SoundType sound,  final SoundCategory category, final Vector3d position, final double volume, final double pitch) {
        this.playSound(sound, category, position, volume, pitch, 0);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void playSound(final SoundType sound,  final SoundCategory category, final Vector3d position, final double volume, final double pitch, final double minVolume) {
        final SoundEvent event;
        try {
            // Check if the event is registered (ie has an integer ID)
            event = SoundEventsAccessor.accessor$getRegisteredSoundEvent(sound.getId());
        } catch (IllegalStateException e) {
            // Otherwise send it as a custom sound
            this.eventListeners.stream()
                .filter(listener -> listener instanceof ServerWorldEventHandlerBridge)
                .map(listener -> (ServerWorldEventHandlerBridge) listener)
                .forEach(listener -> {
                    // There's no method for playing a custom sound to all, so I made one -_-
                    listener.bridge$playCustomSoundToAllNearExcept(null,
                        sound.getId(),
                        (net.minecraft.util.SoundCategory) (Object) category,
                        position.getX(), position.getY(), position.getZ(),
                        (float) Math.max(minVolume, volume), (float) pitch);
                });
            return;
        }

        this.playSound(null, position.getX(), position.getY(), position.getZ(), event, (net.minecraft.util.SoundCategory) (Object) category,
                (float) Math.max(minVolume, volume), (float) pitch);
    }

    @Override
    public void stopSounds() {
        apiImpl$stopSounds(null, null);
    }

    @Override
    public void stopSounds(final SoundType sound) {
        apiImpl$stopSounds(checkNotNull(sound, "sound"), null);
    }

    @Override
    public void stopSounds(final SoundCategory category) {
        apiImpl$stopSounds(null, checkNotNull(category, "category"));
    }

    @Override
    public void stopSounds(final SoundType sound, final SoundCategory category) {
        apiImpl$stopSounds(checkNotNull(sound, "sound"), checkNotNull(category, "category"));
    }

    private void apiImpl$stopSounds(@Nullable final SoundType sound, @Nullable final SoundCategory category) {
        this.server.getPlayerList().sendPacketToAllPlayersInDimension(
                SoundEffectHelper.createStopSoundPacket(sound, category), ((WorldServerBridge) this).bridge$getDimensionId());
    }

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        final List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            final PlayerList playerList = this.server.getPlayerList();

            final double x = position.getX();
            final double y = position.getY();
            final double z = position.getZ();

            for (final Packet<?> packet : packets) {
                playerList.sendToAllNearExcept(null, x, y, z, radius, ((WorldServerBridge) this).bridge$getDimensionId(), packet);
            }
        }
    }

    @Override
    public void playRecord(final Vector3i position, final RecordType recordType) {
        api$playRecord(position, checkNotNull(recordType, "recordType"));
    }

    @Override
    public void stopRecord(final Vector3i position) {
        api$playRecord(position, null);
    }

    private void api$playRecord(final Vector3i position, @Nullable final RecordType recordType) {
        this.server.getPlayerList().sendPacketToAllPlayersInDimension(
                SpongeRecordType.createPacket(position, recordType), ((WorldServerBridge) this).bridge$getDimensionId());
    }

    @Override
    public Weather getWeather() {
        if (this.worldInfo.isThundering()) {
            return Weathers.THUNDER_STORM;
        }
        if (this.worldInfo.isRaining()) {
            return Weathers.RAIN;
        }
        return Weathers.CLEAR;
    }

    @Override
    public long getRemainingDuration() {
        final Weather weather = getWeather();
        if (weather.equals(Weathers.CLEAR)) {
            if (this.worldInfo.getCleanWeatherTime() > 0) {
                return this.worldInfo.getCleanWeatherTime();
            }
            return Math.min(this.worldInfo.getThunderTime(), this.worldInfo.getRainTime());
        }
        if (weather.equals(Weathers.THUNDER_STORM)) {
            return this.worldInfo.getThunderTime();
        }
        if (weather.equals(Weathers.RAIN)) {
            return this.worldInfo.getRainTime();
        }
        return 0;
    }

    @Override
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - ((WorldServerBridge) this).bridge$getWeatherStartTime();
    }

    @Override
    public void setWeather(final Weather weather) {
        this.setWeather(weather, (300 + this.rand.nextInt(600)) * 20);
    }

    @Override
    public void setWeather(final Weather weather, final long duration) {
        ((WorldServerBridge) this).bridge$setPreviousWeather(this.getWeather());
        if (weather.equals(Weathers.CLEAR)) {
            this.worldInfo.setCleanWeatherTime((int) duration);
            this.worldInfo.setRainTime(0);
            this.worldInfo.setThunderTime(0);
            this.worldInfo.setRaining(false);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.RAIN)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(false);
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            this.worldInfo.setCleanWeatherTime(0);
            this.worldInfo.setRainTime((int) duration);
            this.worldInfo.setThunderTime((int) duration);
            this.worldInfo.setRaining(true);
            this.worldInfo.setThundering(true);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getViewDistance() {
        return ((PlayerChunkMapAccessor) this.playerChunkMap).accessor$getPlayerViewRadius();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setViewDistance(final int viewDistance) {
        this.playerChunkMap.setPlayerViewRadius(viewDistance);
        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) this.getWorldInfo()).bridge$getConfigAdapter();
        // don't use the parameter, use the field that has been clamped
        configAdapter.getConfig().getWorld().setViewDistance(((PlayerChunkMapAccessor) this.playerChunkMap).accessor$getPlayerViewRadius());
        configAdapter.save();
    }

    @Override
    public void resetViewDistance() {
        this.setViewDistance(this.server.getPlayerList().getViewDistance());
    }

}
