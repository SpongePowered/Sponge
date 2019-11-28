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
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.property.item.RecordProperty;
import org.spongepowered.api.effect.sound.record.RecordType;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.sound.PlaySoundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.util.math.BlockPosBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldProviderBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderBridge;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.SpongeProxyBlockAccess;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.relocate.co.aikar.timings.TimingHistory;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.SpongeEmptyChunk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.World.class)
public abstract class WorldMixin implements WorldBridge {

    private boolean impl$isDefinitelyFake = false;
    private boolean impl$hasChecked = false;
    @Nullable private SpongeEmptyChunk impl$emptyChunk;

    // @formatter:off
    @Shadow @Final public boolean isRemote;
    @Shadow @Final public WorldProvider provider;
    @Shadow @Final public Random rand;
    @Shadow @Final public Profiler profiler;
    @Shadow @Final public List<EntityPlayer> playerEntities;
    @Shadow @Final public List<net.minecraft.entity.Entity> loadedEntityList;
    @Shadow @Final public List<net.minecraft.entity.Entity> weatherEffects;
    @Shadow @Final protected List<net.minecraft.entity.Entity> unloadedEntityList;
    @Shadow @Final public List<TileEntity> loadedTileEntityList;
    @Shadow @Final public List<TileEntity> tickableTileEntities;
    @Shadow @Final private List<TileEntity> tileEntitiesToBeRemoved;
    @Shadow @Final private List<TileEntity> addedTileEntityList;
    @SuppressWarnings("ShadowModifiers") @Shadow protected int[] lightUpdateBlockList; // Elevated to protected for subclass mixins to access when they're separated by package
    @Shadow private boolean processingLoadedTiles;
    @Shadow protected boolean scheduledUpdatesAreImmediate;
    @Shadow protected WorldInfo worldInfo;
    @Shadow protected IChunkProvider chunkProvider;
    @Shadow @Final private net.minecraft.world.border.WorldBorder worldBorder;
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
    @Shadow @Nullable public abstract Chunk getChunk(BlockPos pos);
    @Shadow public abstract WorldInfo getWorldInfo();
    @Shadow public abstract boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos);
    @Shadow public abstract boolean addTileEntity(TileEntity tile);
    @Shadow protected abstract void onEntityAdded(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty);
    @Shadow protected abstract void onEntityRemoved(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void updateEntity(net.minecraft.entity.Entity ent);
    @Shadow public abstract boolean isBlockLoaded(BlockPos pos);
    @Shadow public void markChunkDirty(final BlockPos pos, final TileEntity unusedTileEntity){};
    @Shadow public abstract boolean addWeatherEffect(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract Biome getBiome(BlockPos pos);
    @Shadow public abstract Chunk getChunk(int chunkX, int chunkZ);
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
    @Shadow @Nullable private TileEntity getPendingTileEntityAt(final BlockPos p_189508_1_) {
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
    @Shadow private boolean isAreaLoaded(
        final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd, final int zEnd, final boolean allowEmpty) { return false; } // SHADOWED
    @Shadow public void updateEntities() { }
    @Shadow @Nullable public abstract TileEntity getTileEntity(BlockPos pos);

    @Shadow public abstract IChunkProvider shadow$getChunkProvider();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProvider;"
                                                                     + "createWorldBorder()Lnet/minecraft/world/border/WorldBorder;"))
    private net.minecraft.world.border.WorldBorder onCreateWorldBorder(final WorldProvider provider) {
        if (this.bridge$isFake()) {
            return provider.func_177501_r();
        }
        return ((WorldProviderBridge) provider).bridge$createServerWorldBorder();
    }

    @Inject(method = "getCollisionBoxes(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void onGetCollisionBoxes(final net.minecraft.entity.Entity entity, final AxisAlignedBB axis, final CallbackInfoReturnable<List<AxisAlignedBB>> cir) {
        if (this.bridge$isFake() || entity == null) {
            return;
        }
        if (entity.field_70170_p != null && !((WorldBridge) entity.field_70170_p).bridge$isFake() && SpongeHooks.checkBoundingBoxSize(entity, axis)) {
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
    protected TileEntity getTileEntityForRemoval(final net.minecraft.world.World world, final BlockPos pos) {
        return world.func_175625_s(pos); // Overridden in WorldServerMixin
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
    protected void onCheckTileEntityForRemoval(final BlockPos pos, final CallbackInfo ci, final TileEntity found, final net.minecraft.world.World thisWorld, final BlockPos samePos) {

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
    protected boolean onSetTileEntityForCapture(final TileEntity tileEntity, final BlockPos pos, final TileEntity sameEntity) {
        return tileEntity.func_145837_r();
    }


    @Override
    public boolean bridge$isFake() {
        if (this.impl$hasChecked) {
            return this.impl$isDefinitelyFake;
        }
        this.impl$isDefinitelyFake = this.isRemote || this.worldInfo == null || this.worldInfo.func_76065_j() == null || !(this instanceof WorldServerBridge);
        this.impl$hasChecked = true;
        return this.impl$isDefinitelyFake;
    }

    @Override
    public void bridge$clearFakeCheck() {
        this.impl$hasChecked = false;
    }

    @SuppressWarnings("Guava")
    @Redirect(method = "isAnyPlayerWithinRangeAt", at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false))
    private boolean onIsAnyPlayerWithinRangePredicate(final com.google.common.base.Predicate<EntityPlayer> predicate, final Object object) {
        final EntityPlayer player = (EntityPlayer) object;
        return !(player.field_70128_L || !((EntityPlayerBridge) player).bridge$affectsSpawning()) && predicate.apply(player);
    }

    // For invisibility
    @Redirect(method = "checkNoEntityCollision(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/entity/Entity;)Z",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;"))
    private List<net.minecraft.entity.Entity> impl$filterInvisibile(final net.minecraft.world.World world, final net.minecraft.entity.Entity entityIn,
        final AxisAlignedBB axisAlignedBB) {
        final List<net.minecraft.entity.Entity> entities = world.func_72839_b(entityIn, axisAlignedBB);
        entities.removeIf(entity -> ((VanishableBridge) entity).bridge$isVanished() && ((VanishableBridge) entity).bridge$isUncollideable());
        return entities;
    }

    @SuppressWarnings("Guava")
    @Redirect(method = "getClosestPlayer(DDDDLcom/google/common/base/Predicate;)Lnet/minecraft/entity/player/EntityPlayer;",
        at = @At(value = "INVOKE", target = "Lcom/google/common/base/Predicate;apply(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$checkIfPlayerIsVanished(final com.google.common.base.Predicate<net.minecraft.entity.Entity> predicate, final Object entityPlayer) {
        return predicate.apply((EntityPlayer) entityPlayer) && !((VanishableBridge) entityPlayer).bridge$isVanished();
    }

    @Inject(method = "playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V",
        at = @At("HEAD"), cancellable = true)
    private void impl$spongePlaySoundAtEntity(@Nullable final EntityPlayer entity, final double x, final double y, final double z,
        final SoundEvent name, final net.minecraft.util.SoundCategory category, final float volume, final float pitch,
        final CallbackInfo callbackInfo) {
        if (entity instanceof EntityBridge) {
            if (((VanishableBridge) entity).bridge$isVanished()) {
                callbackInfo.cancel();
                return;
            }
        }
        if (this.bridge$isFake()) {
            return;
        }
        if(ShouldFire.PLAY_SOUND_EVENT_AT_ENTITY) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

                final PlaySoundEvent.AtEntity event = SpongeCommonEventFactory.callPlaySoundAtEntityEvent(frame.getCurrentCause(),
                        entity, this, x, y, z, category, name, pitch, volume);
                if (event.isCancelled()) {
                    callbackInfo.cancel();
                }
            }
        }
    }

    @Inject(method = "playBroadcastSound(ILnet/minecraft/util/math/BlockPos;I)V", at = @At("HEAD"), cancellable = true)
    private void impl$throwBroadcastSoundEvent(final int effectID, final BlockPos pos, final int soundData, final CallbackInfo callbackInfo) {
        if(!this.bridge$isFake() && ShouldFire.PLAY_SOUND_EVENT_BROADCAST) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                final PlaySoundEvent.Broadcast event = SpongeCommonEventFactory.callPlaySoundBroadcastEvent(frame, this, pos, effectID);
                if (event != null && event.isCancelled()) {
                    callbackInfo.cancel();
                }
            }
        }
    }

    @Inject(method = "playEvent(Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/math/BlockPos;I)V", at = @At("HEAD"), cancellable = true)
    private void impl$throwRecordPlayEvent(final EntityPlayer player, final int type, final BlockPos pos, final int data, final CallbackInfo callbackInfo) {
        if (this.bridge$isFake()) {
            return;
        }
        if(type == Constants.WorldEvents.PLAY_RECORD_EVENT && ShouldFire.PLAY_SOUND_EVENT_RECORD) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                final TileEntity tileEntity = this.getTileEntity(pos);
                if(tileEntity instanceof BlockJukebox.TileEntityJukebox) {
                    final BlockJukebox.TileEntityJukebox jukebox = (BlockJukebox.TileEntityJukebox) tileEntity;
                    final ItemStack record = jukebox.func_145856_a();
                    frame.pushCause(jukebox);
                    frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(record));
                    if(!record.func_190926_b()) {
                        final Optional<RecordProperty> recordProperty = ((org.spongepowered.api.item.inventory.ItemStack) record).getProperty(RecordProperty.class);
                        if(!recordProperty.isPresent()) {
                            //Safeguard for https://github.com/SpongePowered/SpongeCommon/issues/2337
                            return;
                        }
                        final RecordType recordType = recordProperty.get().getValue();
                        final PlaySoundEvent.Record event = SpongeCommonEventFactory.callPlaySoundRecordEvent(frame.getCurrentCause(), jukebox, recordType, data);
                        if (event.isCancelled()) {
                            callbackInfo.cancel();
                        }
                    }
                }
            }
        }
    }

    // These are overriden in WorldServerMixin where they should be.

    @Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playEvent(ILnet/minecraft/util/math/BlockPos;I)V"), cancellable = true)
    public void onDestroyBlock(final BlockPos pos, final boolean dropBlock, final CallbackInfoReturnable<Boolean> cir) {

    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    protected void onUpdateWeatherEffect(final net.minecraft.entity.Entity entityIn) {
        entityIn.func_70071_h_();
    }

    @Redirect(method = "updateEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ITickable;update()V"))
    protected void onUpdateTileEntities(final ITickable tile) {
        tile.func_73660_a();
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onUpdate()V"))
    protected void onCallEntityUpdate(final net.minecraft.entity.Entity entity) {
        entity.func_70071_h_();
    }

    @Redirect(method = "updateEntityWithOptionalForce", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateRidden()V"))
    protected void onCallEntityRidingUpdate(final net.minecraft.entity.Entity entity) {
        entity.func_70098_U();
    }

    @Redirect(method = "updateEntityWithOptionalForce",
        at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;addedToChunk:Z", opcode = Opcodes.GETFIELD),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;chunkCoordX:I", opcode = Opcodes.GETFIELD, ordinal = 0),
            to = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;chunkCoordX:I", opcode = Opcodes.GETFIELD, ordinal = 1)
        )
    )
    private boolean impl$returnTrueToAddedToChunkForMovingEntities(final Entity entity) {
        // Sponge start - Remove active chunk and re-assign new Active Chunk
        final Chunk activeChunk = (Chunk) ((ActiveChunkReferantBridge) entity).bridge$getActiveChunk();
        if (activeChunk != null) {
            activeChunk.func_76608_a(entity, entity.field_70162_ai);
        }
        final int l = MathHelper.func_76128_c(entity.field_70165_t / 16.0D);
        final int j1 = MathHelper.func_76128_c(entity.field_70161_v / 16.0D);
        final ChunkBridge newChunk = (ChunkBridge) ((ChunkProviderBridge) this.shadow$getChunkProvider()).bridge$getLoadedChunkWithoutMarkingActive(l, j1);
        final boolean isPositionDirty = entity.func_184189_br();
        if (newChunk == null || (!isPositionDirty && newChunk.bridge$isQueuedForUnload() && !newChunk.bridge$isPersistedChunk())) {
            entity.field_70175_ag = false;
        } else {
            ((net.minecraft.world.chunk.Chunk) newChunk).func_76612_a(entity);
        }
        // Sponge end
        return false; // Always return false, because we handle re-assigning the chunk owning the entity in the above block
    }

    @Redirect(method = "updateEntityWithOptionalForce",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isChunkLoaded(IIZ)Z"),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "doubleValue=16.0D"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;removeEntityAtIndex(Lnet/minecraft/entity/Entity;I)V")
        )
    )
    private boolean impl$returnFalseForChunkLoadedToAvoidIf(final World world, final int x, final int z, final boolean allowEmpty) {
        return false;
    }

    @Redirect(method = "updateEntityWithOptionalForce",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isChunkLoaded(IIZ)Z"),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPositionNonDirty()Z"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;addedToChunk:Z", opcode = Opcodes.PUTFIELD)
        )
    )
    private boolean impl$returnChunkLoadedAlways(final World world, final int x, final int z, final boolean allowEmpty) {
        return true;
    }

    @Redirect(method = "updateEntityWithOptionalForce",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getChunk(II)Lnet/minecraft/world/chunk/Chunk;"),
        slice = @Slice(
            // Note- we cannot specify the slice to use the entity.addedToChunk putfield because in production/vanilla
            //   that label/block is after the chunk.addEntity label/block, causing a slice index exception.
            //   see https://i.imgur.com/CNnbj3y.png
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPositionNonDirty()Z"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;addEntity(Lnet/minecraft/entity/Entity;)V")
        )
    )
    private Chunk impl$returnEmptyChunk(final World world, final int chunkX, final int chunkZ) {
        if (this.impl$emptyChunk == null) {
            this.impl$emptyChunk = new SpongeEmptyChunk((World) (Object) this, 0, 0);
        }
        return this.impl$emptyChunk;
    }


    @Inject(method = "updateEntityWithOptionalForce",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;ticksExisted:I",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void impl$increaseActivatedEntityTicks(final Entity entityIn, final boolean forceUpdate, final CallbackInfo ci) {
        // ++entityIn.ticksExisted;
        ++TimingHistory.activatedEntityTicks; // Sponge
    }



    @Redirect(method = "addTileEntity",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;"),
                           to =   @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z")))
    private boolean onAddTileEntity(final List<? super TileEntity> list, final Object tile) {
        if (!this.bridge$isFake() && !canTileUpdate((TileEntity) tile)) {
            return false;
        }

        return list.add((TileEntity) tile);
    }

    @SuppressWarnings("ConstantConditions")
    private boolean canTileUpdate(final TileEntity tile) {
        final org.spongepowered.api.block.tileentity.TileEntity spongeTile = (org.spongepowered.api.block.tileentity.TileEntity) tile;
        return spongeTile.getType() == null || ((SpongeTileEntityType) spongeTile.getType()).canTick();
    }

    @Inject(method = "getPlayerEntityByUUID", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerEntityByUUID(@Nullable final UUID uuid, final CallbackInfoReturnable<UUID> cir) {
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
        final Chunk chunk;
        chunk = this.getChunk(pos);
        if (chunk == null || chunk.field_189550_d) {
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
            return Blocks.field_150350_a.func_176223_P();
        }
        final Chunk chunk = this.getChunk(pos);
        return chunk.func_177435_g(pos);
    }

    @Redirect(method = "getTileEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;isOutsideBuildHeight(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean impl$useInlinedMethodInsteadisoutsideBuildHeight(final World world, final BlockPos pos) {
        return ((BlockPosBridge) pos).bridge$isInvalidYPosition();
    }

    @Inject(method = "getTileEntity", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;processingLoadedTiles:Z"), cancellable = true)
    private void impl$checkForAsync(final BlockPos pos, final CallbackInfoReturnable<TileEntity> cir) {
        // Sponge - Don't create or obtain pending tileentity async, simply check if TE exists in chunk
        // Mods such as pixelmon call this method async, so this is a temporary workaround until fixed
        if (!this.bridge$isFake() && !SpongeImpl.getServer().func_152345_ab()) {
            cir.setReturnValue(this.getChunk(pos).func_177424_a(pos, Chunk.EnumCreateEntityType.CHECK));
            return;
        }
        if (this.isTileMarkedForRemoval(pos) && !this.bridge$isFake()) {
            if (PhaseTracker.getInstance().getCurrentState().allowsGettingQueuedRemovedTiles()) {
                cir.setReturnValue(this.getQueuedRemovedTileFromProxy(pos));
                return;
            }
            cir.setReturnValue(null);
        }

    }

    /**
     * @author gabizou - Sometime in 2019 - 1.12
     * @reason Because the {@link SpongeProxyBlockAccess} can have different
     * ways a {@link TileEntity} can exist and not exist, depending on the time
     * that this method is called, we need to also check that the tile entity at
     * the chunk is actually marked as null.
     *
     * @param chunk
     * @param pos
     * @param creationMode
     * @return
     */
    @Redirect(method = "getTileEntity", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/chunk/Chunk;getTileEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/chunk/Chunk$EnumCreateEntityType;)Lnet/minecraft/tileentity/TileEntity;"))
    @Nullable
    TileEntity impl$getTileOrNullIfMarkedForRemoval(final Chunk chunk, final BlockPos pos, final Chunk.EnumCreateEntityType creationMode) {
        return chunk.func_177424_a(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
    }

    @Nullable
    protected TileEntity getQueuedRemovedTileFromProxy(final BlockPos pos) {
        return null;
    }

    protected boolean isTileMarkedAsNull(final BlockPos pos, @Nullable final TileEntity tileentity) {
        return false;
    }

    protected boolean isTileMarkedForRemoval(final BlockPos pos) {
        return false;
    }

    @Nullable
    protected TileEntity getProcessingTileFromProxy(final BlockPos pos) {
        return null;
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
    private boolean isOutsideBuildHeight(final BlockPos pos) { // isOutsideBuildHeight
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
        if (pos.func_177956_o() < 0) {
            pos = new BlockPos(pos.func_177958_n(), 0, pos.func_177952_p());
        }

        // Sponge Start - Replace with inlined method to check
        // if (!this.isValid(pos)) // vanilla
        if (!((BlockPosBridge) pos).bridge$isValidPosition()) {
            // Sponge End
            return type.field_77198_c;
        } else {
            final Chunk chunk = this.getChunk(pos);
            return chunk.func_177413_a(type, pos);
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
                final Chunk chunk = this.getChunk(pos);
                chunk.func_177431_a(type, pos, lightValue);
                this.notifyLightSet(pos);
            }
        }
    }

    @Inject(method = "isAreaLoaded(IIIIIIZ)Z", at = @At("HEAD"), cancellable = true)
    protected void impl$useWorldServerMethodForAvoidingLookups(final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd, final int zEnd, final boolean allowEmpty,
        final CallbackInfoReturnable<Boolean> cir) {
        // DO NOTHING ON NON-SERVER WORLDS
    }

    @Override
    public boolean bridge$isAreaLoaded(
        final int xStart, final int yStart, final int zStart, final int xEnd, final int yEnd, final int zEnd, final boolean allowEmpty) {
        return this.isAreaLoaded(xStart, yStart, zStart, xEnd, yEnd, zEnd, allowEmpty);
    }

    /**
     * @author gabizou - August 4th, 2016
     * @reason Inlines the bridge$isValidXZPosition check to BlockPos.
     *
     * @param bbox The AABB to check
     * @return True if the AABB collides with a block
     */
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    public boolean collidesWithAnyBlock(final AxisAlignedBB bbox) {
        final List<AxisAlignedBB> list = Lists.<AxisAlignedBB>newArrayList();
        final int i = MathHelper.func_76128_c(bbox.field_72340_a) - 1;
        final int j = MathHelper.func_76143_f(bbox.field_72336_d) + 1;
        final int k = MathHelper.func_76128_c(bbox.field_72338_b) - 1;
        final int l = MathHelper.func_76143_f(bbox.field_72337_e) + 1;
        final int i1 = MathHelper.func_76128_c(bbox.field_72339_c) - 1;
        final int j1 = MathHelper.func_76143_f(bbox.field_72334_f) + 1;
        final BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.func_185346_s();

        try {
            for (int k1 = i; k1 < j; ++k1) {
                for (int l1 = i1; l1 < j1; ++l1) {
                    final int i2 = (k1 != i && k1 != j - 1 ? 0 : 1) + (l1 != i1 && l1 != j1 - 1 ? 0 : 1);

                    if (i2 != 2 && this.isBlockLoaded(blockpos$pooledmutableblockpos.func_181079_c(k1, 64, l1))) {
                        for (int j2 = k; j2 < l; ++j2) {
                            if (i2 <= 0 || j2 != k && j2 != l - 1) {
                                blockpos$pooledmutableblockpos.func_181079_c(k1, j2, l1);

                                // Sponge - Replace with inlined method
                                // if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) // Vanilla
                                if (!((BlockPosBridge) (Object) blockpos$pooledmutableblockpos).bridge$isValidXZPosition()) {
                                    // Sponge End
                                    return true;
                                }

                                final IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos);
                                iblockstate.func_185908_a((net.minecraft.world.World) (Object) this, blockpos$pooledmutableblockpos, bbox, list, (net.minecraft.entity.Entity) null, false);

                                if (!list.isEmpty()) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

            return false;
        } finally {
            blockpos$pooledmutableblockpos.func_185344_t();
        }
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
            shift = At.Shift.AFTER), // Basically, we want to position after the startSection
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=global"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/world/World;weatherEffects:Ljava/util/List;", ordinal = 0)
        )
    )
    void impl$startEntityGlobalTimings(final CallbackInfo ci) {
        // this.profiler startSection("global");

        // Sponge -- inject here

        // for (int i = 0; i < this.weatherEffects.size(); ++i) {
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;",
            ordinal = 0
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    void impl$stopTimingForWeatherEntityTickCrash(final CallbackInfo ci, final int index, final Entity entity, final Throwable throwable) {
        // catch (final Throwable throwable2) {
        // sponge - inject here
        // final CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
            shift = At.Shift.BEFORE,
            by = 2
        ),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=remove"),
            to = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/World;unloadedEntityList:Ljava/util/List;",
                opcode = Opcodes.GETFIELD,
                ordinal = 0)
        )
    )
    void impl$stopEntityTickTiming(final CallbackInfo ci) {
         // Sponge -- inject here
        // this.profiler.endStartSection("remove");
    }

    @Redirect(method = "updateEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;addedToChunk:Z"))
    private boolean impl$ReturnFalseUseActiveChunkReferant(final Entity entity) {
        // Sponge start - use cached chunk
        // int j = entity1.chunkCoordX;
        // int k1 = entity1.chunkCoordZ;

        // Sponge start - use cached chunk
        final int l1 = entity.field_70176_ah;
        final int i2 = entity.field_70164_aj;

        final Chunk activeChunk = (Chunk) ((ActiveChunkReferantBridge) entity).bridge$getActiveChunk();
        if (activeChunk == null) {
            this.getChunk(l1, i2).func_76622_b(entity);
        } else {
            activeChunk.func_76622_b(entity);
        }
        // Sponge end
        //if (entity1.addedToChunk && this.isChunkLoaded(j, k1, true))
        //{
        //    this.getChunk(j, k1).removeEntity(entity1);
        //}
        // Sponge end
        // Always return false to avoid the rest of the if statement. we handle it in the active chunk check above.
        return false;
    }

    @Inject(method = "updateEntities",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;tickPlayers()V"))
    void impl$stopEntityRemovalTiming(final CallbackInfo ci) {
        // this.unloadedEntityList.clear();
        // Sponge -- inject here
        // this.tickPlayers();
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
            shift = At.Shift.AFTER
        ),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=regular"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getRidingEntity()Lnet/minecraft/entity/Entity;")
        )
    )
    protected void impl$entityActivationCheck(final CallbackInfo ci) {
        //this.profiler.endStartSection("regular");
        // Sponge -- inject here

        // for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1) {
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V",
            shift = At.Shift.AFTER // We want to inject after the start section call.
        ),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;dismountRidingEntity()V"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V")
        )
    )
    void impl$startEntityTickingForTick(final CallbackInfo ci) {
        // this.profiler.startSection("tick");
        // Sponge -- inject here

        // if (!entity2.isDead && !(entity2 instanceof EntityPlayerMP)) {
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;"),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=Ticking entity", ordinal = 1),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addEntityCrashInfo(Lnet/minecraft/crash/CrashReportCategory;)V", ordinal = 1)
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    void impl$stopEntityAndThrowInfo(final CallbackInfo ci, final int index, final Entity ticking, @Nullable final Entity riding, final Throwable throwable) {
        // } catch (final Throwable throwable1) {
        // Sponge -- inject here
        // final CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
    }

    @Surrogate
    void impl$stopEntityAndThrowInfo(final CallbackInfo ci, final int index, final Entity ticking, final Throwable throwable) {
        this.impl$stopEntityAndThrowInfo(ci, index, ticking, null, throwable);
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;endSection()V",
            shift = At.Shift.BEFORE,
            by = 2 // Allows us to inject before calling `this.profiler`
        ),
        slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;updateEntity(Lnet/minecraft/entity/Entity;)V"),
            to = @At(value = "CONSTANT", args = "stringValue=remove", ordinal = 1)
        )
    )
    void impl$startEntityRemovalTiming(final CallbackInfo callbackInfo) {
        // Sponge -- inject here
        // this.profiler.endSection();
        // this.profiler.startSection("remove");

        // if (entity2.isDead) {
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/profiler/Profiler;endSection()V",
            shift = At.Shift.BEFORE,
            by = 2),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/World;onEntityRemoved(Lnet/minecraft/entity/Entity;)V",
                ordinal = 1),
            to = @At(
                value = "CONSTANT",
                args = "stringValue=blockEntities")
        )
    )
    void impl$stopRemovalTimingAfterentityRemovals(final CallbackInfo ci) {
            // this.onEntityRemoved(entity2);
        // }
        // Sponge -- inject here
        // this.profiler.endSection();
    }

    @Inject(method = "updateEntities",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;processingLoadedTiles:Z", opcode = Opcodes.PUTFIELD),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=blockEntities"),
            // This is slightly ambiguous due to the nature of forge moving tile removals
            // before `this.processingLoadedTiles`, so we have to directly target an after instruction
            // being hasWorld since that is guaranteed to be called before `this.processingLoadedTiles = false`
            to = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasWorld()Z")
        )
    )
    protected void impl$tileActivationStart(final CallbackInfo callbackInfo) {
        //this.profiler.endStartSection("blockEntities");
        // Sponge -- inject here
        // this.processingLoadedTiles = true;
    }

    @Inject(method = "updateEntities", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;next()Ljava/lang/Object;"),
        slice = @Slice(
            // Again, just like above, forge moves the removal of invalid tiles,
            // so we have to target the field getter for the iterator the second time, always, instead
            // of attempting to use the `this.processingLoadedTiles = true` assignment, because forge moves
            // the block of tile entity removal code after the field assignment
            from = @At(value = "FIELD",
                target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;",
                opcode = Opcodes.GETFIELD,
                ordinal = 1),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasWorld()Z")
        )
    )
    void impl$startTileTickTimer(final CallbackInfo ci) {
        // while (iterator.hasNext()) {
        // Sponge -- inject here
        // final TileEntity tileentity = iterator.next();
    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/crash/CrashReport;makeCrashReport(Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/crash/CrashReport;"
        ),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=Ticking block entity"),
            to = @At(value = "CONSTANT", args = "stringValue=Block entity being ticked")
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    void impl$stopTileTickCrash(final CallbackInfo ci, final Iterator<TileEntity> iterator, final TileEntity tickingTile,
        final BlockPos pos, final Throwable throwable) {
        // } catch (final Throwable throwable) {
        // Sponge -- inject here
            // final CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
    }

    @Surrogate
        // BlockPos seems to only exist during development
    void impl$stopTileTickCrash(final CallbackInfo ci, final Iterator<TileEntity> iterator, final TileEntity tickingTile, final Throwable throwable) {
        // } catch (final Throwable throwable) {
        // Sponge -- inject here
        // final CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
    }

    @Redirect(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntity;hasWorld()Z"
        )
    )
    private boolean impl$checkIfTileHasActiveChunk(final TileEntity tileEntity) {
        return tileEntity.func_145830_o() && ((TileEntityBridge) tileEntity).bridge$shouldTick();

    }

    // This would be in common, but Forge rewrites isBlockLoaded(BlockPos) to isBlockLoaded(BlockPos,boolean)....
