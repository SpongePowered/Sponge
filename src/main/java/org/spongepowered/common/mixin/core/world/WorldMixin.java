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

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.data.VanishingBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldProviderBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.mixin.tileentityactivation.WorldServerMixin_TileEntityActivation;
import org.spongepowered.common.util.SpongeHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin implements WorldBridge {

    private boolean impl$isDefinitelyFake = false;
    private boolean impl$hasChecked = false;

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
    @Shadow @Final public List<net.minecraft.tileentity.TileEntity> addedTileEntityList;
    @Shadow protected int[] lightUpdateBlockList; // Elevated to protected for subclass mixins to access when they're separated by package
    @Shadow public boolean processingLoadedTiles;
    @Shadow protected boolean scheduledUpdatesAreImmediate;
    @Shadow protected WorldInfo worldInfo;
    @Shadow protected IChunkProvider chunkProvider;
    @Shadow @Final public net.minecraft.world.border.WorldBorder worldBorder;
    @Shadow protected int updateLCG;

    @Shadow protected abstract void tickPlayers();
    @Shadow public net.minecraft.world.World init() {
        // Should never be overwritten because this is @Shadow'ed
        throw new RuntimeException("Bad things have happened");
    }

    // To be overridden in MixinWorldServer_Lighting
    @Shadow public abstract int getLight(BlockPos pos);
    @Shadow public abstract int getLight(BlockPos pos, boolean checkNeighbors);
    @Shadow protected abstract int getRawLight(BlockPos pos, EnumSkyBlock lightType);
    @Shadow public abstract int getSkylightSubtracted();
    @Shadow @Nullable public abstract net.minecraft.world.chunk.Chunk getChunk(BlockPos pos);
    @Shadow public abstract WorldInfo getWorldInfo();
    @Shadow public abstract boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos);
    @Shadow public abstract boolean addTileEntity(net.minecraft.tileentity.TileEntity tile);
    @Shadow protected abstract void onEntityAdded(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty);
    @Shadow public abstract boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty);
    @Shadow protected abstract void onEntityRemoved(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void updateEntity(net.minecraft.entity.Entity ent);
    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);
    @Shadow public void markChunkDirty(final BlockPos pos, final net.minecraft.tileentity.TileEntity unusedTileEntity){};
    @Shadow public abstract boolean addWeatherEffect(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract Biome getBiome(BlockPos pos);
    @Shadow public abstract net.minecraft.world.chunk.Chunk getChunk(int chunkX, int chunkZ);
    @Shadow public abstract List<net.minecraft.entity.Entity> getEntities(Class<net.minecraft.entity.Entity> entityType,
            com.google.common.base.Predicate<net.minecraft.entity.Entity> filter);
    @Shadow public abstract <T extends net.minecraft.entity.Entity> List<T> getEntitiesWithinAABB(Class <? extends T > clazz, AxisAlignedBB aabb,
            com.google.common.base.Predicate<? super T > filter);
    @Shadow public abstract MinecraftServer getMinecraftServer();
    @Shadow public abstract boolean spawnEntity(net.minecraft.entity.Entity entity); // This is overridden in WorldServerMixin
    @Shadow public abstract void updateAllPlayersSleepingFlag();
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state);
    @Shadow public abstract boolean setBlockState(BlockPos pos, IBlockState state, int flags);
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
    @Shadow public abstract void notifyLightSet(BlockPos pos);
    @Shadow @Nullable private net.minecraft.tileentity.TileEntity getPendingTileEntityAt(final BlockPos p_189508_1_) {
        return null; // Shadowed
    }
    @Shadow public abstract int getHeight(int x, int z);
    @Shadow public boolean destroyBlock(final BlockPos pos, final boolean dropBlock) {
        return false; // shadowed
    }
    @Shadow public abstract void playEvent(int i, BlockPos pos, int stateId);
    @Shadow public abstract WorldBorder getWorldBorder();
    @Shadow public boolean canSeeSky(final BlockPos pos) { return false; } // Shadowed
    @Shadow public abstract long getTotalWorldTime();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;"
                                                                     + "createWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private net.minecraft.world.border.WorldBorder onCreateWorldBorder(final WorldProvider provider) {
        if (this.bridge$isFake()) {
            return provider.createWorldBorder();
        }
        return ((WorldProviderBridge) provider).bridge$createServerWorldBorder();
    }

    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionBoxes(final net.minecraft.entity.Entity entity, final AxisAlignedBB axis, final CallbackInfoReturnable<List<AxisAlignedBB>> cir) {
        if (this.bridge$isFake() || entity == null) {
            return;
        }
        if (entity.world != null && !((WorldBridge) entity.world).bridge$isFake() && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
            // Removing misbehaved living entities
            cir.setReturnValue(new ArrayList<>());
        }
    }

    @Inject(method = "onEntityAdded", at = @At("TAIL"))
    private void onEntityAddedToWorldMarkAsTracked(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake()) { // Only set the value if the entity is not fake
            ((EntityBridge) entityIn).bridge$setWorldTracked(true);
        }
    }

    @Inject(method = "onEntityRemoved", at = @At("TAIL"))
    private void onEntityRemovedFromWorldMarkAsUntracked(final net.minecraft.entity.Entity entityIn, final CallbackInfo ci) {
        if (!this.bridge$isFake() || ((EntityBridge) entityIn).bridge$isWorldTracked()) {
            ((EntityBridge) entityIn).bridge$setWorldTracked(false);
        }
    }

    /**
     * @author gabizou - January 29th, 2019
     * @reason During block events, or other random cases, a TileEntity
     * may be removed/invalidated without the block itself being changed.
     * This can cause issues when cancelling events caused by block events
     * such that the tile entity is no longer processing on valid information,
     * so, we need to soft capture the block snapshot as a modify for later
     * possible restoration. Note that this is only overridden in worldserver.
     *
     * @param pos The position of the tile entity being removed
     */
    @Nullable
    @Redirect(method = "removeTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"))
    protected net.minecraft.tileentity.TileEntity getTileEntityForRemoval(final net.minecraft.world.World world, final BlockPos pos) {
        return world.getTileEntity(pos); // Overridden in WorldServerMixin
    }

    /**
     * @author gabizou - March 5th, 2019 - 1.12.2
     * @reason During block processing of block events, sometimes, we need
     * to be able to cancel a TileEntity removal from the world and chunk
     * since it's already been processed, or will be processed by the end
     * of the phase (that is, removals of added tile entities).
     */
    @Inject(
        method = "removeTileEntity",
        at = @At(
            value = "JUMP",
            opcode = Opcodes.IFNULL
        ),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;"
            ),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/World;processingLoadedTiles:Z",
                opcode = Opcodes.GETFIELD
            )
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION,
        cancellable = true
    )
    protected void onCheckTileEntityForRemoval(final BlockPos pos, final CallbackInfo ci, final net.minecraft.tileentity.TileEntity found, final net.minecraft.world.World thisWorld, final BlockPos samePos) {

    }

    /**
     * @author gabizou - February 25th, 2019
     * @reason During block events, or really any events where TileEntities
     * are being replaced, during processing, we need to be able to track
     * those TileEntities. In some cases, this is not used as often, whereas
     * others, like Pistons movement, we need to be able to capture and
     * "play back" what's going on, without performing the live changes.
     *
     * @param pos The position of the tile entity
     * @param tileEntity The tile entity being set onto that position
     * @return The processing tiles field, used as a hook
     */
    @Redirect(method = "setTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;isInvalid()Z"))
    protected boolean onSetTileEntityForCapture(final net.minecraft.tileentity.TileEntity tileEntity, final BlockPos pos, final net.minecraft.tileentity.TileEntity sameEntity) {
        return tileEntity.isInvalid();
    }


    @Override
    public boolean bridge$isFake() {
        if (this.impl$hasChecked) {
            return this.impl$isDefinitelyFake;
        }
        this.impl$isDefinitelyFake = this.isRemote || this.worldInfo == null || this.worldInfo.getWorldName() == null || !(this instanceof ServerWorldBridge);
        this.impl$hasChecked = true;
        return this.impl$isDefinitelyFake;
    }

    @Override
    public void bridge$clearFakeCheck() {
        this.impl$hasChecked = false;
    }

    @Redirect(method = "isAnyPlayerWithinRangeAt", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false))
    private boolean onIsAnyPlayerWithinRangePredicate(final com.google.common.base.Predicate<EntityPlayer> predicate, final Object object) {
        final EntityPlayer player = (EntityPlayer) object;
        return !(player.isDead || !((PlayerEntityBridge) player).bridge$affectsSpawning()) && predicate.apply(player);
    }

    // For invisibility
    @Redirect(method = "checkNoEntityCollision(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;"))
    private List<net.minecraft.entity.Entity> filterInvisibile(final net.minecraft.world.World world, final net.minecraft.entity.Entity entityIn,
        final AxisAlignedBB axisAlignedBB) {
        final List<net.minecraft.entity.Entity> entities = world.getEntitiesWithinAABBExcludingEntity(entityIn, axisAlignedBB);
        final Iterator<net.minecraft.entity.Entity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            final net.minecraft.entity.Entity entity = iterator.next();
            if (((VanishingBridge) entity).vanish$isVanished() && ((VanishingBridge) entity).vanish$isUncollideable()) {
                iterator.remove();
            }
        }
        return entities;
    }

    @Redirect(method = "getClosestPlayer(DDDDLcom/google/common/base/Predicate;)Lnet/minecraft/entity/player/EntityPlayer;", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false))
    private boolean onGetClosestPlayerCheck(final com.google.common.base.Predicate<net.minecraft.entity.Entity> predicate, final Object entityPlayer) {
        return predicate.apply((EntityPlayer) entityPlayer) && !((VanishingBridge) entityPlayer).vanish$isVanished();
    }

    @Inject(method = "playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V", at = @At("HEAD"), cancellable = true)
    private void spongePlaySoundAtEntity(final EntityPlayer entity, final double x, final double y, final double z, final SoundEvent name, final net.minecraft.util.SoundCategory category, final float volume, final float pitch, final CallbackInfo callbackInfo) {
        if (entity instanceof EntityBridge) {
            if (((VanishingBridge) entity).vanish$isVanished()) {
                callbackInfo.cancel();
            }
        }
    }

    // These are overriden in WorldServerMixin where they should be.

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"), cancellable = true)
    public void onDestroyBlock(final BlockPos pos, final boolean dropBlock, final CallbackInfoReturnable<Boolean> cir) {

    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    protected void onUpdateWeatherEffect(final net.minecraft.entity.Entity entityIn) {
        entityIn.onUpdate();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    protected void onUpdateTileEntities(final ITickable tile) {
        tile.update();
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    protected void onCallEntityUpdate(final net.minecraft.entity.Entity entity) {
        entity.onUpdate();
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateRidden()V"))
    protected void onCallEntityRidingUpdate(final net.minecraft.entity.Entity entity) {
        entity.updateRidden();
    }

    @Redirect(method = "addTileEntity",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;"),
                           to =   @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z")))
    private boolean onAddTileEntity(final List<net.minecraft.tileentity.TileEntity> list, final Object tile) {
        if (!this.bridge$isFake() && !canTileUpdate((net.minecraft.tileentity.TileEntity) tile)) {
            return false;
        }

        return list.add((net.minecraft.tileentity.TileEntity) tile);
    }

    private boolean canTileUpdate(final net.minecraft.tileentity.TileEntity tile) {
        final TileEntity spongeTile = (TileEntity) tile;
        if (spongeTile.getType() != null && !((SpongeTileEntityType) spongeTile.getType()).canTick()) {
            return false;
        }

        return true;
    }

    @Inject(method = "getPlayerEntityByUUID", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerEntityByUUID(final UUID uuid, final CallbackInfoReturnable<UUID> cir) {
        // avoid crashing server if passed a null UUID
        if (uuid == null) {
            cir.setReturnValue(null);
        }
    }


    /**
     * @author gabizou - July 25th, 2016
     * @author gabizou - June 23rd, 2019 - 1.12.2 - Make protected so that WorldServerMixin can override with the chunk provider.
     * @reason Optimizes several blockstate lookups for getting raw light.
     *
     * @param pos The position to get the light for
     * @param enumSkyBlock The light type
     */
    @Inject(method = "getRawLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState" +
            "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"), cancellable = true)
    protected void impl$getRawLightWithoutMarkingChunkActive(final BlockPos pos, final EnumSkyBlock enumSkyBlock, final CallbackInfoReturnable<Integer> cir) {
        final net.minecraft.world.chunk.Chunk chunk;
        chunk = this.getChunk(pos);
        if (chunk == null || chunk.unloadQueued) {
            cir.setReturnValue(0);
        }
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param pos The position
     * @return The block state at the desired position
     */
    @Overwrite
    public IBlockState getBlockState(final BlockPos pos) {
        // Sponge - Replace with inlined method
        // if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((BlockPosBridge) pos).bridge$isInvalidYPosition()) {
            // Sponge end
            return Blocks.AIR.getDefaultState();
        }
        final net.minecraft.world.chunk.Chunk chunk = this.getChunk(pos);
        return chunk.getBlockState(pos);
    }

    /**
     * @author gabizou - August 4th, 2016
     * @author bloodmc - May 10th, 2017 - Added async check
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param pos The position
     * @return The tile entity at the desired position, or else null
     */
    @Overwrite
    @Nullable
    public net.minecraft.tileentity.TileEntity getTileEntity(final BlockPos pos) {
        // Sponge - Replace with inlined method
        //  if (this.isOutsideBuildHeight(pos)) // Vanilla
        if (((BlockPosBridge) pos).bridge$isInvalidYPosition()) {
            return null;
            // Sponge End
        } else {
            net.minecraft.tileentity.TileEntity tileentity = null;

            // Sponge - Don't create or obtain pending tileentity async, simply check if TE exists in chunk
            // Mods such as pixelmon call this method async, so this is a temporary workaround until fixed
            if (!this.bridge$isFake() && !SpongeImpl.getServer().isCallingFromMinecraftThread()) {
                return this.getChunk(pos).getTileEntity(pos, net.minecraft.world.chunk.Chunk.EnumCreateEntityType.CHECK);
            }
            if (this.isTileMarkedForRemoval(pos) && !this.bridge$isFake()) {
                if (PhaseTracker.getInstance().getCurrentState().allowsGettingQueuedRemovedTiles()) {
                    return this.getQueuedRemovedTileFromProxy(pos);
                }
                return null;
            }
            tileentity = this.getProcessingTileFromProxy(pos);
            if (tileentity != null) {
                return tileentity;
            }
            // Sponge end

            if (this.processingLoadedTiles) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            if (tileentity == null) {
                 tileentity = this.getChunk(pos).getTileEntity(pos, net.minecraft.world.chunk.Chunk.EnumCreateEntityType.IMMEDIATE);
                 // Sponge - Make sure the tile entity is not actually marked for being "empty"
                if (this.isTileMarkedAsNull(pos, tileentity)) {
                    tileentity = null;
                }
            }

            if (tileentity == null) {
                tileentity = this.getPendingTileEntityAt(pos);
            }

            return tileentity;
        }
    }

    @Nullable
    protected net.minecraft.tileentity.TileEntity getQueuedRemovedTileFromProxy(final BlockPos pos) {
        return null;
    }

    protected boolean isTileMarkedAsNull(final BlockPos pos, final net.minecraft.tileentity.TileEntity tileentity) {
        return false;
    }

    protected boolean isTileMarkedForRemoval(final BlockPos pos) {
        return false;
    }

    @Nullable
    protected net.minecraft.tileentity.TileEntity getProcessingTileFromProxy(final BlockPos pos) {
        return null;
    }

//    @ModifyArg(method = "updateWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setRainTime(I)V"))
//    int impl$updateRainTimeStart(final int newRainTime) {
//        return newRainTime;
//    }
//
//    @ModifyArg(method = "updateWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/storage/WorldInfo;setThunderTime(I)V"))
//    int impl$updateThunderTimeStart(final int newThunderTime) {
//        return newThunderTime;
//    }

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
    protected boolean spongeIsAreaLoadedForCheckingLight(
        final net.minecraft.world.World thisWorld, final BlockPos pos, final int radius, final boolean allowEmtpy, final EnumSkyBlock lightType, final BlockPos samePosition) {
        return isAreaLoaded(pos, radius, allowEmtpy);
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param pos The position
     * @return True if the block position is valid
     */
    @Overwrite
    public boolean isValid(final BlockPos pos) { // isValid
        return ((BlockPosBridge) pos).bridge$isValidPosition();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param pos The position
     * @return True if the block position is outside build height
     */
    @Overwrite
    public boolean isOutsideBuildHeight(final BlockPos pos) { // isOutsideBuildHeight
        return ((BlockPosBridge) pos).bridge$isInvalidYPosition();
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Rewrites the check to be inlined to {@link BlockPosBridge}.
     *
     * @param type The type of sky lighting
     * @param pos The position
     * @return The light for the defined sky type and block position
     */
    @Overwrite
    public int getLightFor(final EnumSkyBlock type, BlockPos pos) {
        if (pos.getY() < 0) {
            pos = new BlockPos(pos.getX(), 0, pos.getZ());
        }

        // Sponge Start - Replace with inlined method to check
        // if (!this.isValid(pos)) // vanilla
        if (!((BlockPosBridge) pos).bridge$isValidPosition()) {
            // Sponge End
            return type.defaultLightValue;
        } else {
            final net.minecraft.world.chunk.Chunk chunk = this.getChunk(pos);
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
    public void setLightFor(final EnumSkyBlock type, final BlockPos pos, final int lightValue) {
        // Sponge Start - Replace with inlined Valid position check
        // if (this.isValid(pos)) // Vanilla
        if (((BlockPosBridge) pos).bridge$isValidPosition()) { // Sponge - Replace with inlined method to check
            // Sponge End
            if (this.isBlockLoaded(pos)) {
                final net.minecraft.world.chunk.Chunk chunk = this.getChunk(pos);
                chunk.setLightFor(type, pos, lightValue);
                this.notifyLightSet(pos);
            }
        }
    }


    /**
     * @author gabizou - August 4th, 2016
     * @reason Inlines the bridge$isValidXZPosition check to BlockPos.
     *
     * @param bbox The AABB to check
     * @return True if the AABB collides with a block
     */
    @Overwrite
    public boolean collidesWithAnyBlock(final AxisAlignedBB bbox) {
        final List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
        final int i = MathHelper.floor(bbox.minX) - 1;
        final int j = MathHelper.ceil(bbox.maxX) + 1;
        final int k = MathHelper.floor(bbox.minY) - 1;
        final int l = MathHelper.ceil(bbox.maxY) + 1;
        final int i1 = MathHelper.floor(bbox.minZ) - 1;
        final int j1 = MathHelper.ceil(bbox.maxZ) + 1;
        final BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        try {
            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = i1; l1 < j1; ++l1) {
                    final int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);

                    if (i2 != 2 && this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
                        for (int j2 = k; j2 < l; ++j2) {
                            if (i2 <= 0 || j2 != k && j2 != l - 1) {
                                blockpos$pooledmutableblockpos.setPos(k1, j2, l1);

                                // Sponge - Replace with inlined method
                                // if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) // Vanilla
                                if (!((BlockPosBridge) (Object) blockpos$pooledmutableblockpos).bridge$isValidXZPosition()) {
                                    // Sponge End
                                    final boolean flag1 = true;
                                    return flag1;
                                }

                                final IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos);
                                iblockstate.addCollisionBoxToList((net.minecraft.world.World) (Object) this, blockpos$pooledmutableblockpos, bbox, list, (net.minecraft.entity.Entity) null, false);

                                if (!list.isEmpty()) {
                                    final boolean flag = true;
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


    /**
     * @author blood
     * @author gabizou - Ported to 1.9.4 - replace direct field calls to overriden methods in WorldServerMixin
     *
     * @reason Add timing hooks in various areas. This method shouldn't be touched by mods/forge alike
     */
    @Overwrite
    public void updateEntities() {
        this.profiler.startSection("entities");
        this.profiler.startSection("global");
        this.startEntityGlobalTimings(); // Sponge


        for (int i = 0; i < this.weatherEffects.size(); ++i) {
            final net.minecraft.entity.Entity entity = this.weatherEffects.get(i);

            try {
                ++entity.ticksExisted;
                entity.onUpdate();
            } catch (Throwable throwable2) {
                this.stopTimingForWeatherEntityTickCrash(entity); // Sponge - end the entity timing
                final CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                final CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

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
        this.profiler.endStartSection("remove");
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (int k = 0; k < this.unloadedEntityList.size(); ++k) {
            final net.minecraft.entity.Entity entity1 = this.unloadedEntityList.get(k);
            // Sponge start - use cached chunk
            // int j = entity1.chunkCoordX;
            // int k1 = entity1.chunkCoordZ;

            final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((ActiveChunkReferantBridge) entity1).bridge$getActiveChunk();
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
        this.profiler.endStartSection("regular");
        this.entityActivationCheck();

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
            final net.minecraft.entity.Entity entity2 = this.loadedEntityList.get(i1);
            final net.minecraft.entity.Entity entity3 = entity2.getRidingEntity();

            if (entity3 != null) {
                if (!entity3.isDead && entity3.isPassenger(entity2)) {
                    continue;
                }

                entity2.dismountRidingEntity();
            }

            this.profiler.startSection("tick");
            this.startEntityTickTiming(); // Sponge

            if (!entity2.isDead && !(entity2 instanceof EntityPlayerMP)) {
                try {
                    SpongeImplHooks.onEntityTickStart(entity2);
                    this.updateEntity(entity2);
                    SpongeImplHooks.onEntityTickEnd(entity2);
                } catch (Throwable throwable1) {
                    this.stopTimingTickEntityCrash(entity2); // Sponge
                    final CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    final CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Entity being ticked");
                    entity2.addEntityCrashInfo(crashreportcategory1);
                    SpongeImplHooks.onEntityError(entity2, crashreport1);
                }
            }

            this.stopEntityTickSectionBeforeRemove(); // Sponge
            this.profiler.endSection();
            this.profiler.startSection("remove");
            this.startEntityRemovalTick(); // Sponge

            if (entity2.isDead) {
                // Sponge start - use cached chunk
                final int l1 = entity2.chunkCoordX;
                final int i2 = entity2.chunkCoordZ;

                final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((ActiveChunkReferantBridge) entity2).bridge$getActiveChunk();
                if (activeChunk == null) {
                    this.getChunk(l1, i2).removeEntity(entity2);
                } else {
                    activeChunk.removeEntity(entity2);
                }
                // Sponge end

                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity2);
            }

            this.stopEntityRemovalTiming(); // Sponge
            this.profiler.endSection();
        }

         this.profiler.endStartSection("blockEntities");
        spongeTileEntityActivation();
        this.processingLoadedTiles = true;
        final Iterator<net.minecraft.tileentity.TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext()) {
            this.startTileTickTimer(); // Sponge
            final net.minecraft.tileentity.TileEntity tileentity = iterator.next();

            if (!tileentity.isInvalid() && tileentity.hasWorld()) {
                final BlockPos blockpos = tileentity.getPos();

                if (((TileEntityBridge) tileentity).bridge$shouldTick() && this.worldBorder.contains(blockpos)) { // Sponge
                    try {
                        this.profiler.func_194340_a(() -> String.valueOf(net.minecraft.tileentity.TileEntity.getKey(tileentity.getClass())));
                        SpongeImplHooks.onTETickStart(tileentity);
                        ((ITickable) tileentity).update();
                        this.profiler.endSection();
                        SpongeImplHooks.onTETickEnd(tileentity);
                    } catch (Throwable throwable) {
                        this.stopTimingTickTileEntityCrash(tileentity); // Sponge
                        final CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        final CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Block entity being ticked");
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
                final net.minecraft.world.chunk.Chunk activeChunk = (net.minecraft.world.chunk.Chunk) ((ActiveChunkReferantBridge) tileentity).bridge$getActiveChunk();
                if (activeChunk != null) {
                    //this.getChunk(tileentity.getPos()).bridge$removeTileEntity(tileentity.getPos());
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
            for (final Object tile : this.tileEntitiesToBeRemoved) {
               SpongeImplHooks.onTileChunkUnload(((net.minecraft.tileentity.TileEntity)tile));
            }
            // Sponge end

            // forge: faster "contains" makes this removal much more efficient
            final java.util.Set<net.minecraft.tileentity.TileEntity> remove = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
            remove.addAll(this.tileEntitiesToBeRemoved);
            this.tickableTileEntities.removeAll(remove);
            this.loadedTileEntityList.removeAll(remove);
            this.tileEntitiesToBeRemoved.clear();
        }

        if (!this.bridge$isFake()) {
            try (final PhaseContext<?> context = BlockPhase.State.TILE_CHUNK_UNLOAD.createPhaseContext().source(this)) {
                context.buildAndSwitch();
                this.startPendingTileEntityTimings(); // Sponge
            }
        }

        this.processingLoadedTiles = false;  //FML Move below remove to prevent CMEs
         this.profiler.endStartSection("pendingBlockEntities");

        if (!this.addedTileEntityList.isEmpty()) {
            for (int j1 = 0; j1 < this.addedTileEntityList.size(); ++j1) {
                final net.minecraft.tileentity.TileEntity tileentity1 = this.addedTileEntityList.get(j1);

                if (!tileentity1.isInvalid()) {
                    if (!this.loadedTileEntityList.contains(tileentity1)) {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos())) {
                        final net.minecraft.world.chunk.Chunk chunk = this.getChunk(tileentity1.getPos());
                        final IBlockState iblockstate = chunk.getBlockState(tileentity1.getPos());
                        chunk.addTileEntity(tileentity1.getPos(), tileentity1);
                        this.notifyBlockUpdate(tileentity1.getPos(), iblockstate, iblockstate, 3);
                    }
                }
            }

            this.addedTileEntityList.clear();
        }

        this.endPendingTileEntities(); // Sponge
        this.profiler.endSection();
        this.profiler.endSection();
    }

    /**
     * Overridden in {@link WorldServerMixin_TileEntityActivation}
     */
    public void spongeTileEntityActivation() {

    }

    public void entityActivationCheck() {
        // Overridden in WorldServerMixin_Activation
    }

    protected void startEntityGlobalTimings() { }

    protected void stopTimingForWeatherEntityTickCrash(final net.minecraft.entity.Entity updatingEntity) { }

    protected void stopEntityTickTimingStartEntityRemovalTiming() { }

    protected void stopEntityRemovalTiming() { }

    protected void startEntityTickTiming() { }

    protected void stopTimingTickEntityCrash(final net.minecraft.entity.Entity updatingEntity) { }

    protected void stopEntityTickSectionBeforeRemove() { }

    protected void startEntityRemovalTick() { }

    protected void startTileTickTimer() { }

    protected void stopTimingTickTileEntityCrash(final net.minecraft.tileentity.TileEntity updatingTileEntity) { }

    protected void stopTileEntityAndStartRemoval() { }

    protected void stopTileEntityRemovelInWhile() { }

    protected void startPendingTileEntityTimings() {}

    protected void endPendingTileEntities() { }

}
