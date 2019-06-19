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
package org.spongepowered.common.mixin.api.minecraft.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
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
import org.spongepowered.api.data.manipulator.DataManipulator;
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
import org.spongepowered.common.bridge.world.ChunkBridge;
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
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.world.ServerWorldEventHandlerBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.interfaces.world.ServerWorldBridge;
import org.spongepowered.common.util.NonNullArrayList;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(WorldServer.class)
public abstract class MixinWorldServer_API extends MixinWorld_API {

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow @Final private PlayerChunkMap playerChunkMap;
    @Shadow @Final @Mutable private Teleporter worldTeleporter;
    @Shadow @Final private WorldServer.ServerBlockEventList[] blockEventQueue;
    @Shadow private int blockEventCacheIndex;
    @Shadow private int updateEntityTick;

    @Shadow protected abstract void saveLevel() throws MinecraftException;
    @Shadow public abstract boolean fireBlockEvent(BlockEventData event);
    @Shadow protected abstract void createBonusChest();
    @Shadow @Nullable public abstract net.minecraft.entity.Entity getEntityFromUuid(UUID uuid);
    @Shadow public abstract PlayerChunkMap getPlayerChunkMap();
    @Shadow public abstract ChunkProviderServer getChunkProvider();
    @Shadow protected abstract void playerCheckLight();
    @Shadow protected abstract BlockPos adjustPosToNearbyEntity(BlockPos pos);
    @Shadow private boolean canAddEntity(net.minecraft.entity.Entity entityIn) {
        return false; // Shadowed
    }
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
        return ((ServerWorldBridge) this).bridge$getSpongeGenerator();
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        BlockPos pos = new BlockPos(x, y, z);
        this.updateBlockTick(pos, getBlockState(pos).getBlock(), ticks, priority);
        ScheduledBlockUpdate sbu = ((ServerWorldBridge) this).bridge$getScheduledBlockUpdate();
        ((ServerWorldBridge) this).bridge$setScheduledBlockUpdate(null);
        return sbu;
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
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
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        ImmutableList.Builder<ScheduledBlockUpdate> builder = ImmutableList.builder();
        for (NextTickListEntry sbu : this.pendingTickListEntriesTreeSet) {
            if (sbu.position.equals(position)) {
                builder.add((ScheduledBlockUpdate) sbu);
            }
        }
        return builder.build();
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        return Optional.ofNullable((Entity) this.getEntityFromUuid(uuid));
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState blockState, BlockChangeFlag flag) {
        checkBlockBounds(x, y, z);
        final IPhaseState<?> state = PhaseTracker.getInstance().getCurrentState();
        boolean isWorldGen = state.isWorldGeneration();
        boolean handlesOwnCompletion = state.handlesOwnStateCompletion();
        if (!isWorldGen) {
            checkArgument(flag != null, "BlockChangeFlag cannot be null!");
        }
        try (PhaseContext<?> context = isWorldGen || handlesOwnCompletion
                ? null
                : PluginPhase.State.BLOCK_WORKER.createPhaseContext()) {
            if (context != null) {
                context.buildAndSwitch();
            }
            return setBlockState(new BlockPos(x, y, z), (IBlockState) blockState, ((SpongeBlockChangeFlag) flag).getRawFlag());
        }
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        if (!isChunkLoaded(x, z, false)) {
            throw new IllegalStateException("Chunk not loaded");
        }
        BlockPos pos = new BlockPos(x, y, z);
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
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
            for (DataManipulator<?, ?> manipulator : ((CustomDataHolderBridge) tile).getCustomManipulators()) {
                builder.add(manipulator);
            }
            NBTTagCompound nbt = new NBTTagCompound();
            // Some mods like OpenComputers assert if attempting to save robot while moving
            try {
                tile.writeToNBT(nbt);
                builder.unsafeNbt(nbt);
            }
            catch(Throwable t) {
                // ignore
            }
        }
        ((ChunkBridge) chunk).getBlockOwnerUUID(pos).ifPresent(builder::creator);
        ((ChunkBridge) chunk).getBlockNotifierUUID(pos).ifPresent(builder::notifier);

        builder.flag(BlockChangeFlags.NONE);


        return builder.build();
    }

    @Override
    public Collection<Entity> spawnEntities(Iterable<? extends Entity> entities) {
        List<Entity> entitiesToSpawn = new NonNullArrayList<>();
        entities.forEach(entitiesToSpawn::add);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entitiesToSpawn);
        if (Sponge.getEventManager().post(event)) {
            return ImmutableList.of();
        }
        for (Entity entity : event.getEntities()) {
            EntityUtil.processEntitySpawn(entity, Optional::empty);
        }

        final ImmutableList.Builder<Entity> builder = ImmutableList.builder();
        for (Entity entity : event.getEntities()) {
            builder.add(entity);
        }
        return builder.build();
    }

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
            final double x = mcExplosion.x;
            final double y = mcExplosion.y;
            final double z = mcExplosion.z;
            final boolean damagesTerrain = explosion.shouldBreakBlocks();
            final float strength = explosion.getRadius();
            // Sponge End

            mcExplosion.doExplosionA();
            mcExplosion.doExplosionB(false);

            if (!damagesTerrain) {
                mcExplosion.clearAffectedBlockPositions();
            }

            for (EntityPlayer entityplayer : this.playerEntities) {
                if (entityplayer.getDistanceSq(x, y, z) < 4096.0D) {
                    ((EntityPlayerMP) entityplayer).connection
                        .sendPacket(new SPacketExplosion(x, y, z, strength, mcExplosion.getAffectedBlockPositions(),
                            mcExplosion.getPlayerKnockbackMap().get(entityplayer)));
                }
            }

            // Sponge Start - end processing
            this.processingExplosion = false;
        }
        // Sponge End
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        checkNotNull(entity, "The entity cannot be null!");
        if (PhaseTracker.isEntitySpawnInvalid(entity)) {
            return true;
        }
        final PhaseTracker phaseTracker = PhaseTracker.getInstance();
        final IPhaseState<?> state = phaseTracker.getCurrentState();
        if (!state.alreadyCapturingEntitySpawns()) {
            try (final BasicPluginContext context = PluginPhase.State.CUSTOM_SPAWN.createPhaseContext()
                .addCaptures()) {
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
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume) {
        this.playSound(sound, category, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound,  SoundCategory category, Vector3d position, double volume, double pitch) {
        this.playSound(sound, category, position, volume, pitch, 0);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void playSound(SoundType sound,  SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        SoundEvent event;
        try {
            // Check if the event is registered (ie has an integer ID)
            event = SoundEvents.getRegisteredSoundEvent(sound.getId());
        } catch (IllegalStateException e) {
            // Otherwise send it as a custom sound
            this.eventListeners.stream()
                .filter(listener -> listener instanceof ServerWorldEventHandlerBridge)
                .map(listener -> (ServerWorldEventHandlerBridge) listener)
                .forEach(listener -> {
                    // There's no method for playing a custom sound to all, so I made one -_-
                    listener.playCustomSoundToAllNearExcept(null,
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
    public void stopSounds(SoundType sound) {
        apiImpl$stopSounds(checkNotNull(sound, "sound"), null);
    }

    @Override
    public void stopSounds(SoundCategory category) {
        apiImpl$stopSounds(null, checkNotNull(category, "category"));
    }

    @Override
    public void stopSounds(SoundType sound, SoundCategory category) {
        apiImpl$stopSounds(checkNotNull(sound, "sound"), checkNotNull(category, "category"));
    }

    private void apiImpl$stopSounds(@Nullable SoundType sound, @Nullable SoundCategory category) {
        this.server.getPlayerList().sendPacketToAllPlayersInDimension(
                SoundEffectHelper.createStopSoundPacket(sound, category), ((ServerWorldBridge) this).bridge$getDimensionId());
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position) {
        this.spawnParticles(particleEffect, position, Integer.MAX_VALUE);
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            PlayerList playerList = this.server.getPlayerList();

            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            for (Packet<?> packet : packets) {
                playerList.sendToAllNearExcept(null, x, y, z, radius, ((ServerWorldBridge) this).bridge$getDimensionId(), packet);
            }
        }
    }

    @Override
    public void playRecord(Vector3i position, RecordType recordType) {
        api$playRecord(position, checkNotNull(recordType, "recordType"));
    }

    @Override
    public void stopRecord(Vector3i position) {
        api$playRecord(position, null);
    }

    private void api$playRecord(Vector3i position, @Nullable RecordType recordType) {
        this.server.getPlayerList().sendPacketToAllPlayersInDimension(
                SpongeRecordType.createPacket(position, recordType), ((ServerWorldBridge) this).bridge$getDimensionId());
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
        Weather weather = getWeather();
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
        return this.worldInfo.getWorldTotalTime() - ((ServerWorldBridge) this).bridge$getWeatherStartTime();
    }

    @Override
    public void setWeather(Weather weather) {
        this.setWeather(weather, (300 + this.rand.nextInt(600)) * 20);
    }

    @Override
    public void setWeather(Weather weather, long duration) {
        ((ServerWorldBridge) this).bridge$setPreviousWeather(this.getWeather());
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

    @Override
    public int getViewDistance() {
        return this.playerChunkMap.playerViewRadius;
    }

    @Override
    public void setViewDistance(final int viewDistance) {
        this.playerChunkMap.setPlayerViewRadius(viewDistance);
        final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) this.getWorldInfo()).getConfigAdapter();
        // don't use the parameter, use the field that has been clamped
        configAdapter.getConfig().getWorld().setViewDistance(this.playerChunkMap.playerViewRadius);
        configAdapter.save();
    }

    @Override
    public void resetViewDistance() {
        this.setViewDistance(this.server.getPlayerList().getViewDistance());
    }

}
