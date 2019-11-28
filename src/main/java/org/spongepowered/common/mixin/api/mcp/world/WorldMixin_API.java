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

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityPainting.EnumArt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Functional;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.ChunkPreGenerate;
import org.spongepowered.api.world.ChunkRegenerateFlag;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.worker.MutableBiomeVolumeWorker;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.registry.provider.DirectionFacingProvider;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeDimension;
import org.spongepowered.common.world.extent.ExtentViewDownsize;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeVolumeWorker;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;
import org.spongepowered.common.world.pregen.SpongeChunkPreGenerateTask;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin_API implements World {

    // @formatter:off
    @Shadow @Final public boolean isRemote;
    @Shadow @Final public WorldProvider provider;
    @Shadow @Final public Random rand;
    @Shadow @Final public Profiler profiler;
    @Shadow @Final public List<EntityPlayer> playerEntities;
    @Shadow @Final public List<net.minecraft.entity.Entity> loadedEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> loadedTileEntityList;
    @Shadow @Final protected ISaveHandler saveHandler;
    @Shadow protected List<IWorldEventListener> eventListeners;
    @Shadow private int seaLevel;
    @Shadow protected WorldInfo worldInfo;

    @Shadow public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();
    @Shadow public net.minecraft.world.World init() { throw new RuntimeException("Bad things have happened"); }
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunk(BlockPos pos);
    @Shadow public abstract WorldInfo getWorldInfo();
    @Shadow public abstract Biome getBiome(BlockPos pos);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract List<net.minecraft.entity.Entity> getEntities(Class<net.minecraft.entity.Entity> entityType,
            com.google.common.base.Predicate<net.minecraft.entity.Entity> filter);
    @Shadow public abstract <T extends net.minecraft.entity.Entity> List<T> getEntitiesWithinAABB(Class <? extends T > clazz, AxisAlignedBB aabb,
            com.google.common.base.Predicate<? super T > filter);
    @Shadow public abstract MinecraftServer getMinecraftServer();
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state);
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state, int flags);
    @Shadow public abstract void playSound(EntityPlayer p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, net.minecraft.util.SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow public abstract BlockPos getPrecipitationHeight(BlockPos pos);
    @Shadow public abstract List<AxisAlignedBB> getCollisionBoxes(net.minecraft.entity.Entity entityIn, AxisAlignedBB bb);
    @Shadow public abstract int getHeight(int x, int z);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow @Nullable public abstract net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos);

    @Nullable private Context worldContext;
    boolean processingExplosion = false;
    @Nullable private SpongeDimension api$dimension;

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Player> getPlayers() {
        return ImmutableList.copyOf((Collection<Player>) (Object) this.playerEntities);
    }

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
            final WorldInfoBridge mixinWorldInfo = (WorldInfoBridge) properties;
            mixinWorldInfo.bridge$setUniqueId(UUID.randomUUID());
            return properties.getUniqueId();
        }
        return worldId;
    }

    @Override
    public Optional<Chunk> getChunk(int x, int y, int z) {
        if (!SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return Optional.empty();
        }
        final WorldServer worldserver = (WorldServer) (Object) this;
        return Optional.ofNullable((Chunk) worldserver.func_72863_F().func_186026_b(x, z));
    }

    @Override
    public Optional<Chunk> loadChunk(int x, int y, int z, boolean shouldGenerate) {
        if (!SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return Optional.empty();
        }
        final WorldServer worldserver = (WorldServer) (Object) this;
        // If we aren't generating, return the chunk
        if (!shouldGenerate) {
            return Optional.ofNullable((Chunk) worldserver.func_72863_F().func_186028_c(x, z));
        }
        return Optional.ofNullable((Chunk) worldserver.func_72863_F().func_186025_d(x, z));
    }

    @Override
    public Optional<Chunk> regenerateChunk(int cx, int cy, int cz, ChunkRegenerateFlag flag) {
        return Optional.empty(); // World does not do this, WorldServer can, but not WorldClient.
    }

    @Override
    public int getHighestYAt(int x, int z) {
        return this.getHeight(x, z);
    }

    @Override
    public int getPrecipitationLevelAt(int x, int z) {
        return this.getPrecipitationHeight(new BlockPos(x, 0, z)).func_177956_o();
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        // avoid intermediate object creation from using BlockState
        return (BlockType) getChunk(x >> 4, z >> 4).func_177435_g(new BlockPos(x, y, z)).func_177230_c();
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        return setBlock(x, y, z, block, BlockChangeFlags.ALL);
    }


    @Override
    public BiomeType getBiome(int x, int y, int z) {
        return (BiomeType) this.getBiome(new BlockPos(x, y, z));
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        checkBiomeBounds(x, y, z);
        ((Chunk) getChunk(x >> 4, z >> 4)).setBiome(x, y, z, biome);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Entity> getEntities() {
        return Lists.newArrayList((Collection<Entity>) (Object) this.loadedEntityList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Entity> getEntities(Predicate<Entity> filter) {
        // This already returns a new copy
        return (Collection<Entity>) (Object) this.getEntities(net.minecraft.entity.Entity.class,
                Functional.java8ToGuava((Predicate<net.minecraft.entity.Entity>) (Object) filter));
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

        Entity entity = null;

        Class<? extends Entity> entityClass = type.getEntityClass();
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        if (entityClass.isAssignableFrom(EntityPlayerMP.class) || entityClass.isAssignableFrom(MultiPartEntityPart.class)) {
            // Unable to construct these
            throw new IllegalArgumentException("Cannot construct " + type.getId() + " please look to using entity types correctly!");
        }

        net.minecraft.world.World world = (net.minecraft.world.World) (Object) this;

        // TODO - archetypes should solve the problem of calling the correct constructor
        // Not all entities have a single World parameter as their constructor
        if (entityClass.isAssignableFrom(EntityLightningBolt.class)) {
            entity = (Entity) new EntityLightningBolt(world, x, y, z, false);
        } else if (entityClass.isAssignableFrom(EntityEnderPearl.class)) {
            EntityArmorStand tempEntity = new EntityArmorStand(world, x, y, z);
            tempEntity.field_70163_u -= tempEntity.func_70047_e();
            entity = (Entity) new EntityEnderPearl(world, tempEntity);
            ((EnderPearl) entity).setShooter(ProjectileSource.UNKNOWN);
        }

        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (entityClass.isAssignableFrom(EntityFallingBlock.class)) {
            entity = (Entity) new EntityFallingBlock(world, x, y, z, Blocks.field_150354_m.func_176223_P());
        } else if (entityClass.isAssignableFrom(EntityItem.class)) {
            entity = (Entity) new EntityItem(world, x, y, z, new ItemStack(Blocks.field_150348_b));
        }

        if (entity == null) {
            try {
                entity = ConstructorUtils.invokeConstructor(entityClass, this);
                ((net.minecraft.entity.Entity) entity).func_70107_b(x, y, z);
            } catch (Exception e) {
                throw new RuntimeException("There was an issue attempting to construct " + type.getId(), e);
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

        if (naturally && entity instanceof EntityLiving) {
            // Adding the default equipment
            ((EntityLiving)entity).func_180482_a(world.func_175649_E(new BlockPos(x, y, z)), null);
        }

        if (entity instanceof EntityPainting) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            ((EntityPainting) entity).field_70522_e = EnumArt.KEBAB;
        }

        return entity;
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer) {
        // TODO once entity containers are implemented
        return Optional.empty();
    }

    @Override
    public Optional<Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        // TODO once entity containers are implemented
        return Optional.empty();
    }

    @Override
    public Optional<Entity> restoreSnapshot(EntitySnapshot snapshot, Vector3d position) {
        EntitySnapshot entitySnapshot = snapshot.withLocation(new Location<>(this, position));
        return entitySnapshot.restore();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return (WorldBorder) shadow$getWorldBorder();
    }

    @Override
    public ChunkPreGenerate.Builder newChunkPreGenerate(Vector3d center, double diameter) {
        return new SpongeChunkPreGenerateTask.Builder(this, center, diameter);
    }


    @Override
    public Dimension getDimension() {
        if (this.api$dimension == null) {
            this.api$dimension = new SpongeDimension(this.provider);
        }
        return this.api$dimension;
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        // Note that WorldServerMixin is properly overriding this to use it's own mapping.
        for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
            if (entity.func_110124_au().equals(uuid)) {
                return Optional.of((Entity) entity);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Iterable<Chunk> getLoadedChunks() {
        if (((WorldBridge) this).bridge$isFake()) { // If we're client side, we can't know solidly what loaded chunks are... need to do this in MixinWorldClient in forge.
            return Collections.emptyList();
        }
        return (List<Chunk>) (List<?>) Lists.newArrayList(((WorldServer) (Object) this).func_72863_F().func_189548_a());
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        checkArgument(chunk != null, "Chunk cannot be null!");
        return chunk.unloadChunk();
    }

    @Override
    public WorldProperties getProperties() {
        return (WorldProperties) this.worldInfo;
    }

    @Override
    public Context getContext() {
        if (this.worldContext == null) {
            WorldInfo worldInfo = getWorldInfo();
            if (worldInfo == null) {
                // We still have to consider some mods are making dummy worlds that
                // override getWorldInfo with a null, or submit a null value.
                worldInfo = new WorldInfo(new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.field_77137_b), "sponge$dummy_World");
            }
            this.worldContext = new Context(Context.WORLD_KEY, worldInfo.func_76065_j());
        }
        return this.worldContext;
    }
    @Override
    public Optional<TileEntity> getTileEntity(int x, int y, int z) {
        net.minecraft.tileentity.TileEntity tileEntity = getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null) {
            return Optional.empty();
        }
        return Optional.of((TileEntity) tileEntity);
    }

    @Override
    public Vector3i getBiomeMin() {
        return Constants.World.BIOME_MIN;
    }

    @Override
    public Vector3i getBiomeMax() {
        return Constants.World.BIOME_MAX;
    }

    @Override
    public Vector3i getBiomeSize() {
        return Constants.World.BIOME_SIZE;
    }

    @Override
    public Vector3i getBlockMin() {
        return Constants.World.BLOCK_MIN;
    }

    @Override
    public Vector3i getBlockMax() {
        return Constants.World.BLOCK_MAX;
    }

    @Override
    public Vector3i getBlockSize() {
        return Constants.World.BLOCK_SIZE;
    }

    @Override
    public boolean containsBiome(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, Constants.World.BIOME_MIN, Constants.World.BIOME_MAX);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, Constants.World.BLOCK_MIN, Constants.World.BLOCK_MAX);
    }

    private void checkBiomeBounds(int x, int y, int z) {
        if (!containsBiome(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), Constants.World.BIOME_MIN, Constants.World.BIOME_MAX);
        }
    }

    protected void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), Constants.World.BLOCK_MIN, Constants.World.BLOCK_MAX);
        }
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
    public void sendTitle(Title title) {
        checkNotNull(title, "title");

        for (Player player : getPlayers()) {
            player.sendTitle(title);
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

    @SuppressWarnings("unchecked")
    @Override
    public Collection<TileEntity> getTileEntities() {
        return Lists.newArrayList((List<TileEntity>) (Object) this.loadedTileEntityList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<TileEntity> getTileEntities(Predicate<TileEntity> filter) {
        return ((List<TileEntity>) (Object) this.loadedTileEntityList).stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    @Override
    public void triggerExplosion(Explosion explosion) {
        checkNotNull(explosion, "explosion");
        ((net.minecraft.world.Explosion) explosion).func_77278_a();
        ((net.minecraft.world.Explosion) explosion).func_77279_a(true);
    }

    @Override
    public Extent getExtentView(Vector3i newMin, Vector3i newMax) {
        return new ExtentViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBiomeVolumeWorker<World> getBiomeWorker() {
        return new SpongeMutableBiomeVolumeWorker<>(this);
    }

    @Override
    public MutableBlockVolumeWorker<World> getBlockWorker() {
        return new SpongeMutableBlockVolumeWorker<>(this);
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            return BlockSnapshot.NONE;
        }
        World world = this;
        BlockState state = world.getBlock(x, y, z);
        Optional<TileEntity> te = world.getTileEntity(x, y, z);
        SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled()
                .blockState(state)
                .worldId(world.getUniqueId())
                .position(new Vector3i(x, y, z));
        Optional<UUID> creator = getCreator(x, y, z);
        Optional<UUID> notifier = getNotifier(x, y, z);
        if (creator.isPresent()) {
            builder.creator(creator.get());
        }
        if (notifier.isPresent()) {
            builder.notifier(notifier.get());
        }
        if (te.isPresent()) {
            final TileEntity tileEntity = te.get();
            for (DataManipulator<?, ?> manipulator : ((CustomDataHolderBridge) tileEntity).bridge$getCustomManipulators()) {
                builder.add(manipulator);
            }
            final NBTTagCompound compound = new NBTTagCompound();
            ((net.minecraft.tileentity.TileEntity) tileEntity).func_189515_b(compound);
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
        snapshot = snapshot.withLocation(new Location<>(this, new Vector3i(x, y, z)));
        return snapshot.restore(force, flag);
    }

    @Override
    public Optional<UUID> getCreator(int x, int y, int z) {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getNotifier(int x, int y, int z) {
        return Optional.empty();
    }

    @Override
    public void setCreator(int x, int y, int z, @Nullable UUID uuid) {
    }

    @Override
    public void setNotifier(int x, int y, int z, @Nullable UUID uuid) {
    }

    @Override
    public Optional<AABB> getBlockSelectionBox(int x, int y, int z) {
        final BlockPos pos = new BlockPos(x, y, z);
        final IBlockState state = getBlockState(pos);
        final AxisAlignedBB box = state.func_185900_c((IBlockAccess) this, pos);
        try {
            return Optional.of(VecHelper.toSpongeAABB(box).offset(x, y, z));
        } catch (IllegalArgumentException exception) {
            // Box is degenerate
            return Optional.empty();
        }
    }

    @Override
    public Set<Entity> getIntersectingEntities(AABB box, Predicate<Entity> filter) {
        checkNotNull(box, "box");
        checkNotNull(filter, "filter");
        return getEntitiesWithinAABB(net.minecraft.entity.Entity.class, VecHelper.toMinecraftAABB(box), entity -> filter.test((Entity) entity))
                .stream()
                .map(entity -> (Entity) entity)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<AABB> getIntersectingBlockCollisionBoxes(AABB box) {
        checkNotNull(box, "box");
        return getCollisionBoxes(null, VecHelper.toMinecraftAABB(box)).stream()
                .map(VecHelper::toSpongeAABB)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<AABB> getIntersectingCollisionBoxes(Entity owner, AABB box) {
        checkNotNull(owner, "owner");
        checkNotNull(box, "box");
        return getCollisionBoxes((net.minecraft.entity.Entity) owner, VecHelper.toMinecraftAABB(box)).stream()
                .map(VecHelper::toSpongeAABB)
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
                final Optional<Chunk> chunk = getChunkAtBlock(xChunk, 0, zChunk);
                if (chunk.isPresent()) {
                    ((ChunkBridge) chunk.get())
                            .bridge$getIntersectingEntities(chunkStart, direction, remainingDistance, filter, chunkStart.getY(), yNext, intersecting);
                }
                // If the intersections are near another chunk, its entities might be partially in the current chunk, so include it also
                final ChunkBridge nearIntersections = getChunkNearIntersections(xChunk, zChunk, xCurrent, zCurrent, xNext, zNext);
                if (nearIntersections != null) {
                    nearIntersections
                            .bridge$getIntersectingEntities(chunkStart, direction, remainingDistance, filter, chunkStart.getY(), yNext, intersecting);
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

    private ChunkBridge getChunkNearIntersections(int xChunk, int zChunk, double xCurrent, double zCurrent, double xNext, double zNext) {
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
            return (ChunkBridge) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk - chunkWidth).orElse(null);
        }
        final boolean d12 = c2.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d22 = c2.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d12 && d22) {
            // Near corner +x, -z
            return (ChunkBridge) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk - chunkWidth).orElse(null);
        }
        final boolean d13 = c3.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d23 = c3.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d13 && d23) {
            // Near corner -x, +z
            return (ChunkBridge) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk + chunkWidth).orElse(null);
        }
        final boolean d14 = c4.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d24 = c4.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d14 && d24) {
            // Near corner +x, +z
            return (ChunkBridge) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk + chunkWidth).orElse(null);
        }
        // Look for two intersections being near the corners on the same face
        if (d11 && d23 || d21 && d13) {
            // Near face -x
            return (ChunkBridge) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk).orElse(null);
        }
        if (d11 && d22 || d21 && d12) {
            // Near face -z
            return (ChunkBridge) getChunkAtBlock(xChunk, 0, zChunk - chunkWidth).orElse(null);
        }
        if (d14 && d22 || d24 && d12) {
            // Near face +x
            return (ChunkBridge) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk).orElse(null);
        }
        if (d14 && d23 || d24 && d13) {
            // Near face +z
            return (ChunkBridge) getChunkAtBlock(xChunk, 0, zChunk + chunkWidth).orElse(null);
        }
        return null;
    }

    private Set<EntityHit> getIntersectingEntities(Vector3d start, double yDirection, double distance, Predicate<EntityHit> filter) {
        final Set<EntityHit> intersecting = new HashSet<>();
        // Current chunk
        final Vector3d direction = yDirection < 0 ? Vector3d.UNIT_Y.negate() : Vector3d.UNIT_Y;
        final double endY = start.getY() + yDirection * distance;
        final Vector3i chunkPos = SpongeChunkLayout.instance.forceToChunk(start.toInt());
        ((ChunkBridge) getChunk(chunkPos).get()).bridge$getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        // Check adjacent chunks if near them
        final int nearDistance = 2;
        // Neighbour -x chunk
        final Vector3i chunkBlockPos = SpongeChunkLayout.instance.forceToWorld(chunkPos);
        if (start.getX() - chunkBlockPos.getX() <= nearDistance) {
            ((ChunkBridge) getChunk(chunkPos.add(-1, 0, 0)).get())
                    .bridge$getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour -z chunk
        if (start.getZ() - chunkBlockPos.getZ() <= nearDistance) {
            ((ChunkBridge) getChunk(chunkPos.add(0, 0, -1)).get())
                    .bridge$getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour +x chunk
        final int chunkWidth = SpongeChunkLayout.CHUNK_SIZE.getX();
        if (chunkBlockPos.getX() + chunkWidth - start.getX() <= nearDistance) {
            ((ChunkBridge) getChunk(chunkPos.add(1, 0, 0)).get())
                    .bridge$getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour +z chunk
        if (chunkBlockPos.getZ() + chunkWidth - start.getZ() <= nearDistance) {
            ((ChunkBridge) getChunk(chunkPos.add(0, 0, 1)).get())
                    .bridge$getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        return intersecting;
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        checkNotNull(state, "state");
        SPacketBlockChange packet = new SPacketBlockChange();
        packet.field_179828_a = new BlockPos(x, y, z);
        packet.field_148883_d = (IBlockState) state;

        for (EntityPlayer player : this.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).field_71135_a.func_147359_a(packet);
            }
        }
    }

    @Override
    public void resetBlockChange(int x, int y, int z) {
        SPacketBlockChange packet = new SPacketBlockChange((net.minecraft.world.World) (Object) this, new BlockPos(x, y, z));

        for (EntityPlayer player : this.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) player).field_71135_a.func_147359_a(packet);
            }
        }
    }


    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }


    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Class<T> propertyClass) {
        final Optional<PropertyStore<T>> optional = Sponge.getPropertyRegistry().getStore(propertyClass);
        return optional.flatMap(tPropertyStore -> tPropertyStore.getFor(new Location<>(this, x, y, z)));
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Direction direction, Class<T> propertyClass) {
        final Optional<PropertyStore<T>> optional = Sponge.getPropertyRegistry().getStore(propertyClass);
        return optional.flatMap(tPropertyStore -> tPropertyStore.getFor(new Location<>(this, x, y, z), direction));
    }

    @Override
    public Collection<Property<?, ?>> getProperties(int x, int y, int z) {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(new Location<World>(this, x, y, z));
    }

    @Override
    public Collection<Direction> getFacesWithProperty(int x, int y, int z, Class<? extends Property<?, ?>> propertyClass) {
        final Optional<? extends PropertyStore<? extends Property<?, ?>>> optional = Sponge.getPropertyRegistry().getStore(propertyClass);
        if (!optional.isPresent()) {
            return Collections.emptyList();
        }
        final PropertyStore<? extends Property<?, ?>> store = optional.get();
        final Location<World> loc = new Location<>(this, x, y, z);
        ImmutableList.Builder<Direction> faces = ImmutableList.builder();
        for (EnumFacing facing : EnumFacing.values()) {
            Direction direction = DirectionFacingProvider.getInstance().getKey(facing).get();
            if (store.getFor(loc, direction).isPresent()) {
                faces.add(direction);
            }
        }
        return faces.build();
    }

    @Override
    public <E> Optional<E> get(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        final Optional<E> optional = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z)).get(key);
        if (optional.isPresent()) {
            return optional;
        }
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        return tileEntityOptional.flatMap(tileEntity -> tileEntity.get(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> get(int x, int y, int z, Class<T> manipulatorClass) {
        final Collection<DataManipulator<?, ?>> manipulators = getManipulators(x, y, z);
        for (DataManipulator<?, ?> manipulator : manipulators) {
            if (manipulatorClass.isInstance(manipulator)) {
                return Optional.of((T) manipulator);
            }
        }
        return Optional.empty();
    }

    @Override
    public <T extends DataManipulator<?, ?>> Optional<T> getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        final Optional<T> optional = get(x, y, z, manipulatorClass);
        if (optional.isPresent()) {
            return optional;
        }
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        return tileEntity.flatMap(tileEntity1 -> tileEntity1.getOrCreate(manipulatorClass));
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(int x, int y, int z, Key<V> key) {
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        if (blockState.supports(key)) {
            return blockState.getValue(key);
        }
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        if (tileEntity.isPresent() && tileEntity.get().supports(key)) {
            return tileEntity.get().getValue(key);
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(int x, int y, int z, Key<?> key) {
        final BlockState blockState = getBlock(x, y, z);
        final boolean blockSupports = blockState.supports(key);
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        final boolean tileEntitySupports = tileEntity.isPresent() && tileEntity.get().supports(key);
        return blockSupports || tileEntitySupports;
    }

    @Override
    public boolean supports(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        final BlockState blockState = getBlock(x, y, z);
        final List<ImmutableDataManipulator<?, ?>> immutableDataManipulators = blockState.getManipulators();
        boolean blockSupports = false;
        for (ImmutableDataManipulator<?, ?> manipulator : immutableDataManipulators) {
            if (manipulator.asMutable().getClass().isAssignableFrom(manipulatorClass)) {
                blockSupports = true;
                break;
            }
        }
        if (!blockSupports) {
            final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
            final boolean tileEntitySupports;
            tileEntitySupports = tileEntity.isPresent() && tileEntity.get().supports(manipulatorClass);
            return tileEntitySupports;
        }
        return true;
    }

    @Override
    public Set<Key<?>> getKeys(int x, int y, int z) {
        final ImmutableSet.Builder<Key<?>> builder = ImmutableSet.builder();
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        builder.addAll(blockState.getKeys());
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        tileEntity.ifPresent(tileEntity1 -> builder.addAll(tileEntity1.getKeys()));
        return builder.build();
    }

    @Override
    public Set<ImmutableValue<?>> getValues(int x, int y, int z) {
        final ImmutableSet.Builder<ImmutableValue<?>> builder = ImmutableSet.builder();
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        builder.addAll(blockState.getValues());
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        tileEntity.ifPresent(tileEntity1 -> builder.addAll(tileEntity1.getValues()));
        return builder.build();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value) {
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        if (blockState.supports(key)) {
            // The cast to (Key) must be there because of a silly capture generic failure of some JDK's. The
            // Generics used will likely succeed in some IDE compilers, but occasionally fails for some javac
            // Refer to https://gist.github.com/gabizou/c14ade79b02deeddd8f9bd17a43a4b20 for example of compiler error
            ImmutableValue<E> old = getValue(x, y, z, key).map(v -> (Value<E>) v).get().asImmutable();
            setBlock(x, y, z, blockState.with(key, value).get());
            ImmutableValue<E> newVal = getValue(x, y, z, key).map(v -> (Value<E>) v).get().asImmutable();
            return DataTransactionResult.successReplaceResult(newVal, old);
        }
        return getTileEntity(x, y, z)
            .map(tileEntity ->  tileEntity.offer(key, value))
            .orElseGet(DataTransactionResult::failNoData);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function) {
        final BlockState blockState = getBlock(x, y, z).withExtendedProperties(new Location<>(this, x, y, z));
        final ImmutableDataManipulator<?, ?> immutableDataManipulator = manipulator.asImmutable();
        if (blockState.supports((Class) immutableDataManipulator.getClass())) {
            final List<ImmutableValue<?>> old = new ArrayList<>(blockState.getValues());
            final BlockState newState = blockState.with(immutableDataManipulator).get();
            old.removeAll(newState.getValues());
            setBlock(x, y, z, newState);
            return DataTransactionResult.successReplaceResult(old, manipulator.getValues());
        }
        return getTileEntity(x, y, z)
            .map(tileEntity -> tileEntity.offer(manipulator, function))
            .orElseGet(() -> DataTransactionResult.failResult(manipulator.getValues()));
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        return tileEntityOptional
            .map(tileEntity -> tileEntity.remove(manipulatorClass))
            .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Key<?> key) {
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        return tileEntityOptional
            .map(tileEntity -> tileEntity.remove(key))
            .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    public DataTransactionResult undo(int x, int y, int z, DataTransactionResult result) {
        return DataTransactionResult.failNoData(); // todo
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        return copyFrom(xTo, yTo, zTo, from, MergeFunction.IGNORE_ALL);
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        final DataTransactionResult.Builder builder = DataTransactionResult.builder();
        final Collection<DataManipulator<?, ?>> manipulators = from.getContainers();
        for (DataManipulator<?, ?> manipulator : manipulators) {
            builder.absorbResult(offer(xTo, yTo, zTo, manipulator, function));
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom, MergeFunction function) {
        return copyFrom(xTo, yTo, zTo, new Location<World>(this, xFrom, yFrom, zFrom), function);
    }

    @Override
    public Collection<DataManipulator<?, ?>> getManipulators(int x, int y, int z) {
        final List<DataManipulator<?, ?>> list = new ArrayList<>();
        final Collection<ImmutableDataManipulator<?, ?>> manipulators = this.getBlock(x, y, z)
            .withExtendedProperties(new Location<>(this, x, y, z))
            .getManipulators();
        for (ImmutableDataManipulator<?, ?> immutableDataManipulator : manipulators) {
            list.add(immutableDataManipulator.asMutable());
        }
        final Optional<TileEntity> optional = getTileEntity(x, y, z);
        optional
            .ifPresent(tileEntity -> list.addAll(tileEntity.getContainers()));
        return list;
    }

    @Override
    public boolean validateRawData(int x, int y, int z, DataView container) {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public void setRawData(int x, int y, int z, DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException(); // TODO Data API
    }
}
