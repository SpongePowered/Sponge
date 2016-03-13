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

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.api.world.extent.worker.MutableBiomeAreaWorker;
import org.spongepowered.api.world.extent.worker.MutableBlockVolumeWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.extent.ExtentViewDownsize;
import org.spongepowered.common.world.extent.ExtentViewTransform;
import org.spongepowered.common.world.extent.worker.SpongeMutableBiomeAreaWorker;
import org.spongepowered.common.world.extent.worker.SpongeMutableBlockVolumeWorker;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class MixinChunk implements Chunk, IMixinChunk {

    private org.spongepowered.api.world.World world;
    private UUID uuid;

    private static final int NUM_XZ_BITS = 4;
    private static final int NUM_SHORT_Y_BITS = 8;
    private static final int NUM_INT_Y_BITS = 24;
    private static final int Y_SHIFT = NUM_XZ_BITS;
    private static final int Z_SHORT_SHIFT = Y_SHIFT + NUM_SHORT_Y_BITS;
    private static final int Z_INT_SHIFT = Y_SHIFT + NUM_INT_Y_BITS;
    private static final short XZ_MASK = 0xF;
    private static final short Y_SHORT_MASK = 0xFF;
    private static final int Y_INT_MASK = 0xFFFFFF;

    private static final Vector2i BIOME_SIZE = SpongeChunkLayout.CHUNK_SIZE.toVector2(true);
    private Vector3i chunkPos;
    private Vector3i blockMin;
    private Vector3i blockMax;
    private Vector2i biomeMin;
    private Vector2i biomeMax;

    @Shadow @Final private World worldObj;
    @Shadow @Final public int xPosition;
    @Shadow @Final public int zPosition;
    @Shadow @Final private ExtendedBlockStorage[] storageArrays;
    @Shadow @Final private int[] precipitationHeightMap;
    @Shadow @Final private int[] heightMap;
    @Shadow @Final private ClassInheritanceMultiMap<Entity>[] entityLists;
    @Shadow @Final private Map<BlockPos, TileEntity> chunkTileEntityMap;
    @Shadow private long inhabitedTime;
    @Shadow private boolean isChunkLoaded;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean isModified;

    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType p_177424_2_);
    @Shadow public abstract void generateSkylightMap();
    @Shadow protected abstract void relightBlock(int x, int y, int z);
    @Shadow public abstract int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos);
    @Shadow protected abstract void propagateSkylightOcclusion(int x, int z);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract BiomeGenBase getBiome(BlockPos pos, WorldChunkManager chunkManager);
    @Shadow public abstract byte[] getBiomeArray();
    @Shadow public abstract void setBiomeArray(byte[] biomeArray);
    @Shadow public abstract Block shadow$getBlock(int x, int y, int z);

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"), remap = false)
    public void onConstructed(World world, int x, int z, CallbackInfo ci) {
        this.chunkPos = new Vector3i(x, 0, z);
        this.blockMin = SpongeChunkLayout.instance.toWorld(this.chunkPos).get();
        this.blockMax = this.blockMin.add(SpongeChunkLayout.CHUNK_SIZE).sub(1, 1, 1);
        this.biomeMin = this.blockMin.toVector2(true);
        this.biomeMax = this.blockMax.toVector2(true);
        this.world = (org.spongepowered.api.world.World) world;
        if (this.world.getUniqueId() != null) { // Client worlds have no UUID
            this.uuid = new UUID(this.world.getUniqueId().getMostSignificantBits() ^ (x * 2 + 1),
                    this.world.getUniqueId().getLeastSignificantBits() ^ (z * 2 + 1));
        }
    }

    @Inject(method = "onChunkLoad()V", at = @At("RETURN"))
    public void onChunkLoadInject(CallbackInfo ci) {
        if (!this.worldObj.isRemote) {
            SpongeHooks.logChunkLoad(this.worldObj, this.chunkPos);
        }
    }

    @Inject(method = "onChunkUnload()V", at = @At("RETURN"))
    public void onChunkUnloadInject(CallbackInfo ci) {
        if (!this.worldObj.isRemote) {
            SpongeHooks.logChunkUnload(this.worldObj, this.chunkPos);
        }
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
        return this.isChunkLoaded;
    }

    @Override
    public boolean isPopulated() {
        return this.isTerrainPopulated;
    }

    @Override
    public boolean loadChunk(boolean generate) {
        WorldServer worldserver = (WorldServer) this.worldObj;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(this.xPosition, this.zPosition) || generate) {
            chunk = worldserver.theChunkProviderServer.loadChunk(this.xPosition, this.zPosition);
        }

        return chunk != null;
    }

    @Override
    public int getInhabittedTime() {
        return (int) this.inhabitedTime;
    }

    @Override
    public double getRegionalDifficultyFactor() {
        final boolean flag = this.worldObj.getDifficulty() == EnumDifficulty.HARD;
        float moon = this.worldObj.getCurrentMoonPhaseFactor();
        float f2 = MathHelper.clamp_float(((float)this.worldObj.getWorldTime() + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
        float f3 = 0.0F;
        f3 += MathHelper.clamp_float((float)this.inhabitedTime / 3600000.0F, 0.0F, 1.0F) * (flag ? 1.0F : 0.75F);
        f3 += MathHelper.clamp_float(moon * 0.25F, 0.0F, f2);
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
            return (region - 2.0)/ 2.0;
        }
    }

    @Override
    public org.spongepowered.api.world.World getWorld() {
        return this.world;
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkBiomeBounds(x, z);
        return (BiomeType) getBiome(new BlockPos(x, 0, z), this.worldObj.getWorldChunkManager());
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkBiomeBounds(x, z);
        // Taken from Chunk#getBiome
        byte[] biomeArray = getBiomeArray();
        int i = x & 15;
        int j = z & 15;
        biomeArray[j << 4 | i] = (byte) (((BiomeGenBase) biome).biomeID & 255);
        setBiomeArray(biomeArray);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        checkBlockBounds(x, y, z);
        SpongeHooks.setBlockState((net.minecraft.world.chunk.Chunk) (Object) this, x, y, z, block, false);
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block, boolean notifyNeighbors) {
        SpongeHooks.setBlockState((net.minecraft.world.chunk.Chunk) (Object) this, this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15),
                block, notifyNeighbors);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockType) shadow$getBlock(x, y, z);
    }

    @Override
    public BlockSnapshot createSnapshot(int x, int y, int z) {
        return this.world.createSnapshot(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15));
    }

    @Override
    public boolean restoreSnapshot(BlockSnapshot snapshot, boolean force, boolean notifyNeighbors) {
        return this.world.restoreSnapshot(snapshot, force, notifyNeighbors);
    }

    @Override
    public boolean restoreSnapshot(int x, int y, int z, BlockSnapshot snapshot, boolean force, boolean notifyNeighbors) {
        return this.world.restoreSnapshot(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), snapshot, force, notifyNeighbors);
    }

    @Override
    public Vector2i getBiomeMin() {
        return this.biomeMin;
    }

    @Override
    public Vector2i getBiomeMax() {
        return this.biomeMax;
    }

    @Override
    public Vector2i getBiomeSize() {
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
    public boolean containsBiome(int x, int z) {
        return VecHelper.inBounds(x, z, this.biomeMin, this.biomeMax);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.blockMin, this.blockMax);
    }

    private void checkBiomeBounds(int x, int z) {
        if (!containsBiome(x, z)) {
            throw new PositionOutOfBoundsException(new Vector2i(x, z), this.biomeMin, this.biomeMax);
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
    public Extent getExtentView(DiscreteTransform3 transform) {
        return new ExtentViewTransform(this, transform);
    }

    @Override
    public MutableBiomeAreaWorker<? extends Chunk> getBiomeWorker() {
        return new SpongeMutableBiomeAreaWorker<>(this);
    }

    @Override
    public MutableBlockVolumeWorker<? extends Chunk> getBlockWorker() {
        return new SpongeMutableBlockVolumeWorker<>(this);
    }

    @SuppressWarnings({"unchecked"})
    @Inject(method = "getEntitiesWithinAABBForEntity", at = @At(value = "RETURN"))
    public void onGetEntitiesWithinAABBForEntity(Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<Entity> p_177414_4_, CallbackInfo ci) {
        if (this.worldObj.isRemote) {
            return;
        }

        if (listToFill.size() == 0) {
            return;
        }

        CollideEntityEvent event = SpongeCommonEventFactory.callCollideEntityEvent(this.worldObj, entityIn, listToFill);
        if (event == null || event.isCancelled()) {
            listToFill.clear();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "getEntitiesOfTypeWithinAAAB", at = @At(value = "RETURN"))
    public void onGetEntitiesOfTypeWithinAAAB(Class<? extends Entity> entityClass, AxisAlignedBB aabb, List listToFill, Predicate<Entity> p_177430_4_, CallbackInfo ci) {
        if (this.worldObj.isRemote) {
            return;
        }

        if (listToFill.size() == 0) {
            return;
        }

        CollideEntityEvent event = SpongeCommonEventFactory.callCollideEntityEvent(this.worldObj, null, listToFill);
        if (event == null || event.isCancelled()) {
            listToFill.clear();
        }
    }


    @Nullable
    @Overwrite
    public IBlockState setBlockState(BlockPos pos, IBlockState state) {
        IBlockState iblockstate1 = this.getBlockState(pos);

        if (iblockstate1 == state) {
            return null;
        }
        // Sponge - reroute to new method that accepts snapshot to prevent a second snapshot from being created.
        return setBlockState(pos, state, iblockstate1, null);
    }

    @Override
    @Nullable
    public IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, @Nullable BlockSnapshot newBlockSnapshot) {
        int xPos = pos.getX() & 15;
        int yPos = pos.getY();
        int zPos = pos.getZ() & 15;
        int combinedPos = zPos << 4 | xPos;

        if (yPos >= this.precipitationHeightMap[combinedPos] - 1) {
            this.precipitationHeightMap[combinedPos] = -999;
        }

        int currentHeight = this.heightMap[combinedPos];

        // Sponge - remove blockstate check as we handle it in world.setBlockState
        Block newBlock = newState.getBlock();
        Block currentBlock = currentState.getBlock();
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[yPos >> 4];
        final boolean requiresNewLightCalculations;

        if (extendedblockstorage == null) {
            if (newBlock == Blocks.air) {
                return null;
            }

            extendedblockstorage = this.storageArrays[yPos >> 4] = new ExtendedBlockStorage(yPos >> 4 << 4, !this.worldObj.provider.getHasNoSky());
            requiresNewLightCalculations = yPos >= currentHeight;
        } else {
            requiresNewLightCalculations = false;
        }

        int newBlockLightOpacity = SpongeImplHooks.getBlockLightOpacity(newBlock, this.worldObj, pos);

        extendedblockstorage.set(xPos, yPos & 15, zPos, newState);

        // if (block1 != block)
        {
            if (!this.worldObj.isRemote) {
                // Only fire block breaks when the block changes.
                if (currentState.getBlock() != newState.getBlock()) {
                    currentBlock.breakBlock(this.worldObj, pos, currentState);
                }
                TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (te != null && SpongeImplHooks.shouldRefresh(te, this.worldObj, pos, currentState, newState)) {
                    this.worldObj.removeTileEntity(pos);
                }
            } else if (SpongeImplHooks.blockHasTileEntity(currentBlock, currentState)) {
                TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (te != null && SpongeImplHooks.shouldRefresh(te, this.worldObj, pos, currentState, newState)) {
                    this.worldObj.removeTileEntity(pos);
                }
            }
        }

        if (extendedblockstorage.getBlockByExtId(xPos, yPos & 15, zPos) != newBlock) {
            return null;
        } else {
            if (requiresNewLightCalculations) {
                this.generateSkylightMap();
            } else {
                int postNewBlockLightOpacity = SpongeImplHooks.getBlockLightOpacity(newBlock, this.worldObj, pos);

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

            @Nullable TileEntity tileentity;

            if (!this.worldObj.isRemote && currentBlock != newBlock) {
                // Sponge start - Ignore block activations during block placement captures unless it's
                // a BlockContainer. Prevents blocks such as TNT from activating when
                // cancelled.
                final CauseTracker causeTracker = ((IMixinWorldServer) this.worldObj).getCauseTracker();
                final PhaseData peek = causeTracker.getPhases().peek();
                final boolean requiresCapturing = peek.getState().getPhase().requiresBlockCapturing(peek.getState());
                if (!requiresCapturing || SpongeImplHooks.blockHasTileEntity(newBlock, newState)) {
                    if (newBlockSnapshot == null) {
                        newBlock.onBlockAdded(this.worldObj, pos, newState);
                    }
                }
                // Sponge end
            }

            if (SpongeImplHooks.blockHasTileEntity(newBlock, newState)) {
                tileentity = this.getTileEntity(pos, EnumCreateEntityType.CHECK);

                if (tileentity == null) {
                    tileentity = SpongeImplHooks.createTileEntity(newBlock, this.worldObj, newState);
                    this.worldObj.setTileEntity(pos, tileentity);
                }

                if (tileentity != null) {
                    tileentity.updateContainingBlockInfo();
                }
            }

            this.isModified = true;
            return currentState;
        }
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
    public Optional<org.spongepowered.api.entity.Entity> createEntity(EntityType type, Vector3d position) {
        return this.world.createEntity(type, this.chunkPos.mul(16).toDouble().add(position.min(15, this.blockMax.getY(), 15)));
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(DataContainer entityContainer) {
        return this.world.createEntity(entityContainer);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> createEntity(DataContainer entityContainer, Vector3d position) {
        return this.world.createEntity(entityContainer, this.chunkPos.mul(16).toDouble().add(position.min(15, this.blockMax.getY(), 15)));
    }

    @Override
    public boolean spawnEntity(org.spongepowered.api.entity.Entity entity, Cause cause) {
        return this.world.spawnEntity(entity, cause);
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
        for (ClassInheritanceMultiMap entityClassMap : this.entityLists) {
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
        return Sets.newHashSet((Collection) this.chunkTileEntityMap.values());
    }

    @Override
    public Collection<org.spongepowered.api.block.tileentity.TileEntity>
            getTileEntities(java.util.function.Predicate<org.spongepowered.api.block.tileentity.TileEntity> filter) {
        Set<org.spongepowered.api.block.tileentity.TileEntity> tiles = Sets.newHashSet();
        for (Entry<BlockPos, TileEntity> entry : this.chunkTileEntityMap.entrySet()) {
            if (filter.test((org.spongepowered.api.block.tileentity.TileEntity) entry.getValue())) {
                tiles.add((org.spongepowered.api.block.tileentity.TileEntity) entry.getValue());
            }
        }
        return tiles;
    }

    @Override
    public Optional<org.spongepowered.api.block.tileentity.TileEntity> getTileEntity(int x, int y, int z) {
        return Optional.ofNullable((org.spongepowered.api.block.tileentity.TileEntity) this.getTileEntity(
                new BlockPos(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15)), EnumCreateEntityType.CHECK));
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> restoreSnapshot(EntitySnapshot snapshot, Vector3d position) {
        return this.world.restoreSnapshot(snapshot, position);
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        return this.world.getScheduledUpdates(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15));
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        return this.world.addScheduledUpdate(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), priority, ticks);
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        this.world.removeScheduledUpdate(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), update);
    }

    @Override
    public boolean hitBlock(int x, int y, int z, Direction side, Cause cause) {
        return this.world.hitBlock(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), side, cause);
    }

    @Override
    public boolean interactBlock(int x, int y, int z, Direction side, Cause cause) {
        return this.world.interactBlock(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), side, cause);
    }

    @Override
    public boolean placeBlock(int x, int y, int z, BlockState block, Direction side, Cause cause) {
        return this.world.placeBlock(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), block, side, cause);
    }

    @Override
    public boolean interactBlockWith(int x, int y, int z, ItemStack itemStack, Direction side, Cause cause) {
        return this.world.interactBlockWith(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), itemStack, side, cause);
    }

    @Override
    public boolean digBlock(int x, int y, int z, Cause cause) {
        return this.world.digBlock(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), cause);
    }

    @Override
    public boolean digBlockWith(int x, int y, int z, ItemStack itemStack, Cause cause) {
        return this.world.digBlockWith(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), itemStack, cause);
    }

    @Override
    public int getBlockDigTimeWith(int x, int y, int z, ItemStack itemStack, Cause cause) {
        return this.world.getBlockDigTimeWith(this.xPosition << 4 + (x & 15), y, this.zPosition << 4 + (z & 15), itemStack, cause);
    }

}