//    @Redirect(method = "updateEntities",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;)Z"),
//        slice = @Slice(
//            from = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;isInvalid()Z", ordinal = 0),
//            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z")
//        )
//    )
//    @Group(name = "isBlockLoadedTargetingUpdateEntities", min = 1)
//    private boolean impl$useTileActiveChunk(final World world, final BlockPos pos) {
//        return true; // If we got to here, we already have the method `bridge$shouldTick()` passing
//    }

    @Inject(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntity;isInvalid()Z",
            shift = At.Shift.BEFORE,
            by = 1
        ),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=Block entity being ticked"),
            to = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V")
        )
    )
    void impl$stopTileTickAndStartRemoval(final CallbackInfo callbackInfo) {
        // Sponge - inject here

        // if (tileentity.isInvalid()) {
        //     iterator.remove();
    }

    @Redirect(method = "updateEntities",
        at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
    private boolean impl$removeTileFromLoadedList(final List<?> loadedTileEntityList, final Object tile) {
        final boolean remove = loadedTileEntityList.remove(tile);
        final TileEntity tileEntity = (TileEntity) tile;
        // Sponge start - use cached chunk
        final Chunk activeChunk = (Chunk) ((ActiveChunkReferantBridge) tileEntity).bridge$getActiveChunk();
        if (activeChunk != null) {
            //this.getChunk(tileentity.getPos()).removeTileEntity(tileentity.getPos());
            //Forge: Bugfix: If we set the tile entity it immediately sets it in the chunk, so we could be desynced
            if (activeChunk.func_177424_a(tileEntity.func_174877_v(), Chunk.EnumCreateEntityType.CHECK) == tileEntity) {
                activeChunk.func_177425_e(tileEntity.func_174877_v());
            }
        }
        // Sponge end

        return remove;

    }

    @Redirect(method = "updateEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;isBlockLoaded(Lnet/minecraft/util/math/BlockPos;)Z"),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Ljava/util/Iterator;remove()V",
                remap = false),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/chunk/Chunk;removeTileEntity(Lnet/minecraft/util/math/BlockPos;)V")
        )
    )
    private boolean impl$ignoreisBlockLoaded(final World self, final BlockPos pos) {
        return false;
    }

    @Redirect(method = "updateEntities",
        at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"),
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lnet/minecraft/world/World;tickableTileEntities:Ljava/util/List;",
                ordinal = 1
            ),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;hasWorld()Z")
        )
    )
    boolean impl$stopTileRemovalTimingIfHasNext(final Iterator<TileEntity> iterator) {
        // We override to stop timing if it's been flipped
        return iterator.hasNext();
    }

    @Inject(method = "updateEntities", at = @At(value = "CONSTANT", args = "stringValue=pendingBlockEntities", shift = At.Shift.BEFORE, by = 2))
    void impl$startPendingBlockEntities(final CallbackInfo callbackInfo) {
        // this.processingLoadedTiles = false;
        // Sponge - Inject here, we start timings
        // this.profiler.endStartSection("pendingBlockEntities");
    }

    @Inject(method = "updateEntities", at = @At("TAIL"))
    void impl$endPendingTileEntities(final CallbackInfo ci) {
        // this.profiler.endSection();
        // this.profiler.endSection();
        // Sponge - here we turn off timings
    }

}
