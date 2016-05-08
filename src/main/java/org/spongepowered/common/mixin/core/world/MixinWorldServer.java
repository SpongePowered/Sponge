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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServer.ServerBlockEventList;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.cause.entity.spawn.WeatherSpawnCause;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.interfaces.IMixinNextTickListEntry;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends MixinWorld {

    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow private ServerBlockEventList[] blockEventQueue;
    @Shadow private int blockEventCacheIndex;

    @Shadow @Nullable public abstract net.minecraft.entity.Entity getEntityFromUuid(UUID uuid);

    @Inject(method = "createSpawnPosition(Lnet/minecraft/world/WorldSettings;)V", at = @At("HEAD"), cancellable = true)
    public void onCreateSpawnPosition(WorldSettings settings, CallbackInfo ci) {
        GeneratorType generatorType = (GeneratorType) settings.getTerrainType();
        if (generatorType != null && generatorType.equals(GeneratorTypes.THE_END)) {
            this.worldInfo.setSpawn(new BlockPos(55, 60, 0));
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At("HEAD"))
    public void beforeInit(CallbackInfoReturnable<World> cir) {
        super.init(); // Call the super (vanilla doesn't do this)
        updateWorldGenerator();
    }

    @Redirect(method = "updateBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlocks(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.hasTickingBlock() || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker
                .isChunkSpawnerRunning()) {
            block.randomTick(worldIn, pos, state, rand);
            return;
        }

        causeTracker.randomTickBlock(block, pos, state, rand);
    }

    @Redirect(method = "updateBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;isRainingAt(Lnet/minecraft/util/BlockPos;)Z"))
    private boolean onLightningCheck(WorldServer world, BlockPos blockPos) {
        if (world.isRainingAt(blockPos)) {
            Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) this,
                    VecHelper.toVector(blockPos).toDouble());
            SpawnCause cause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
            ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)),
                    EntityTypes.LIGHTNING, transform);
            SpongeImpl.postEvent(event);
            return !event.isCancelled();
        }
        return false;
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"), remap = false)
    public boolean onQueueScheduledBlockUpdate(Set<NextTickListEntry> pendingSet, Object obj) {
        final CauseTracker causeTracker = this.getCauseTracker();
        // If we don't have a notifier or the nextticklistentry has one, skip
        if (!causeTracker.hasNotifier() || ((IMixinNextTickListEntry) obj).hasSourceUser()) {
            pendingSet.add((NextTickListEntry) obj);
            return true;
        }

        IMixinNextTickListEntry nextTickListEntry = (IMixinNextTickListEntry) obj;
        User sourceUser = causeTracker.getCurrentNotifier().get();
        nextTickListEntry.setSourceUser(sourceUser);

        if (causeTracker.hasTickingTileEntity()) {
            nextTickListEntry.setCurrentTickTileEntity(causeTracker.getCurrentTickTileEntity().get());
        }
        if (causeTracker.hasTickingBlock()) {
            nextTickListEntry.setCurrentTickBlock(causeTracker.getCurrentTickBlock().get());
        }

        pendingSet.add((NextTickListEntry) obj);
        return true;
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlockTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker
                .isChunkSpawnerRunning()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }
        causeTracker.updateTickBlock(block, pos, state, rand);
    }

    // Before ticking pending updates, we need to check if we have any tracking info and set it accordingly
    @Inject(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;"
            + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onUpdateTick(boolean p_72955_1_, CallbackInfoReturnable<Boolean> cir, int i, Iterator<NextTickListEntry> iterator, NextTickListEntry nextticklistentry1, int k, IBlockState iblockstate) {
        final CauseTracker causeTracker = this.getCauseTracker();
        IMixinNextTickListEntry nextTickListEntry = (IMixinNextTickListEntry) nextticklistentry1;
        causeTracker.currentPendingBlockUpdate = nextTickListEntry;
    }

    // This ticks pending updates to blocks, Requires mixin for NextTickListEntry so we use the correct tracking
    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;"
            + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker
                .isChunkSpawnerRunning()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        causeTracker.updateTickBlock(block, pos, state, rand);
    }

    /**
     * @author bloodmc - May 5th, 2016
     *
     * @reason Used for tracking players when vanilla blocks such as pistons add an event to queue
     */
    @Overwrite
    public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
        BlockEventData blockeventdata = new BlockEventData(pos, blockIn, eventID, eventParam);

        for (BlockEventData blockeventdata1 : this.blockEventQueue[this.blockEventCacheIndex]) {
            if (blockeventdata1.equals(blockeventdata)) {
                return;
            }
        }

        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker.isChunkSpawnerRunning()) {
            this.blockEventQueue[this.blockEventCacheIndex].add(blockeventdata);
            return;
        }

        IMixinBlockEventData blockEvent = (IMixinBlockEventData) blockeventdata;
        Block block = blockEvent.getEventBlock();
        BlockPos targetPos = blockEvent.getEventBlockPosition();
        Optional<User> sourceUser = causeTracker.tryAndTrackActiveUser(targetPos, PlayerTracker.Type.NOTIFIER);

        if (!sourceUser.isPresent()) {
            if (causeTracker.hasTickingTileEntity()) {
                TileEntity tileEntity = causeTracker.getCurrentTickTileEntity().get();
                blockEvent.setCurrentTickTileEntity(tileEntity);
                if (!causeTracker.hasNotifier()) {
                    sourceUser = causeTracker.trackTargetBlockFromSource(tileEntity, ((net.minecraft.tileentity.TileEntity) tileEntity).getPos(), block, targetPos, PlayerTracker.Type.NOTIFIER);
                }
            } else if (causeTracker.hasTickingBlock()) {
                BlockSnapshot blockSnapshot = causeTracker.getCurrentTickBlock().get();
                blockEvent.setCurrentTickBlock(blockSnapshot);
                if (!causeTracker.hasNotifier()) {
                    sourceUser = causeTracker.trackTargetBlockFromSource(blockSnapshot, ((SpongeBlockSnapshot) blockSnapshot).getBlockPos(), block, targetPos, PlayerTracker.Type.NOTIFIER);
                }
            }
        }
        if (sourceUser.isPresent()) {
            blockEvent.setSourceUser(sourceUser.get());
        }

        this.blockEventQueue[this.blockEventCacheIndex].add(blockeventdata);
    }

    /**
     * @author bloodmc - May 5th, 2016
     *
     * @reason Used to set proper tracking info before block event is processed
     */
    @Overwrite
    private boolean fireBlockEvent(BlockEventData event) {
        final CauseTracker causeTracker = this.getCauseTracker();
        IBlockState currentState = ((WorldServer)(Object)this).getBlockState(event.getPosition());
        boolean result = false;
        if (currentState.getBlock() == event.getBlock()) {
            if (this.isRemote || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker.isChunkSpawnerRunning()) {
                return currentState.getBlock().onBlockEventReceived(((WorldServer)(Object) this), event.getPosition(), currentState, event.getEventID(), event.getEventParameter());
            }

            IMixinBlockEventData blockEvent = (IMixinBlockEventData) event;
            if (blockEvent.hasTickingBlock()) {
                causeTracker.setCurrentTickBlock(blockEvent.getCurrentTickBlock().get());
            } else if (!causeTracker.hasTickingTileEntity() && blockEvent.hasTickingTileEntity()) {
                causeTracker.setCurrentTickTileEntity(blockEvent.getCurrentTickTileEntity().get());
            }
            if (blockEvent.hasSourceUser()) {
                causeTracker.setCurrentNotifier(blockEvent.getSourceUser().get());
            }

            causeTracker.setProcessingVanillaBlockEvent(true);
            result = currentState.getBlock().onBlockEventReceived(((WorldServer)(Object) this), event.getPosition(), currentState, event.getEventID(), event.getEventParameter());
            causeTracker.setProcessingVanillaBlockEvent(false);
            causeTracker.setCurrentTickTileEntity(null);
            causeTracker.setCurrentTickBlock(null);
            causeTracker.setCurrentNotifier(null);
        }
        return result;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTickEnd(CallbackInfo ci) {
        // clear some things
        this.getCauseTracker().setCurrentNotifier(null);
    }

    @Override
    public Collection<ScheduledBlockUpdate> getScheduledUpdates(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        ImmutableList.Builder<ScheduledBlockUpdate> builder = ImmutableList.builder();
        for (NextTickListEntry sbu : this.pendingTickListEntriesTreeSet) {
            if (sbu.position.equals(position)) {
                builder.add((ScheduledBlockUpdate) sbu);
            }
        }
        return builder.build();
    }

    private NextTickListEntry tmpScheduledObj;

    @Redirect(method = "updateBlockTick(Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/Block;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/NextTickListEntry;setPriority(I)V"))
    private void onUpdateScheduledBlock(NextTickListEntry sbu, int priority) {
        this.onCreateScheduledBlockUpdate(sbu, priority);
    }

    @Redirect(method = "scheduleBlockUpdate(Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/Block;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/NextTickListEntry;setPriority(I)V"))
    private void onCreateScheduledBlockUpdate(NextTickListEntry sbu, int priority) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker.isChunkSpawnerRunning()) {
            this.tmpScheduledObj = sbu;
            return;
        }

        sbu.setPriority(priority);
        ((IMixinNextTickListEntry) sbu).setWorld((WorldServer) (Object) this);
        if (!((net.minecraft.world.World)(Object) this).isBlockLoaded(sbu.position)) {
            this.tmpScheduledObj = sbu;
            return;
        }

        this.tmpScheduledObj = sbu;
    }

    @Override
    public ScheduledBlockUpdate addScheduledUpdate(int x, int y, int z, int priority, int ticks) {
        BlockPos pos = new BlockPos(x, y, z);
        ((WorldServer) (Object) this).scheduleBlockUpdate(pos, getBlockState(pos).getBlock(), ticks, priority);
        ScheduledBlockUpdate sbu = (ScheduledBlockUpdate) this.tmpScheduledObj;
        this.tmpScheduledObj = null;
        return sbu;
    }

    @Override
    public void removeScheduledUpdate(int x, int y, int z, ScheduledBlockUpdate update) {
        // Note: Ignores position argument
        this.pendingTickListEntriesHashSet.remove(update);
        this.pendingTickListEntriesTreeSet.remove(update);
    }

    @Redirect(method = "updateAllPlayersSleepingFlag()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean isSpectatorOrIgnored(EntityPlayer entityPlayer) {
        // spectators are excluded from the sleep tally in vanilla
        // this redirect expands that check to include sleep-ignored players as well
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return ignore || entityPlayer.isSpectator();
    }

    @Redirect(method = "areAllPlayersAsleep()Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isPlayerFullyAsleep()Z"))
    public boolean isPlayerFullyAsleep(EntityPlayer entityPlayer) {
        // if isPlayerFullyAsleep() returns false areAllPlayerAsleep() breaks its loop and returns false
        // this redirect forces it to return true if the player is sleep-ignored even if they're not sleeping
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return ignore || entityPlayer.isPlayerFullyAsleep();
    }

    @Redirect(method = "areAllPlayersAsleep()Z", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean isSpectatorAndNotIgnored(EntityPlayer entityPlayer) {
        // if a player is marked as a spectator areAllPlayersAsleep() breaks its loop and returns false
        // this redirect forces it to return false if a player is sleep-ignored even if they're a spectator
        boolean ignore = entityPlayer instanceof Player && ((Player)entityPlayer).isSleepingIgnored();
        return !ignore && entityPlayer.isSpectator();
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        return Optional.ofNullable((Entity) this.getEntityFromUuid(uuid));
    }

    @Inject(method = "getSpawnListEntryForTypeAt", at = @At("HEAD"))
    private void onGetSpawnList(EnumCreatureType creatureType, BlockPos pos, CallbackInfoReturnable<BiomeGenBase.SpawnListEntry> callbackInfo) {
        StaticMixinHelper.gettingSpawnList = true;
    }

    @Inject(method = "newExplosion", at = @At(value = "HEAD"))
    public void onExplosionHead(net.minecraft.entity.Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking, CallbackInfoReturnable<net.minecraft.world.Explosion> cir) {
        this.processingExplosion = true;
    }

    @Inject(method = "newExplosion", at = @At(value = "RETURN"))
    public void onExplosionReturn(net.minecraft.entity.Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking, CallbackInfoReturnable<net.minecraft.world.Explosion> cir) {
        this.processingExplosion = false;
    }
}
