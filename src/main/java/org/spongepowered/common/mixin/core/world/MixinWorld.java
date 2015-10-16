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

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityPainting.EnumArt;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.spongepowered.api.Platform;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.world.chunk.PopulateChunkEvent;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.util.Functional;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.SpongeImplFactory;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.configuration.SpongeConfig;
import org.spongepowered.common.data.property.SpongePropertyRegistry;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.IMixinWorldSettings;
import org.spongepowered.common.interfaces.IMixinWorldType;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.scoreboard.SpongeScoreboard;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.border.PlayerBorderListener;
import org.spongepowered.common.world.extent.ExtentViewDownsize;
import org.spongepowered.common.world.extent.ExtentViewTransform;
import org.spongepowered.common.world.gen.CustomChunkProviderGenerate;
import org.spongepowered.common.world.gen.CustomWorldChunkManager;
import org.spongepowered.common.world.gen.SpongeBiomeGenerator;
import org.spongepowered.common.world.gen.SpongeGeneratorPopulator;
import org.spongepowered.common.world.gen.SpongePopulatorType;
import org.spongepowered.common.world.gen.SpongeWorldGenerator;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.world.World.class)
public abstract class MixinWorld implements World, IMixinWorld {

    private static final Vector3i BLOCK_MIN = new Vector3i(-30000000, 0, -30000000);
    private static final Vector3i BLOCK_MAX = new Vector3i(30000000, 256, 30000000).sub(1, 1, 1);
    private static final Vector3i BLOCK_SIZE = BLOCK_MAX.sub(BLOCK_MIN).add(1, 1, 1);
    private static final Vector2i BIOME_MIN = BLOCK_MIN.toVector2(true);
    private static final Vector2i BIOME_MAX = BLOCK_MAX.toVector2(true);
    private static final Vector2i BIOME_SIZE = BIOME_MAX.sub(BIOME_MIN).add(1, 1);
    private long weatherStartTime;
    public boolean processingCaptureCause = false;
    public boolean captureEntitySpawns = true;
    public boolean captureTerrainGen = false;
    public boolean captureBlocks = false;
    public boolean restoringBlocks = false;
    public List<Entity> capturedEntities = new ArrayList<Entity>();
    public List<Entity> capturedEntityItems = new ArrayList<Entity>();
    public List<Entity> capturedOnBlockAddedEntities = new ArrayList<Entity>();
    public List<Entity> capturedOnBlockAddedItems = new ArrayList<Entity>();
    public BlockSnapshot currentTickBlock = null;
    public BlockSnapshot currentTickOnBlockAdded = null;
    public Entity currentTickEntity = null;
    public TileEntity currentTickTileEntity = null;
    public SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
    public List<BlockSnapshot> capturedSpongeBlockBreaks = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockPlaces = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockModifications = new ArrayList<BlockSnapshot>();
    public List<BlockSnapshot> capturedSpongeBlockFluids = new ArrayList<BlockSnapshot>();
    public Map<PopulatorType, List<Transaction<BlockSnapshot>>> capturedSpongePopulators = Maps.newHashMap();
    public Map<CaptureType, List<BlockSnapshot>> captureBlockLists = Maps.newHashMap();
    private boolean keepSpawnLoaded;
    private boolean worldSpawnerRunning;
    private boolean chunkSpawnerRunning;
    public SpongeConfig<SpongeConfig.WorldConfig> worldConfig;
    @Nullable private volatile Context worldContext;
    private ImmutableList<Populator> populators;
    private ImmutableList<GeneratorPopulator> generatorPopulators;

    protected SpongeScoreboard spongeScoreboard = new SpongeScoreboard();

    @Shadow public Profiler theProfiler;
    @Shadow public boolean isRemote;
    @Shadow protected boolean scheduledUpdatesAreImmediate;
    @Shadow public WorldProvider provider;
    @Shadow protected WorldInfo worldInfo;
    @Shadow public Random rand;
    @Shadow public List<net.minecraft.entity.Entity> loadedEntityList;
    @Shadow public Scoreboard worldScoreboard;
    @Shadow public List<net.minecraft.tileentity.TileEntity> loadedTileEntityList;
    @Shadow private net.minecraft.world.border.WorldBorder worldBorder;

    @Shadow(prefix = "shadow$") public abstract net.minecraft.world.border.WorldBorder shadow$getWorldBorder();
    @Shadow(prefix = "shadow$") public abstract EnumDifficulty shadow$getDifficulty();

