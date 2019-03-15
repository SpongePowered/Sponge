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
package org.spongepowered.common.mixin.core.world;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Fluids;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumLightType;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.property.Property;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.music.MusicDisc;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.ScheduledUpdateList;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.BookView;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.HeightType;
import org.spongepowered.api.util.TemporalUnits;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.LightType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.api.world.chunk.ProtoChunk;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.teleport.PortalAgent;
import org.spongepowered.api.world.volume.EntityHit;
import org.spongepowered.api.world.volume.biome.ImmutableBiomeVolume;
import org.spongepowered.api.world.volume.biome.UnmodifiableBiomeVolume;
import org.spongepowered.api.world.volume.biome.worker.MutableBiomeVolumeStream;
import org.spongepowered.api.world.volume.block.ImmutableBlockVolume;
import org.spongepowered.api.world.volume.block.UnmodifiableBlockVolume;
import org.spongepowered.api.world.volume.block.worker.MutableBlockVolumeStream;
import org.spongepowered.api.world.volume.entity.ImmutableEntityVolume;
import org.spongepowered.api.world.volume.entity.UnmodifiableEntityVolume;
import org.spongepowered.api.world.volume.entity.worker.MutableEntityStream;
import org.spongepowered.api.world.volume.tileentity.worker.TileEntityStream;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.effect.record.SpongeMusicDisc;
import org.spongepowered.common.effect.sound.SoundEffectHelper;
import org.spongepowered.common.entity.EntityFactory;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinServerWorldEventHandler;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.mixin.tracking.world.MixinWorld_Tracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeEmptyChunk;
import org.spongepowered.common.world.WorldUtil;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.volume.biome.UnmodifiableDownsizedBiomeVolume;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

/**
 * Core API implementation of API added methods for {@link World} and
 * {@link org.spongepowered.api.world.World} that is also delegated from
 * other mixins, such as {@link MixinIWorld_API} that does a lot of over
 * riding for the sake of keeping it clear where our implementation is going.
 */
@SuppressWarnings("unchecked")
@Mixin(World.class)
public abstract class MixinWorld_API implements MixinIWorld_API<org.spongepowered.api.world.World>, org.spongepowered.api.world.World {

    @Nullable private Context worldContext;

    // Shadows
    @Shadow @Final public Random rand;
    @Shadow @Final public List<EntityPlayer> playerEntities;
    @Shadow @Final public List<net.minecraft.entity.Entity> loadedEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> loadedTileEntityList;
    @Shadow protected WorldInfo worldInfo;
    @Shadow protected List<IWorldEventListener> eventListeners;

