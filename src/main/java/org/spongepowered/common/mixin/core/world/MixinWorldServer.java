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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreboardSaveData;
import net.minecraft.util.BlockPos;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.interfaces.IMixinBlockUpdate;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinScoreboardSaveData;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

@NonnullByDefault
@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends MixinWorld {

    private Map<BlockPos, User> trackedBlockEvents = Maps.newHashMap();

    @Shadow public abstract void updateBlockTick(BlockPos p_175654_1_, Block p_175654_2_, int p_175654_3_, int p_175654_4_);
    @Shadow public abstract boolean fireBlockEvent(BlockEventData event);
    @Shadow private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;

    @Inject(method = "createSpawnPosition(Lnet/minecraft/world/WorldSettings;)V", at = @At("HEAD"), cancellable = true)
    public void onCreateSpawnPosition(WorldSettings settings, CallbackInfo ci) {
        GeneratorType generatorType = (GeneratorType) settings.getTerrainType();
        if (generatorType != null && generatorType.equals(GeneratorTypes.THE_END)) {
            this.worldInfo.setSpawn(new BlockPos(55, 60, 0));
            ci.cancel();
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/ScoreboardSaveData;setScoreboard(Lnet/minecraft/scoreboard/Scoreboard;)V", shift = At.Shift.BEFORE),
            locals = LocalCapture.CAPTURE_FAILHARD)
    public void onInit(CallbackInfoReturnable<World> cir, String s, VillageCollection villagecollection, ScoreboardSaveData scoreboardsavedata) {
        ((IMixinScoreboardSaveData) scoreboardsavedata).setSpongeScoreboard(this.spongeScoreboard);
        this.spongeScoreboard.getScoreboards().add(this.worldScoreboard);
    }

    @Surrogate
    public void onInit(CallbackInfoReturnable<World> cir, ScoreboardSaveData scoreboardsavedata) {
        ((IMixinScoreboardSaveData) scoreboardsavedata).setSpongeScoreboard(this.spongeScoreboard);
        this.spongeScoreboard.getScoreboards().add(this.worldScoreboard);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void onPostInit(CallbackInfoReturnable<World> ci) {
        net.minecraft.world.World world = ci.getReturnValue();
        if (!((IMixinWorldInfo) world.getWorldInfo()).getIsMod()) {
            // Run the world generator modifiers in the init method
            // (the "init" method, not the "<init>" constructor)
            IMixinWorld mixinWorld = (IMixinWorld) world;
            mixinWorld.updateWorldGenerator();
        }
    }

    @Redirect(method = "updateBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlocks(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null) {
            block.randomTick(worldIn, pos, state, rand);
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickBlock = createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, (IBlockAccess) this, pos), pos, 0);
        block.randomTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(NamedCause.source(this.currentTickBlock)));
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateBlockTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickBlock = createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, (IBlockAccess) this, pos), pos, 0);
        block.updateTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(NamedCause.source(this.currentTickBlock)));
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
    }
 
    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;updateTick(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;"
            + "Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"))
    public void onUpdateTick(Block block, net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (this.isRemote || this.currentTickBlock != null) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        this.processingCaptureCause = true;
        this.currentTickBlock = createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, (IBlockAccess) this, pos), pos, 0);
        block.updateTick(worldIn, pos, state, rand);
        handlePostTickCaptures(Cause.of(NamedCause.source(this.currentTickBlock)));
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
    }

    @Inject(method = "addBlockEvent", at = @At(value = "HEAD"))
    public void onAddBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam, CallbackInfo ci) {
        if (StaticMixinHelper.packetPlayer != null) {
            // Add player to block event position
            if (isBlockLoaded(pos)) {
                IMixinChunk spongeChunk = (IMixinChunk) getChunkFromBlockCoords(pos);
                Optional<User> owner = spongeChunk.getBlockOwner(pos);
                Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
                if (notifier.isPresent()) {
                    spongeChunk.addTrackedBlockPosition(blockIn, pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
                    this.trackedBlockEvents.put(pos, notifier.get());
                } else if (owner.isPresent()) {
                    spongeChunk.addTrackedBlockPosition(blockIn, pos, owner.get(), PlayerTracker.Type.NOTIFIER);
                    this.trackedBlockEvents.put(pos, owner.get());
                }
            }
        } else {
            BlockPos sourcePos = null;
            if (this.currentTickBlock != null) {
                sourcePos = VecHelper.toBlockPos(this.currentTickBlock.getPosition());
            } else if (this.currentTickOnBlockAdded != null) {
                sourcePos = VecHelper.toBlockPos(this.currentTickOnBlockAdded.getPosition());
            } else if (this.currentTickTileEntity != null) {
                sourcePos = ((net.minecraft.tileentity.TileEntity) this.currentTickTileEntity).getPos();
            }
            if (sourcePos != null && isBlockLoaded(sourcePos)) {
                IMixinChunk spongeChunk = (IMixinChunk) getChunkFromBlockCoords(sourcePos);
                Optional<User> owner = spongeChunk.getBlockOwner(sourcePos);
                Optional<User> notifier = spongeChunk.getBlockNotifier(sourcePos);
                if (notifier.isPresent()) {
                    spongeChunk.addTrackedBlockPosition(blockIn, pos, notifier.get(), PlayerTracker.Type.NOTIFIER);
                    this.trackedBlockEvents.put(pos, notifier.get());
                } else if (owner.isPresent()) {
                    spongeChunk.addTrackedBlockPosition(blockIn, pos, owner.get(), PlayerTracker.Type.NOTIFIER);
                    this.trackedBlockEvents.put(pos, owner.get());
                }
            }
        }
    }

    // special handling for Pistons since they use their own event system
    @Redirect(method = "sendQueuedBlockEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;fireBlockEvent(Lnet/minecraft/block/BlockEventData;)Z"))
    public boolean onFireBlockEvent(net.minecraft.world.WorldServer worldIn, BlockEventData event) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        this.processingCaptureCause = true;
        this.currentTickBlock = createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState, (IBlockAccess) this, event.getPosition()), event.getPosition(), 3);
        Cause cause = Cause.of(NamedCause.source(this.currentTickBlock));
        if (this.trackedBlockEvents.get(event.getPosition()) != null) {
            User user = this.trackedBlockEvents.get(event.getPosition());
            cause = cause.with(NamedCause.notifier(user));
            StaticMixinHelper.blockEventUser = user;
        }
        boolean result = fireBlockEvent(event);
        this.handlePostTickCaptures(cause);
        StaticMixinHelper.blockEventUser = null;
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
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
        sbu.setPriority(priority);
        ((IMixinBlockUpdate) sbu).setWorld((WorldServer) (Object) this);
        // Pistons, Beacons, Notes, Comparators etc. schedule block updates so we must track these positions
        if (this.currentTickBlock != null) {
            BlockPos pos = VecHelper.toBlockPos(this.currentTickBlock.getPosition());
            SpongeHooks.tryToTrackBlock((net.minecraft.world.World)(Object) this, this.currentTickBlock, pos, sbu.getBlock(), sbu.position, PlayerTracker.Type.NOTIFIER);
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
}
