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
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import org.spongepowered.api.block.ScheduledBlockUpdate;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.interfaces.IMixinBlockUpdate;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends MixinWorld {

    private Map<BlockPos, User> trackedBlockEvents = Maps.newHashMap();

    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;

    @Shadow public abstract void updateBlockTick(BlockPos p_175654_1_, Block p_175654_2_, int p_175654_3_, int p_175654_4_);
    @Shadow public abstract boolean fireBlockEvent(BlockEventData event);
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

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlockTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.hasTickingBlock() || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker
                .isChunkSpawnerRunning()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }
        causeTracker.updateTickBlock(block, pos, state, rand);
    }

    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;"
            + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (this.isRemote || causeTracker.hasTickingBlock() || causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker
                .isChunkSpawnerRunning()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }
        causeTracker.updateTickBlock(block, pos, state, rand);
    }

    @Inject(method = "addBlockEvent", at = @At(value = "HEAD"))
    public void onAddBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam, CallbackInfo ci) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker.isChunkSpawnerRunning()) {
            return;
        }

        if (StaticMixinHelper.packetPlayer != null) {
            // Add player to block event position
            if (isBlockLoaded(pos)) {
                IMixinChunk spongeChunk = (IMixinChunk) getChunkFromBlockCoords(pos);
                Optional<User> owner = spongeChunk.getBlockOwner(pos);
                Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
                userTracking(blockIn, pos, notifier, owner, spongeChunk);
            }
        } else {
            BlockPos sourcePos = null;
            if (causeTracker.hasTickingBlock()) {
                sourcePos = VecHelper.toBlockPos(causeTracker.getCurrentTickBlock().get().getPosition());
            } else if (causeTracker.hasTickingTileEntity()) {
                sourcePos = ((net.minecraft.tileentity.TileEntity) causeTracker.getCurrentTickTileEntity().get()).getPos();
            }
            if (sourcePos != null && isBlockLoaded(sourcePos)) {
                IMixinChunk spongeChunk = (IMixinChunk) getChunkFromBlockCoords(sourcePos);
                Optional<User> owner = spongeChunk.getBlockOwner(sourcePos);
                Optional<User> notifier = spongeChunk.getBlockNotifier(sourcePos);
                userTracking(blockIn, pos, notifier, owner, spongeChunk);
            }
        }
    }

    private void userTracking(Block block, BlockPos pos, Optional<User> notifier, Optional<User> owner, IMixinChunk spongeChunk) {
        if (notifier.isPresent()) {
            spongeChunk.addTrackedBlockPosition(block, pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
            this.trackedBlockEvents.put(pos, notifier.get());
        } else if (owner.isPresent()) {
            spongeChunk.addTrackedBlockPosition(block, pos, owner.get(), PlayerTracker.Type.NOTIFIER);
            this.trackedBlockEvents.put(pos, owner.get());
        }
    }

    // special handling for Pistons since they use their own event system
    @Redirect(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;fireBlockEvent(Lnet/minecraft/block/BlockEventData;)Z"))
    public boolean onFireBlockEvent(net.minecraft.world.WorldServer worldIn, BlockEventData event) {
        final CauseTracker causeTracker = this.getCauseTracker();
        if (causeTracker.isCapturingTerrainGen() || causeTracker.isWorldSpawnerRunning() || causeTracker.isChunkSpawnerRunning()) {
            return fireBlockEvent(event);
        }

        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        causeTracker.setProcessingCaptureCause(true);
        causeTracker.setCurrentTickBlock(createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState, (IBlockAccess) this, event.getPosition()), event.getPosition(), 3));
        Cause cause = Cause.of(NamedCause.source(causeTracker.getCurrentTickBlock().get()));
        if (this.trackedBlockEvents.get(event.getPosition()) != null) {
            User user = this.trackedBlockEvents.get(event.getPosition());
            cause = cause.with(NamedCause.notifier(user));
            StaticMixinHelper.blockEventUser = user;
        }
        boolean result = fireBlockEvent(event);
        causeTracker.handlePostTickCaptures(cause);
        StaticMixinHelper.blockEventUser = null;
        causeTracker.setCurrentTickBlock(null);
        causeTracker.setProcessingCaptureCause(false);
        this.trackedBlockEvents.remove(event.getPosition());
        return result;
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
        ((IMixinBlockUpdate) sbu).setWorld((WorldServer) (Object) this);
        if (!((net.minecraft.world.World)(Object) this).isBlockLoaded(sbu.position)) {
            this.tmpScheduledObj = sbu;
            return;
        }

        // Pistons, Beacons, Notes, Comparators etc. schedule block updates so we must track these positions
        if (causeTracker.hasTickingBlock()) {
            BlockPos pos = VecHelper.toBlockPos(causeTracker.getCurrentTickBlock().get().getPosition());
            SpongeHooks.tryToTrackBlock((net.minecraft.world.World)(Object) this, causeTracker.getCurrentTickBlock().get(), pos, sbu.getBlock(), sbu.position, PlayerTracker.Type.NOTIFIER);
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