    @Shadow @Nullable public abstract EntityPlayer shadow$getClosestPlayer(double x, double y, double z, double distance, Predicate<net.minecraft.entity.Entity> predicate);
    @Shadow public abstract <T extends net.minecraft.entity.Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter);
    @Shadow public abstract boolean addTileEntity(net.minecraft.tileentity.TileEntity tile);
    @Shadow @Nullable public abstract net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos);
    @Shadow public abstract IChunkProvider getChunkProvider();
    @Shadow public abstract void playSound(EntityPlayer player, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, net.minecraft.util.SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract boolean isBlockPresent(BlockPos pos);
    @Shadow public abstract int getSeaLevel();
    @Shadow public abstract <T extends net.minecraft.entity.Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter);
    @Shadow public abstract <T extends net.minecraft.entity.Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter);
    @Shadow public abstract boolean removeBlock(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);

    @Shadow @Final public List<net.minecraft.entity.Entity> weatherEffects;

    /**
     * Specifically verify the {@link UUID} for this world is going to be valid, in
     * certain cases, there are mod worlds that are extending {@link net.minecraft.world.World}
     * and have custom {@link WorldInfo}s, which ends up causing issues with
     * plugins expecting a valid uuid for each world.
     *
     * <p>TODO There may be some issues with plugins somehow picking up these "fake"
     * worlds with regards to their block changes, and therefor cause issues when
     * those plugins are finding those worlds, instead of traditional
     * {@link WorldServer} instances.</p>
     *
     * @return The world id, verified from the properties
     */
    @Override
    public UUID getUniqueId() {
        final WorldProperties properties = this.getProperties();
        final UUID worldId = properties.getUniqueId();
        if (worldId == null) {
            // Some mod worlds make their own WorldInfos for "fake" worlds.
            // Specifically fixes https://github.com/SpongePowered/SpongeForge/issues/1527
            // and https://github.com/BuildCraft/BuildCraft/issues/3594
            final IMixinWorldInfo mixinWorldInfo = (IMixinWorldInfo) properties;
            mixinWorldInfo.setUniqueId(UUID.randomUUID());
            return properties.getUniqueId();
        }
        return worldId;
    }

    @Override
    public Collection<Player> getPlayers() {
        // This sanitizes our list so that we only get Player instances, regardless whether
        // we're on client or server world, we only care about EntityPlayerMP's for now.
        // TODO - change to support client worlds.
        return (List) this.getPlayers(EntityPlayerMP.class, player -> player instanceof Player);
    }


    @Override
    public Optional<Player> getClosestPlayer(int x, int y, int z, double distance, Predicate<? super Player> predicate) {
        final Predicate<net.minecraft.entity.Entity> entityPredicate = entity -> entity instanceof Player;
        final EntityPlayer
            closest =
            shadow$getClosestPlayer(x, y, z, distance,  entityPredicate.and((Predicate<net.minecraft.entity.Entity>) (Predicate) predicate));
        return Optional.ofNullable(EntityUtil.toPlayer(closest));
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            return BlockSnapshot.NONE;
        }
        final BlockPos target = new BlockPos(x, y, z);
        final IChunk chunk = getChunk(x << 4, z << 4);
        // Empty chunks have nothing. We do not want to chunk load at all
        if (((ProtoChunk) chunk).isEmpty()) {
            return BlockSnapshot.NONE;
        }
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
        final IBlockState blockState = chunk.getBlockState(target);
        final net.minecraft.tileentity.TileEntity tile = chunk.getTileEntity(target);
        builder.blockState(BlockUtil.fromNative(blockState))
            .worldId(getUniqueId())
            .position(new Vector3i(x, y, z));
        if (chunk instanceof IMixinChunk) {
            ((IMixinChunk) chunk).getBlockNotifierUUID(x, y, z).ifPresent(builder::notifier);
            ((IMixinChunk) chunk).getBlockOwnerUUID(x, y, z).ifPresent(builder::creator);
        }
        if (tile != null) {
            ((IMixinCustomDataHolder) tile).getCustomManipulators().forEach(builder::add);
            final NBTTagCompound compound = new NBTTagCompound();
            tile.write(compound);
            builder.unsafeNbt(compound);
        }
        return builder.build();
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        return snapshot.restore(force, flag);
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, BlockChangeFlag flag) {
        snapshot = snapshot.withLocation(new Location(this, new Vector3i(x, y, z)));
        return snapshot.restore(force, flag);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Chunk getChunk(int cx, int cy, int cz) {
        if (!SpongeChunkLayout.instance.isValidChunk(cx, cy, cz)) {
            throw new IllegalStateException(String.format("Invalid chunk position %s %s %s", cx, cy, cz));
        }
        final WorldServer worldserver = (WorldServer) (Object) this;
        final net.minecraft.world.chunk.Chunk loadedChunk = worldserver.getChunkProvider().getLoadedChunk(cx, cz);
        final Vector3i chunkPos = SpongeChunkLayout.instance.forceToChunk(cx, cy, cz);
        return  loadedChunk == null ? (Chunk) new SpongeEmptyChunk((World) (Object) this, chunkPos.getX(), chunkPos.getZ()) : (Chunk) loadedChunk;
    }

    @Override
    public org.spongepowered.api.world.World getWorld() {
        return WorldUtil.fromNative(this.shadow$getWorld());
    }

    @Override
    public boolean setBiome(int x, int y, int z, BiomeType biome) {
        return getChunk(x, y, z).setBiome(x, y, z, biome);
    }

    @Override
    public MutableBiomeVolumeStream<org.spongepowered.api.world.World> toBiomeStream() {
        return null; // TODO - implement streams
    }

    @Override
    public UnmodifiableBiomeVolume<?> asUnmodifiableBiomeVolume() {
        return new UnmodifiableDownsizedBiomeVolume(this, WorldUtil.BIOME_MIN, WorldUtil.BIOME_MAX);
    }

    @Override
    public ImmutableBiomeVolume asImmutableBiomeVolume() {
        return null; // TODO - implement buffers
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of(EntityUtil.fromNative(entity));
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<Entity> getEntities() {
        return Lists.newArrayList((Collection<Entity>) (Object) this.loadedEntityList);
    }

    @Override
    public Collection<Entity> getEntities(Predicate<Entity> filter) {
        // This already returns a new copy
        return (Collection<Entity>) (Object) this.getEntities(net.minecraft.entity.Entity.class,
            (Predicate<net.minecraft.entity.Entity>) (Object) filter);
    }

    @Override
    public UnmodifiableEntityVolume<?> asUnmodifiableEntityVolume() {
        return null; // todo - implement
    }

    @Override
    public ImmutableEntityVolume asImmutableEntityVolume() {
        return null; // Todo - implement. I don't know if this will be possible though...
    }

    @Override
    public Collection<TileEntity> getTileEntities() {
        return Lists.newArrayList((List<TileEntity>) (Object) this.loadedTileEntityList);
    }

    @Override
    public Optional<TileEntity> getTileEntity(int x, int y, int z) {
        return Optional.ofNullable((TileEntity) getTileEntity(new BlockPos(x, y, z)));
    }

    @Override
    public Vector3i getBlockMin() {
        return WorldUtil.BLOCK_MIN;
    }

    @Override
    public Vector3i getBlockMax() {
        return WorldUtil.BLOCK_MAX;
    }

    @Override
    public Vector3i getBlockSize() {
        return WorldUtil.BLOCK_SIZE;
    }


    @Override
    public boolean isAreaAvailable(int x, int y, int z) {
        return isChunkLoaded(x, y, z, false);
    }

    @Override
    public org.spongepowered.api.world.World getView(Vector3i newMin, Vector3i newMax) {
        return this;
    }

    @Override
    public TileEntityStream<org.spongepowered.api.world.World, ?> toTileEntityStream() {
        return null; // TODO - implement
    }

    @Override
    public long getSeed() {
        return this.worldInfo.getSeed();
    }

    @Override
    public TerrainGenerator<?> getTerrainGenerator() {
        return (TerrainGenerator) this.getChunkProvider().getChunkGenerator();
    }

    @Override
    public WorldProperties getProperties() {
        return (WorldProperties) this.worldInfo;
    }

    @Override
    public Optional<Chunk> loadChunk(int cx, int cy, int cz, boolean shouldGenerate) {
        if (!SpongeChunkLayout.instance.isValidChunk(cx, cy, cz)) {
            return Optional.empty();
        }
        final WorldServer worldserver = (WorldServer) (Object) this;
        // If we aren't generating, return the chunk
        if (!shouldGenerate) {
            return Optional.ofNullable((Chunk) worldserver.getChunkProvider().loadChunk(cx, cz));
        }
        return Optional.ofNullable((Chunk) worldserver.getChunkProvider().provideChunk(cx, cz));
    }

    @Override
    public Optional<Chunk> regenerateChunk(int cx, int cy, int cz, ChunkRegenerateFlag flag) {
        return Optional.empty(); // Todo - reimplement with new system.
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        checkArgument(chunk != null, "Chunk cannot be null!");
        return chunk.unloadChunk();
    }

    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return Collections.emptyList(); // Implement in WorldServer/Client
    }

    @Override
    public Path getDirectory() {
        return null; // Todo - Zidane?
    }

    @Override
    public WorldStorage getWorldStorage() {
        return null; // TODO - zidane?
    }

    @Override
    public void triggerExplosion(Explosion explosion) {
        checkNotNull(explosion, "explosion");
        ((net.minecraft.world.Explosion) explosion).doExplosionA();
        ((net.minecraft.world.Explosion) explosion).doExplosionB(true);
    }

    @Override
    public PortalAgent getPortalAgent() {
        return null; // TODO - zidane?
    }

    @Override
    public boolean save() throws IOException {
        return false; // TODO - Zidane?
    }

    @Override
    public int getViewDistance() {
        return 0; // TODO - Zidane? Maybe we need to consider some of these to be dependent on World type so it's not an error on client.
    }

    @Override
    public void setViewDistance(int viewDistance) {
        // TODO - Zidane?
    }

    @Override
    public void resetViewDistance() {
        // TODO - Zidane?
    }

    @Override
    public boolean isLoaded() {
        return false; // TODO - Zidane?
    }

    @Override
    public void spawnParticles(ParticleEffect particleEffect, Vector3d position, int radius) {
        checkNotNull(particleEffect, "The particle effect cannot be null!");
        checkNotNull(position, "The position cannot be null");
        checkArgument(radius > 0, "The radius has to be greater then zero!");

        List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            for (Player player : this.getPlayers()) {
                if (player.getPosition().distanceSquared(position) < radius * radius) {
                    // TODO - this will not function on client because the player
                    // entities are not PlayerMP's.
                    final NetHandlerPlayServer connection = EntityUtil.toNative(player).connection;
                    packets.forEach(connection::sendPacket);
                }
            }
        }
    }

    @Override
    public void playSound(SoundType sound, SoundCategory category, Vector3d position, double volume, double pitch, double minVolume) {
        // Check if the event is registered (ie has an integer ID)
        SoundEvent event = SoundEvent.REGISTRY.get(new ResourceLocation(sound.getKey().toString()));
        if (event == null) {
            // Otherwise send it as a custom sound
            this.playCustomSound(null, position.getX(), position.getY(), position.getZ(), (ResourceLocation) (Object) sound.getKey(),
                (net.minecraft.util.SoundCategory) (Object) category, (float) Math.max(minVolume, volume), (float) pitch);
            return;
        }
        this.playSound(null, position.getX(), position.getY(), position.getZ(), event, (net.minecraft.util.SoundCategory) (Object) category,
            (float) Math.max(minVolume, volume), (float) pitch);

    }

    protected void playCustomSound(@Nullable EntityPlayer player, double x, double y, double z, ResourceLocation soundIn, net.minecraft.util.SoundCategory category,
        float volume, float pitch) {

        if (player instanceof IMixinEntity) {
            if (((IMixinEntity) player).isVanished()) {
                return;
            }
        }

        this.eventListeners.stream()
            .filter(listener -> listener instanceof IMixinServerWorldEventHandler)
            .map(listener -> (IMixinServerWorldEventHandler) listener)
            .forEach(listener -> {
                // There's no method for playing a custom sound to all, so I made one -_-
                listener.playCustomSoundToAllNearExcept(null, soundIn, category, x, y, z, volume, pitch);
            });
    }

    @Override
    public void stopSounds() {
        stopSounds0(null, null);
    }

    @Override
    public void stopSounds(SoundType sound) {
        stopSounds0(checkNotNull(sound, "sound"), null);
    }

    @Override
    public void stopSounds(SoundCategory category) {
        stopSounds0(null, checkNotNull(category, "category"));
    }

    @Override
    public void stopSounds(SoundType sound, SoundCategory category) {
        stopSounds0(checkNotNull(sound, "sound"), checkNotNull(category, "category"));
    }

    private void stopSounds0(@Nullable SoundType sound, @Nullable SoundCategory category) {
        for (Player player : this.getPlayers()) {
            EntityUtil.toNative(player).connection.sendPacket(SoundEffectHelper.createStopSoundPacket(sound, category));
        }
    }

    @Override
    public void playMusicDisc(Vector3i position, MusicDisc musicDiscType) {
        for (Player player : this.getPlayers()) {
            EntityUtil.toNative(player).connection.sendPacket(SpongeMusicDisc.createPacket(position, musicDiscType));
        }
    }

    @Override
    public void stopMusicDisc(Vector3i position) {
        for (Player player : this.getPlayers()) {
            EntityUtil.toNative(player).connection.sendPacket(SpongeMusicDisc.createPacket(position, null));
        }
    }

    @Override
    public void resetTitle() {
        getPlayers().forEach(Player::resetTitle);
    }

    @Override
    public void clearTitle() {
        getPlayers().forEach(Player::clearTitle);
    }

    @Override
    public void sendTitle(Title title) {
        checkNotNull(title, "title");

        for (Player player : getPlayers()) {
            player.sendTitle(title);
        }
    }

    @Override
    public void sendBookView(BookView bookView) {
        // TODO - can't really do that...
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        checkNotNull(state, "state");
        final BlockPos pos = new BlockPos(x, y, z);
        SPacketBlockChange packet = new SPacketBlockChange(BlockUtil.readerOf(BlockUtil.toNative(state), pos), pos);

        for (EntityPlayer player : this.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).connection.sendPacket(packet);
            }
        }
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        SPacketBlockChange packet = new SPacketBlockChange((net.minecraft.world.World) (Object) this, new BlockPos(x, y, z));

        for (EntityPlayer player : this.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).connection.sendPacket(packet);
            }
        }
    }

    @Override
    public Context getContext() {
        if (this.worldContext == null) {
            WorldInfo worldInfo = getWorldInfo();
            if (worldInfo == null) {
                // We still have to consider some mods are making dummy worlds that
                // override getWorldInfo with a null, or submit a null value.
                worldInfo = new WorldInfo(new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT), "sponge$dummy_World");
            }
            this.worldContext = new Context(Context.WORLD_KEY, worldInfo.getWorldName());
        }
        return this.worldContext;
    }

    @Override
    public void sendMessage(ChatType type, Text message) {
        checkNotNull(type, "type");
        checkNotNull(message, "message");

        for (Player player : this.getPlayers()) {
            player.sendMessage(type, message);
        }
    }

    @Override
    public void sendMessage(Text message) {
        checkNotNull(message, "Message");
        getPlayers().forEach(player -> player.sendMessage(message));
    }

    @Override
    public MessageChannel getMessageChannel() {
        return null; // TODO - how the hell has this not been implemented at all?
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        // TODO - this doesn't appear to have ever been implemented.
    }

    @Override
    public Location getLocation(Vector3i position) {
        return new Location(this, position);
    }

    @Override
    public Location getLocation(Vector3d position) {
        return new Location(this, position);
    }

    @Override
    public boolean hitBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return false; // TODO - implement?
    }

    @Override
    public boolean interactBlock(int x, int y, int z, Direction side, GameProfile profile) {
        return false; // TODO - implement?
    }

    @Override
    public boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, GameProfile profile) {
        return false; // TODO - implement?
    }

    @Override
    public boolean placeBlock(int x, int y, int z, BlockState block, Direction side, GameProfile profile) {
        return false; // TODO - implement?
    }

    @Override
    public boolean digBlock(int x, int y, int z, GameProfile profile) {
        return false; // TODO - implement?
    }

    @Override
    public boolean digBlockWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return false; // TODO - implement?
    }

    @Override
    public Duration getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, GameProfile profile) {
        return null; // TODO - implement
    }

    @Override
    public boolean canSeeSky(int x, int y, int z) {
        final net.minecraft.world.chunk.Chunk loadedChunk = getChunkProvider().getLoadedChunk(x >> 4, z >> 4);
        return (loadedChunk != null && !loadedChunk.isEmpty()) && loadedChunk.canSeeSky(new BlockPos(x, y, z));
    }

    @Override
    public boolean hasWater(int x, int y, int z) {
        if (y < 0 || y >= 256) {
            return false;
        } else {
            net.minecraft.world.chunk.Chunk chunk = this.getChunkProvider().getLoadedChunk(x >> 4, z >> 4);
            if (chunk != null && !chunk.isEmpty()) {
                return chunk.getFluidState(x, y, z).isTagged(FluidTags.WATER);
            }
            return false;
        }
    }

    @Override
    public Random getRandom() {
        return this.rand;
    }

    @Override
    public int getHeight(HeightType type, int x, int z) {
        net.minecraft.world.chunk.Chunk chunk = this.getChunkProvider().getLoadedChunk(x >> 4, z >> 4);
        if (chunk != null && !chunk.isEmpty()) {
            return chunk.getTopBlockY((Heightmap.Type) (Object) type, x & 15, z & 15) + 1;
        }
        return getSeaLevel() + 1;
    }

    @Override
    public int getLight(LightType type, int x, int y, int z) {
        if (!VecHelper.inBounds(x, y, z, getBlockMin(), getBlockMax())) {
            return ((EnumLightType) (Object) type).defaultLightValue;
        }
        net.minecraft.world.chunk.Chunk chunk = this.getChunkProvider().getLoadedChunk(x >> 4, z >> 4);
        if (chunk != null && !chunk.isEmpty()) {
            return chunk.getLightFor((EnumLightType) (Object) type, new BlockPos(x, y, z));
        }
        return ((EnumLightType) (Object) type).defaultLightValue;
    }

    /*
     * Enabled via Mixins with {@link MixinWorld_Tracker}
     */
    @Override
    public Optional<UUID> getCreator(int x, int y, int z) {
        return Optional.empty();
    }

    /*
     * Enabled via Mixins with {@link MixinWorld_Tracker}
     */
    @Override
    public Optional<UUID> getNotifier(int x, int y, int z) {
        return Optional.empty();
    }

    /*
     * Enabled via Mixins with {@link MixinWorld_Tracker}
     */
    @Override
    public void setCreator(int x, int y, int z, @Nullable UUID uuid) {

    }

    /*
     * Enabled via Mixins with {@link MixinWorld_Tracker}
     */
    @Override
    public void setNotifier(int x, int y, int z, @Nullable UUID uuid) {

    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState blockState, BlockChangeFlag flag) {
        // Default to use this, because otherwise we want to have all Worlds still using this method
        // but, eventually, with client support, we will need to be able to send the block changes via client worlds.
        throw new UnsupportedOperationException("Oh no! You've found an implementation of World not part of Sponge implementation!");
    }

    @Override
    public Set<Entity> getIntersectingEntities(AABB box, Predicate<Entity> filter) {
        checkNotNull(box, "box");
        checkNotNull(filter, "filter");
        return getEntitiesWithinAABB(net.minecraft.entity.Entity.class, VecHelper.toMinecraftAABB(box), entity -> filter.test(EntityUtil.fromNative(entity)))
            .stream()
            .map(entity -> (Entity) entity)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, Predicate<EntityHit> filter) {
        checkNotNull(start, "start");
        checkNotNull(end, "end");
        final Vector3d diff = end.sub(start);
        return getIntersectingEntities(start, diff.normalize(), diff.length(), filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d direction, double distance, Predicate<EntityHit> filter) {
        checkNotNull(start, "start");
        checkNotNull(direction, "direction");
        checkNotNull(filter, "filter");
        // Ensure that the direction has unit length
        direction = direction.normalize();
        // If the direction is vertical only, we don't need to do any chunk tracing, just defer immediately to the containing chunk
        if (direction.getX() == 0 && direction.getZ() == 0) {
            return getIntersectingEntities(start, direction.getY(), distance, filter);

        }
        // Adapted from BlockRay
        final int chunkWidth = SpongeChunkLayout.CHUNK_SIZE.getX();
        // Figure out the direction of the ray for each axis
        final int xPlaneIncrement = direction.getX() >= 0 ? chunkWidth : -chunkWidth;
        final int zPlaneIncrement = direction.getZ() >= 0 ? chunkWidth : -chunkWidth;
        // First planes are for the chunk that contains the coordinates
        double xInChunk = GenericMath.mod(start.getX(), chunkWidth);
        double zInChunk = GenericMath.mod(start.getZ(), chunkWidth);
        int xPlaneNext = (int) (start.getX() - xInChunk);
        int zPlaneNext = (int) (start.getZ() - zInChunk);
        // Correct the next planes to they start just behind the starting position
        if (xInChunk != 0 && direction.getX() < 0) {
            xPlaneNext += chunkWidth;
        }
        if (zInChunk != 0 && direction.getZ() < 0) {
            zPlaneNext += chunkWidth;
        }
        // Compute the first intersection solutions for each plane
        double xPlaneT = (xPlaneNext - start.getX()) / direction.getX();
        double zPlaneT = (zPlaneNext - start.getZ()) / direction.getZ();
        // Keep track of the last distance using the t multiplier
        double currentT = 0;
        // Keep tack of the last intersection y coordinate
        double xCurrent = start.getX();
        double yCurrent = start.getY();
        double zCurrent = start.getZ();
        // Trace each chunks until the remaining distance goes below 0
        double remainingDistance = distance;
        // Trace the chunks in 2D to find which contain possibly intersecting entities
        final Set<EntityHit> intersecting = new HashSet<>();
        do {
            final double nextT;
            final double xNext;
            final double yNext;
            final double zNext;
            // Find the closest intersection and its coordinates
            if (xPlaneT < zPlaneT) {
                nextT = xPlaneT;
                // Update current position
                xNext = xPlaneNext;
                zNext = direction.getZ() * nextT + start.getZ();
                // Prepare next intersection
                xPlaneNext += xPlaneIncrement;
                xPlaneT = (xPlaneNext - start.getX()) / direction.getX();
            } else {
                nextT = zPlaneT;
                // Update current position
                xNext = direction.getX() * nextT + start.getX();
                zNext = zPlaneNext;
                // Prepare next intersection
                zPlaneNext += zPlaneIncrement;
                zPlaneT = (zPlaneNext - start.getZ()) / direction.getZ();
            }
            // Don't go over the distance when calculating the next intersection y
            yNext = direction.getY() * Math.min(nextT, distance) + start.getY();
            // Ignore the first few intersections behind the starting position
            // although we still use them to position the current coordinates on a chunk boundary
            if (nextT >= 0) {
                // Get the coordinates of the chunk that was last entered (correct for entering from the back plane)
                xInChunk = GenericMath.mod(xCurrent, chunkWidth);
                zInChunk = GenericMath.mod(zCurrent, chunkWidth);
                final int xChunk = (int) (xCurrent - (xInChunk == 0 && direction.getX() < 0 ? chunkWidth : xInChunk));
                final int zChunk = (int) (zCurrent - (zInChunk == 0 && direction.getZ() < 0 ? chunkWidth : zInChunk));
                // Make sure the start position in the chunk is not before the world start position
                final Vector3d chunkStart = currentT <= 0 ? start : new Vector3d(xCurrent, yCurrent, zCurrent);
                // Get the chunk and call the intersection method to perform the task locally
                final Chunk chunk = getChunkAtBlock(xChunk, 0, zChunk);
                if (!chunk.isEmpty()) {
                    ((IMixinChunk) chunk).getIntersectingEntities(chunkStart, direction, remainingDistance, filter, chunkStart.getY(), yNext, intersecting);
                }
                // If the intersections are near another chunk, its entities might be partially in the current chunk, so include it also
                final IMixinChunk nearIntersections = getChunkNearIntersections(xChunk, zChunk, xCurrent, zCurrent, xNext, zNext);
                if (nearIntersections != null && !nearIntersections.isEmpty()) {
                    nearIntersections
                        .getIntersectingEntities(chunkStart, direction, remainingDistance, filter, chunkStart.getY(), yNext, intersecting);
                }
                // Remove the chunk from the distance
                remainingDistance -= nextT - Math.max(0, currentT);
            }
            // Update the current intersection to the new one
            currentT = nextT;
            xCurrent = xNext;
            yCurrent = yNext;
            zCurrent = zNext;
        } while (remainingDistance >= 0);
        return intersecting;
    }

    private Set<EntityHit> getIntersectingEntities(Vector3d start, double yDirection, double distance, Predicate<EntityHit> filter) {
        final Set<EntityHit> intersecting = new HashSet<>();
        // Current chunk
        final Vector3d direction = yDirection < 0 ? Vector3d.UNIT_Y.negate() : Vector3d.UNIT_Y;
        final double endY = start.getY() + yDirection * distance;
        final Vector3i chunkPos = SpongeChunkLayout.instance.forceToChunk(start.toInt());
        ((IMixinChunk) getChunk(chunkPos)).getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        // Check adjacent chunks if near them
        final int nearDistance = 2;
        // Neighbour -x chunk
        final Vector3i chunkBlockPos = SpongeChunkLayout.instance.forceToWorld(chunkPos);
        if (start.getX() - chunkBlockPos.getX() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(-1, 0, 0)))
                .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour -z chunk
        if (start.getZ() - chunkBlockPos.getZ() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(0, 0, -1)))
                .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour +x chunk
        final int chunkWidth = SpongeChunkLayout.CHUNK_SIZE.getX();
        if (chunkBlockPos.getX() + chunkWidth - start.getX() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(1, 0, 0)))
                .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour +z chunk
        if (chunkBlockPos.getZ() + chunkWidth - start.getZ() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(0, 0, 1)))
                .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        return intersecting;
    }

    @Nullable
    private IMixinChunk getChunkNearIntersections(int xChunk, int zChunk, double xCurrent, double zCurrent, double xNext, double zNext) {
        final int chunkWidth = SpongeChunkLayout.CHUNK_SIZE.getX();
        // Chunk corner coordinates
        final Vector2d c1 = new Vector2d(xChunk, zChunk);
        final Vector2d c2 = new Vector2d(xChunk + chunkWidth, zChunk);
        final Vector2d c3 = new Vector2d(xChunk, zChunk + chunkWidth);
        final Vector2d c4 = new Vector2d(xChunk + chunkWidth, zChunk + chunkWidth);
        // The square of the distance we consider as being near
        final int nearDistance2 = 2 * 2;
        // Under the assumption that both intersections aren't on the same face
        // Look for two intersection being near the same corner
        final boolean d11 = c1.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d21 = c1.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d11 && d21) {
            // Near corner -x, -z
            return (IMixinChunk) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk - chunkWidth);
        }
        final boolean d12 = c2.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d22 = c2.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d12 && d22) {
            // Near corner +x, -z
            return (IMixinChunk) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk - chunkWidth);
        }
        final boolean d13 = c3.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d23 = c3.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d13 && d23) {
            // Near corner -x, +z
            return (IMixinChunk) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk + chunkWidth);
        }
        final boolean d14 = c4.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d24 = c4.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d14 && d24) {
            // Near corner +x, +z
            return (IMixinChunk) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk + chunkWidth);
        }
        // Look for two intersections being near the corners on the same face
        if (d11 && d23 || d21 && d13) {
            // Near face -x
            return (IMixinChunk) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk);
        }
        if (d11 && d22 || d21 && d12) {
            // Near face -z
            return (IMixinChunk) getChunkAtBlock(xChunk, 0, zChunk - chunkWidth);
        }
        if (d14 && d22 || d24 && d12) {
            // Near face +x
            return (IMixinChunk) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk);
        }
        if (d14 && d23 || d24 && d13) {
            // Near face +z
            return (IMixinChunk) getChunkAtBlock(xChunk, 0, zChunk + chunkWidth);
        }
        return null;
    }


    @Override
    public Optional<AABB> getBlockSelectionBox(int x, int y, int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        final IBlockState state = getBlockState(pos);
        final AxisAlignedBB box = state.getShape((World) (Object) this, pos).getBoundingBox();
        try {
            return Optional.of(VecHelper.toSpongeAABB(box).offset(x, y, z));
        } catch (IllegalArgumentException exception) {
            // Box is degenerate
            return Optional.empty();
        }
    }

    @Override
    public Set<AABB> getIntersectingBlockCollisionBoxes(AABB box) {
        return null;
    }

    @Override
    public Set<AABB> getIntersectingCollisionBoxes(Entity owner, AABB box) {
        return null;
    }


    @Override
    public Entity createEntity(EntityType type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return this.createEntity(type, position, false);
    }

    @Override
    public Entity createEntityNaturally(EntityType type, Vector3d position) throws IllegalArgumentException, IllegalStateException {
        return this.createEntity(type, position, true);
    }

    private Entity createEntity(EntityType type, Vector3d position, boolean naturally) throws IllegalArgumentException, IllegalStateException {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");
        checkState(type instanceof SpongeEntityType<?, ?>, "Don't know how to create a %s", type.getKey());
        return (Entity) EntityFactory
            .create((SpongeEntityType<?, ?>) type, (net.minecraft.world.World) (Object) this, position.getX(), position.getY(), position.getZ(), naturally);
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        return false; // TODO - deterine when to use PhasseTracker for methods like these
    }

    @Override
    public Collection<Entity> spawnEntities(Iterable<? extends Entity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
            .map(EntityUtil::toNative)
            .filter(this::spawnEntity)
            .map(EntityUtil::fromNative)
            .collect(Collectors.toList());
    }

    @Override
    public MutableEntityStream<org.spongepowered.api.world.World> toEntityStream() {
        return null; // todo - implement streams
    }

    @Override
    public boolean removeBlock(int x, int y, int z) {
        return removeBlock(new BlockPos(x, y,  z));
    }

    @Override
    public MutableBlockVolumeStream<org.spongepowered.api.world.World> toBlockStream() {
        return null; // todo - implement streams
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        if (y < 0 || y >= 256) {
           return BlockTypes.AIR.getDefaultState();
        }
        final net.minecraft.world.chunk.Chunk loadedChunk = this.getChunkProvider().getLoadedChunk(x >> 4, z >> 4);
        if (loadedChunk != null) {
            return BlockUtil.fromNative(loadedChunk.getBlockState(x, y, z));
        }
        return BlockTypes.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluid(int x, int y, int z) {
        return getBlock(x, y, z).getFluidState();
    }

    @Override
    public UnmodifiableBlockVolume<?> asUnmodifiableBlockVolume() {
        return null; // TODO - implement wrappers
    }

    @Override
    public ImmutableBlockVolume asImmutableBlockVolume() {
        return null; // Probably will never do it.
    }

    @Override
    public int getHighestYAt(int x, int z) {
        return getHeight(Heightmap.Type.WORLD_SURFACE, x, z);
    }

    @Override
    public Weather getWeather() {
        if (this.worldInfo.isThundering()) {
            return Weathers.THUNDER_STORM;
        } else if (this.worldInfo.isRaining()) {
            return Weathers.RAIN;
        } else {
            return Weathers.CLEAR;
        }
    }

    @Override
    public Duration getRemainingWeatherDuration() {
        Weather weather = getWeather();
        if (weather.equals(Weathers.CLEAR)) {
            if (this.worldInfo.getClearWeatherTime() > 0) {
                return Duration.of(this.worldInfo.getClearWeatherTime(), TemporalUnits.MINECRAFT_TICKS);
            }
            return Duration.of(Math.min(this.worldInfo.getThunderTime(), this.worldInfo.getRainTime()), TemporalUnits.MINECRAFT_TICKS);
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            return Duration.of(this.worldInfo.getThunderTime(), TemporalUnits.MINECRAFT_TICKS);
        } else if (weather.equals(Weathers.RAIN)) {
            return Duration.of(this.worldInfo.getRainTime(), TemporalUnits.MINECRAFT_TICKS);
        }
        return Duration.ZERO;
    }

    @Override
    public Duration getRunningWeatherDuration() {
        throw new UnsupportedOperationException("Oh no! You've found an implementation of World not part of Sponge implementation!");
    }

    @Override
    public void setWeather(Weather weather) {
        throw new UnsupportedOperationException("Oh no! You've found an implementation of World not part of Sponge implementation!");
    }

    @Override
    public void setWeather(Weather weather, Duration duration) {
        throw new UnsupportedOperationException("Oh no! You've found an implementation of World not part of Sponge implementation!");
    }
}