    @Shadow public abstract void onEntityAdded(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);
    @Shadow public abstract void updateEntity(net.minecraft.entity.Entity ent);
    @Shadow public abstract boolean isValid(BlockPos pos);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunkFromBlockCoords(BlockPos pos);
    @Shadow public abstract boolean checkLight(BlockPos pos);
    @Shadow public abstract void markBlockForUpdate(BlockPos pos);
    @Shadow public abstract void notifyNeighborsRespectDebug(BlockPos pos, Block blockType);
    @Shadow public abstract void updateComparatorOutputLevel(BlockPos pos, Block blockIn);
    @Shadow public abstract boolean addWeatherEffect(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract List<net.minecraft.entity.Entity> getEntities(Class<net.minecraft.entity.Entity> entityType,
            com.google.common.base.Predicate<net.minecraft.entity.Entity> filter);
    @Shadow public abstract void playSoundEffect(double x, double y, double z, String soundName, float volume, float pitch);
    @Shadow public abstract BiomeGenBase getBiomeGenForCoords(BlockPos pos);
    @Shadow public abstract IChunkProvider getChunkProvider();
    @Shadow public abstract WorldChunkManager getWorldChunkManager();
    @Shadow public abstract net.minecraft.tileentity.TileEntity getTileEntity(BlockPos pos);
    @Shadow public abstract boolean isBlockPowered(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunkFromChunkCoords(int chunkX, int chunkZ);
    @Shadow public abstract boolean isChunkLoaded(int x, int z, boolean allowEmpty);
    @Shadow public abstract int getRedstonePower(BlockPos pos, EnumFacing facing);
    @Shadow public abstract int getStrongPower(BlockPos pos, EnumFacing direction);
    @Shadow public abstract int isBlockIndirectlyGettingPowered(BlockPos pos);
    @Shadow public abstract net.minecraft.world.Explosion newExplosion(net.minecraft.entity.Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking);

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstructed(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client,
            CallbackInfo ci) {
        if (!client) {
            String providerName = providerIn.getDimensionName().toLowerCase().replace(" ", "_").replace("[^A-Za-z0-9_]", "");
            this.worldConfig =
                    new SpongeConfig<SpongeConfig.WorldConfig>(SpongeConfig.Type.WORLD,
                            new File(Sponge.getModConfigDirectory() + File.separator + "worlds" + File.separator
                                    + providerName + File.separator
                                    + (providerIn.getDimensionId() == 0 ? "DIM0" :
                                            Sponge.getSpongeRegistry().getWorldFolder(providerIn.getDimensionId()))
                                    , "world.conf"), Sponge.ECOSYSTEM_NAME.toLowerCase());
        }

        if (Sponge.getGame().getPlatform().getType() == Platform.Type.SERVER) {
            this.worldBorder.addListener(new PlayerBorderListener());
        }
        this.keepSpawnLoaded = ((WorldProperties) info).doesKeepSpawnLoaded();
        // Turn on capturing
        this.captureBlocks = true;
        this.captureEntitySpawns = true;
        this.captureBlockLists.put(CaptureType.BREAK, this.capturedSpongeBlockBreaks);
        this.captureBlockLists.put(CaptureType.FLUID, this.capturedSpongeBlockFluids);
        this.captureBlockLists.put(CaptureType.MODIFY, this.capturedSpongeBlockModifications);
        this.captureBlockLists.put(CaptureType.PLACE, this.capturedSpongeBlockPlaces);
    }

    @SuppressWarnings("rawtypes")
    @Overwrite
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (!this.isValid(pos)) {
            return false;
        } else if (!this.isRemote && this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD) {
            return false;
        } else {
            net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
            IBlockState currentState = chunk.getBlockState(pos);
            if (currentState == newState) {
                return false;
            }

            Block block = newState.getBlock();
            BlockSnapshot originalBlockSnapshot = null;
            BlockSnapshot newBlockSnapshot = null;
            Transaction<BlockSnapshot> transaction = null;

            if (!this.isRemote && !this.restoringBlocks) { // don't capture if we are restoring blocks
                if (StaticMixinHelper.processingPacket instanceof C08PacketPlayerBlockPlacement) {
                    IMixinChunk spongeChunk = (IMixinChunk) chunk;
                    if (block != currentState.getBlock()) { // Place
                        spongeChunk.addTrackedBlockPosition(block, pos, StaticMixinHelper.processingPlayer);
                    }
                }
                // remove block position if broken
                if (block == Blocks.air) {
                    ((IMixinChunk) chunk).removeTrackedPlayerPosition(pos);
                }
                originalBlockSnapshot = createSpongeBlockSnapshot(currentState, pos, flags);

                // black magic to track populators
                Class clazz = StaticMixinHelper.getCallerClass(3);

                if (net.minecraft.world.gen.feature.WorldGenerator.class.isAssignableFrom(clazz)) {
                    SpongePopulatorType populatorType = null;
                    populatorType = StaticMixinHelper.populator;

                    if (clazz == net.minecraft.world.gen.feature.WorldGenerator.class) {
                        if (StaticMixinHelper.lastPopulatorClass != null) {
                            clazz = StaticMixinHelper.lastPopulatorClass;
                        } else {
                            int level = 3;
                            // locate correct generator class
                            while (clazz == net.minecraft.world.gen.feature.WorldGenerator.class || clazz == net.minecraft.world.gen.feature.WorldGenHugeTrees.class) {
                                clazz = StaticMixinHelper.getCallerClass(level);
                                level++;
                            }
                        }
                    }

                    if (populatorType == null) {
                        populatorType = (SpongePopulatorType) Sponge.getSpongeRegistry().populatorClassToTypeMappings.get(clazz);
                    }

                    if (populatorType != null) {
                        if (this.capturedSpongePopulators.get(populatorType) == null) {
                            this.capturedSpongePopulators.put(populatorType, new ArrayList<Transaction<BlockSnapshot>>());
                        }

                        transaction = new Transaction<BlockSnapshot>(originalBlockSnapshot, originalBlockSnapshot.withState((BlockState)newState));
                        this.capturedSpongePopulators.get(populatorType).add(transaction);
                    }
                } else if (block.getMaterial().isLiquid() || currentState.getBlock().getMaterial().isLiquid()) {
                    this.capturedSpongeBlockFluids.add(originalBlockSnapshot);
                } else if (block == Blocks.air) {
                    this.capturedSpongeBlockBreaks.add(originalBlockSnapshot);
                } else if (block != currentState.getBlock()) {
                    this.capturedSpongeBlockPlaces.add(originalBlockSnapshot);
                } else {
                    this.capturedSpongeBlockModifications.add(originalBlockSnapshot);
                }
            }

            int oldLight = currentState.getBlock().getLightValue();

            IBlockState iblockstate1 = ((IMixinChunk)chunk).setBlockState(pos, newState, currentState, newBlockSnapshot);

            if (iblockstate1 == null) {
                if (originalBlockSnapshot != null) {
                    this.capturedSpongeBlockBreaks.remove(originalBlockSnapshot);
                    this.capturedSpongeBlockFluids.remove(originalBlockSnapshot);
                    this.capturedSpongeBlockPlaces.remove(originalBlockSnapshot);
                    this.capturedSpongeBlockModifications.remove(originalBlockSnapshot);
                }
                return false;
            } else {
                Block block1 = iblockstate1.getBlock();

                if (block.getLightOpacity() != block1.getLightOpacity() || block.getLightValue() != oldLight) {
                    this.theProfiler.startSection("checkLight");
                    this.checkLight(pos);
                    this.theProfiler.endSection();
                }

                if (originalBlockSnapshot == null) { // Don't notify clients or update physics while capturing blockstates
                    markAndNotifyNeighbors(pos, chunk, iblockstate1, newState, flags); // Modularize client and physic updates
                }

                return true;
            }
        }
    }

    public void markAndNotifyNeighbors(BlockPos pos, net.minecraft.world.chunk.Chunk chunk, IBlockState old, IBlockState new_, int flags) {
        if ((flags & 2) != 0 && (!this.isRemote || (flags & 4) == 0) && (chunk == null || chunk.isPopulated())) {
            this.markBlockForUpdate(pos);
        }

        if (!this.isRemote && (flags & 1) != 0) {
            this.notifyNeighborsRespectDebug(pos, old.getBlock());

            if (new_.getBlock().hasComparatorInputOverride()) {
                this.updateComparatorOutputLevel(pos, new_.getBlock());
            }
        }
    }

    @Override
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - this.weatherStartTime;
    }

