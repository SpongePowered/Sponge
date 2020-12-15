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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import net.kyori.adventure.sound.Sound;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.HeightTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.entity.MobEntityAccessor;
import org.spongepowered.common.accessor.network.play.server.SChangeBlockPacketAccessor;
import org.spongepowered.common.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeMusicDisc;
import org.spongepowered.common.entity.projectile.UnknownProjectileSource;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin_API<W extends World<W>> implements IWorldMixin_API<W>, World<W>, AutoCloseable {

    // @formatter:off
    @Shadow public @Final Random random;
    @Shadow protected @Final WorldInfo worldInfo;
    @Shadow @Final public List<TileEntity> blockEntityList;

    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract net.minecraft.world.chunk.Chunk shadow$getChunkAt(BlockPos p_175726_1_);
    @Shadow public abstract BlockState shadow$getBlockState(BlockPos p_180495_1_);
    @Shadow public abstract void shadow$playSound(@javax.annotation.Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract WorldInfo shadow$getWorldInfo();
    @Shadow public abstract boolean shadow$isThundering();
    @Shadow public abstract boolean shadow$isRaining();
    @Shadow public abstract void shadow$setBlockEntity(BlockPos pos, @javax.annotation.Nullable TileEntity tileEntityIn);
    @Shadow public abstract void shadow$removeBlockEntity(BlockPos pos);
    @Shadow public abstract DifficultyInstance shadow$getCurrentDifficultyAt(BlockPos p_175649_1_);
    @Shadow public abstract RegistryKey<net.minecraft.world.World> shadow$dimension();
    // @formatter:on

    private Context impl$context;

    // World

    @Override
    public Optional<? extends Player> getClosestPlayer(final int x, final int y, final int z, final double distance, final Predicate<? super Player> predicate) {
        final PlayerEntity player = ((net.minecraft.world.World) (Object) this).getNearestPlayer(x, y, z, distance, (Predicate) predicate);
        return Optional.ofNullable((Player) player);
    }

    @Override
    public BlockSnapshot createSnapshot(final int x, final int y, final int z) {
        if (!this.containsBlock(x, y, z)) {
            return BlockSnapshot.empty();
        }

        if (!this.isChunkLoaded(x, y, z, false)) { // TODO bitshift in old impl?
            return BlockSnapshot.empty();
        }
        final BlockPos pos = new BlockPos(x, y, z);
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.world((ServerWorld) (Object) this)
               .position(new Vector3i(x, y, z));
        final net.minecraft.world.chunk.Chunk chunk = this.shadow$getChunkAt(pos);
        final net.minecraft.block.BlockState state = chunk.getBlockState(pos);
        builder.blockState(state);
        final net.minecraft.tileentity.TileEntity tile = chunk.getBlockEntity(pos, net.minecraft.world.chunk.Chunk.CreateEntityType.CHECK);
        if (tile != null) {
            TrackingUtil.addTileEntityToBuilder(tile, builder);
        }
        ((ChunkBridge) chunk).bridge$getBlockCreatorUUID(pos).ifPresent(builder::creator);
        ((ChunkBridge) chunk).bridge$getBlockNotifierUUID(pos).ifPresent(builder::notifier);

        builder.flag(BlockChangeFlags.NONE);
        return builder.build();
    }

    @Override
    public boolean restoreSnapshot(final BlockSnapshot snapshot, final boolean force, final BlockChangeFlag flag) {
        return snapshot.restore(force, flag);
    }

    @Override
    public boolean restoreSnapshot(
        final int x, final int y, final int z, final BlockSnapshot snapshot, final boolean force, final BlockChangeFlag flag) {
        return snapshot.withLocation(this.getLocation(x, y, z)).restore(force, flag);
    }

    @Override
    public Chunk getChunk(final int cx, final int cy, final int cz) {
        return (Chunk) ((net.minecraft.world.World) (Object) this).getChunk(cx >> 4, cz >> 4, ChunkStatus.EMPTY, true);
    }

    @Override
    public Optional<Chunk> loadChunk(final int cx, final int cy, final int cz, final boolean shouldGenerate) {
        if (!SpongeChunkLayout.instance.isValidChunk(cx, cy, cz)) {
            return Optional.empty();
        }
        final AbstractChunkProvider chunkProvider = this.shadow$getChunkSource();
        // If we aren't generating, return the chunk
        if (!shouldGenerate) {
            // TODO correct ChunkStatus?
            return Optional.ofNullable((Chunk) chunkProvider.getChunk(cx, cz, ChunkStatus.EMPTY, true));
        }
        // TODO correct ChunkStatus?
        return Optional.ofNullable((Chunk) chunkProvider.getChunk(cx, cz, ChunkStatus.FULL, true));
    }

    @Override
    public Iterable<Chunk> getLoadedChunks() {
        final AbstractChunkProvider chunkProvider = this.shadow$getChunkSource();
        if (chunkProvider instanceof ServerChunkProvider) {
            final ChunkManagerAccessor chunkManager = (ChunkManagerAccessor) ((ServerChunkProvider) chunkProvider).chunkMap;
            final List<Chunk> chunks = new ArrayList<>();
            chunkManager.invoker$getChunks().forEach(holder -> chunks.add((Chunk) holder.getChunkIfComplete()));
            return chunks;
        }
        return Collections.emptyList();
    }

    // ReadableBlockVolume

    @Override
    public int getHighestYAt(final int x, final int z) {
        return this.getHeight(HeightTypes.WORLD_SURFACE.get(), x, z);
    }

    // Volume

    @Override
    public Vector3i getBlockMin() {
        return Constants.World.BLOCK_MIN;
    }

    @Override
    public Vector3i getBlockMax() {
        return Constants.World.BIOME_MAX;
    }

    @Override
    public Vector3i getBlockSize() {
        return Constants.World.BLOCK_SIZE;
    }

    // ContextSource
    
    @Override
    public Context getContext() {
        if (this.impl$context == null) {
            final RegistryKey<net.minecraft.world.World> worldKey = this.shadow$dimension();
            this.impl$context = new Context(Context.WORLD_KEY, worldKey.location().toString());
        }
        return this.impl$context;
    }

    // Viewer

    @Override
    public void spawnParticles(final ParticleEffect particleEffect, final Vector3d position, final int radius) {
        Preconditions.checkNotNull(particleEffect, "The particle effect cannot be null!");
        Preconditions.checkNotNull(position, "The position cannot be null");
        Preconditions.checkArgument(radius > 0, "The radius has to be greater then zero!");

        SpongeParticleHelper.sendPackets(particleEffect, position, radius,
                this.shadow$dimension(), this.shadow$getServer().getPlayerList());
    }

    private void api$playRecord(final Vector3i position, @javax.annotation.Nullable final MusicDisc recordType) {
        this.shadow$getServer().getPlayerList().broadcastAll(
                SpongeMusicDisc.createPacket(position, recordType), this.shadow$dimension());
    }

    @Override
    public void playMusicDisc(final Vector3i position, final MusicDisc musicDiscType) {
        this.api$playRecord(position, Preconditions.checkNotNull(musicDiscType, "recordType"));
    }

    @Override
    public void playMusicDisc(final Vector3i position, final Supplier<? extends MusicDisc> musicDiscType) {
        this.playMusicDisc(position, musicDiscType.get());
    }

    @Override
    public void stopMusicDisc(final Vector3i position) {
        this.api$playRecord(position, null);
    }

    @Override
    public void sendBlockChange(final int x, final int y, final int z, final org.spongepowered.api.block.BlockState state) {
        Preconditions.checkNotNull(state, "state");
        final SChangeBlockPacket packet = new SChangeBlockPacket();
        ((SChangeBlockPacketAccessor) packet).accessor$pos(new BlockPos(x, y, z));
        ((SChangeBlockPacketAccessor) packet).accessor$blockState((BlockState) state);

        ((net.minecraft.world.World) (Object) this).players().stream()
                .filter(ServerPlayerEntity.class::isInstance)
                .map(ServerPlayerEntity.class::cast)
                .forEach(p -> p.connection.send(packet));
    }

    @Override
    public void resetBlockChange(final int x, final int y, final int z) {
        final SChangeBlockPacket packet = new SChangeBlockPacket((IWorldReader) this, new BlockPos(x, y, z));

        ((net.minecraft.world.World) (Object) this).players().stream()
                .filter(ServerPlayerEntity.class::isInstance)
                .map(ServerPlayerEntity.class::cast)
                .forEach(p -> p.connection.send(packet));
    }

    // ArchetypeVolumeCreator

    // Audience

    @Override
    public void playSound(final Sound sound, final double x, final double y, final double z) {
        // Check if the event is registered (ie has an integer ID)
        final ResourceLocation soundKey = SpongeAdventure.asVanilla(sound.name());
        final Optional<SoundEvent> event = Registry.SOUND_EVENT.getOptional(soundKey);
        final SoundCategory soundCategory = SpongeAdventure.asVanilla(sound.source());
        if (event.isPresent()) {
            this.shadow$playSound(null,x, y, z, event.get(), soundCategory, sound.volume(), sound.pitch());
        } else {
            // Otherwise send it as a custom sound
            final float volume = sound.volume();
            final double radius = volume > 1.0f ? (16.0f * volume) : 16.0d;
            final SPlaySoundPacket packet = new SPlaySoundPacket(soundKey, soundCategory, new net.minecraft.util.math.vector.Vector3d(x, y, z), volume, sound.pitch());
            this.shadow$getServer().getPlayerList().broadcast(null, x, y, z, radius, this.shadow$dimension(), packet);
        }
    }

    @Override
    public Collection<? extends BlockEntity> getBlockEntities() {
        return (Collection) this.blockEntityList;
    }

    public boolean allowsPlayerRespawns() {
        return this.shadow$getDimension().canRespawnHere();
    }

    @Override
    public boolean doesWaterEvaporate() {
        return this.shadow$getDimension().doesWaterVaporize();
    }

    @Override
    public boolean hasSkylight() {
        return this.shadow$getDimension().hasSkyLight();
    }

    @Override
    public boolean isCaveWorld() {
        return this.shadow$getDimension().isNether();
    }

    @Override
    public boolean isSurfaceWorld() {
        return this.shadow$getDimension().isSurfaceWorld();
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "BlockEntity cannot be null!");
        final TileEntity tileEntity = (TileEntity) blockEntity;
        this.shadow$setBlockEntity(new BlockPos(x, y, z), tileEntity);
    }

    // MutableEntityVolume

    @Override
    public <E extends org.spongepowered.api.entity.Entity> E createEntity(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException,
            IllegalStateException {
        return this.impl$createEntity(type, position, false);
    }

    @Override
    public <E extends org.spongepowered.api.entity.Entity> E createEntityNaturally(final EntityType<E> type, final Vector3d position) throws IllegalArgumentException,
            IllegalStateException {
        return this.impl$createEntity(type, position, true);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(final DataContainer entityContainer) {
        throw new UnsupportedOperationException("Implement me"); // TODO implement me
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(final DataContainer entityContainer, final Vector3d position) {
        throw new UnsupportedOperationException("Implement me"); // TODO implement me
    }

    public <E extends org.spongepowered.api.entity.Entity> E impl$createEntity(final EntityType<E> type, final Vector3d position, final boolean naturally) throws IllegalArgumentException,
            IllegalStateException {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");

        if (type == net.minecraft.entity.EntityType.PLAYER) {
            // Unable to construct these
            throw new IllegalArgumentException("Cannot construct " + type.getKey() + " please look to using entity types correctly!");
        }

        net.minecraft.entity.Entity entity = null;
        final double x = position.getX();
        final double y = position.getY();
        final double z = position.getZ();
        final net.minecraft.world.World thisWorld = (net.minecraft.world.World) (Object) this;
        // Not all entities have a single World parameter as their constructor
        if (type == net.minecraft.entity.EntityType.LIGHTNING_BOLT) {
            entity = net.minecraft.entity.EntityType.LIGHTNING_BOLT.create(thisWorld);
            entity.moveTo(x, y, z);
            ((LightningBoltEntity) entity).setVisualOnly(false);
        }
        // TODO - archetypes should solve the problem of calling the correct constructor
        if (type == net.minecraft.entity.EntityType.ENDER_PEARL) {
            final ArmorStandEntity tempEntity = new ArmorStandEntity(thisWorld, x, y, z);
            tempEntity.setPos(tempEntity.getX(), tempEntity.getY() - tempEntity.getEyeHeight(), tempEntity.getZ());
            entity = new EnderPearlEntity(thisWorld, tempEntity);
            ((EnderPearl) entity).offer(Keys.SHOOTER, UnknownProjectileSource.UNKNOWN);
        }
        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (type == net.minecraft.entity.EntityType.FALLING_BLOCK) {
            entity = new FallingBlockEntity(thisWorld, x, y, z, Blocks.SAND.defaultBlockState());
        }
        if (type == net.minecraft.entity.EntityType.ITEM) {
            entity = new ItemEntity(thisWorld, x, y, z, new ItemStack(Blocks.STONE));
        }

        if (entity == null) {
            try {
                entity = ((net.minecraft.entity.EntityType) type).create(thisWorld);
                entity.moveTo(x, y, z);
            } catch (final Exception e) {
                throw new RuntimeException("There was an issue attempting to construct " + type.getKey(), e);
            }
        }

        // TODO - replace this with an actual check
        /*
        if (entity instanceof EntityHanging) {
            if (((EntityHanging) entity).facingDirection == null) {
                // TODO Some sort of detection of a valid direction?
                // i.e scan immediate blocks for something to attach onto.
                ((EntityHanging) entity).facingDirection = EnumFacing.NORTH;
            }
            if (!((EntityHanging) entity).onValidSurface()) {
                return Optional.empty();
            }
        }*/

        if (naturally && entity instanceof MobEntity) {
            // Adding the default equipment
            final DifficultyInstance difficulty = this.shadow$getCurrentDifficultyAt(new BlockPos(x, y, z));
            ((MobEntityAccessor)entity).invoker$populateDefaultEquipmentSlots(difficulty);
        }

        if (entity instanceof PaintingEntity) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            ((PaintingEntity) entity).motive = PaintingType.KEBAB;
        }

        return (E) entity;
    }
}
