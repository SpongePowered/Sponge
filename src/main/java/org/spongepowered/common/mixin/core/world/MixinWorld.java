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

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
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
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
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
import org.spongepowered.api.util.Functional;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.ChunkPreGenerate;
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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.util.math.IMixinBlockPos;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.mixin.tileentityactivation.MixinWorldServer_TileEntityActivation;
import org.spongepowered.common.util.SpongeHooks;
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(Vector3i.ONE);
    private static final Vector3i BLOCK_SIZE = BLOCK_MAX.sub(BLOCK_MIN).add(Vector3i.ONE);
    private static final Vector3i BIOME_MIN = new Vector3i(BLOCK_MIN.getX(), 0, BLOCK_MIN.getZ());
    private static final Vector3i BIOME_MAX = new Vector3i(BLOCK_MAX.getX(), 256, BLOCK_MAX.getZ());
    private static final Vector3i BIOME_SIZE = BIOME_MAX.sub(BIOME_MIN).add(Vector3i.ONE);
    private static final String
            CHECK_NO_ENTITY_COLLISION =
            "checkNoEntityCollision(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Z";
    private static final String
            GET_ENTITIES_WITHIN_AABB =
            "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;";
    public SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();

    @Nullable private Context worldContext;
    protected boolean processingExplosion = false;
    protected boolean isDefinitelyFake = false;
    protected boolean hasChecked = false;
    protected SpongeDimension spongeDimensionWrapper;

    // @formatter:off
    @Shadow @Final public boolean isRemote;
    @Shadow @Final public WorldProvider provider;
    @Shadow @Final public Random rand;
    @Shadow @Final public Profiler profiler;
    @Shadow @Final public List<EntityPlayer> playerEntities;
    @Shadow @Final public List<net.minecraft.entity.Entity> loadedEntityList;
    @Shadow @Final public List<net.minecraft.entity.Entity> weatherEffects;
    @Shadow @Final public List<net.minecraft.entity.Entity> unloadedEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> loadedTileEntityList;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tickableTileEntities;
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> tileEntitiesToBeRemoved;
    @Shadow @Final private List<net.minecraft.tileentity.TileEntity> addedTileEntityList;
    @Shadow @Final protected ISaveHandler saveHandler;
    @Shadow protected List<IWorldEventListener> eventListeners;
    @Shadow public int[] lightUpdateBlockList;
    @Shadow public int skylightSubtracted;
    @Shadow public int seaLevel;

    @Shadow public boolean processingLoadedTiles;
    @Shadow protected boolean scheduledUpdatesAreImmediate;
    @Shadow protected WorldInfo worldInfo;
    @Shadow protected IChunkProvider chunkProvider;
    @Shadow @Final public net.minecraft.world.border.WorldBorder worldBorder;

    @Shadow protected int updateLCG;
    @Shadow public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();
    @Shadow public abstract EnumDifficulty shadow$getDifficulty();

    @Shadow protected abstract void tickPlayers();

    @Shadow public net.minecraft.world.World init() {
        // Should never be overwritten because this is @Shadow'ed
        throw new RuntimeException("Bad things have happened");
    }

    // To be overridden in MixinWorldServer_Lighting
    @Shadow public abstract int getLight(BlockPos pos);
    @Shadow public abstract int getLight(BlockPos pos, boolean checkNeighbors);
    @Shadow public abstract int getRawLight(BlockPos pos, EnumSkyBlock lightType);
    @Shadow public abstract int getSkylightSubtracted();
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunkFromBlockCoords(BlockPos pos);
    @Shadow public abstract WorldInfo getWorldInfo();
    @Shadow public abstract boolean checkLight(BlockPos pos);
    @Shadow public abstract boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos);
    @Shadow public abstract boolean addTileEntity(net.minecraft.tileentity.TileEntity tile);
    @Shadow public abstract void onEntityAdded(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract boolean isAreaLoaded(BlockPos from, BlockPos to);
    @Shadow public abstract boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty);
    @Shadow public abstract boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);
    @Shadow public abstract void onEntityRemoved(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void updateEntity(net.minecraft.entity.Entity ent);
    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);
    @Shadow public void markChunkDirty(BlockPos pos, net.minecraft.tileentity.TileEntity unusedTileEntity){};
   // @Shadow public abstract List<Entity> getEntitiesInAABBexcluding(@Nullable net.minecraft.entity.Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate <? super net.minecraft.entity.Entity > predicate);
    @Shadow public abstract boolean addWeatherEffect(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract Biome getBiome(BlockPos pos);
    @Shadow public abstract BiomeProvider getBiomeProvider();
    @Shadow public abstract boolean isBlockPowered(BlockPos pos);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunkFromChunkCoords(int chunkX, int chunkZ);
    @Shadow public abstract net.minecraft.world.Explosion newExplosion(@Nullable net.minecraft.entity.Entity entityIn, double x, double y, double z, float strength,
            boolean isFlaming, boolean isSmoking);
    @Shadow public abstract List<net.minecraft.entity.Entity> getEntities(Class<net.minecraft.entity.Entity> entityType,
            com.google.common.base.Predicate<net.minecraft.entity.Entity> filter);
    @Shadow public abstract <T extends net.minecraft.entity.Entity> List<T> getEntitiesWithinAABB(Class <? extends T > clazz, AxisAlignedBB aabb,
            com.google.common.base.Predicate<? super T > filter);
    @Shadow public abstract List<net.minecraft.entity.Entity> getEntitiesWithinAABBExcludingEntity(net.minecraft.entity.Entity entityIn, AxisAlignedBB bb);
    @Shadow public abstract MinecraftServer getMinecraftServer();
    // Methods needed for MixinWorldServer & Tracking
    @Shadow public abstract boolean spawnEntity(net.minecraft.entity.Entity entity); // This is overridden in MixinWorldServer
    @Shadow public abstract void updateAllPlayersSleepingFlag();
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state);
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state, int flags);
    @Shadow public abstract void immediateBlockTick(BlockPos pos, IBlockState state, Random random);
    @Shadow public abstract void updateComparatorOutputLevel(BlockPos pos, Block blockIn);
    @Shadow public abstract void neighborChanged(BlockPos pos, final Block blockIn, BlockPos otherPos);
    @Shadow public abstract void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide);
    @Shadow public abstract void updateObservingBlocksAt(BlockPos pos, Block blockType);
    @Shadow public abstract void notifyNeighborsRespectDebug(BlockPos pos, Block blockType, boolean updateObserverBlocks);
    @Shadow public abstract void notifyNeighborsOfStateChange(BlockPos pos, Block blockType, boolean updateObserverBlocks);
    @Shadow public abstract void notifyBlockUpdate(BlockPos pos, IBlockState oldState, IBlockState newState, int flags);
    @Shadow public abstract void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority); // this is really scheduleUpdate
    @Shadow public abstract void playSound(EntityPlayer p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, net.minecraft.util.SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_);
    @Shadow protected abstract void updateBlocks();
    @Shadow public abstract GameRules shadow$getGameRules();
    @Shadow public abstract boolean isRaining();
    @Shadow public abstract boolean isThundering();
    @Shadow public abstract boolean isRainingAt(BlockPos strikePosition);
    @Shadow public abstract DifficultyInstance getDifficultyForLocation(BlockPos pos);
    @Shadow public abstract BlockPos getPrecipitationHeight(BlockPos pos);
    @Shadow public abstract boolean canBlockFreezeNoWater(BlockPos pos);
    @Shadow public abstract boolean canSnowAt(BlockPos pos, boolean checkLight);
    @Shadow public abstract List<AxisAlignedBB> getCollisionBoxes(net.minecraft.entity.Entity entityIn, AxisAlignedBB bb);
    @Shadow public abstract void notifyLightSet(BlockPos pos);
    @Shadow @Nullable private net.minecraft.tileentity.TileEntity getPendingTileEntityAt(BlockPos p_189508_1_) {
        return null; // Shadowed
    }
    @Shadow public abstract int getHeight(int x, int z);

    // @formatter:on

    @Shadow
    public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
        return true; // shadowed so we can call from MixinWorldServer in spongeforge.
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onInit(CallbackInfo ci) {
        this.spongeDimensionWrapper = new SpongeDimension(this.provider);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;"
                                                                     + "createWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private net.minecraft.world.border.WorldBorder onCreateWorldBorder(WorldProvider provider) {
        if (this.isFake()) {
            return provider.createWorldBorder();
        }
        return ((IMixinWorldProvider) provider).createServerWorldBorder();
    }

    @SuppressWarnings("rawtypes")
    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    public void onGetCollisionBoxes(net.minecraft.entity.Entity entity, AxisAlignedBB axis, CallbackInfoReturnable<List<AxisAlignedBB>> cir) {
        if (this.isFake() || entity == null) {
            return;
        }
        if (entity.world != null && !((IMixinWorld) entity.world).isFake() && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
            // Removing misbehaved living entities
            cir.setReturnValue(new ArrayList<>());
        }
    }

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
            final IMixinWorldInfo mixinWorldInfo = (IMixinWorldInfo) properties;
            mixinWorldInfo.setUniqueId(UUID.randomUUID());
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
        return Optional.ofNullable((Chunk) worldserver.getChunkProvider().getLoadedChunk(x, z));
    }

    @Override
    public Optional<Chunk> loadChunk(int x, int y, int z, boolean shouldGenerate) {
        if (!SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return Optional.empty();
        }
        final WorldServer worldserver = (WorldServer) (Object) this;
        // If we aren't generating, return the chunk
        if (!shouldGenerate) {
            return Optional.ofNullable((Chunk) worldserver.getChunkProvider().loadChunk(x, z));
        }
        return Optional.ofNullable((Chunk) worldserver.getChunkProvider().provideChunk(x, z));
    }

    @Override
    public int getHighestYAt(int x, int z) {
        return this.getHeight(x, z);
    }

    @Override
    public int getPrecipitationLevelAt(int x, int z) {
        return this.getPrecipitationHeight(new BlockPos(x, 0, z)).getY();
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            return BlockTypes.AIR.getDefaultState();
        }
        checkBlockBounds(x, y, z);
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            return BlockTypes.AIR;
        }
        checkBlockBounds(x, y, z);
        // avoid intermediate object creation from using BlockState
        return (BlockType) getChunkFromChunkCoords(x >> 4, z >> 4).getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block) {
        return setBlock(x, y, z, block, BlockChangeFlags.ALL);
    }


    @Override
    public BiomeType getBiome(int x, int y, int z) {
        checkBiomeBounds(x, y, z);
        return (BiomeType) this.getBiome(new BlockPos(x, y, z));
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        checkBiomeBounds(x, y, z);
        ((Chunk) getChunkFromChunkCoords(x >> 4, z >> 4)).setBiome(x, y, z, biome);
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
            tempEntity.posY -= tempEntity.getEyeHeight();
            entity = (Entity) new EntityEnderPearl(world, tempEntity);
            ((EnderPearl) entity).setShooter(ProjectileSource.UNKNOWN);
        }

        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (entityClass.isAssignableFrom(EntityFallingBlock.class)) {
            entity = (Entity) new EntityFallingBlock(world, x, y, z, Blocks.SAND.getDefaultState());
        } else if (entityClass.isAssignableFrom(EntityItem.class)) {
            entity = (Entity) new EntityItem(world, x, y, z, new ItemStack(Blocks.STONE));
        }

        if (entity == null) {
            try {
                entity = ConstructorUtils.invokeConstructor(entityClass, this);
                ((net.minecraft.entity.Entity) entity).setPosition(x, y, z);
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
            ((EntityLiving)entity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(x, y, z)), null);
        }

        if (entity instanceof EntityPainting) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            ((EntityPainting) entity).art = EnumArt.KEBAB;
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
        return this.spongeDimensionWrapper;
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        // Note that MixinWorldServer is properly overriding this to use it's own mapping.
        for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of((Entity) entity);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Iterable<Chunk> getLoadedChunks() {
        if (this.isFake()) { // If we're client side, we can't know solidly what loaded chunks are... need to do this in MixinWorldClient in forge.
            return Collections.emptyList();
        }
        return (List<Chunk>) (List<?>) Lists.newArrayList(((WorldServer) (Object) this).getChunkProvider().getLoadedChunks());
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
                worldInfo = new WorldInfo(new WorldSettings(0, GameType.NOT_SET, false, false, WorldType.DEFAULT), "sponge$dummy_World");
            }
            this.worldContext = new Context(Context.WORLD_KEY, worldInfo.getWorldName());
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
        return BIOME_MIN;
    }

    @Override
    public Vector3i getBiomeMax() {
        return BIOME_MAX;
    }

    @Override
    public Vector3i getBiomeSize() {
        return BIOME_SIZE;
    }

    @Override
    public Vector3i getBlockMin() {
        return BLOCK_MIN;
    }

    @Override
    public Vector3i getBlockMax() {
        return BLOCK_MAX;
    }

    @Override
    public Vector3i getBlockSize() {
        return BLOCK_SIZE;
    }

    @Override
    public boolean containsBiome(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, BIOME_MIN, BIOME_MAX);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, BLOCK_MIN, BLOCK_MAX);
    }

    private void checkBiomeBounds(int x, int y, int z) {
        if (!containsBiome(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BIOME_MIN, BIOME_MAX);
        }
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BLOCK_MIN, BLOCK_MAX);
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
        Location<World> origin = explosion.getLocation();
        checkNotNull(origin, "location");
        newExplosion(EntityUtil.toNullableNative(explosion.getSourceExplosive().orElse(null)), origin.getX(),
                origin.getY(), origin.getZ(), explosion.getRadius(), explosion.canCauseFire(),
                explosion.shouldBreakBlocks()
        );
    }

    @Override
    public Extent getExtentView(Vector3i newMin, Vector3i newMax) {
        checkBlockBounds(newMin.getX(), newMin.getY(), newMin.getZ());
        checkBlockBounds(newMax.getX(), newMax.getY(), newMax.getZ());
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
        SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder()
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
            for (DataManipulator<?, ?> manipulator : ((IMixinCustomDataHolder) tileEntity).getCustomManipulators()) {
                builder.add(manipulator);
            }
            final NBTTagCompound compound = new NBTTagCompound();
            ((net.minecraft.tileentity.TileEntity) tileEntity).writeToNBT(compound);
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
        checkBlockBounds(x, y, z);
        final BlockPos pos = new BlockPos(x, y, z);
        final IBlockState state = getBlockState(pos);
        final AxisAlignedBB box = state.getBoundingBox((IBlockAccess) this, pos);
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
                    ((IMixinChunk) chunk.get())
                            .getIntersectingEntities(chunkStart, direction, remainingDistance, filter, chunkStart.getY(), yNext, intersecting);
                }
                // If the intersections are near another chunk, its entities might be partially in the current chunk, so include it also
                final IMixinChunk nearIntersections = getChunkNearIntersections(xChunk, zChunk, xCurrent, zCurrent, xNext, zNext);
                if (nearIntersections != null) {
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
            return (IMixinChunk) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk - chunkWidth).orElse(null);
        }
        final boolean d12 = c2.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d22 = c2.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d12 && d22) {
            // Near corner +x, -z
            return (IMixinChunk) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk - chunkWidth).orElse(null);
        }
        final boolean d13 = c3.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d23 = c3.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d13 && d23) {
            // Near corner -x, +z
            return (IMixinChunk) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk + chunkWidth).orElse(null);
        }
        final boolean d14 = c4.distanceSquared(xCurrent, zCurrent) <= nearDistance2;
        final boolean d24 = c4.distanceSquared(xNext, zNext) <= nearDistance2;
        if (d14 && d24) {
            // Near corner +x, +z
            return (IMixinChunk) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk + chunkWidth).orElse(null);
        }
        // Look for two intersections being near the corners on the same face
        if (d11 && d23 || d21 && d13) {
            // Near face -x
            return (IMixinChunk) getChunkAtBlock(xChunk - chunkWidth, 0, zChunk).orElse(null);
        }
        if (d11 && d22 || d21 && d12) {
            // Near face -z
            return (IMixinChunk) getChunkAtBlock(xChunk, 0, zChunk - chunkWidth).orElse(null);
        }
        if (d14 && d22 || d24 && d12) {
            // Near face +x
            return (IMixinChunk) getChunkAtBlock(xChunk + chunkWidth, 0, zChunk).orElse(null);
        }
        if (d14 && d23 || d24 && d13) {
            // Near face +z
            return (IMixinChunk) getChunkAtBlock(xChunk, 0, zChunk + chunkWidth).orElse(null);
        }
        return null;
    }

    private Set<EntityHit> getIntersectingEntities(Vector3d start, double yDirection, double distance, Predicate<EntityHit> filter) {
        final Set<EntityHit> intersecting = new HashSet<>();
        // Current chunk
        final Vector3d direction = yDirection < 0 ? Vector3d.UNIT_Y.negate() : Vector3d.UNIT_Y;
        final double endY = start.getY() + yDirection * distance;
        final Vector3i chunkPos = SpongeChunkLayout.instance.forceToChunk(start.toInt());
        ((IMixinChunk) getChunk(chunkPos).get()).getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        // Check adjacent chunks if near them
        final int nearDistance = 2;
        // Neighbour -x chunk
        final Vector3i chunkBlockPos = SpongeChunkLayout.instance.forceToWorld(chunkPos);
        if (start.getX() - chunkBlockPos.getX() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(-1, 0, 0)).get())
                    .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour -z chunk
        if (start.getZ() - chunkBlockPos.getZ() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(0, 0, -1)).get())
                    .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour +x chunk
        final int chunkWidth = SpongeChunkLayout.CHUNK_SIZE.getX();
        if (chunkBlockPos.getX() + chunkWidth - start.getX() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(1, 0, 0)).get())
                    .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        // Neighbour +z chunk
        if (chunkBlockPos.getZ() + chunkWidth - start.getZ() <= nearDistance) {
            ((IMixinChunk) getChunk(chunkPos.add(0, 0, 1)).get())
                    .getIntersectingEntities(start, direction, distance, filter, start.getY(), endY, intersecting);
        }
        return intersecting;
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayerToEntityWhoAffectsSpawning(net.minecraft.entity.Entity entity, double distance) {
        return this.getClosestPlayerWhoAffectsSpawning(entity.posX, entity.posY, entity.posZ, distance);
    }

    @Nullable
    @Override
    public EntityPlayer getClosestPlayerWhoAffectsSpawning(double x, double y, double z, double distance) {
        double bestDistance = -1.0D;
        EntityPlayer result = null;

        for (Object entity : this.playerEntities) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player == null || player.isDead || !((IMixinEntityPlayer) player).affectsSpawning()) {
                continue;
            }

            double playerDistance = player.getDistanceSq(x, y, z);

            if ((distance < 0.0D || playerDistance < distance * distance) && (bestDistance == -1.0D || playerDistance < bestDistance)) {
                bestDistance = playerDistance;
                result = player;
            }
        }

        return result;
    }

    @Override
    public boolean isFake() {
        if (this.hasChecked) {
            return this.isDefinitelyFake;
        }
        this.isDefinitelyFake = this.isRemote || this.worldInfo == null || !(this instanceof IMixinWorldServer);
        this.hasChecked = true;
        return this.isDefinitelyFake;
    }

    @Redirect(method = "isAnyPlayerWithinRangeAt", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false))
    public boolean onIsAnyPlayerWithinRangePredicate(com.google.common.base.Predicate<EntityPlayer> predicate, Object object) {
        EntityPlayer player = (EntityPlayer) object;
        return !(player.isDead || !((IMixinEntityPlayer) player).affectsSpawning()) && predicate.apply(player);
    }

    // For invisibility
    @Redirect(method = CHECK_NO_ENTITY_COLLISION, at = @At(value = "INVOKE", target = GET_ENTITIES_WITHIN_AABB))
    public List<net.minecraft.entity.Entity> filterInvisibile(net.minecraft.world.World world, net.minecraft.entity.Entity entityIn,
            AxisAlignedBB axisAlignedBB) {
        List<net.minecraft.entity.Entity> entities = world.getEntitiesWithinAABBExcludingEntity(entityIn, axisAlignedBB);
        Iterator<net.minecraft.entity.Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            net.minecraft.entity.Entity entity = iterator.next();
            if (((IMixinEntity) entity).isVanished() && ((IMixinEntity) entity).ignoresCollision()) {
                iterator.remove();
            }
        }
        return entities;
    }

    @Redirect(method = "getClosestPlayer(DDDDLcom/google/common/base/Predicate;)Lnet/minecraft/entity/player/EntityPlayer;", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false))
    private boolean onGetClosestPlayerCheck(com.google.common.base.Predicate<net.minecraft.entity.Entity> predicate, Object entityPlayer) {
        EntityPlayer player = (EntityPlayer) entityPlayer;
        IMixinEntity mixinEntity = (IMixinEntity) player;
        return predicate.apply(player) && !mixinEntity.isVanished();
    }

    @Inject(method = "playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", at = @At("HEAD"), cancellable = true)
    private void spongePlaySoundAtEntity(EntityPlayer entity, double x, double y, double z, SoundEvent name, net.minecraft.util.SoundCategory category, float volume, float pitch, CallbackInfo callbackInfo) {
        if (entity instanceof IMixinEntity) {
            if (((IMixinEntity) entity).isVanished()) {
                callbackInfo.cancel();
            }
        }
    }

    @Override
    public void sendBlockChange(int x, int y, int z, BlockState state) {
        checkNotNull(state, "state");
        SPacketBlockChange packet = new SPacketBlockChange();
        packet.blockPosition = new BlockPos(x, y, z);
        packet.blockState = BlockUtil.toNative(state);

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

    // These are overriden in MixinWorldServer where they should be.

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    protected void onUpdateWeatherEffect(net.minecraft.entity.Entity entityIn) {
        entityIn.onUpdate();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    protected void onUpdateTileEntities(ITickable tile) {
        tile.update();
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    protected void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        entity.onUpdate();
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateRidden()V"))
    protected void onCallEntityRidingUpdate(net.minecraft.entity.Entity entity) {
        entity.updateRidden();
    }

    @Redirect(method = "addTileEntity",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;"),
                           to =   @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z")))
    private boolean onAddTileEntity(List<net.minecraft.tileentity.TileEntity> list, Object tile) {
        if (!this.isFake() && !canTileUpdate((net.minecraft.tileentity.TileEntity) tile)) {
            return false;
        }

        return list.add((net.minecraft.tileentity.TileEntity) tile);
    }

    private boolean canTileUpdate(net.minecraft.tileentity.TileEntity tile) {
        TileEntity spongeTile = (TileEntity) tile;
        if (spongeTile.getType() != null && !((SpongeTileEntityType) spongeTile.getType()).canTick()) {
            return false;
        }

        return true;
    }

    @Inject(method = "getPlayerEntityByUUID", at = @At("HEAD"), cancellable = true)
    public void onGetPlayerEntityByUUID(UUID uuid, CallbackInfoReturnable<UUID> cir) {
        // avoid crashing server if passed a null UUID
        if (uuid == null) {
            cir.setReturnValue(null);
        }
    }

    /**
     * @author blood - February 20th, 2017
     * @reason Avoids loading unloaded chunk when checking for sky.
     *
     * @param pos The position to get the light for
     * @return Whether block position can see sky
     */
    @Overwrite
    public boolean canSeeSky(BlockPos pos) {
        final net.minecraft.world.chunk.Chunk chunk = ((IMixinChunkProviderServer) this.chunkProvider).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null || chunk.unloadQueued) {
            return false;
        }

        return chunk.canSeeSky(pos);
    }

    @Override
    public int getRawBlockLight(BlockPos pos, EnumSkyBlock lightType) {
        return this.getRawLight(pos, lightType);
    }

    /**
     * @author gabizou - July 25th, 2016
     * @reason Optimizes several blockstate lookups for getting raw light.
     *
     * @param pos The position to get the light for
     * @param enumSkyBlock The light type
     * @return The raw light
     */
    @Inject(method = "getRawLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState" +
            "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"), cancellable = true)
    private void onLightGetBlockState(BlockPos pos, EnumSkyBlock enumSkyBlock, CallbackInfoReturnable<Integer> cir) {
        final net.minecraft.world.chunk.Chunk chunk;
        if (!this.isFake()) {
            chunk = ((IMixinChunkProviderServer) ((WorldServer) (Object) this).getChunkProvider()).getLoadedChunkWithoutMarkingActive(pos.getX() >> 4, pos.getZ() >> 4);
        } else {
            chunk = this.getChunkFromBlockCoords(pos);
        }
        if (chunk == null || chunk.unloadQueued) {
            cir.setReturnValue(0);
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return The block state at the desired position
     */
    @Overwrite
    public IBlockState getBlockState(BlockPos pos) {
        // Sponge - Replace with inlined method
        // if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isInvalidYPosition()) {
            // Sponge end
            return Blocks.AIR.getDefaultState();
        }
        net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
        return chunk.getBlockState(pos);
    }

    /**
     * @author gabizou - August 4th, 2016
     * @author bloodmc - May 10th, 2017 - Added async check
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return The tile entity at the desired position, or else null
     */
    @Overwrite
    @Nullable
    public net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos) {
        // Sponge - Replace with inlined method
        //  if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isInvalidYPosition()) {
            return null;
            // Sponge End
        } else {
            net.minecraft.tileentity.TileEntity tileentity = null;

            // Sponge - Don't create or obtain pending tileentity async, simply check if TE exists in chunk
            // Mods such as pixelmon call this method async, so this is a temporary workaround until fixed
            if (!this.isFake() && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
                return this.getChunkFromBlockCoords(pos).getTileEntity(pos, net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK);
            }
            // Sponge end

            if (this.processingLoadedTiles) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            if (tileentity == null) {
                 tileentity = this.getChunkFromBlockCoords(pos).getTileEntity(pos, net.minecraft.world.chunk.Chunk.EnumCreateEntityType.IMMEDIATE);
            }

            if (tileentity == null) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            return tileentity;
        }
    }


    /**
     * @author gabizou
     * @reason Adds a redirector to use instead of an injector to avoid duplicate chunk area loaded lookups.
     * This is overridden in MixinWorldServer_Lighting.
     *
     * @param thisWorld This world
     * @param pos The block position to check light for
     * @param radius The radius, always 17
     * @param allowEmtpy Whether to allow empty chunks, always false
     * @param lightType The light type
     * @param samePosition The block position to check light for, again.
     * @return True if the area is loaded
     */
    @Redirect(method = "checkLightFor", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAreaLoaded(Lnet/minecraft/util/math/BlockPos;IZ)Z"))
    protected boolean spongeIsAreaLoadedForCheckingLight(net.minecraft.world.World thisWorld, BlockPos pos, int radius, boolean allowEmtpy, EnumSkyBlock lightType, BlockPos samePosition) {
        return isAreaLoaded(pos, radius, allowEmtpy);
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return True if the block position is valid
     */
    @Overwrite
    public boolean isValid(BlockPos pos) { // isValid
        return ((IMixinBlockPos) pos).isValidPosition();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param pos The position
     * @return True if the block position is outside build height
     */
    @Overwrite
    public boolean isOutsideBuildHeight(BlockPos pos) { // isOutsideBuildHeight
        return ((IMixinBlockPos) pos).isInvalidYPosition();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link IMixinBlockPos}.
     *
     * @param type The type of sky lighting
     * @param pos The position
     * @return The light for the defined sky type and block position
     */
    @Overwrite
    public int getLightFor(EnumSkyBlock type, BlockPos pos) {
        if (pos.getY() < 0) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());
        }

        // Sponge Start - Replace with inlined method to check
        // if (!this.isValid(pos)) // vanilla
        if (!((IMixinBlockPos) pos).isValidPosition()) {
            // Sponge End
            return type.defaultLightValue;
        } else {
            net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
            return chunk.getLightFor(type, pos);
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Inlines the isValid check to BlockPos.
     *
     * @param type The type of sky lighting
     * @param pos The block position
     * @param lightValue The light value to set to
     */
    @Overwrite
    public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
        // Sponge Start - Replace with inlined Valid position check
        // if (this.isValid(pos)) // Vanilla
        if (((IMixinBlockPos) pos).isValidPosition()) { // Sponge - Replace with inlined method to check
            // Sponge End
            if (this.isBlockLoaded(pos)) {
                net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
                chunk.setLightFor(type, pos, lightValue);
                this.notifyLightSet(pos);
            }
        }
    }


    /**
     * @author gabizou - August 4th, 2016
     * @reason Inlines the isValidXZPosition check to BlockPos.
     *
     * @param bbox The AABB to check
     * @return True if the AABB collides with a block
     */
    @Overwrite
    public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
        List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
        int i = MathHelper.floor(bbox.minX) - 1;
        int j = MathHelper.ceil(bbox.maxX) + 1;
        int k = MathHelper.floor(bbox.minY) - 1;
        int l = MathHelper.ceil(bbox.maxY) + 1;
        int i1 = MathHelper.floor(bbox.minZ) - 1;
        int j1 = MathHelper.ceil(bbox.maxZ) + 1;
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        try {
            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = i1; l1 < j1; ++l1) {
                    int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);

                    if (i2 != 2 && this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
                        for (int j2 = k; j2 < l; ++j2) {
                            if (i2 <= 0 || j2 != k && j2 != l - 1) {
                                blockpos$pooledmutableblockpos.setPos(k1, j2, l1);

                                // Sponge - Replace with inlined method
                                // if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) // Vanilla
                                if (!((IMixinBlockPos) (Object) blockpos$pooledmutableblockpos).isValidXZPosition()) {
                                    // Sponge End
                                    boolean flag1 = true;
                                    return flag1;
                                }

                                IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos);
                                iblockstate.addCollisionBoxToList((net.minecraft.world.World) (Object) this, blockpos$pooledmutableblockpos, bbox, list, (net.minecraft.entity.Entity) null, false);

                                if (!list.isEmpty()) {
                                    boolean flag = true;
                                    return flag;
                                }
                            }
                        }
                    }
                }
            }

            return false;
        } finally {
            blockpos$pooledmutableblockpos.release();
        }
    }


    /*********************** TIMINGS ***********************/

    /**
     * @author blood
     * @author gabizou - Ported to 1.9.4 - replace direct field calls to overriden methods in MixinWorldServer
     *
     * @reason Add timing hooks in various areas. This method shouldn't be touched by mods/forge alike
     */
    @Overwrite
    public void updateEntities() {
        //this.profiler.startSection("entities"); // Sponge - Don't use the profiler
        //this.profiler.startSection("global"); // Sponge - Don't use the profiler
        this.startEntityGlobalTimings(); // Sponge


        for (int i = 0; i < this.weatherEffects.size(); ++i) {
            net.minecraft.entity.Entity entity = this.weatherEffects.get(i);

            try {
                ++entity.ticksExisted;
                entity.onUpdate();
            } catch (Throwable throwable2) {
                this.stopTimingForWeatherEntityTickCrash(entity); // Sponge - end the entity timing
                CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null) {
                    crashreportcategory.addCrashSection("Entity", "~~NULL~~");
                } else {
                    entity.addEntityCrashInfo(crashreportcategory);
                }

                SpongeImplHooks.onEntityError(entity, crashreport);
            }

            if (entity.isDead) {
                this.weatherEffects.remove(i--);
            }
        }

        this.stopEntityTickTimingStartEntityRemovalTiming(); // Sponge
        // this.profiler.endStartSection("remove"); // Sponge - Don't use the profiler
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (int k = 0; k < this.unloadedEntityList.size(); ++k) {
            net.minecraft.entity.Entity entity1 = this.unloadedEntityList.get(k);
            // Sponge start - use cached chunk
            // int j = entity1.chunkCoordX;
            // int k1 = entity1.chunkCoordZ;

            final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((IMixinEntity) entity1).getActiveChunk();
            if (activeChunk != null) {
                activeChunk.removeEntity(entity1);
            }
            // Sponge end
        }

        for (int l = 0; l < this.unloadedEntityList.size(); ++l) {
            this.onEntityRemoved(this.unloadedEntityList.get(l));
        }

        this.unloadedEntityList.clear();
        this.stopEntityRemovalTiming(); // Sponge
        this.tickPlayers();
        // this.profiler.endStartSection("regular"); // Sponge - Don't use the profiler
        this.entityActivationCheck();

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
            net.minecraft.entity.Entity entity2 = this.loadedEntityList.get(i1);
            net.minecraft.entity.Entity entity3 = entity2.getRidingEntity();

            if (entity3 != null) {
                if (!entity3.isDead && entity3.isPassenger(entity2)) {
                    continue;
                }

                entity2.dismountRidingEntity();
            }

            // this.profiler.startSection("tick"); // Sponge - Don't use the profiler
            this.startEntityTickTiming(); // Sponge

            if (!entity2.isDead && !(entity2 instanceof EntityPlayerMP)) {
                try {
                    this.updateEntity(entity2);
                } catch (Throwable throwable1) {
                    this.stopTimingTickEntityCrash(entity2); // Sponge
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Entity being ticked");
                    entity2.addEntityCrashInfo(crashreportcategory1);
                    SpongeImplHooks.onEntityError(entity2, crashreport1);
                }
            }

            this.stopEntityTickSectionBeforeRemove(); // Sponge
            // this.profiler.endSection(); // Sponge - Don't use the profiler
            // this.profiler.startSection("remove"); // Sponge - Don't use the profiler
            this.startEntityRemovalTick(); // Sponge

            if (entity2.isDead) {
                // Sponge start - use cached chunk
                // int l1 = entity2.chunkCoordX;
                // int i2 = entity2.chunkCoordZ;

                final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((IMixinEntity) entity2).getActiveChunk();
                if (activeChunk != null) {
                    activeChunk.removeEntity(entity2);
                }
                // Sponge end

                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity2);
            }

            this.stopEntityRemovalTiming(); // Sponge
            // this.profiler.endSection(); // Sponge - Don't use the profiler
        }

        // this.profiler.endStartSection("blockEntities"); // Sponge - Don't use the profiler
        spongeTileEntityActivation();
        this.processingLoadedTiles = true;
        Iterator<net.minecraft.tileentity.TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext()) {
            this.startTileTickTimer(); // Sponge
            net.minecraft.tileentity.TileEntity tileentity = iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorld()) {
                BlockPos blockpos = tileentity.getPos();

                if (((IMixinTileEntity) tileentity).shouldTick() && this.worldBorder.contains(blockpos)) { // Sponge
                    try {
                        //this.profiler.startSection(tileentity.getClass().getSimpleName());
                        ((ITickable) tileentity).update();
                        //this.profiler.endSection();
                    } catch (Throwable throwable) {
                        this.stopTimingTickTileEntityCrash(tileentity); // Sponge
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory2);
                        SpongeImplHooks.onTileEntityError(tileentity, crashreport2);
                    }
                }
            }

            this.stopTileEntityAndStartRemoval(); // Sponge

            if (tileentity.isInvalid()) {
                iterator.remove();
                this.loadedTileEntityList.remove(tileentity);
                // Sponge start - use cached chunk
                final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((IMixinTileEntity) tileentity).getActiveChunk();
                if (activeChunk != null) {
                    //this.getChunkFromBlockCoords(tileentity.getPos()).removeTileEntity(tileentity.getPos());
                    //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desynced
                    if (activeChunk.getTileEntity(tileentity.getPos(), net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK) == tileentity) {
                        activeChunk.removeTileEntity(tileentity.getPos());
                    }
                }
                // Sponge end
            }

            this.stopTileEntityRemovelInWhile(); // Sponge
        }

        if (!this.tileEntitiesToBeRemoved.isEmpty()) {
            // Sponge start - use forge hook
            for (Object tile : this.tileEntitiesToBeRemoved) {
               SpongeImplHooks.onTileChunkUnload(((net.minecraft.tileentity.TileEntity)tile));
            }
            // Sponge end

            // forge: faster "contains" makes this removal much more efficient
            java.util.Set<net.minecraft.tileentity.TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(this.tileEntitiesToBeRemoved);
            this.tickableTileEntities.removeAll(remove);
            this.loadedTileEntityList.removeAll(remove);
            this.tileEntitiesToBeRemoved.clear();
        }

        if (!this.isFake()) {
            try (final PhaseContext<?> context = BlockPhase.State.TILE_CHUNK_UNLOAD.createPhaseContext().source(this)) {
                context.buildAndSwitch();
                this.startPendingTileEntityTimings(); // Sponge
            }
        }

        this.processingLoadedTiles = false;  //FML Move below remove to prevent CMEs
        // this.profiler.endStartSection("pendingBlockEntities"); // Sponge - Don't use the profiler

        if (!this.addedTileEntityList.isEmpty()) {
            for (int j1 = 0; j1 < this.addedTileEntityList.size(); ++j1) {
                net.minecraft.tileentity.TileEntity tileentity1 = this.addedTileEntityList.get(j1);

                if (!tileentity1.isInvalid()) {
                    if (!this.loadedTileEntityList.contains(tileentity1)) {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos())) {
                        net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(tileentity1.getPos());
                        IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                        chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                        this.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
                    }
                }
            }

            this.addedTileEntityList.clear();
        }

        this.endPendingTileEntities(); // Sponge
        // this.profiler.endSection(); // Sponge - Don't use the profiler
        // this.profiler.endSection(); // Sponge - Don't use the profiler
    }

    /**
     * Overridden in {@link MixinWorldServer_TileEntityActivation}
     */
    public void spongeTileEntityActivation() {

    }

    public void entityActivationCheck() {
        // Overridden in MixinWorldServer_Activation
    }

    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }

    protected void startEntityGlobalTimings() { }

    protected void stopTimingForWeatherEntityTickCrash(net.minecraft.entity.Entity updatingEntity) { }

    protected void stopEntityTickTimingStartEntityRemovalTiming() { }

    protected void stopEntityRemovalTiming() { }

    protected void startEntityTickTiming() { }

    protected void stopTimingTickEntityCrash(net.minecraft.entity.Entity updatingEntity) { }

    protected void stopEntityTickSectionBeforeRemove() { }

    protected void startEntityRemovalTick() { }

    protected void startTileTickTimer() { }

    protected void stopTimingTickTileEntityCrash(net.minecraft.tileentity.TileEntity updatingTileEntity) { }

    protected void stopTileEntityAndStartRemoval() { }

    protected void stopTileEntityRemovelInWhile() { }

    protected void startPendingTileEntityTimings() {}

    protected void endPendingTileEntities() { }

}
