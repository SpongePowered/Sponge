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
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplFactory;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.extent.ExtentViewDownsize;
import org.spongepowered.common.world.extent.ExtentViewTransform;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@NonnullByDefault
@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class MixinChunk implements Chunk, IMixinChunk {

    public Map<Integer, PlayerTracker> trackedIntBlockPositions = Maps.newHashMap();
    public Map<Short, PlayerTracker> trackedShortBlockPositions = Maps.newHashMap();
    private Cause populateCause;

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

    @Shadow private World worldObj;
    @Shadow public int xPosition;
    @Shadow public int zPosition;
    @Shadow private long inhabitedTime;
    @Shadow private boolean isChunkLoaded;
    @Shadow private boolean isTerrainPopulated;
    @Shadow private boolean isModified;
    @Shadow private ExtendedBlockStorage[] storageArrays;
    @Shadow private int[] precipitationHeightMap;
    @Shadow private int[] heightMap;
    @Shadow private ClassInheritanceMultiMap[] entityLists;
    @Shadow private Map<BlockPos, TileEntity> chunkTileEntityMap;

    @Shadow public abstract TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType p_177424_2_);
    @Shadow public abstract void generateSkylightMap();
    @Shadow protected abstract void relightBlock(int x, int y, int z);
    @Shadow public abstract int getLightFor(EnumSkyBlock p_177413_1_, BlockPos pos);
    @Shadow protected abstract void propagateSkylightOcclusion(int x, int z);
    @Shadow public abstract IBlockState getBlockState(BlockPos pos);
    @Shadow public abstract BiomeGenBase getBiome(BlockPos pos, WorldChunkManager chunkManager);
    @Shadow public abstract byte[] getBiomeArray();
    @Shadow public abstract void setBiomeArray(byte[] biomeArray);
    @Shadow(prefix = "shadow$")
    public abstract Block shadow$getBlock(int x, int y, int z);

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"), remap = false)
    public void onConstructed(World world, int x, int z, CallbackInfo ci) {
        this.chunkPos = new Vector3i(x, 0, z);
        this.blockMin = SpongeChunkLayout.instance.toWorld(this.chunkPos).get();
        this.blockMax = this.blockMin.add(SpongeChunkLayout.CHUNK_SIZE).sub(1, 1, 1);
        this.biomeMin = this.blockMin.toVector2(true);
        this.biomeMax = this.blockMax.toVector2(true);
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
        return (org.spongepowered.api.world.World) this.worldObj;
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
        SpongeHooks.setBlockState((net.minecraft.world.chunk.Chunk) (Object) this, x, y, z, block);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockType) shadow$getBlock(x, y, z);
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
        if (event == null) {
            return;
        } else if (event.isCancelled()) {
            listToFill.clear();
        } else {
            listToFill = (List<net.minecraft.entity.Entity>)(List<?>) event.getEntities();
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
        if (event == null) {
            return;
        } else if (event.isCancelled()) {
            listToFill.clear();
        } else {
            listToFill = (List<net.minecraft.entity.Entity>)(List<?>) event.getEntities();
        }
    }

    @Override
    public Cause getCurrentPopulateCause() {
        return this.populateCause;
    }

    @Redirect(method = "populateChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunkProvider;populate(Lnet/minecraft/world/chunk/IChunkProvider;II)V") )
    public void onPopulateChunkPre(IChunkProvider chunkProviderServer, IChunkProvider chunkProvider, int x, int z) {
        if (this.worldObj.isRemote) {
            chunkProviderServer.populate(chunkProvider, x, z);
            return;
        }

        IMixinWorld world = (IMixinWorld) this.worldObj;
        world.setProcessingCaptureCause(true);
        world.setCapturingTerrainGen(true);
        this.populateCause = Cause.of(NamedCause.source(this), NamedCause.of("ChunkProvider", chunkProvider));
        chunkProviderServer.populate(chunkProvider, x, z);
        world.handlePostTickCaptures(this.populateCause);
        world.setCapturingTerrainGen(false);
        world.setProcessingCaptureCause(false);
        this.populateCause = null;
    }

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
    public IBlockState setBlockState(BlockPos pos, IBlockState newState, IBlockState currentState, BlockSnapshot newBlockSnapshot) {
        int i = pos.getX() & 15;
        int j = pos.getY();
        int k = pos.getZ() & 15;
        int l = k << 4 | i;

        if (j >= this.precipitationHeightMap[l] - 1) {
            this.precipitationHeightMap[l] = -999;
        }

        int i1 = this.heightMap[l];

        // Sponge - remove blockstate check as we handle it in world.setBlockState
        Block block = newState.getBlock();
        Block block1 = currentState.getBlock();
        ExtendedBlockStorage extendedblockstorage = this.storageArrays[j >> 4];
        boolean flag = false;

        if (extendedblockstorage == null) {
            if (block == Blocks.air) {
                return null;
            }

            extendedblockstorage = this.storageArrays[j >> 4] = new ExtendedBlockStorage(j >> 4 << 4, !this.worldObj.provider.getHasNoSky());
            flag = j >= i1;
        }

        int j1 = SpongeImplFactory.getBlockLightOpacity(block, this.worldObj, pos);

        extendedblockstorage.set(i, j & 15, k, newState);

        // if (block1 != block)
        {
            if (!this.worldObj.isRemote) {
                // Only fire block breaks when the block changes.
                if (currentState.getBlock() != newState.getBlock()) {
                    block1.breakBlock(this.worldObj, pos, currentState);
                }
                TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (te != null && SpongeImplFactory.shouldRefresh(te, this.worldObj, pos, currentState, newState)) {
                    this.worldObj.removeTileEntity(pos);
                }
            } else if (SpongeImplFactory.blockHasTileEntity(block1, currentState)) {
                TileEntity te = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (te != null && SpongeImplFactory.shouldRefresh(te, this.worldObj, pos, currentState, newState)) {
                    this.worldObj.removeTileEntity(pos);
                }
            }
        }

        if (extendedblockstorage.getBlockByExtId(i, j & 15, k) != block) {
            return null;
        } else {
            if (flag) {
                this.generateSkylightMap();
            } else {
                int k1 = SpongeImplFactory.getBlockLightOpacity(block, this.worldObj, pos);

                if (j1 > 0) {
                    if (j >= i1) {
                        this.relightBlock(i, j + 1, k);
                    }
                } else if (j == i1 - 1) {
                    this.relightBlock(i, j, k);
                }

                if (j1 != k1 && (j1 < k1 || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                    this.propagateSkylightOcclusion(i, k);
                }
            }

            TileEntity tileentity;

            if (!this.worldObj.isRemote && block1 != block) {
                // Sponge start - Ignore block activations during block placement captures unless it's
                // a BlockContainer. Prevents blocks such as TNT from activating when
                // cancelled.
                if (!((IMixinWorld)this.worldObj).capturingBlocks() || SpongeImplFactory.blockHasTileEntity(block, newState)) {
                    if (newBlockSnapshot == null) {
                        block.onBlockAdded(this.worldObj, pos, newState);
                    }
                }
                // Sponge end
            }

            if (SpongeImplFactory.blockHasTileEntity(block, newState)) {
                tileentity = this.getTileEntity(pos, EnumCreateEntityType.CHECK);

                if (tileentity == null) {
                    tileentity = SpongeImplFactory.createTileEntity(block, this.worldObj, newState);
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

    @Override
    public void addTrackedBlockPosition(Block block, BlockPos pos, User user, PlayerTracker.Type trackerType) {
        if (this.worldObj.isRemote || !SpongeHooks.getActiveConfig(this.worldObj).getConfig().getBlockTracking().isEnabled()) {
            return;
        } else {
            IMixinWorld spongeWorld = (IMixinWorld) this.worldObj;
            if (spongeWorld.capturingTerrainGen()) {
                // Don't track chunk gen
                return;
            }
        }

        if (!SpongeHooks.getActiveConfig(this.worldObj).getConfig().getBlockTracking().getBlockBlacklist().contains(((BlockType)block).getId())) {
            SpongeHooks.logBlockTrack(this.worldObj, block, pos, user, true);
        } else {
            SpongeHooks.logBlockTrack(this.worldObj, block, pos, user, false);
        }

        if (pos.getY() <= 255) {
            short blockPos = blockPosToShort(pos);
            if (this.trackedShortBlockPositions.get(blockPos) != null) {
                if (trackerType == PlayerTracker.Type.OWNER) {
                    this.trackedShortBlockPositions.get(blockPos).ownerIndex = ((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(user.getUniqueId());
                    this.trackedShortBlockPositions.get(blockPos).notifierIndex = ((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(user.getUniqueId());
                } else {
                    this.trackedShortBlockPositions.get(blockPos).notifierIndex = ((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(user.getUniqueId());
                }
            } else {
                this.trackedShortBlockPositions.put(blockPos, new PlayerTracker(((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(user.getUniqueId()), trackerType));
            }
        } else {
            int blockPos = blockPosToInt(pos);
            if (this.trackedIntBlockPositions.get(blockPos) != null) {
                if (trackerType == PlayerTracker.Type.OWNER) {
                    this.trackedIntBlockPositions.get(blockPos).ownerIndex = ((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(user.getUniqueId());
                } else {
                    this.trackedIntBlockPositions.get(blockPos).notifierIndex = ((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(user.getUniqueId());
                }
            } else {
                this.trackedIntBlockPositions.put(blockPos, new PlayerTracker(((IMixinWorldInfo) this.worldObj.getWorldInfo()).getIndexForUniqueId(user.getUniqueId()), trackerType));
            }
        }
    }

    @Override
    public Map<Integer, PlayerTracker> getTrackedIntPlayerPositions() {
        return this.trackedIntBlockPositions;
    }

    @Override
    public Map<Short, PlayerTracker> getTrackedShortPlayerPositions() {
        return this.trackedShortBlockPositions;
    }

    @Override
    public Optional<User> getBlockOwner(BlockPos pos) {
        if (this.trackedIntBlockPositions.get(blockPosToInt(pos)) != null) {
            PlayerTracker tracker = this.trackedIntBlockPositions.get(blockPosToInt(pos));
            Optional<UUID> uuid = (((IMixinWorldInfo) this.worldObj.getWorldInfo()).getUniqueIdForIndex(tracker.ownerIndex));
            if (uuid.isPresent()) {
                // get player if online
                EntityPlayer player = this.worldObj.getPlayerEntityByUUID(uuid.get());
                if (player != null) {
                    return Optional.of((User) player);
                }
                // player is not online, get user from storage if one exists
                return SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get().get(uuid.get());
            }
        } else if (this.trackedShortBlockPositions.get(blockPosToShort(pos)) != null) {
            PlayerTracker tracker = this.trackedShortBlockPositions.get(blockPosToShort(pos));
            Optional<UUID> uuid = (((IMixinWorldInfo) this.worldObj.getWorldInfo()).getUniqueIdForIndex(tracker.ownerIndex));
            if (uuid.isPresent()) {
                // get player if online
                EntityPlayer player = this.worldObj.getPlayerEntityByUUID(uuid.get());
                if (player != null) {
                    return Optional.of((User) player);
                }
                // player is not online, get user from storage if one exists
                return SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get().get(uuid.get());
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> getBlockNotifier(BlockPos pos) {
        if (this.trackedIntBlockPositions.get(blockPosToInt(pos)) != null) {
            PlayerTracker tracker = this.trackedIntBlockPositions.get(blockPosToInt(pos));
            Optional<UUID> uuid = (((IMixinWorldInfo) this.worldObj.getWorldInfo()).getUniqueIdForIndex(tracker.notifierIndex));
            if (uuid.isPresent()) {
                // get player if online
                EntityPlayer player = this.worldObj.getPlayerEntityByUUID(uuid.get());
                if (player != null) {
                    return Optional.of((User) player);
                }
                // player is not online, get user from storage if one exists
                return SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get().get(uuid.get());
            }
        } else if (this.trackedShortBlockPositions.get(blockPosToShort(pos)) != null) {
            PlayerTracker tracker = this.trackedShortBlockPositions.get(blockPosToShort(pos));
            Optional<UUID> uuid = (((IMixinWorldInfo) this.worldObj.getWorldInfo()).getUniqueIdForIndex(tracker.notifierIndex));
            if (uuid.isPresent()) {
                // get player if online
                EntityPlayer player = this.worldObj.getPlayerEntityByUUID(uuid.get());
                if (player != null) {
                    return Optional.of((User) player);
                }
                // player is not online, get user from storage if one exists
                return SpongeImpl.getGame().getServiceManager().provide(UserStorageService.class).get().get(uuid.get());
            }
        }

        return Optional.empty();
    }

    @Override
    public void setTrackedIntPlayerPositions(Map<Integer, PlayerTracker> trackedPositions) {
        this.trackedIntBlockPositions = trackedPositions;
    }

    @Override
    public void setTrackedShortPlayerPositions(Map<Short, PlayerTracker> trackedPositions) {
        this.trackedShortBlockPositions = trackedPositions;
    }

    /**
    * Modifies bits in an integer.
    *
    * @param num Integer to modify
    * @param data Bits of data to add
    * @param which Index of nibble to start at
    * @param bitsToReplace The number of bits to replace starting from nibble index
    * @return The modified integer
    */
    public int setNibble(int num, int data, int which, int bitsToReplace) {
        return (num & ~(bitsToReplace << (which * 4)) | (data << (which * 4)));
    }

    /**
     * Serialize this BlockPos into a short value
     */
    public short blockPosToShort(BlockPos pos) {
        short serialized = (short) setNibble(0, pos.getX() & XZ_MASK, 0, NUM_XZ_BITS);
        serialized = (short) setNibble(serialized, pos.getY() & Y_SHORT_MASK, 1, NUM_SHORT_Y_BITS);
        serialized = (short) setNibble(serialized, pos.getZ() & XZ_MASK, 3, NUM_XZ_BITS);
        return serialized;
    }

    /**
     * Create a BlockPos from a serialized chunk position
     */
    public BlockPos blockPosFromShort(short serialized) {
        int x = this.xPosition * 16 + (serialized & XZ_MASK);
        int y = (serialized >> Y_SHIFT) & Y_SHORT_MASK;
        int z = this.zPosition * 16 + ((serialized >> Z_SHORT_SHIFT) & XZ_MASK);
        return new BlockPos(x, y, z);
    }

    /**
     * Serialize this BlockPos into an int value
     */
    public int blockPosToInt(BlockPos pos) {
        int serialized = setNibble(0, pos.getX() & XZ_MASK, 0, NUM_XZ_BITS);
        serialized = setNibble(serialized, pos.getY() & Y_INT_MASK, 1, NUM_INT_Y_BITS);
        serialized = setNibble(serialized, pos.getZ() & XZ_MASK, 7, NUM_XZ_BITS);
        return serialized;
    }

    /**
     * Create a BlockPos from a serialized chunk position
     */
    public BlockPos blockPosFromInt(int serialized) {
        int x = this.xPosition * 16 + (serialized & XZ_MASK);
        int y = (serialized >> Y_SHIFT) & Y_INT_MASK;
        int z = this.zPosition * 16 + ((serialized >> Z_INT_SHIFT) & XZ_MASK);
        return new BlockPos(x, y, z);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<org.spongepowered.api.entity.Entity> getEntities() {
        Set<org.spongepowered.api.entity.Entity> entities = Sets.newHashSet();
        for (ClassInheritanceMultiMap entityList : this.entityLists) {
            entities.addAll(entityList);
        }
        return entities;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Collection<org.spongepowered.api.block.tileentity.TileEntity> getTileEntities() {
        return (Collection) this.chunkTileEntityMap.values();
    }
}
