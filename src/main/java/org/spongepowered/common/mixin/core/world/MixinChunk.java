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
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.worker.MutableBiomeVolumeWorker;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.IMixinCachable;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerChunkMapEntry;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.extent.ExtentViewDownsize;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeVolumeWorker;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class MixinChunk implements Chunk, IMixinChunk, IMixinCachable {

    private org.spongepowered.api.world.World sponge_world;
    private UUID uuid;
    private long scheduledForUnload = -1; // delay chunk unloads
    private boolean persistedChunk = false;
    private boolean isSpawning = false;
    private net.minecraft.world.chunk.Chunk[] neighbors = new net.minecraft.world.chunk.Chunk[4];
    private long cacheKey;
    private Cause chunkCause;
    private static final Direction[] CARDINAL_DIRECTIONS = new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private static final Vector3i BIOME_SIZE = new Vector3i(SpongeChunkLayout.CHUNK_SIZE.getX(), 1, SpongeChunkLayout.CHUNK_SIZE.getZ());
    private Vector3i chunkPos;
    private Vector3i blockMin;
    private Vector3i blockMax;
    private Vector3i biomeMin;
    private Vector3i biomeMax;

    @Shadow @Final private World world;
    @Shadow @Final public int x;
    @Shadow @Final public int z;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;
    @Shadow @Final private int[] precipitationHeightMap;
    @Shadow @Final private int[] heightMap;
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;
    @Shadow @Final private Map<BlockPos, TileEntity> tileEntities;
    @Shadow private long inhabitedTime;
    @Shadow private boolean loaded;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean dirty;
    @Shadow public boolean unloadQueued;

    // @formatter:off
    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType p_177424_2_);
    @Shadow public abstract void generateSkylightMap();
    @Shadow public abstract int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract IBlockState getBlockState(int x, int y, int z);
    @Shadow public abstract Biome getBiome(BlockPos pos, BiomeProvider chunkManager);
    @Shadow public abstract byte[] getBiomeArray();
    @Shadow public abstract void setBiomeArray(byte[] biomeArray);
    @Shadow public abstract void checkLight();
    @Shadow public abstract <T extends Entity> void getEntitiesOfTypeWithinAABB(Class <? extends T > entityClass, AxisAlignedBB aabb,
    List<T> listToFill, Predicate <? super T > p_177430_4_);
    @Shadow private void propagateSkylightOcclusion(int x, int z) { };
    @Shadow private void relightBlock(int x, int y, int z) { };
    // @formatter:on

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"), remap = false)
    public void onConstructed(World world, int x, int z, CallbackInfo ci) {
        this.chunkPos = new Vector3i(x, 0, z);
        this.blockMin = SpongeChunkLayout.instance.toWorld(this.chunkPos).get();
        this.blockMax = this.blockMin.add(SpongeChunkLayout.CHUNK_SIZE).sub(1, 1, 1);
        this.biomeMin = new Vector3i(this.blockMin.getX(), 0, this.blockMin.getZ());
        this.biomeMax = new Vector3i(this.blockMax.getX(), 0, this.blockMax.getZ());
        this.sponge_world = (org.spongepowered.api.world.World) world;
        if (this.sponge_world.getUniqueId() != null) { // Client worlds have no UUID
            this.uuid = new UUID(this.sponge_world.getUniqueId().getMostSignificantBits() ^ (x * 2 + 1),
                    this.sponge_world.getUniqueId().getLeastSignificantBits() ^ (z * 2 + 1));
        }
        this.cacheKey = ChunkPos.asLong(this.x, this.z);
        this.chunkCause = Cause.source(this.world).build();
    }

    @Override
    public long getCacheKey() {
        return this.cacheKey;
    }

    @Override
    public boolean isChunkLoaded() {
        return this.loaded;
    }

    @Override
    public boolean isQueuedForUnload() {
        return this.unloadQueued;
    }

    @Override
    public boolean isPersistedChunk() {
        return this.persistedChunk;
    }

    @Override
    public void setPersistedChunk(boolean flag) {
        this.persistedChunk = flag;
        // update persisted status for entities and TE's
        for (TileEntity tileEntity : this.tileEntities.values()) {
            ((IMixinTileEntity) tileEntity).setActiveChunk(this);
        }
        for (ClassInheritanceMultiMap<Entity> entityList : this.entityLists) {
            for (Entity entity : entityList) {
                ((IMixinEntity) entity).setActiveChunk(this);
            }
        }
    }

    @Override
    public boolean isSpawning() {
        return this.isSpawning;
    }

    @Override
    public void setIsSpawning(boolean spawning) {
        this.isSpawning = spawning;
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void onChunkAddEntity(Entity entityIn, CallbackInfo ci) {
        if (!entityIn.isDead) {
            ((IMixinEntity) entityIn).setActiveChunk(this);
        }
    }

    @Inject(method = "addTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;validate()V"))
    private void onChunkAddTileEntity(BlockPos pos, TileEntity tileEntityIn, CallbackInfo ci) {
        ((IMixinTileEntity) tileEntityIn).setActiveChunk(this);
    }

    @Inject(method = "removeEntityAtIndex", at = @At("RETURN"))
    private void onChunkRemoveEntityAtIndex(Entity entityIn, int index, CallbackInfo ci) {
        ((IMixinEntity) entityIn).setActiveChunk(null);
    }

    @Redirect(method = "removeTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;invalidate()V"))
    private void onChunkRemoveTileEntity(TileEntity tileEntityIn) {
        ((IMixinTileEntity) tileEntityIn).setActiveChunk(null);
        tileEntityIn.invalidate();
    }

    @Inject(method = "onLoad()V", at = @At("RETURN"))
    public void onLoadInject(CallbackInfo ci) {
        if (!this.world.isRemote) {
            SpongeHooks.logChunkLoad(this.world, this.chunkPos);
        }

        for (Direction direction : CARDINAL_DIRECTIONS) {
            Vector3i neighborPosition = this.getPosition().add(direction.asBlockOffset());
            IMixinChunkProviderServer spongeChunkProvider = (IMixinChunkProviderServer) this.world.getChunkProvider();
            net.minecraft.world.chunk.Chunk neighbor = spongeChunkProvider.getLoadedChunkWithoutMarkingActive
                    (neighborPosition.getX(), neighborPosition.getZ());
            if (neighbor != null) {
                int neighborIndex = directionToIndex(direction);
                int oppositeNeighborIndex = directionToIndex(direction.getOpposite());
                this.setNeighborChunk(neighborIndex, neighbor);
                ((IMixinChunk) neighbor).setNeighborChunk(oppositeNeighborIndex, (net.minecraft.world.chunk.Chunk) (Object) this);
            }
        }
        SpongeImpl.postEvent(SpongeEventFactory.createLoadChunkEvent(this.chunkCause, (Chunk) this));
    }

    @Inject(method = "onUnload()V", at = @At("RETURN"))
    public void onUnloadInject(CallbackInfo ci) {
        if (!this.world.isRemote) {
            SpongeHooks.logChunkUnload(this.world, this.chunkPos);
        }

        for (Direction direction : CARDINAL_DIRECTIONS) {
            Vector3i neighborPosition = this.getPosition().add(direction.asBlockOffset());
            IMixinChunkProviderServer spongeChunkProvider = (IMixinChunkProviderServer) this.world.getChunkProvider();
            net.minecraft.world.chunk.Chunk neighbor = spongeChunkProvider.getLoadedChunkWithoutMarkingActive
                    (neighborPosition.getX(), neighborPosition.getZ());
            if (neighbor != null) {
                int neighborIndex = directionToIndex(direction);
                int oppositeNeighborIndex = directionToIndex(direction.getOpposite());
                this.setNeighborChunk(neighborIndex, null);
                ((IMixinChunk) neighbor).setNeighborChunk(oppositeNeighborIndex, null);
            }
        }
        SpongeImpl.postEvent(SpongeEventFactory.createUnloadChunkEvent(this.chunkCause, (Chunk) this));
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public Vector3i getPosition() {
        return this.chunkPos;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public boolean loadChunk(boolean generate) {
        WorldServer worldserver = (WorldServer) this.world;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.getChunkProvider().chunkExists(this.x, this.z) || generate) {
            chunk = worldserver.getChunkProvider().loadChunk(this.x, this.z);
        }

        return chunk != null;
    }

    @Override
    public int getInhabittedTime() {
        return (int) this.inhabitedTime;
    }

    @Override
    public double getRegionalDifficultyFactor() {
        final boolean flag = this.world.getDifficulty() == EnumDifficulty.HARD;
        float moon = this.world.getCurrentMoonPhaseFactor();
        float f2 = MathHelper.clamp(((float) this.world.getWorldTime() + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
        float f3 = 0.0F;
        f3 += MathHelper.clamp((float) this.inhabitedTime / 3600000.0F, 0.0F, 1.0F) * (flag ? 1.0F : 0.75F);
        f3 += MathHelper.clamp(moon * 0.25F, 0.0F, f2);
        return f3;
    }

    @Override
    public double getRegionalDifficultyPercentage() {
        final double region = getRegionalDifficultyFactor();
        if (region < 2) {
            return 0;
        } else if (region > 4) {
            return 1.0;
        } else {
            return (region - 2.0) / 2.0;
        }
    }

    @Override
    public org.spongepowered.api.world.World getWorld() {
        return this.sponge_world;
    }

    @Override
    public BiomeType getBiome(int x, int y, int z) {
        checkBiomeBounds(x, y, z);
        return (BiomeType) getBiome(new BlockPos(x, y, z), this.world.getBiomeProvider());
    }

    @Override
    public void setBiome(int x, int y, int z, BiomeType biome) {
        checkBiomeBounds(x, y, z);
        // Taken from Chunk#getBiome
        byte[] biomeArray = getBiomeArray();
        int i = x & 15;
        int j = z & 15;
        biomeArray[j << 4 | i] = (byte) (Biome.getIdForBiome((Biome) biome) & 255);
        setBiomeArray(biomeArray);

        if (this.world instanceof WorldServer) {
            final PlayerChunkMapEntry entry = ((WorldServer) this.world).getPlayerChunkMap().getEntry(this.x, this.z);
            if (entry != null) {
                ((IMixinPlayerChunkMapEntry) entry).markBiomesForUpdate();
            }
        }
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block, Cause cause) {
        checkBlockBounds(x, y, z);
        return BlockUtil.setBlockState((net.minecraft.world.chunk.Chunk) (Object) this, x, y, z, block, false);
    }

    @Override
    public boolean setBlock(int x, int y, int z, BlockState block, BlockChangeFlag flag, Cause cause) {
        return BlockUtil.setBlockState((net.minecraft.world.chunk.Chunk) (Object) this, (this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15),
                block, flag.updateNeighbors());
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockType) getBlockState(x, y, z).getBlock();
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        return this.sponge_world.createSnapshot((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15));
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, BlockChangeFlag flag, Cause cause) {
        return this.sponge_world.restoreSnapshot(snapshot, force, flag, cause);
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, BlockChangeFlag flag, Cause cause) {
        return this.sponge_world.restoreSnapshot((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), snapshot, force, flag, cause);
    }

    public double getHighestYAt(double x, double z) {
        return this.sponge_world.getHighestYAt(this.x << 4 + ((int)x & 15), this.z << 4 + ((int)z & 15));
    }

    @Override
    public Vector3i getBiomeMin() {
        return this.biomeMin;
    }

    @Override
    public Vector3i getBiomeMax() {
        return this.biomeMax;
    }

    @Override
    public Vector3i getBiomeSize() {
        return BIOME_SIZE;
    }

    @Override
    public Vector3i getBlockMin() {
        return this.blockMin;
    }

    @Override
    public Vector3i getBlockMax() {
        return this.blockMax;
    }

    @Override
    public Vector3i getBlockSize() {
        return SpongeChunkLayout.CHUNK_SIZE;
    }

    @Override
    public boolean containsBiome(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.biomeMin, this.biomeMax);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.blockMin, this.blockMax);
    }

    private void checkBiomeBounds(int x, int y, int z) {
        if (!containsBiome(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.biomeMin, this.biomeMax);
        }
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.blockMin, this.blockMax);
        }
    }

    @Override
    public Extent getExtentView(Vector3i newMin, Vector3i newMax) {
        checkBlockBounds(newMin.getX(), newMin.getY(), newMin.getZ());
        checkBlockBounds(newMax.getX(), newMax.getY(), newMax.getZ());
        return new ExtentViewDownsize(this, newMin, newMax);
    }

    @Override
    public MutableBiomeVolumeWorker<Chunk> getBiomeWorker() {
        return new SpongeMutableBiomeVolumeWorker<>(this);
    }

    @Override
    public MutableBlockVolumeWorker<Chunk> getBlockWorker(Cause cause) {
        return new SpongeMutableBlockVolumeWorker<>(this, cause);
    }

    @Inject(method = "getEntitiesWithinAABBForEntity", at = @At(value = "RETURN"))
    public void onGetEntitiesWithinAABBForEntity(Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<Entity> p_177414_4_,
            CallbackInfo ci) {
        if (this.world.isRemote || CauseTracker.getInstance().getCurrentPhaseData().state.ignoresEntityCollisions()) {
            return;
        }

        if (listToFill.size() == 0) {
            return;
        }

        CollideEntityEvent event = SpongeCommonEventFactory.callCollideEntityEvent(this.world, entityIn, listToFill);
        final PhaseData peek = CauseTracker.getInstance().getCurrentPhaseData();

        if (event == null || event.isCancelled()) {
            if (event == null && !peek.state.getPhase().isTicking(peek.state)) {
                return;
            }
            listToFill.clear();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "getEntitiesOfTypeWithinAABB", at = @At(value = "RETURN"))
    public void onGetEntitiesOfTypeWithinAAAB(Class<? extends Entity> entityClass, AxisAlignedBB aabb, List listToFill, Predicate<Entity> p_177430_4_,
            CallbackInfo ci) {
        if (this.world.isRemote || CauseTracker.getInstance().getCurrentPhaseData().state.ignoresEntityCollisions()) {
            return;
        }

        if (listToFill.size() == 0) {
            return;
        }

        CollideEntityEvent event = SpongeCommonEventFactory.callCollideEntityEvent(this.world, null, listToFill);
        final PhaseData peek = CauseTracker.getInstance().getCurrentPhaseData();

        if (event == null || event.isCancelled()) {
            if (event == null && !peek.state.getPhase().isTicking(peek.state)) {
                return;
            }
            listToFill.clear();
        }
    }


    /**
     * @author blood
     * @reason cause tracking
     *
     * @param pos The position to set
     * @param state The state to set
     * @return The changed state
     */
    @Nullable
    @Overwrite
    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        IBlockState iblockstate1 = this.getBlockState(pos);

        // Sponge - reroute to new method that accepts snapshot to prevent a second snapshot from being created.
        return setBlockState(pos, state, iblockstate1, null, BlockChangeFlag.ALL);
    }

    @Nullable
    @Override
    public IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, @Nullable BlockSnapshot originalBlockSnapshot) {
        return this.setBlockState(pos, newState, currentState, originalBlockSnapshot, BlockChangeFlag.ALL);
    }

    /**
     * @author blood - November 2015
     * @author gabizou - updated April 10th, 2016 - Add cause tracking refactor changes
     *
     *
     * @param pos The position changing
     * @param newState The new state
     * @param currentState The current state - passed in from either chunk or world
     * @param newBlockSnapshot The new snapshot. This can be null when calling {@link MixinChunk#setBlockState(BlockPos, IBlockState)} directly,
     *      as there's no block snapshot to change.
     * @return The changed block state if not null
     */
    @Override
    @Nullable
    public IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, @Nullable BlockSnapshot newBlockSnapshot, BlockChangeFlag flag) {
        int xPos = pos.getX() & 15;
        int yPos = pos.getY();
        int zPos = pos.getZ() & 15;
        int combinedPos = zPos << 4 | xPos;

        if (yPos >= this.precipitationHeightMap[combinedPos] - 1) {
            this.precipitationHeightMap[combinedPos] = -999;
        }

        int currentHeight = this.heightMap[combinedPos];

        // Sponge Start - remove blockstate check as we handle it in world.setBlockState
        // IBlockState iblockstate = this.getBlockState(pos);
        //
        // if (iblockstate == state) {
        //    return null;
        // } else {
        Block newBlock = newState.getBlock();
        Block currentBlock = currentState.getBlock();
        // Sponge End

        ExtendedBlockStorage extendedblockstorage = this.storageArrays[yPos >> 4];
        // Sponge - make this final so we don't change it later
        final boolean requiresNewLightCalculations;

        // Sponge - Forge moves this from
        int newBlockLightOpacity = SpongeImplHooks.getBlockLightOpacity(newState, this.world, pos);

        if (extendedblockstorage == net.minecraft.world.chunk.Chunk.NULL_BLOCK_STORAGE) {
            if (newBlock == Blocks.AIR) {
                return null;
            }

            extendedblockstorage = this.storageArrays[yPos >> 4] = new ExtendedBlockStorage(yPos >> 4 << 4, this.world.provider.hasSkyLight());
            requiresNewLightCalculations = yPos >= currentHeight;
            // Sponge Start - properly initialize variable
        } else {
            requiresNewLightCalculations = false;
        }
        // Sponge end

        extendedblockstorage.set(xPos, yPos & 15, zPos, newState);

        // Sponge Start
        // if (block1 != block) // Sponge - Forge removes this change.
        {
            if (!this.world.isRemote) {
                // Sponge - Forge adds this change for block changes to only fire events when necessary
                if (currentState.getBlock() != newState.getBlock()) {
                    currentBlock.breakBlock(this.world, pos, currentState);
                }
                // Sponge - Add several tile entity hook checks. Mainly for forge added hooks, but these
                // still work by themselves in vanilla.
                TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (te != null && SpongeImplHooks.shouldRefresh(te, this.world, pos, currentState, newState)) {
                    this.world.removeTileEntity(pos);
                }
            // } else if (currentBlock instanceof ITileEntityProvider) { // Sponge - remove since forge has a special hook we need to add here
            } else if (SpongeImplHooks.hasBlockTileEntity(currentBlock, currentState)) {
                TileEntity tileEntity = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                // Sponge - Add hook for refreshing, because again, forge hooks.
                if (tileEntity != null && SpongeImplHooks.shouldRefresh(tileEntity, this.world, pos, currentState, newState)) {
                    // Sponge End
                    this.world.removeTileEntity(pos);
                }
            }
        }

        if (extendedblockstorage.get(xPos, yPos & 15, zPos).getBlock() != newBlock) {
            return null;
        }
        // Sponge Start - Slight modifications
        // } else { // Sponge - remove unnecessary else
        if (requiresNewLightCalculations) {
            this.generateSkylightMap();
        } else {

            // int newBlockLightOpacity = state.getLightOpacity(); - Sponge Forge moves this all the way up before tile entities are removed.
            // int postNewBlockLightOpacity = newState.getLightOpacity(this.worldObj, pos); - Sponge use the SpongeImplHooks for forge compatibility
            int postNewBlockLightOpacity = SpongeImplHooks.getBlockLightOpacity(newState, this.world, pos);
            // Sponge End

            if (newBlockLightOpacity > 0) {
                if (yPos >= currentHeight) {
                    this.relightBlock(xPos, yPos + 1, zPos);
                }
            } else if (yPos == currentHeight - 1) {
                this.relightBlock(xPos, yPos, zPos);
            }

            if (newBlockLightOpacity != postNewBlockLightOpacity && (newBlockLightOpacity < postNewBlockLightOpacity || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                this.propagateSkylightOcclusion(xPos, zPos);
            }
        }

        if (!this.world.isRemote && currentBlock != newBlock) {
            // Sponge start - Ignore block activations during block placement captures unless it's
            // a BlockContainer. Prevents blocks such as TNT from activating when
            // cancelled.
            final CauseTracker causeTracker = CauseTracker.getInstance();
            final PhaseData peek = causeTracker.getCurrentPhaseData();
            final boolean requiresCapturing = peek.state.getPhase().requiresBlockCapturing(peek.state);
            if (!requiresCapturing || SpongeImplHooks.hasBlockTileEntity(newBlock, newState)) {
                // The new block state is null if called directly from Chunk#setBlockState(BlockPos, IBlockState)
                // If it is null, then directly call the onBlockAdded logic.
                if (newBlockSnapshot == null && flag.performBlockPhysics()) {
                    newBlock.onBlockAdded(this.world, pos, newState);
                }
            }
            // Sponge end
        }

        // Sponge Start - Use SpongeImplHooks for forge compatibility
        // if (block instanceof ITileEntityProvider) { // Sponge
        if (SpongeImplHooks.hasBlockTileEntity(newBlock, newState)) {
            // Sponge End
            TileEntity tileentity = this.getTileEntity(pos, EnumCreateEntityType.CHECK);

            if (tileentity == null) {
                // Sponge Start - use SpongeImplHooks for forge compatibility
                // tileentity = ((ITileEntityProvider)block).createNewTileEntity(this.worldObj, block.getMetaFromState(state)); // Sponge
                tileentity = SpongeImplHooks.createTileEntity(newBlock, this.world, newState);
                // Sponge End
                this.world.setTileEntity(pos, tileentity);
            }

            if (tileentity != null) {
                tileentity.updateContainingBlockInfo();
            }
        }

        this.dirty = true;
        return currentState;
    }

    // These methods are enabled in MixinChunk_Tracker as a Mixin plugin

    @Override
    public void addTrackedBlockPosition(Block block, BlockPos pos, User user, PlayerTracker.Type trackerType) {

    }

    @Override
    public Map<Integer, PlayerTracker> getTrackedIntPlayerPositions() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Short, PlayerTracker> getTrackedShortPlayerPositions() {
        return Collections.emptyMap();
    }

    @Override
    public Optional<User> getBlockOwner(BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public Optional<User> getBlockNotifier(BlockPos pos) {
        return Optional.empty();
    }

    @Override
    public void setBlockNotifier(BlockPos pos, @Nullable UUID uuid) {

    }

    @Override
    public void setBlockCreator(BlockPos pos, @Nullable UUID uuid) {

    }

    @Override
    public void setTrackedIntPlayerPositions(Map<Integer, PlayerTracker> trackedPositions) {
    }

    @Override
    public void setTrackedShortPlayerPositions(Map<Short, PlayerTracker> trackedPositions) {
    }

    // Continuing the rest of the implementation

    @Override
    public org.spongepowered.api.entity.Entity createEntity(EntityType type, Vector3d position)
            throws IllegalArgumentException, IllegalStateException {
        return this.sponge_world.createEntity(type, this.chunkPos.mul(16).toDouble().add(position.min(15, this.blockMax.getY(), 15)));
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(DataContainer entityContainer) {
        return this.sponge_world.createEntity(entityContainer);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        return this.sponge_world.createEntity(entityContainer, this.chunkPos.mul(16).toDouble().add(position.min(15, this.blockMax.getY(), 15)));
    }

    @Override
    public boolean spawnEntity(org.spongepowered.api.entity.Entity entity, Cause cause) {
        return this.sponge_world.spawnEntity(entity, cause);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities() {
        Set<org.spongepowered.api.entity.Entity> entities = Sets.newHashSet();
        for (ClassInheritanceMultiMap entityList : this.entityLists) {
            entities.addAll(entityList);
        }
        return entities;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities(java.util.function.Predicate<org.spongepowered.api.entity.Entity> filter) {
        Set<org.spongepowered.api.entity.Entity> entities = Sets.newHashSet();
        for (ClassInheritanceMultiMap<Entity> entityClassMap : this.entityLists) {
            for (Object entity : entityClassMap) {
                if (filter.test((org.spongepowered.api.entity.Entity) entity)) {
                    entities.add((org.spongepowered.api.entity.Entity) entity);
                }
            }
        }
        return entities;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Collection<org.spongepowered.api.block.tileentity.TileEntity> getTileEntities() {
        return Sets.newHashSet((Collection) this.tileEntities.values());
    }

    @Override
    public Collection<org.spongepowered.api.block.tileentity.TileEntity>
    getTileEntities(java.util.function.Predicate<org.spongepowered.api.block.tileentity.TileEntity> filter) {
        Set<org.spongepowered.api.block.tileentity.TileEntity> tiles = Sets.newHashSet();
        for (Entry<BlockPos, TileEntity> entry : this.tileEntities.entrySet()) {
            if (filter.test((org.spongepowered.api.block.tileentity.TileEntity) entry.getValue())) {
                tiles.add((org.spongepowered.api.block.tileentity.TileEntity) entry.getValue());
            }
        }
        return tiles;
    }

    @Override
    public Optional<org.spongepowered.api.block.tileentity.TileEntity> getTileEntity(int x, int y, int z) {
        return Optional.ofNullable((org.spongepowered.api.block.tileentity.TileEntity) this.getTileEntity(
                new BlockPos((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15)), EnumCreateEntityType.CHECK));
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> restoreSnapshot(EntitySnapshot snapshot, Vector3d position) {
        return this.sponge_world.restoreSnapshot(snapshot, position);
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        return this.sponge_world.getScheduledUpdates((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15));
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        return this.sponge_world.addScheduledUpdate((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), priority, ticks);
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        this.sponge_world.removeScheduledUpdate((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), update);
    }

    @Override
    public boolean hitBlock(int x, int y, int z, Direction side, Cause cause) {
        return this.sponge_world.hitBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), side, cause);
    }

    @Override
    public boolean interactBlock(int x, int y, int z, Direction side, Cause cause) {
        return this.sponge_world.interactBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), side, cause);
    }

    @Override
    public boolean placeBlock(int x, int y, int z, BlockState block, Direction side, Cause cause) {
        return this.sponge_world.placeBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), block, side, cause);
    }

    @Override
    public boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, Cause cause) {
        return this.sponge_world.interactBlockWith((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), itemStack, side, cause);
    }

    @Override
    public boolean digBlock(int x, int y, int z, Cause cause) {
        return this.sponge_world.digBlock((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), cause);
    }

    @Override
    public boolean digBlockWith(int x, int y, int z, ItemStack itemStack, Cause cause) {
        return this.sponge_world.digBlockWith((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), itemStack, cause);
    }

    @Override
    public int getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, Cause cause) {
        return this.sponge_world.getBlockDigTimeWith((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15), itemStack, cause);
    }

    @Redirect(method = "populate(Lnet/minecraft/world/chunk/IChunkProvider;Lnet/minecraft/world/chunk/IChunkGenerator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkProvider;getLoadedChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
    public net.minecraft.world.chunk.Chunk onPopulateLoadChunk(IChunkProvider chunkProvider, int x, int z) {
        // Don't mark chunks as active
        return ((IMixinChunkProviderServer) chunkProvider).getLoadedChunkWithoutMarkingActive(x, z);
    }

    @Inject(method = "populate(Lnet/minecraft/world/chunk/IChunkGenerator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkGenerator;populate(II)V"))
    private void onPopulate(IChunkGenerator generator, CallbackInfo callbackInfo) {
        if (CauseTracker.ENABLED && !this.world.isRemote) {
            final CauseTracker causeTracker = CauseTracker.getInstance();
            causeTracker.switchToPhase(GenerationPhase.State.TERRAIN_GENERATION, PhaseContext.start()
                    .add(NamedCause.of(InternalNamedCauses.WorldGeneration.WORLD, this.world))
                    .addCaptures()
                    .complete());
        }
    }

    @Inject(method = "populate(Lnet/minecraft/world/chunk/IChunkGenerator;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;markDirty()V"),
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkGenerator;populate(II)V"))
    )
    private void onPopulateFinish(IChunkGenerator generator, CallbackInfo info) {
        if (CauseTracker.ENABLED && !this.world.isRemote) {
            CauseTracker.getInstance().completePhase(GenerationPhase.State.TERRAIN_GENERATION);
        }
    }

    @Override
    public Optional<AABB> getBlockSelectionBox(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return this.sponge_world.getBlockSelectionBox((this.x << 4) + (x & 15), y, (this.z << 4) + (z & 15));
    }

    @Override
    public Set<org.spongepowered.api.entity.Entity> getIntersectingEntities(AABB box,
            java.util.function.Predicate<org.spongepowered.api.entity.Entity> filter) {
        checkNotNull(box, "box");
        checkNotNull(filter, "filter");
        final List<Entity> entities = new ArrayList<>();
        getEntitiesOfTypeWithinAABB(net.minecraft.entity.Entity.class, VecHelper.toMC(box), entities,
            entity -> filter.test((org.spongepowered.api.entity.Entity) entity));
        return entities.stream().map(entity -> (org.spongepowered.api.entity.Entity) entity).collect(Collectors.toSet());
    }

    @Override
    public Set<AABB> getIntersectingBlockCollisionBoxes(AABB box) {
        final Vector3i max = this.blockMax.add(Vector3i.ONE);
        return this.sponge_world.getIntersectingBlockCollisionBoxes(box).stream()
                .filter(aabb -> VecHelper.inBounds(aabb.getCenter(), this.blockMin, max))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<AABB> getIntersectingCollisionBoxes(org.spongepowered.api.entity.Entity owner, AABB box) {
        final Vector3i max = this.blockMax.add(Vector3i.ONE);
        return this.sponge_world.getIntersectingCollisionBoxes(owner, box).stream()
                .filter(aabb -> VecHelper.inBounds(aabb.getCenter(), this.blockMin, max))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, java.util.function.Predicate<EntityHit> filter) {
        checkNotNull(start, "start");
        checkNotNull(end, "end");
        checkNotNull(filter, "filter");
        final Vector3d diff = end.sub(start);
        return getIntersectingEntities(start, end, diff.normalize(), diff.length(), filter);
    }

    @Override
    public Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter) {
        checkNotNull(start, "start");
        checkNotNull(direction, "direction");
        checkNotNull(filter, "filter");
        direction = direction.normalize();
        return getIntersectingEntities(start, start.add(direction.mul(distance)), direction, distance, filter);
    }

    private Set<EntityHit> getIntersectingEntities(Vector3d start, Vector3d end, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter) {
        final Vector2d entryAndExitY = getEntryAndExitY(start, end, direction, distance);
        if (entryAndExitY == null) {
            // Doesn't intersect the chunk, ignore it
            return Collections.emptySet();
        }
        final Set<EntityHit> intersections = new HashSet<>();
        getIntersectingEntities(start, direction, distance, filter, entryAndExitY.getX(), entryAndExitY.getY(), intersections);
        return intersections;
    }

    @Nullable
    private Vector2d getEntryAndExitY(Vector3d start, Vector3d end, Vector3d direction, double distance) {
        // Modified from AABB.intersects(ray)
        // Increase the bounds to the whole chunk plus a margin of two blocks
        final Vector3i min = getBlockMin().sub(2, 2, 2);
        final Vector3i max = getBlockMax().add(3, 3, 3);
        // Find the intersections on the -x and +x planes, oriented by direction
        final double txMin;
        final double txMax;
        if (Math.copySign(1, direction.getX()) > 0) {
            txMin = (min.getX() - start.getX()) / direction.getX();
            txMax = (max.getX() - start.getX()) / direction.getX();
        } else {
            txMin = (max.getX() - start.getX()) / direction.getX();
            txMax = (min.getX() - start.getX()) / direction.getX();
        }
        // Find the intersections on the -z and +z planes, oriented by direction
        final double tzMin;
        final double tzMax;
        if (Math.copySign(1, direction.getZ()) > 0) {
            tzMin = (min.getZ() - start.getZ()) / direction.getZ();
            tzMax = (max.getZ() - start.getZ()) / direction.getZ();
        } else {
            tzMin = (max.getZ() - start.getZ()) / direction.getZ();
            tzMax = (min.getZ() - start.getZ()) / direction.getZ();
        }
        // The ray should intersect the -x plane before the +z plane and intersect
        // the -z plane before the +x plane, else it is outside the column
        if (txMin > tzMax || txMax < tzMin) {
            return null;
        }
        // The ray intersects only the furthest min plane on the column and only the closest
        // max plane on the column
        final double tMin = tzMin > txMin ? tzMin : txMin;
        final double tMax = tzMax < txMax ? tzMax : txMax;
        // If both intersection points are behind the start, there are no intersections
        if (tMax < 0) {
            return null;
        }
        // If the closest intersection is before the start, use the start y instead
        final double yEntry = tMin < 0 ? start.getY() : direction.getY() * tMin + start.getY();
        // If the furthest intersection is after the end, use the end y instead
        final double yExit = tMax > distance ? end.getY() : direction.getY() * tMax + start.getY();
        //noinspection SuspiciousNameCombination
        return new Vector2d(yEntry, yExit);
    }

    @Override
    public void getIntersectingEntities(Vector3d start, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter, double entryY, double exitY, Set<EntityHit> intersections) {
        // Order the entry and exit y coordinates by magnitude
        final double yMin = Math.min(entryY, exitY);
        final double yMax = Math.max(entryY, exitY);
        // Added offset matches the one in Chunk.getEntitiesWithinAABBForEntity
        final int lowestSubChunk = GenericMath.clamp(GenericMath.floor((yMin - 2) / 16D), 0, this.entityLists.length - 1);
        final int highestSubChunk = GenericMath.clamp(GenericMath.floor((yMax + 2) / 16D), 0, this.entityLists.length - 1);
        // For each sub-chunk, perform intersections in its entity list
        for (int i = lowestSubChunk; i <= highestSubChunk; i++) {
            getIntersectingEntities(this.entityLists[i], start, direction, distance, filter, intersections);
        }
    }

    private void getIntersectingEntities(Collection<Entity> entities, Vector3d start, Vector3d direction, double distance,
            java.util.function.Predicate<EntityHit> filter, Set<EntityHit> intersections) {
        // Check each entity in the list
        for (Entity entity : entities) {
            final org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) entity;
            final Optional<AABB> box = spongeEntity.getBoundingBox();
            // Can't intersect if the entity doesn't have a bounding box
            if (!box.isPresent()) {
                continue;
            }
            // Ignore entities that didn't intersect
            final Optional<Tuple<Vector3d, Vector3d>> optionalIntersection = box.get().intersects(start, direction);
            if (!optionalIntersection.isPresent()) {
                continue;
            }
            // Check that the entity isn't too far away
            final Tuple<Vector3d, Vector3d> intersection = optionalIntersection.get();
            final double distanceSquared = intersection.getFirst().sub(start).lengthSquared();
            if (distanceSquared > distance * distance) {
                continue;
            }
            // Now test the filter on the entity and intersection
            final EntityHit hit = new EntityHit(spongeEntity, intersection.getFirst(), intersection.getSecond(), Math.sqrt(distanceSquared));
            if (!filter.test(hit)) {
                continue;
            }
            // If everything passes we have an intersection!
            intersections.add(hit);
            // If the entity has part, recurse on these
            final Entity[] parts = entity.getParts();
            if (parts != null && parts.length > 0) {
                getIntersectingEntities(Arrays.asList(parts), start, direction, distance, filter, intersections);
            }
        }
    }

    // Fast neighbor methods for internal use
    @Override
    public void setNeighborChunk(int index, @Nullable net.minecraft.world.chunk.Chunk chunk) {
        this.neighbors[index] = chunk;
    }

    @Nullable
    @Override
    public net.minecraft.world.chunk.Chunk getNeighborChunk(int index) {
        return this.neighbors[index];
    }

    @Override
    public List<net.minecraft.world.chunk.Chunk> getNeighbors() {
        List<net.minecraft.world.chunk.Chunk> neighborList = new ArrayList<>();
        for (net.minecraft.world.chunk.Chunk neighbor : this.neighbors) {
            if (neighbor != null) {
                neighborList.add(neighbor);
            }
        }
        return neighborList;
    }

    @Override
    public boolean areNeighborsLoaded() {
        for (int i = 0; i < 4; i++) {
            if (this.neighbors[i] == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setNeighbor(Direction direction, @Nullable Chunk neighbor) {
        this.neighbors[directionToIndex(direction)] = (net.minecraft.world.chunk.Chunk) neighbor;
    }

    @Override
    public Optional<Chunk> getNeighbor(Direction direction, boolean shouldLoad) {
        checkNotNull(direction, "direction");
        checkArgument(!direction.isSecondaryOrdinal(), "Secondary cardinal directions can't be used here");

        if (direction.isUpright() || direction == Direction.NONE) {
            return Optional.of(this);
        }

        int index = directionToIndex(direction);
        Direction secondary = getSecondaryDirection(direction);
        Chunk neighbor = null;
        neighbor = (Chunk) this.neighbors[index];

        if (neighbor == null && shouldLoad) {
            Vector3i neighborPosition = this.getPosition().add(getCardinalDirection(direction).asBlockOffset());
            Optional<Chunk> cardinal = this.getWorld().loadChunk(neighborPosition, true);
            if (cardinal.isPresent()) {
                neighbor = cardinal.get();
            }
        }

        if (neighbor != null) {
            if (secondary != Direction.NONE) {
                return neighbor.getNeighbor(secondary, shouldLoad);
            } else {
                return Optional.of(neighbor);
            }
        }

        return Optional.empty();
    }

    private static int directionToIndex(Direction direction) {
        switch (direction) {
            case NORTH:
            case NORTHEAST:
            case NORTHWEST:
                return 0;
            case SOUTH:
            case SOUTHEAST:
            case SOUTHWEST:
                return 1;
            case EAST:
                return 2;
            case WEST:
                return 3;
            default:
                throw new IllegalArgumentException("Unexpected direction");
        }
    }

    private static Direction getCardinalDirection(Direction direction) {
        switch (direction) {
            case NORTH:
            case NORTHEAST:
            case NORTHWEST:
                return Direction.NORTH;
            case SOUTH:
            case SOUTHEAST:
            case SOUTHWEST:
                return Direction.SOUTH;
            case EAST:
                return Direction.EAST;
            case WEST:
                return Direction.WEST;
            default:
                throw new IllegalArgumentException("Unexpected direction");
        }
    }

    private static Direction getSecondaryDirection(Direction direction) {
        switch (direction) {
            case NORTHEAST:
            case SOUTHEAST:
                return Direction.EAST;
            case NORTHWEST:
            case SOUTHWEST:
                return Direction.WEST;
            default:
                return Direction.NONE;
        }
    }

    @Override
    public long getScheduledForUnload() {
        return this.scheduledForUnload;
    }

    @Override
    public void setScheduledForUnload(long scheduled) {
        this.scheduledForUnload = scheduled;
    }

    @Inject(method = "generateSkylightMap", at = @At("HEAD"), cancellable = true)
    public void onGenerateSkylightMap(CallbackInfo ci) {
        if (!WorldGenConstants.lightingEnabled) {
            ci.cancel();
        }
    }

    @Override
    public void fill(ChunkPrimer primer) {
        boolean flag = this.world.provider.hasSkyLight();
        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                for (int y = 0; y < 256; ++y) {
                    IBlockState block = primer.getBlockState(x, y, z);
                    if (block.getMaterial() != Material.AIR) {
                        int section = y >> 4;
                        if (this.storageArrays[section] == net.minecraft.world.chunk.Chunk.NULL_BLOCK_STORAGE) {
                            this.storageArrays[section] = new ExtendedBlockStorage(section << 4, flag);
                        }
                        this.storageArrays[section].set(x, y & 15, z, block);
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("World", this.world)
                .add("Position", this.x + this.z)
                .toString();
    }
}