    @Redirect(method = "forceBlockUpdateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onForceBlockUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null || ((IMixinWorld) worldIn).capturingTerrainGen()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickBlock = createSpongeBlockSnapshot(state, pos, 0); // flag doesn't matter here
        block.updateTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(this.currentTickBlock));
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onUpdateEntities(net.minecraft.entity.Entity entityIn) {
        if (this.isRemote || this.currentTickEntity != null) {
            entityIn.onUpdate();
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickEntity = (Entity) entityIn;
        entityIn.onUpdate();
        handlePostTickCaptures(Cause.of(entityIn));
        this.currentTickEntity = null;
        this.processingCaptureCause = false;
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/gui/IUpdatePlayerListBox;update()V"))
    public void onUpdateTileEntities(IUpdatePlayerListBox tile) {
        if (this.isRemote || this.currentTickTileEntity != null) {
            tile.update();
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickTileEntity = (TileEntity) tile;
        tile.update();
        handlePostTickCaptures(Cause.of(tile));
        this.currentTickTileEntity = null;
        this.processingCaptureCause = false;
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    public void onCallEntityUpdate(net.minecraft.entity.Entity entity) {
        if (this.isRemote || this.currentTickEntity != null || StaticMixinHelper.processingPlayer != null) {
            entity.onUpdate();
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickEntity = (Entity) entity;
        entity.onUpdate();
        handlePostTickCaptures(Cause.of(entity));
        this.currentTickEntity = null;
        this.processingCaptureCause = false;
    }

    @Overwrite
    public boolean spawnEntityInWorld(net.minecraft.entity.Entity entity) {
        return spawnEntity((Entity) entity, Cause.of(this));
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean spawnEntity(Entity entity, Cause cause) {
        checkNotNull(entity, "Entity cannot be null!");
        checkNotNull(cause, "Cause cannot be null!");

        net.minecraft.entity.Entity entityIn = (net.minecraft.entity.Entity) entity;
        // do not drop any items while restoring blocksnapshots. Prevents dupes
        if (!this.isRemote && (entityIn == null || (entityIn instanceof net.minecraft.entity.item.EntityItem && this.restoringBlocks))) return false;

        int i = MathHelper.floor_double(entityIn.posX / 16.0D);
        int j = MathHelper.floor_double(entityIn.posZ / 16.0D);
        boolean flag = entityIn.forceSpawn;

        if (entityIn instanceof EntityPlayer) {
            flag = true;
        }

        if (!flag && !this.isChunkLoaded(i, j, true)) {
            return false;
        } else {
            if (entityIn instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer)entityIn;
                net.minecraft.world.World world = (net.minecraft.world.World)(Object) this;
                world.playerEntities.add(entityplayer);
                world.updateAllPlayersSleepingFlag();
            }

            if (this.isRemote || flag) {
                //if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.EntityJoinWorldEvent(entityIn, (net.minecraft.world.World)(Object)this)) && !flag) return false;

                this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
                this.loadedEntityList.add(entityIn);
                this.onEntityAdded(entityIn);
                return true;
            }

            if (!flag && this.processingCaptureCause) {
                if (entityIn instanceof EntityItem) {
                    if (this.currentTickOnBlockAdded != null) {
                        this.capturedOnBlockAddedItems.add((Item) entityIn);
                    } else {
                        this.capturedEntityItems.add((Item) entityIn);
                    }
                } else {
                    if (this.currentTickOnBlockAdded != null) {
                        this.capturedOnBlockAddedEntities.add((Entity) entityIn);
                    } else {
                        this.capturedEntities.add((Entity) entityIn);
                    }
                }
                return true;
            } else { // Custom

                if (entityIn instanceof EntityFishHook && ((EntityFishHook) entityIn).angler == null) {
                    // TODO MixinEntityFishHook.setShooter makes angler null sometimes,
                    // but that will cause NPE when ticking
                    return false;
                }

                EntityLivingBase specialCause = null;
                // Special case for throwables
                if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityThrowable) {
                    EntityThrowable throwable = (EntityThrowable) entityIn;
                    specialCause = throwable.getThrower();

                    if (specialCause != null) {
                        if (specialCause instanceof Player) {
                            Player player = (Player) specialCause;
                            setCreatorEntityNbt(((IMixinEntity)entityIn).getSpongeData(), player.getUniqueId());
                        }
                    }
                }
                // Special case for TNT
                else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTNTPrimed) {
                    EntityTNTPrimed tntEntity = (EntityTNTPrimed) entityIn;
                    specialCause = tntEntity.getTntPlacedBy();

                    if (specialCause instanceof Player) {
                        Player player = (Player) specialCause;
                        setCreatorEntityNbt(((IMixinEntity)entityIn).getSpongeData(), player.getUniqueId());
                    }
                }
                // Special case for Tameables
                else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entityIn;
                    if (tameable.getOwnerEntity() != null) {
                        specialCause = tameable.getOwnerEntity();
                    }
                }

                if (specialCause != null) {
                    if (!cause.all().contains(specialCause)) {
                        cause = cause.with(specialCause);
                    }
                }

                org.spongepowered.api.event.Event event = null;
                ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
                entitySnapshotBuilder.add(((Entity) entityIn).createSnapshot());

                if (entityIn instanceof EntityItem) {
                    this.capturedEntityItems.add((Item) entityIn);
                    event = SpongeEventFactory.createDropItemEventCustom(Sponge.getGame(), cause, (List<Entity>)(List<?>)this.capturedEntityItems, entitySnapshotBuilder.build(), (World)(Object) this);
                } else {
                    event = SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getGame(), cause, this.capturedEntities, entitySnapshotBuilder.build(), (World)(Object) this);
                }
                Sponge.getGame().getEventManager().post(event);

                if (!((Cancellable)event).isCancelled()) {
                    if(entityIn instanceof net.minecraft.entity.effect.EntityWeatherEffect) {
                        return addWeatherEffect(entityIn);
                    }

                    this.getChunkFromChunkCoords(i, j).addEntity(entityIn);
                    this.loadedEntityList.add(entityIn);
                    this.onEntityAdded(entityIn);
                    if (entityIn instanceof EntityItem) {
                        this.capturedEntityItems.remove(entityIn);
                    } else {
                        this.capturedEntities.remove(entityIn);
                    }
                    return true;
                }

                return false;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handlePostTickCaptures(Cause cause) {
        if (this.isRemote || this.restoringBlocks || cause == null || cause.isEmpty()) {
            return;
        } else if (this.capturedEntities.size() == 0 && this.capturedEntityItems.size() == 0 && this.capturedSpongeBlockBreaks.size() == 0
                && this.capturedSpongeBlockModifications.size() == 0 && this.capturedSpongeBlockPlaces.size() == 0
                && this.capturedSpongeBlockFluids.size() == 0 && this.capturedSpongePopulators.size() == 0) {
            return; // nothing was captured, return
        }

        net.minecraft.world.World world = (net.minecraft.world.World)(Object) this;
        List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<Transaction<BlockSnapshot>>();
        boolean destructDrop = false;

        // Attempt to find a Player cause if we do not have one
        if (!cause.first(User.class).isPresent()) {
            if ((cause.first(BlockSnapshot.class).isPresent() || cause.first(TileEntity.class).isPresent())) {
                // Check for player at pos of first transaction
                Optional<BlockSnapshot> snapshot = cause.first(BlockSnapshot.class);
                Optional<TileEntity> te = cause.first(TileEntity.class);
                BlockPos pos = null;
                if (snapshot.isPresent()) {
                    pos = VecHelper.toBlockPos(snapshot.get().getPosition());
                } else {
                    pos = ((net.minecraft.tileentity.TileEntity)te.get()).getPos();
                }
                net.minecraft.world.chunk.Chunk chunk = this.getChunkFromBlockCoords(pos);
                if (chunk != null) {
                    IMixinChunk spongeChunk = (IMixinChunk) chunk;

                    Optional<User> user = spongeChunk.getBlockPosOwner(pos);
                    if (user.isPresent()) {
                        cause = cause.with(user.get());
                    }
                }
            } else if (cause.first(Entity.class).isPresent()) {
                Entity entity = cause.first(Entity.class).get();
                if (entity instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entity;
                    if (tameable.getOwnerEntity() != null) {
                        cause = cause.with(tameable.getOwnerEntity());
                    }
                } else {
                    if (((IMixinEntity) entity).getSpongeCreator().isPresent()) {
                       cause = cause.with(((IMixinEntity) entity).getSpongeCreator().get());
                    }
                }
            }
        }

        // Check root cause for additional information
        if (cause.root().get() instanceof EntityLivingBase) {
            EntityLivingBase rootEntity = (EntityLivingBase)cause.root().get();
            EntitySnapshot lastKilledEntity = ((IMixinEntityLivingBase)rootEntity).getLastKilledTarget();
            EntityLivingBase lastActiveTarget = ((IMixinEntityLivingBase)rootEntity).getLastActiveTarget();
            // Check for targeted entity
            if (lastKilledEntity != null) {
                // add the last targeted entity
                if (!cause.all().contains(lastKilledEntity)) {
                    Cause newCause = Cause.of(lastKilledEntity);
                    newCause = newCause.with(cause.all());
                    cause = newCause;
                    destructDrop = true;
                }
            }  else if (lastActiveTarget != null) {
                if (!cause.all().contains(lastActiveTarget)) {
                    if (lastActiveTarget.getHealth() <= 0) {
                        Cause newCause = Cause.of(((Entity)lastActiveTarget).createSnapshot());
                        newCause = newCause.with(cause.all());
                        cause = newCause;
                        destructDrop = true;
                    } else {
                        cause = cause.with(lastActiveTarget);
                    }
                }
            }
            if (rootEntity.getHealth() <= 0) {
                destructDrop = true;
            }
        } else {
            if (cause.root().get() instanceof net.minecraft.entity.Entity) {
                net.minecraft.entity.Entity entity = (net.minecraft.entity.Entity) cause.root().get();
                if (entity.isDead) {
                    destructDrop = true;
                }
            }
        }

        // Handle Block captures
        for (Map.Entry<CaptureType, List<BlockSnapshot>> mapEntry : this.captureBlockLists.entrySet()) {
            CaptureType captureType = mapEntry.getKey();
            List<BlockSnapshot> capturedBlockList = mapEntry.getValue();

            if (capturedBlockList.size() > 0) {
                ImmutableList<Transaction<BlockSnapshot>> blockTransactions;
                ImmutableList.Builder<Transaction<BlockSnapshot>> builder = new ImmutableList.Builder<Transaction<BlockSnapshot>>();

                Iterator<BlockSnapshot> iterator = capturedBlockList.iterator();
                while (iterator.hasNext()) {
                    BlockSnapshot blockSnapshot = iterator.next();
                    BlockPos pos = VecHelper.toBlockPos(blockSnapshot.getPosition());
                    IBlockState currentState = getBlockState(pos);
                    builder.add(new Transaction<BlockSnapshot>(blockSnapshot, createSpongeBlockSnapshot(currentState, pos, 0)));
                    iterator.remove();
                }
                blockTransactions = builder.build();

                if (blockTransactions.size() > 0) {
                    ChangeBlockEvent event = null;
                    // Check for tracked user at pos of first transaction
                    if (!cause.first(User.class).isPresent()) {
                        Transaction<BlockSnapshot> blockTransaction = blockTransactions.get(0);
                        Optional<Chunk> chunk = this.getChunk(blockTransaction.getOriginal().getPosition());
                        if (chunk.isPresent()) {
                            IMixinChunk spongeChunk = (IMixinChunk) chunk.get();
                            BlockPos pos = VecHelper.toBlockPos(blockTransaction.getOriginal().getPosition());
                            Optional<User> user = spongeChunk.getBlockPosOwner(pos);
                            if (user.isPresent()) {
                                cause = cause.with(user);
                            }
                        }
                    }

                    if (captureType == CaptureType.BREAK) {
                        if (StaticMixinHelper.processingPlayer != null && StaticMixinHelper.processingPacket instanceof C07PacketPlayerDigging) {
                            C07PacketPlayerDigging digPacket = (C07PacketPlayerDigging) StaticMixinHelper.processingPacket;
                            if (digPacket.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                                destructDrop = true;
                                Cause newCause = Cause.of(blockTransactions.get(0).getOriginal()); // make the first destroyed block root
                                newCause = newCause.with(cause.all());
                                cause = newCause;
                            }
                        }

                        event = SpongeEventFactory.createChangeBlockEventBreak(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.FLUID) {
                        event = SpongeEventFactory.createChangeBlockEventFluid(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.MODIFY) {
                        event = SpongeEventFactory.createChangeBlockEventModify(Sponge.getGame(), cause, (World) world, blockTransactions);
                    } else if (captureType == CaptureType.PLACE) {
                        event = SpongeEventFactory.createChangeBlockEventPlace(Sponge.getGame(), cause, (World) world, blockTransactions);
                    }

                    Sponge.getGame().getEventManager().post(event);

                    C08PacketPlayerBlockPlacement packet = null;

                    if (StaticMixinHelper.processingPacket instanceof C08PacketPlayerBlockPlacement) { // player place
                        packet = (C08PacketPlayerBlockPlacement) StaticMixinHelper.processingPacket;
                    }

                    for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                        if (!transaction.isValid()) {
                            this.restoringBlocks = true;
                            transaction.getOriginal().restore(true, false);
                            this.restoringBlocks = false;
                            invalidTransactions.add(transaction);
                        }
                    }

                    if (event.isCancelled()) {
                        // Restore original blocks
                        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                            this.restoringBlocks = true;
                            transaction.getOriginal().restore(true, false);
                            this.restoringBlocks = false;
                        }

                        handlePostPlayerBlockEvent(captureType, StaticMixinHelper.processingPlayer, world, event.getTransactions());

                        // clear entity list and return to avoid spawning items
                        this.capturedEntities.clear();
                        this.capturedEntityItems.clear();
                        return;
                    } else {
                        if (invalidTransactions.size() > 0) {
                            handlePostPlayerBlockEvent(captureType, StaticMixinHelper.processingPlayer, world, invalidTransactions);
                        }

                        if (this.capturedEntityItems.size() > 0) {
                            handleDroppedItems(cause, (List<Entity>)(List<?>)this.capturedEntityItems, invalidTransactions, captureType == CaptureType.BREAK ? true : destructDrop);
                        }

                        markAndNotifyBlockPost(event.getTransactions(), captureType, cause);

                        if (captureType == CaptureType.PLACE && StaticMixinHelper.processingPlayer != null && packet != null && packet.getStack() != null) {
                            StaticMixinHelper.processingPlayer.addStat(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(packet.getStack().getItem())], 1);
                        }
                    }
                }
            }
        }

        // Handle Populators
        boolean handlePopulators = false;

        for (List<Transaction<BlockSnapshot>> transactions : this.capturedSpongePopulators.values()) {
            if (transactions.size() > 0) {
                handlePopulators = true;
                break;
            }
        }

        if (handlePopulators && cause.first(Chunk.class).isPresent()) {
            Chunk targetChunk = cause.first(Chunk.class).get();
            PopulateChunkEvent.Post event = SpongeEventFactory.createPopulateChunkEventPost(Sponge.getGame(), cause, ImmutableMap.copyOf(this.capturedSpongePopulators), targetChunk);
            Sponge.getGame().getEventManager().post(event);

            for (List<Transaction<BlockSnapshot>> transactions : event.getPopulatedTransactions().values()) {
                markAndNotifyBlockPost(transactions, CaptureType.POPULATE, cause);
            }

            for (List<Transaction<BlockSnapshot>> transactions : this.capturedSpongePopulators.values()) {
                transactions.clear();
            }
        }

        // Handle Player Toss
        if (StaticMixinHelper.processingPlayer != null && StaticMixinHelper.processingPacket instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging digPacket = (C07PacketPlayerDigging) StaticMixinHelper.processingPacket;
            if (digPacket.getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM) {
                destructDrop = false;
            }
        }

        // Handle Player kill commands
        if (StaticMixinHelper.processingPlayer != null && StaticMixinHelper.processingPacket instanceof C01PacketChatMessage) {
            C01PacketChatMessage chatPacket = (C01PacketChatMessage) StaticMixinHelper.processingPacket;
            if (chatPacket.getMessage().contains("kill")) {
                if (!cause.all().contains(StaticMixinHelper.processingPlayer)) {
                    cause = cause.with(StaticMixinHelper.processingPlayer);
                }
                destructDrop = true;
            }
        }

        // Handle Entity captures
        if (this.capturedEntityItems.size() > 0) {
            handleDroppedItems(cause, (List<Entity>)(List<?>)this.capturedEntityItems, invalidTransactions, destructDrop);
        }
        if (this.capturedEntities.size() > 0) {
            handleEntitySpawns(cause, this.capturedEntities, invalidTransactions);
        }
    }

    private void handlePostPlayerBlockEvent(CaptureType captureType, EntityPlayerMP player, net.minecraft.world.World world, List<Transaction<BlockSnapshot>> transactions) {
        if (captureType == CaptureType.BREAK && player != null) {
            // Let the client know the blocks still exist
            for (Transaction<BlockSnapshot> transaction : transactions) {
                BlockSnapshot snapshot = transaction.getOriginal();
                BlockPos pos = VecHelper.toBlockPos(snapshot.getPosition());
                player.playerNetServerHandler.sendPacket(new S23PacketBlockChange(world, pos));

                // Update any tile entity data for this block
                net.minecraft.tileentity.TileEntity tileentity = world.getTileEntity(pos);
                if (tileentity != null)  {
                    Packet pkt = tileentity.getDescriptionPacket();
                    if (pkt != null) {
                        player.playerNetServerHandler.sendPacket(pkt);
                    }
                }
            }
        } else if (captureType == CaptureType.PLACE && player != null) {
            sendItemChangeToPlayer(player);
        }
    }

    private void sendItemChangeToPlayer(EntityPlayerMP player) {
        if (StaticMixinHelper.lastPlayerItem == null) {
            return;
        }

        // handle revert
        player.isChangingQuantityOnly = true;
        player.inventory.mainInventory[player.inventory.currentItem] = StaticMixinHelper.lastPlayerItem;
        Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
        player.openContainer.detectAndSendChanges();
        player.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber, StaticMixinHelper.lastPlayerItem));
    }

    private void handleDroppedItems(Cause cause, List<Entity> entities, List<Transaction<BlockSnapshot>> invalidTransactions, boolean destructItems) {
        Iterator<Entity> iter = entities.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                setCreatorEntityNbt(((IMixinEntity)currentEntity).getSpongeData(), user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getSpongeCreator();
                if (owner.isPresent()) {
                    if (!cause.all().contains(owner.get())) {
                        cause = cause.with(owner.get());
                    }
                    setCreatorEntityNbt(((IMixinEntity)currentEntity).getSpongeData(), owner.get().getUniqueId());
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        DropItemEvent event = null;

        if (destructItems) {
            event = SpongeEventFactory.createDropItemEventDestruct(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build() , (World) this);
        } else {
            event = SpongeEventFactory.createDropItemEventDispense(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build() , (World) this);
        }

        if (!(Sponge.getGame().getEventManager().post(event))) {
            // Handle player deaths
            if (cause.first(Player.class).isPresent()) {
                EntityPlayerMP player = (EntityPlayerMP) cause.first(Player.class).get();
                if (player.getHealth() <= 0 && (player.theItemInWorldManager.getGameType() != GameType.CREATIVE) && !player.worldObj.getGameRules().getGameRuleBooleanValue("keepInventory")) {
                    player.inventory.clear();
                } else if (player.getHealth() <= 0) { // don't drop anything if creative or keepInventory
                    this.capturedEntityItems.clear();
                }
            }

            // TODO blood fix this
            final Iterator<Entity> iterator = event instanceof DropItemEvent.Destruct ? ((DropItemEvent.Destruct)event).getEntities().iterator() :
                            ((DropItemEvent.Dispense)event).getEntities().iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                boolean invalidSpawn = false;
                if (invalidTransactions != null) {
                    for (Transaction<BlockSnapshot> transaction : invalidTransactions) {
                        if (transaction.getOriginal().getLocation().get().getBlockPosition().equals(entity.getLocation().getBlockPosition())) {
                            invalidSpawn = true;
                            break;
                        }
                    }

                    if (invalidSpawn) {
                        iterator.remove();
                        continue;
                    }
                }

                net.minecraft.entity.Entity nmsEntity = (net.minecraft.entity.Entity) entity;
                int x = MathHelper.floor_double(nmsEntity.posX / 16.0D);
                int z = MathHelper.floor_double(nmsEntity.posZ / 16.0D);
                this.getChunkFromChunkCoords(x, z).addEntity(nmsEntity);
                this.loadedEntityList.add(nmsEntity);
                this.onEntityAdded(nmsEntity);
                SpongeHooks.logEntitySpawn(cause, nmsEntity);
                iterator.remove();
            }
        } else {
            if (StaticMixinHelper.processingPlayer != null) {
                sendItemChangeToPlayer(StaticMixinHelper.processingPlayer);
            }
            this.capturedEntityItems.clear();
        }
    }

    private void handleEntitySpawns(Cause cause, List<Entity> entities, List<Transaction<BlockSnapshot>> invalidTransactions) {
        Iterator<Entity> iter = entities.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<EntitySnapshot>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                setCreatorEntityNbt(((IMixinEntity)currentEntity).getSpongeData(), user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getSpongeCreator();
                if (owner.isPresent()) {
                    if (!cause.all().contains(owner.get())) {
                        cause = cause.with(owner.get());
                    }
                    setCreatorEntityNbt(((IMixinEntity)currentEntity).getSpongeData(), owner.get().getUniqueId());
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        SpawnEntityEvent event = null;

        if (this.worldSpawnerRunning) {
            event = SpongeEventFactory.createSpawnEntityEventSpawner(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build(), (World)(Object)this);
        } else if (this.chunkSpawnerRunning){
            event = SpongeEventFactory.createSpawnEntityEventChunkLoad(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build(), (World)(Object)this);
        } else {
            event = SpongeEventFactory.createSpawnEntityEvent(Sponge.getGame(), cause, entities, entitySnapshotBuilder.build(), (World)(Object)this);
        }

        if (!(Sponge.getGame().getEventManager().post(event))) {
            Iterator<Entity> iterator = event.getEntities().iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                boolean invalidSpawn = false;
                if (invalidTransactions != null) {
                    for (Transaction<BlockSnapshot> transaction : invalidTransactions) {
                        if (transaction.getOriginal().getLocation().get().getBlockPosition().equals(entity.getLocation().getBlockPosition())) {
                            invalidSpawn = true;
                            break;
                        }
                    }

                    if (invalidSpawn) {
                        iterator.remove();
                        continue;
                    }
                }

                net.minecraft.entity.Entity nmsEntity = (net.minecraft.entity.Entity) entity;
                int x = MathHelper.floor_double(nmsEntity.posX / 16.0D);
                int z = MathHelper.floor_double(nmsEntity.posZ / 16.0D);
                this.getChunkFromChunkCoords(x, z).addEntity(nmsEntity);
                this.loadedEntityList.add(nmsEntity);
                this.onEntityAdded(nmsEntity);
                SpongeHooks.logEntitySpawn(cause, nmsEntity);
                iterator.remove();
            }
        } else {
            this.capturedEntities.clear();
        }
    }

    private void setCreatorEntityNbt(NBTTagCompound nbt, UUID uuid) {
        if (!nbt.hasKey(NbtDataUtil.SPONGE_ENTITY_CREATOR)){
            NBTTagCompound creatorNbt = new NBTTagCompound();
            creatorNbt.setLong("uuid_least", uuid.getLeastSignificantBits());
            creatorNbt.setLong("uuid_most", uuid.getMostSignificantBits());
            nbt.setTag(NbtDataUtil.SPONGE_ENTITY_CREATOR, creatorNbt);
        } else {
            nbt.getCompoundTag(NbtDataUtil.SPONGE_ENTITY_CREATOR).setLong("uuid_least", uuid.getLeastSignificantBits());
            nbt.getCompoundTag(NbtDataUtil.SPONGE_ENTITY_CREATOR).setLong("uuid_most", uuid.getMostSignificantBits());
        }
    }

    @Override
    public Optional<BlockSnapshot> getCurrentTickBlock() {
        return Optional.ofNullable(this.currentTickBlock);
    }

    @Override
    public Optional<Entity> getCurrentTickEntity() {
        return Optional.ofNullable(this.currentTickEntity);
    }

    @Override
    public Optional<TileEntity> getCurrentTickTileEntity() {
        return Optional.ofNullable(this.currentTickTileEntity);
    }

    @Override
    public boolean restoringBlocks() {
        return this.restoringBlocks;
    }

    @Override
    public boolean capturingBlocks() {
        return this.captureBlocks;
    }

    @Override
    public boolean capturingTerrainGen() {
        return this.captureTerrainGen;
    }

    @Override
    public void setCapturingTerrainGen(boolean flag) {
        this.captureTerrainGen = flag;
    }

    @Override
    public void setCapturingEntitySpawns(boolean flag) {
        this.captureEntitySpawns = flag;
    }

    @Override
    public List<BlockSnapshot> getBlockBreakList() {
        return this.capturedSpongeBlockBreaks;
    }

    @Override
    public boolean isWorldSpawnerRunning() {
        return this.worldSpawnerRunning;
    }

    @Override
    public void setWorldSpawnerRunning(boolean flag) {
        this.worldSpawnerRunning = flag;
    }

    @Override
    public boolean isChunkSpawnerRunning() {
        return this.chunkSpawnerRunning;
    }

    @Override
    public void setChunkSpawnerRunning(boolean flag) {
        this.chunkSpawnerRunning = flag;
    }

    @Override
    public boolean processingCaptureCause() {
        return this.processingCaptureCause;
    }

    @Override
    public void setProcessingCaptureCause(boolean flag) {
        this.processingCaptureCause = flag;
    }

    @Override
    public void setCurrentTickBlock(BlockSnapshot snapshot) {
        this.currentTickBlock = snapshot;
    }

    private void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, CaptureType type, Cause cause) {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            // Handle custom replacements
            if (transaction.isValid() && transaction.getCustom().isPresent()) {
                this.restoringBlocks = true;
                transaction.getFinal().restore(true, false);
                this.restoringBlocks = false;
            }

            SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot)transaction.getOriginal();
            SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot)transaction.getFinal();
            SpongeHooks.logBlockAction(cause, (net.minecraft.world.World)(Object) this, type, transaction);
            int updateFlag = oldBlockSnapshot.getUpdateFlag();
            BlockPos pos = VecHelper.toBlockPos(oldBlockSnapshot.getPosition());
            IBlockState originalState = (IBlockState)oldBlockSnapshot.getState();
            IBlockState newState = (IBlockState)newBlockSnapshot.getState();
            if (newState != null && !SpongeImplFactory.blockHasTileEntity(newState.getBlock(), newState)) { // Containers get placed automatically
                this.currentTickOnBlockAdded = this.createSpongeBlockSnapshot(newState, pos, updateFlag);
                newState.getBlock().onBlockAdded((net.minecraft.world.World)(Object)this, pos, newState);
                if (this.capturedOnBlockAddedItems.size() > 0) {
                    Cause blockCause = Cause.of(this.currentTickOnBlockAdded);
                    if (this.captureTerrainGen) {
                        net.minecraft.world.chunk.Chunk chunk = getChunkFromBlockCoords(pos);
                        if (chunk != null && ((IMixinChunk) chunk).getCurrentPopulateCause() != null) {
                            blockCause = blockCause.with(((IMixinChunk) chunk).getCurrentPopulateCause().all());
                        }
                    }
                    handleDroppedItems(blockCause, this.capturedOnBlockAddedItems, null, getBlockState(pos) != newState);
                }
                if (this.capturedOnBlockAddedEntities.size() > 0) {
                    Cause blockCause = Cause.of(this.currentTickOnBlockAdded);
                    if (this.captureTerrainGen) {
                        net.minecraft.world.chunk.Chunk chunk = getChunkFromBlockCoords(pos);
                        if (chunk != null && ((IMixinChunk) chunk).getCurrentPopulateCause() != null) {
                            blockCause = blockCause.with(((IMixinChunk) chunk).getCurrentPopulateCause().all());
                        }
                    }
                    handleEntitySpawns(blockCause, this.capturedOnBlockAddedEntities, null);
                }

                this.currentTickOnBlockAdded = null;
            }

            markAndNotifyNeighbors(pos, null, originalState, newState, updateFlag);
        }
    }

    @Override
    public SpongeBlockSnapshot createSpongeBlockSnapshot(IBlockState state, BlockPos pos, int updateFlag) {
        builder.reset();
        Location<World> location = new Location<World>((World) this, VecHelper.toVector(pos));
        builder.blockState((BlockState) state)
            .worldId(location.getExtent().getUniqueId())
            .position(location.getBlockPosition());
        net.minecraft.tileentity.TileEntity te = getTileEntity(pos);
        NBTTagCompound nbt = null;
        if (te != null) {
            nbt = new NBTTagCompound();
            te.writeToNBT(nbt);
        }
        if (nbt != null) {
            builder.unsafeNbt(nbt);
        }

        return new SpongeBlockSnapshot(builder, updateFlag);
    }

    @Shadow
    public abstract int getSkylightSubtracted();

    @Shadow
    public abstract int getLightFor(EnumSkyBlock type, BlockPos pos);

    @SuppressWarnings("rawtypes")
    @Inject(method = "getCollidingBoundingBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    public void onGetCollidingBoundingBoxes(net.minecraft.entity.Entity entity, AxisAlignedBB axis,
            CallbackInfoReturnable<List> cir) {
        if (!entity.worldObj.isRemote && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
            // Removing misbehaved living entities
            cir.setReturnValue(new ArrayList());
        }
    }

    @Override
    public UUID getUniqueId() {
        return ((WorldProperties) this.worldInfo).getUniqueId();
    }

    @Override
    public String getName() {
        return this.worldInfo.getWorldName();
    }

    @Override
    public Optional<Chunk> getChunk(Vector3i position) {
        return getChunk(position.getX(), position.getY(), position.getZ());
    }

    @Override
    public Optional<Chunk> getChunk(int x, int y, int z) {
        if (!SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return Optional.empty();
        }
        WorldServer worldserver = (WorldServer) (Object) this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(x, z)) {
            chunk = worldserver.theChunkProviderServer.provideChunk(x, z);
        }
        return Optional.ofNullable((Chunk) chunk);
    }

    @Override
    public Optional<Chunk> loadChunk(Vector3i position, boolean shouldGenerate) {
        return loadChunk(position.getX(), position.getY(), position.getZ(), shouldGenerate);
    }

    @Override
    public Optional<Chunk> loadChunk(int x, int y, int z, boolean shouldGenerate) {
        if (!SpongeChunkLayout.instance.isValidChunk(x, y, z)) {
            return Optional.empty();
        }
        WorldServer worldserver = (WorldServer) (Object) this;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(x, z) || shouldGenerate) {
            chunk = worldserver.theChunkProviderServer.loadChunk(x, z);
        }
        return Optional.ofNullable((Chunk) chunk);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        // avoid intermediate object creation from using BlockState
        return (BlockType) getChunkFromChunkCoords(x >> 4, z >> 4).getBlock(x, y, z);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        checkBlockBounds(x, y, z);
        SpongeHooks.setBlockState(((net.minecraft.world.World) (Object) this), x, y, z, block);
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkBiomeBounds(x, z);
        return (BiomeType) this.getBiomeGenForCoords(new BlockPos(x, 0, z));
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkBiomeBounds(x, z);
        ((Chunk) getChunkFromChunkCoords(x >> 4, z >> 4)).setBiome(x, z, biome);
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
    public Optional<Entity> createEntity(EntityType type, Vector3d position) {
        checkNotNull(type, "The entity type cannot be null!");
        checkNotNull(position, "The position cannot be null!");

        Entity entity = null;

        Class<? extends Entity> entityClass = type.getEntityClass();
        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        if (entityClass.isAssignableFrom(EntityPlayerMP.class) || entityClass.isAssignableFrom(EntityDragonPart.class)) {
            // Unable to construct these
            return Optional.empty();
        }

        net.minecraft.world.World world = (net.minecraft.world.World) (Object) this;

        // Not all entities have a single World parameter as their constructor
        if (entityClass.isAssignableFrom(EntityLightningBolt.class)) {
            entity = (Entity) new EntityLightningBolt(world, x, y, z);
        } else if (entityClass.isAssignableFrom(EntityEnderPearl.class)) {
            EntityArmorStand tempEntity = new EntityArmorStand(world, x, y, z);
            entity = (Entity) new EntityEnderPearl(world, tempEntity);
            ((EnderPearl) entity).setShooter(ProjectileSource.UNKNOWN);
        }

        // Some entities need to have non-null fields (and the easiest way to
        // set them is to use the more specialised constructor).
        if (entityClass.isAssignableFrom(EntityFallingBlock.class)) {
            entity = (Entity) new EntityFallingBlock(world, x, y, z, Blocks.sand.getDefaultState());
        } else if (entityClass.isAssignableFrom(EntityItem.class)) {
            entity = (Entity) new EntityItem(world, x, y, z, new ItemStack(Blocks.stone));
        }

        if (entity == null) {
            try {
                entity = ConstructorUtils.invokeConstructor(entityClass, this);
                ((net.minecraft.entity.Entity) entity).setPosition(x, y, z);
            } catch (Exception e) {
                Sponge.getLogger().error(ExceptionUtils.getStackTrace(e));
            }
        }

        if (entity instanceof EntityHanging) {
            if (((EntityHanging) entity).facingDirection == null) {
                // TODO Some sort of detection of a valid direction?
                // i.e scan immediate blocks for something to attach onto.
                ((EntityHanging) entity).facingDirection = EnumFacing.NORTH;
            }
            if (!((EntityHanging) entity).onValidSurface()) {
                return Optional.empty();
            }
        }

        // Last chance to fix null fields
        if (entity instanceof EntityPotion) {
            // make sure EntityPotion.potionDamage is not null
            ((EntityPotion) entity).getPotionDamage();
        } else if (entity instanceof EntityPainting) {
            // This is default when art is null when reading from NBT, could
            // choose a random art instead?
            ((EntityPainting) entity).art = EnumArt.KEBAB;
        }

        return Optional.ofNullable(entity);
    }

    @Override
    public Optional<Entity> createEntity(EntityType type, Vector3i position) {
        return this.createEntity(type, position.toDouble());
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
        EntitySnapshot entitySnapshot = snapshot.withLocation(new Location<World>(this, position));
        return entitySnapshot.restore();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return (WorldBorder) shadow$getWorldBorder();
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

        List<Packet> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();

            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            for (Packet packet : packets) {
                manager.sendToAllNear(x, y, z, radius, this.provider.getDimensionId(), packet);
            }
        }
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
    public long getRemainingDuration() {
        Weather weather = getWeather();
        if (weather.equals(Weathers.CLEAR)) {
            if (this.worldInfo.getCleanWeatherTime() > 0) {
                return this.worldInfo.getCleanWeatherTime();
            } else {
                return Math.min(this.worldInfo.getThunderTime(), this.worldInfo.getRainTime());
            }
        } else if (weather.equals(Weathers.THUNDER_STORM)) {
            return this.worldInfo.getThunderTime();
        } else if (weather.equals(Weathers.RAIN)) {
            return this.worldInfo.getRainTime();
        }
        return 0;
    }

    @Override
    public void forecast(Weather weather) {
        this.forecast(weather, (300 + this.rand.nextInt(600)) * 20);
    }

    @Override
    public void forecast(Weather weather, long duration) {
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
    public Dimension getDimension() {
        return (Dimension) this.provider;
    }

    @Override
    public boolean doesKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    @Override
    public void setKeepSpawnLoaded(boolean keepLoaded) {
        this.keepSpawnLoaded = keepLoaded;
    }

    @Override
    public SpongeConfig<SpongeConfig.WorldConfig> getWorldConfig() {
        return this.worldConfig;
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume) {
        this.playSound(sound, position, volume, 1);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch) {
        this.playSound(sound, position, volume, pitch, 0);
    }

    @Override
    public void playSound(SoundType sound, Vector3d position, double volume, double pitch, double minVolume) {
        this.playSoundEffect(position.getX(), position.getY(), position.getZ(), sound.getName(), (float) Math.max(minVolume, volume), (float) pitch);
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        World spongeWorld = this;
        if (spongeWorld instanceof WorldServer) {
            return Optional.ofNullable((Entity) ((WorldServer) (Object) this).getEntityFromUuid(uuid));
        }
        for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
            if (entity.getUniqueID().equals(uuid)) {
                return Optional.of((Entity) entity);
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Chunk> getLoadedChunks() {
        return ((ChunkProviderServer) this.getChunkProvider()).loadedChunks;
    }

    @Override
    public boolean unloadChunk(Chunk chunk) {
        return chunk != null && chunk.unloadChunk();
    }

    @Override
    public WorldCreationSettings getCreationSettings() {
        WorldProperties properties = this.getProperties();

        // Create based on WorldProperties
        WorldSettings settings = new WorldSettings(this.worldInfo);
        IMixinWorldSettings mixin = (IMixinWorldSettings) (Object) settings;
        mixin.setDimensionType(properties.getDimensionType());
        mixin.setGeneratorSettings(properties.getGeneratorSettings());
        mixin.setGeneratorModifiers(properties.getGeneratorModifiers());
        mixin.setEnabled(true);
        mixin.setKeepSpawnLoaded(this.keepSpawnLoaded);
        mixin.setLoadOnStartup(properties.loadOnStartup());

        return (WorldCreationSettings) (Object) settings;
    }

    @Override
    public void updateWorldGenerator() {
        // No need to wrap generator if no modifiers are present
        if (this.getProperties().getGeneratorModifiers().isEmpty()) {
            return;
        }

        IMixinWorldType worldType = (IMixinWorldType) this.getProperties().getGeneratorType();

        // Get the default generator for the world type
        DataContainer generatorSettings = this.getProperties().getGeneratorSettings();
        SpongeWorldGenerator newGenerator = worldType.createGenerator(this, generatorSettings);

        // Re-apply all world generator modifiers
        WorldCreationSettings creationSettings = this.getCreationSettings();

        for (WorldGeneratorModifier modifier : this.getProperties().getGeneratorModifiers()) {
            modifier.modifyWorldGenerator(creationSettings, generatorSettings, newGenerator);
        }

        // Set this world generator
        this.setWorldGenerator(newGenerator);
    }

    @Override
    public ImmutableList<Populator> getPopulators() {
        if (this.populators == null) {
            this.populators = ImmutableList.of();
        }
        return this.populators;
    }

    @Override
    public ImmutableList<GeneratorPopulator> getGeneratorPopulators() {
        if (this.generatorPopulators == null) {
            this.generatorPopulators = ImmutableList.of();
        }
        return this.generatorPopulators;
    }

    @Override
    public WorldProperties getProperties() {
        return (WorldProperties) this.worldInfo;
    }

    @Override
    public Location<World> getSpawnLocation() {
        return new Location<World>(this, this.worldInfo.getSpawnX(), this.worldInfo.getSpawnY(), this.worldInfo.getSpawnZ());
    }

    @Override
    public Context getContext() {
        if (this.worldContext == null) {
            this.worldContext = new Context(Context.WORLD_KEY, getName());
        }
        return this.worldContext;
    }

    @Override
    public Optional<TileEntity> getTileEntity(int x, int y, int z) {
        net.minecraft.tileentity.TileEntity tileEntity = getTileEntity(new BlockPos(x, y, z));
        if (tileEntity == null) {
            return Optional.empty();
        } else {
            return Optional.of((TileEntity) tileEntity);
        }
    }

    @Override
    public Vector2i getBiomeMin() {
        return BIOME_MIN;
    }

    @Override
    public Vector2i getBiomeMax() {
        return BIOME_MAX;
    }

    @Override
    public Vector2i getBiomeSize() {
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
    public boolean containsBiome(int x, int z) {
        return VecHelper.inBounds(x, z, BIOME_MIN, BIOME_MAX);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, BLOCK_MIN, BLOCK_MAX);
    }

    @Override
    public void setWorldGenerator(WorldGenerator generator) {
        // Replace populators with possibly modified list
        this.populators = ImmutableList.copyOf(generator.getPopulators());
        this.generatorPopulators = ImmutableList.copyOf(generator.getGeneratorPopulators());

        // Replace biome generator with possible modified one
        BiomeGenerator biomeGenerator = generator.getBiomeGenerator();
        WorldServer thisWorld = (WorldServer) (Object) this;
        thisWorld.provider.worldChunkMgr = CustomWorldChunkManager.of(biomeGenerator);

        // Replace generator populator with possibly modified one
        ((ChunkProviderServer) this.getChunkProvider()).serverChunkGenerator =
                CustomChunkProviderGenerate.of(thisWorld, biomeGenerator, generator.getBaseGeneratorPopulator(), this.generatorPopulators);
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        // We have to create a new instance every time to satisfy the contract
        // of this method, namely that changing the state of the returned
        // instance does not affect the world without setWorldGenerator being
        // called
        ChunkProviderServer serverChunkProvider = (ChunkProviderServer) this.getChunkProvider();
        WorldServer world = (WorldServer) (Object) this;
        return new SpongeWorldGenerator(
                SpongeBiomeGenerator.of(getWorldChunkManager()),
                SpongeGeneratorPopulator.of(world, serverChunkProvider.serverChunkGenerator),
                getGeneratorPopulators(),
                getPopulators());
    }

    private void checkBiomeBounds(int x, int z) {
        if (!containsBiome(x, z)) {
            throw new PositionOutOfBoundsException(new Vector2i(x, z), BIOME_MIN, BIOME_MAX);
        }
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), BLOCK_MIN, BLOCK_MAX);
        }
    }

    @Override
    public org.spongepowered.api.scoreboard.Scoreboard getScoreboard() {
        return this.spongeScoreboard;
    }

    @Override
    public void setScoreboard(org.spongepowered.api.scoreboard.Scoreboard scoreboard) {
        this.spongeScoreboard = checkNotNull(((SpongeScoreboard) scoreboard), "Scoreboard cannot be null!");
        this.worldScoreboard = ((SpongeScoreboard) scoreboard).createScoreboard(scoreboard);
    }

    @Override
    public Difficulty getDifficulty() {
        return (Difficulty) (Object) this.shadow$getDifficulty();
    }

    @SuppressWarnings("unchecked")
    private List<Player> getPlayers() {
        return ((net.minecraft.world.World) (Object) this).getPlayers(Player.class, Predicates.alwaysTrue());
    }

    @Override
    public void sendMessage(ChatType type, Text... messages) {
        for (Player player : getPlayers()) {
            player.sendMessage(type, messages);
        }
    }

    @Override
    public void sendMessage(ChatType type, Iterable<Text> messages) {
        for (Player player : getPlayers()) {
            player.sendMessage(type, messages);
        }
    }

    @Override
    public void sendTitle(Title title) {
        for (Player player : getPlayers()) {
            player.sendTitle(title);
        }
    }

    @Override
    public void resetTitle() {
        for (Player player : getPlayers()) {
            player.resetTitle();
        }
    }

    @Override
    public void clearTitle() {
        for (Player player : getPlayers()) {
            player.clearTitle();
        }
    }

    public Collection<Direction> getPoweredBlockFaces(int x, int y, int z) {
        // Similar to World.getStrongPower(BlockPos)
        BlockPos pos = new BlockPos(x, y, z);
        ImmutableList.Builder<Direction> faces = ImmutableList.builder();
        for (EnumFacing facing : EnumFacing.values()) {
            if (this.getStrongPower(pos.offset(facing), facing) > 0) {
                faces.add(SpongeGameRegistry.directionMap.inverse().get(facing));
            }
        }
        return faces.build();
    }

    public Collection<Direction> getIndirectlyPoweredBlockFaces(int x, int y, int z) {
        // Similar to World.isBlockIndirectlyGettingPowered
        BlockPos pos = new BlockPos(x, y, z);
        ImmutableList.Builder<Direction> faces = ImmutableList.builder();
        for (EnumFacing facing : EnumFacing.values()) {
            if (this.getRedstonePower(pos.offset(facing), facing) > 0) {
                faces.add(SpongeGameRegistry.directionMap.inverse().get(facing));
            }
        }
        return faces.build();
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
    public boolean isLoaded() {
        return DimensionManager.getWorldFromDimId(this.provider.getDimensionId()) != null;
    }

    @Override
    public Optional<String> getGameRule(String gameRule) {
        return this.getProperties().getGameRule(gameRule);
    }

    @Override
    public Map<String, String> getGameRules() {
        return this.getProperties().getGameRules();
    }

    @Override
    public void triggerExplosion(Explosion explosion) {
        checkNotNull(explosion, "explosion");
        checkNotNull(explosion.getOrigin(), "origin");

        newExplosion((net.minecraft.entity.Entity) explosion.getSourceExplosive().orElse(null), explosion
                .getOrigin().getX(), explosion.getOrigin().getY(), explosion.getOrigin().getZ(), explosion.getRadius(), explosion.canCauseFire(),
                explosion.shouldBreakBlocks());
    }

    @Override
    public Extent getExtentView(Vector3i newMin, Vector3i newMax) {
        checkBlockBounds(newMin.getX(), newMin.getY(), newMin.getZ());
        checkBlockBounds(newMax.getX(), newMax.getY(), newMax.getZ());
        return ExtentViewDownsize.newInstance(this, newMin, newMax);
    }

    @Override
    public Extent getExtentView(DiscreteTransform3 transform) {
        return ExtentViewTransform.newInstance(this, transform);
    }

    @Override
    public Extent getRelativeExtentView() {
        return getExtentView(DiscreteTransform3.fromTranslation(getBlockMin().negate()));
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        World world = ((World) this);
        BlockState state = world.getBlock(x, y, z);
        Optional<TileEntity> te = world.getTileEntity(x, y, z);
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder()
            .blockState(state)
            .worldId(world.getUniqueId())
            .position(new Vector3i(x, y, z));
        if (te.isPresent()) {
            final TileEntity tileEntity = te.get();
            for (DataManipulator<?, ?> manipulator : tileEntity.getContainers()) {
                builder.add(manipulator);
            }
            final NBTTagCompound compound = new NBTTagCompound();
            ((net.minecraft.tileentity.TileEntity) tileEntity).writeToNBT(compound);
            builder.unsafeNbt(compound);
        }
        return builder.build();
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, boolean notifyNeighbors) {
        return snapshot.restore(force, notifyNeighbors);
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, boolean notifyNeighbors) {
        snapshot = snapshot.withLocation(new Location<World>(this, new Vector3i(x, y, z)));
        return snapshot.restore(force, notifyNeighbors);
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Class<T> propertyClass) {
        final Optional<PropertyStore<T>> optional = SpongePropertyRegistry.getInstance().getStore(propertyClass);
        if (optional.isPresent()) {
            return optional.get().getFor(new Location<World>(this, x, y, z));
        }
        return Optional.empty();
    }

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(int x, int y, int z, Direction direction, Class<T> propertyClass) {
        final Optional<PropertyStore<T>> optional = SpongePropertyRegistry.getInstance().getStore(propertyClass);
        if (optional.isPresent()) {
            return optional.get().getFor(new Location<World>(this, x, y, z), direction);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getProperties(int x, int y, int z) {
        return SpongePropertyRegistry.getInstance().getPropertiesFor(new Location<World>(this, x, y, z));
    }

    @Override
    public <E> Optional<E> get(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        final Optional<E> optional = getBlock(x, y, z).get(key);
        if (optional.isPresent()) {
            return optional;
        } else {
            final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
            if (tileEntityOptional.isPresent()) {
                return tileEntityOptional.get().get(key);
            }
        }
        return Optional.empty();
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
        } else {
            final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
            if (tileEntity.isPresent()) {
                return tileEntity.get().getOrCreate(manipulatorClass);
            }
        }
        return Optional.empty();
    }

    @Override
    public <E> E getOrNull(int x, int y, int z, Key<? extends BaseValue<E>> key) {
        return get(x, y, z, key).orElse(null);
    }

    @Override
    public <E> E getOrElse(int x, int y, int z, Key<? extends BaseValue<E>> key, E defaultValue) {
        return get(x, y, z, key).orElse(defaultValue);
    }

    @Override
    public <E, V extends BaseValue<E>> Optional<V> getValue(int x, int y, int z, Key<V> key) {
        final BlockState blockState = getBlock(x, y, z);
        if (blockState.supports(key)) {
            return blockState.getValue(key);
        } else {
            final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
            if (tileEntity.isPresent() && tileEntity.get().supports(key)) {
                return tileEntity.get().getValue(key);
            }
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
    public boolean supports(int x, int y, int z, BaseValue<?> value) {
        return supports(x, y, z, value.getKey());
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
            if (tileEntity.isPresent()) {
                tileEntitySupports = tileEntity.get().supports(manipulatorClass);
            } else {
                tileEntitySupports = false;
            }
            return tileEntitySupports;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean supports(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        return supports(x, y, z, (Class<DataManipulator<?,?>>) manipulator.getClass());
    }

    @Override
    public Set<Key<?>> getKeys(int x, int y, int z) {
        final ImmutableSet.Builder<Key<?>> builder = ImmutableSet.builder();
        final BlockState blockState = getBlock(x, y, z);
        builder.addAll(blockState.getKeys());
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        if (tileEntity.isPresent()) {
            builder.addAll(tileEntity.get().getKeys());
        }
        return builder.build();
    }

    @Override
    public Set<ImmutableValue<?>> getValues(int x, int y, int z) {
        final ImmutableSet.Builder<ImmutableValue<?>> builder = ImmutableSet.builder();
        final BlockState blockState = getBlock(x, y, z);
        builder.addAll(blockState.getValues());
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        if (tileEntity.isPresent()) {
            builder.addAll(tileEntity.get().getValues());
        }
        return builder.build();
    }

    @Override
    public <E> DataTransactionResult transform(int x, int y, int z, Key<? extends BaseValue<E>> key, Function<E, E> function) {
        if (supports(x, y, z, key)) {
            final Optional<E> optional = get(x, y, z, key);
            return offer(x, y, z, key, function.apply(optional.get()));
        }
        return DataTransactionBuilder.failNoData();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, Key<? extends BaseValue<E>> key, E value) {
        final BlockState blockState = getBlock(x, y, z);
        if (blockState.supports(key)) {
            ImmutableValue<E> old = ((Value<E>)getValue(x, y, z, (Key) key).get()).asImmutable();
            setBlock(x, y, z, blockState.with(key, value).get());
            ImmutableValue<E> newVal = ((Value<E>) getValue(x, y, z, (Key) key).get()).asImmutable();
            return DataTransactionBuilder.successReplaceResult(newVal, old);
        }
        final Optional<TileEntity> tileEntity = getTileEntity(x, y, z);
        if (tileEntity.isPresent() && tileEntity.get().supports(key)) {
            return tileEntity.get().offer(key, value);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public <E> DataTransactionResult offer(int x, int y, int z, BaseValue<E> value) {
        return offer(x, y, z, value.getKey(), value.get());
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator) {
        return offer(x, y, z, manipulator, MergeFunction.IGNORE_ALL);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public DataTransactionResult offer(int x, int y, int z, DataManipulator<?, ?> manipulator, MergeFunction function) {
        final BlockState blockState = getBlock(x, y, z);
        final ImmutableDataManipulator<?, ?> immutableDataManipulator = manipulator.asImmutable();
        if (blockState.supports((Class) immutableDataManipulator.getClass())) {
            final List<ImmutableValue<?>> old = new ArrayList<ImmutableValue<?>>(blockState.getValues());
            final BlockState newState = blockState.with(immutableDataManipulator).get();
            old.removeAll(newState.getValues());
            setBlock(x, y, z, newState);
            return DataTransactionBuilder.successReplaceResult(old, manipulator.getValues());
        } else {
            final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
            if (tileEntityOptional.isPresent()) {
                return tileEntityOptional.get().offer(manipulator, function);
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public DataTransactionResult offer(int x, int y, int z, Iterable<DataManipulator<?, ?>> manipulators) {
        final DataTransactionBuilder builder = DataTransactionBuilder.builder();
        for (DataManipulator<?, ?> manipulator : manipulators) {
            builder.absorbResult(offer(x, y, z, manipulator));
        }
        return builder.build();
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Class<? extends DataManipulator<?, ?>> manipulatorClass) {
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        if (tileEntityOptional.isPresent()) {
            return tileEntityOptional.get().remove(manipulatorClass);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult remove(int x, int y, int z, Key<?> key) {
        final Optional<TileEntity> tileEntityOptional = getTileEntity(x, y, z);
        if (tileEntityOptional.isPresent()) {
            return tileEntityOptional.get().remove(key);
        }
        return DataTransactionBuilder.failNoData();
    }

    @Override
    public DataTransactionResult undo(int x, int y, int z, DataTransactionResult result) {
        return DataTransactionBuilder.failNoData(); // todo
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from) {
        return copyFrom(xTo, yTo, zTo, from, MergeFunction.IGNORE_ALL);
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, int xFrom, int yFrom, int zFrom) {
        return copyFrom(xTo, yTo, zTo, new Location<World>(this, xFrom, yFrom, zFrom));
    }

    @Override
    public DataTransactionResult copyFrom(int xTo, int yTo, int zTo, DataHolder from, MergeFunction function) {
        final DataTransactionBuilder builder = DataTransactionBuilder.builder();
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
        final List<DataManipulator<?, ?>> list = new ArrayList<DataManipulator<?, ?>>();
        final Collection<ImmutableDataManipulator<?, ?>> manipulators = this.getBlock(x, y, z).getManipulators();
        for (ImmutableDataManipulator<?, ?> immutableDataManipulator : manipulators) {
            list.add(immutableDataManipulator.asMutable());
        }
        final Optional<TileEntity> optional = getTileEntity(x, y, z);
        if (optional.isPresent()) {
            list.addAll(optional.get().getContainers());
        }
        return list;
    }

    @Override
    public boolean validateRawData(int x, int y, int z, DataView container) {
        return false; // todo
    }

    @Override
    public void setRawData(int x, int y, int z, DataView container) throws InvalidDataException {
        // todo
    }

    @Override
    public long getWeatherStartTime() {
        return this.weatherStartTime;
    }

    @Override
    public void setWeatherStartTime(long weatherStartTime) {
        this.weatherStartTime = weatherStartTime;
    }
}
