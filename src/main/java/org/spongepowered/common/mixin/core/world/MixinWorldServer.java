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

import co.aikar.timings.TimingHistory;
import co.aikar.timings.WorldTimingsHandler;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServer.ServerBlockEventList;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkProviderServer;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.ScheduledBlockUpdate;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.sound.SoundType;
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
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.PortalAgentTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.storage.WorldStorage;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.api.world.weather.Weathers;
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
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.effect.particle.SpongeParticleEffect;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinNextTickListEntry;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerManager;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.mixin.plugin.interfaces.IModData;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(WorldServer.class)
public abstract class MixinWorldServer extends MixinWorld implements IMixinWorldServer {

    private WorldServer mcWorldServer = (WorldServer)(Object) this;
    private static final String PROFILER_SS = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V";
    private static final String PROFILER_ESS = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V";

    @Shadow @Final private Set<NextTickListEntry> pendingTickListEntriesHashSet;
    @Shadow @Final private TreeSet<NextTickListEntry> pendingTickListEntriesTreeSet;
    @Shadow private Teleporter worldTeleporter;
    @Shadow private ServerBlockEventList[] blockEventQueue;
    @Shadow private int blockEventCacheIndex;
    @Shadow private int updateEntityTick;
    @Shadow @Nullable public abstract net.minecraft.entity.Entity getEntityFromUuid(UUID uuid);
    @Shadow public abstract void resetUpdateEntityTick();
    @Shadow public abstract PlayerManager getPlayerManager();
    @Shadow protected abstract BlockPos adjustPosToNearbyEntity(BlockPos pos);

    protected long weatherStartTime;
    protected Weather prevWeather;
    private int chunkGCTickCount = 0;
    private int chunkGCLoadThreshold = 0;
    private int chunkGCTickInterval = 600;
    private long chunkUnloadDelay = 30000;
    private boolean isCapturingBlocks = false;
    private boolean weatherThunderEnabled = true;
    private boolean weatherIceAndSnowEnabled = true;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(CallbackInfo ci) {
        this.prevWeather = getWeather();
        this.weatherStartTime = this.worldInfo.getWorldTotalTime();
        PortalAgentType portalAgentType = ((WorldProperties) this.worldInfo).getPortalAgentType();
        if (!portalAgentType.equals(PortalAgentTypes.DEFAULT)) {
            try {
                this.worldTeleporter = (Teleporter) portalAgentType.getPortalAgentClass().getConstructor(new Class<?>[] {WorldServer.class})
                        .newInstance(new Object[] {this});
            } catch (Exception e) {
                SpongeImpl.getLogger().log(Level.ERROR, "Could not create PortalAgent of type " + portalAgentType.getId()
                        + " for world " + this.getName() + ": " + e.getMessage() + ". Falling back to default...");
            }
        }

        this.timings = new WorldTimingsHandler((net.minecraft.world.World) (Object) this);
        this.causeTracker = new CauseTracker((net.minecraft.world.World) (Object) this);
        updateWorldGenerator();
        this.chunkGCLoadThreshold = this.getActiveConfig().getConfig().getWorld().getChunkLoadThreadhold();
        this.chunkGCTickInterval = this.getActiveConfig().getConfig().getWorld().getTickInterval();
        this.weatherIceAndSnowEnabled = this.getActiveConfig().getConfig().getWorld().getWeatherIceAndSnow();
        this.weatherThunderEnabled = this.getActiveConfig().getConfig().getWorld().getWeatherThunder();
    }

    @Inject(method = "createSpawnPosition(Lnet/minecraft/world/WorldSettings;)V", at = @At("HEAD"), cancellable = true)
    public void onCreateSpawnPosition(WorldSettings settings, CallbackInfo ci) {
        GeneratorType generatorType = (GeneratorType) settings.getTerrainType();
        if (generatorType != null && generatorType.equals(GeneratorTypes.THE_END)) {
            this.worldInfo.setSpawn(new BlockPos(55, 60, 0));
            ci.cancel();
        }
    }

    /**
     * @author blood - July 1st, 2016
     *
     * @reason Added chunk and block tick optimizations.
     */
    @Overwrite
    protected void updateBlocks()
    {
        super.updateBlocks();

        if (this.worldInfo.getTerrainType() == WorldType.DEBUG_WORLD)
        {
            for (ChunkCoordIntPair chunkcoordintpair1 : this.activeChunkSet)
            {
                this.getChunkFromChunkCoords(chunkcoordintpair1.chunkXPos, chunkcoordintpair1.chunkZPos).func_150804_b(false);
            }
        }
        else
        {
            // Sponge start - unused
            //int i = 0;
            //int j = 0;

            final CauseTracker causeTracker = this.getCauseTracker();
            boolean captureBlocks = causeTracker.isCapturingBlocks();
            Iterator<ChunkCoordIntPair> iterator = this.activeChunkSet.iterator();
            while (iterator.hasNext())
            {
                // Sponge end
                ChunkCoordIntPair chunkcoordintpair = iterator.next();
                int k = chunkcoordintpair.chunkXPos * 16;
                int l = chunkcoordintpair.chunkZPos * 16;
                this.theProfiler.startSection("getChunk");
                // Sponge start - if the active chunk is no longer loaded, remove and cancel
                //Chunk chunk = this.getChunkFromChunkCoords(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
                final Chunk chunk = ((IMixinChunkProviderServer) this.getChunkProvider()).getChunkIfLoaded(chunkcoordintpair.chunkXPos, chunkcoordintpair.chunkZPos);
                if (chunk == null) {
                    iterator.remove();
                    continue;
                }
                // Sponge end
                this.timings.updateBlocksCheckNextLight.startTiming();
                this.playMoodSoundAndCheckLight(k, l, chunk);
                this.timings.updateBlocksCheckNextLight.stopTiming();
                this.theProfiler.endStartSection("tickChunk");
                this.timings.updateBlocksChunkTick.startTiming();
                chunk.func_150804_b(false);
                this.timings.updateBlocksChunkTick.stopTiming();
                // Sponge start - if surrounding neighbors are not loaded, skip
                if (!((IMixinChunk) chunk).areNeighborsLoaded()) {
                    continue;
                }
                // Sponge end
                this.theProfiler.endStartSection("thunder");
                this.timings.updateBlocksThunder.startTiming();
                causeTracker.setCaptureBlocks(true);
                if (this.weatherThunderEnabled && SpongeImplHooks.canDoLightning(this.provider, chunk) && this.rand.nextInt(100000) == 0 && this.mcWorldServer.isRaining() && this.mcWorldServer.isThundering())
                {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int i1 = this.updateLCG >> 2;
                    BlockPos blockpos = this.adjustPosToNearbyEntity(new BlockPos(k + (i1 & 15), 0, l + (i1 >> 8 & 15)));

                    // Sponge start
                    if (this.mcWorldServer.isRainingAt(blockpos)) {
                        Transform<org.spongepowered.api.world.World> transform = new Transform<>((org.spongepowered.api.world.World) this,
                                VecHelper.toVector(blockpos).toDouble());
                        SpawnCause cause = WeatherSpawnCause.builder().weather(this.getWeather()).type(SpawnTypes.WEATHER).build();
                        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)),
                                EntityTypes.LIGHTNING, transform);
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            this.addWeatherEffect(new EntityLightningBolt(this.mcWorldServer, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()));
                        }
                    }
                    // Sponge end
                }

                this.timings.updateBlocksThunder.stopTiming();
                this.timings.updateBlocksIceAndSnow.startTiming();
                this.theProfiler.endStartSection("iceandsnow");

                if (this.weatherIceAndSnowEnabled && SpongeImplHooks.canDoRainSnowIce(this.provider, chunk)  && this.rand.nextInt(16) == 0)
                {
                    this.updateLCG = this.updateLCG * 3 + 1013904223;
                    int k2 = this.updateLCG >> 2;
                    BlockPos blockpos2 = this.mcWorldServer.getPrecipitationHeight(new BlockPos(k + (k2 & 15), 0, l + (k2 >> 8 & 15)));
                    BlockPos blockpos1 = blockpos2.down();

                    if (this.mcWorldServer.canBlockFreezeNoWater(blockpos1))
                    {
                        this.mcWorldServer.setBlockState(blockpos1, Blocks.ice.getDefaultState());
                    }

                    if (this.mcWorldServer.isRaining() && this.mcWorldServer.canSnowAt(blockpos2, true))
                    {
                        this.mcWorldServer.setBlockState(blockpos2, Blocks.snow_layer.getDefaultState());
                    }

                    if (this.mcWorldServer.isRaining() && this.mcWorldServer.getBiomeGenForCoords(blockpos1).canRain())
                    {
                        this.getBlockState(blockpos1).getBlock().fillWithRain(this.mcWorldServer, blockpos1);
                    }
                }

                causeTracker.setCaptureBlocks(captureBlocks);
                this.timings.updateBlocksIceAndSnow.stopTiming();
                this.timings.updateBlocksRandomTick.startTiming();
                this.theProfiler.endStartSection("tickBlocks");
                int l2 = this.mcWorldServer.getGameRules().getInt("randomTickSpeed");

                if (l2 > 0)
                {
                    for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray())
                    {
                        if (extendedblockstorage != null && extendedblockstorage.getNeedsRandomTick())
                        {
                            for (int j1 = 0; j1 < l2; ++j1)
                            {
                                this.updateLCG = this.updateLCG * 3 + 1013904223;
                                int k1 = this.updateLCG >> 2;
                                int l1 = k1 & 15;
                                int i2 = k1 >> 8 & 15;
                                int j2 = k1 >> 16 & 15;
                                // ++j; // Sponge - unused
                                IBlockState iblockstate = extendedblockstorage.get(l1, j2, i2);
                                Block block = iblockstate.getBlock();

                                if (block.getTickRandomly())
                                {
                                    // Sponge start - capture random tick
                                    // ++i;
                                    BlockPos pos = new BlockPos(l1 + k, j2 + extendedblockstorage.getYLocation(), i2 + l);
                                    IMixinBlock spongeBlock = (IMixinBlock) block;
                                    spongeBlock.getTimingsHandler().startTiming();
                                    if (causeTracker.hasTickingBlock() || causeTracker.isIgnoringCaptures()) {
                                        block.randomTick(this.mcWorldServer, pos, iblockstate, this.rand);
                                    } else {
                                        causeTracker.randomTickBlock(block, pos, iblockstate, this.rand);
                                    }
                                    spongeBlock.getTimingsHandler().stopTiming();
                                    // Sponge end
                                }
                            }
                        }
                    }
                }

                this.timings.updateBlocksRandomTick.stopTiming();
                this.theProfiler.endSection();
            }
        }
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target= "Lnet/minecraft/world/WorldServer;isAreaLoaded(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/BlockPos;)Z"))
    public boolean onBlockTickIsAreaLoaded(WorldServer worldIn, BlockPos fromPos, BlockPos toPos) {
        int posX = fromPos.getX() + 8;
        int posZ = fromPos.getZ() + 8;
        // Forge passes the same block position for forced chunks
        if (fromPos.equals(toPos)) {
            posX = fromPos.getX();
            posZ = fromPos.getZ();
        }
        final Chunk chunk = ((IMixinChunkProviderServer) this.getChunkProvider()).getChunkIfLoaded(posX >> 4, posZ >> 4);
        if (chunk == null || !((IMixinChunk) chunk).areNeighborsLoaded()) {
            return false;
        }

        return true;
    }

    @Redirect(method = "updateBlockTick", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"), remap = false)
    public boolean onQueueScheduledBlockUpdate(Set<NextTickListEntry> pendingSet, Object obj) {
        final CauseTracker causeTracker = this.getCauseTracker();
        // If we don't have a notifier or the nextticklistentry has one, skip
        if (causeTracker.isIgnoringCaptures() || !causeTracker.hasNotifier() || ((IMixinNextTickListEntry) obj).hasSourceUser()) {
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
        if (causeTracker.isIgnoringCaptures()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }
        causeTracker.updateTickBlock(block, pos, state, rand);
    }

    @Redirect(method = "tickUpdates", at = @At(value = "INVOKE", target= "Lnet/minecraft/world/WorldServer;isAreaLoaded(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/BlockPos;)Z"))
    public boolean onTickUpdateIsAreaLoaded(WorldServer worldIn, BlockPos fromPos, BlockPos toPos) {
        int posX = fromPos.getX() + 8;
        int posZ = fromPos.getZ() + 8;
        // Forge passes the same block position for forced chunks
        if (fromPos.equals(toPos)) {
            posX = fromPos.getX();
            posZ = fromPos.getZ();
        }
        final Chunk chunk = ((IMixinChunkProviderServer) this.getChunkProvider()).getChunkIfLoaded(posX >> 4, posZ >> 4);
        if (chunk == null || !((IMixinChunk) chunk).areNeighborsLoaded()) {
            return false;
        }

        return true;
    }

    @Inject(method = "tickUpdates", at = @At(value = "INVOKE_STRING", target = PROFILER_SS, args = "ldc=cleaning"))
    private void onTickUpdatesCleanup(boolean flag, CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksCleanup.startTiming();
    }

    @Inject(method = "tickUpdates", at = @At(value = "INVOKE_STRING", target = PROFILER_SS, args = "ldc=ticking"))
    private void onTickUpdatesTickingStart(boolean flag, CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksCleanup.stopTiming();
        this.timings.scheduledBlocksTicking.startTiming();
    }

    @Inject(method = "tickUpdates", at = @At("RETURN"))
    private void onTickUpdatesTickingEnd(CallbackInfoReturnable<Boolean> cir) {
        this.timings.scheduledBlocksTicking.stopTiming();
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
        if (causeTracker.isIgnoringCaptures()) {
            block.updateTick(worldIn, pos, state, rand);
            return;
        }

        IMixinBlock spongeBlock = (IMixinBlock) block;
        spongeBlock.getTimingsHandler().startTiming();
        causeTracker.updateTickBlock(block, pos, state, rand);
        spongeBlock.getTimingsHandler().stopTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=tickPending") )
    private void onBeginTickBlockUpdate(CallbackInfo ci) {
        this.timings.scheduledBlocks.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=tickBlocks") )
    private void onAfterTickBlockUpdate(CallbackInfo ci) {
        this.timings.scheduledBlocks.stopTiming();
        this.timings.updateBlocks.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=chunkMap") )
    private void onBeginUpdateBlocks(CallbackInfo ci) {
        this.timings.updateBlocks.stopTiming();
        this.timings.doChunkMap.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=village") )
    private void onBeginUpdateVillage(CallbackInfo ci) {
        this.timings.doChunkMap.stopTiming();
        this.timings.doVillages.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE_STRING", target = PROFILER_ESS, args = "ldc=portalForcer") )
    private void onBeginUpdatePortal(CallbackInfo ci) {
        this.timings.doVillages.stopTiming();
        this.timings.doPortalForcer.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V") )
    private void onEndUpdatePortal(CallbackInfo ci) {
        this.timings.doPortalForcer.stopTiming();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onTickEnd(CallbackInfo ci) {
        // Make sure we clear our current notifier
        this.getCauseTracker().setCurrentNotifier(null);
        // Clean up any leaked chunks
        this.doChunkGC();
    }

    // Chunk GC
    private void doChunkGC() {
        this.chunkGCTickCount++;

        ChunkProviderServer chunkProviderServer = (ChunkProviderServer) this.getChunkProvider();
        int chunkLoadCount = this.getChunkProvider().getLoadedChunkCount();
        if (chunkLoadCount >= this.chunkGCLoadThreshold && this.chunkGCLoadThreshold > 0) {
            chunkLoadCount = 0;
        } else if (this.chunkGCTickCount >= this.chunkGCTickInterval && this.chunkGCTickInterval > 0) {
            this.chunkGCTickCount = 0;
        } else {
            return;
        }

        long now = System.currentTimeMillis();
        long unloadAfter = this.chunkUnloadDelay;
        for (Chunk chunk : chunkProviderServer.loadedChunks) {
            IMixinChunk spongeChunk = (IMixinChunk) chunk;
            if (spongeChunk.getScheduledForUnload() != null && (now - spongeChunk.getScheduledForUnload()) > unloadAfter) {
                spongeChunk.setScheduledForUnload(null);
            }
            // If a player is currently using the chunk, skip it
            if (spongeChunk.getScheduledForUnload() != null || ((IMixinPlayerManager) this.getPlayerManager()).isChunkInUse(chunk.xPosition, chunk.zPosition)) {
                continue;
            }

            // Queue chunk for unload
            chunkProviderServer.dropChunk(chunk.xPosition, chunk.zPosition);
            SpongeHooks.logChunkGCQueueUnload(chunkProviderServer.worldObj, chunk);
        }
    }

    @Inject(method = "saveAllChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/ChunkProviderServer;func_152380_a()Ljava/util/List;"), cancellable = true)
    public void onSaveAllChunks(boolean saveAllChunks, IProgressUpdate progressCallback, CallbackInfo ci) {
        // The chunk GC handles all queuing for chunk unloads so we cancel here to avoid it during a save.
        if (this.chunkGCTickInterval > 0) {
            ci.cancel();
        }
    }

    /**
     * @author blood - May 26th, 2016
     *
     * @reason Rewritten due to the amount of injections required for both
     *     timing and capturing.
     */
    @Overwrite
    public void updateEntities()
    {
        if (this.playerEntities.isEmpty()) {
            if (this.updateEntityTick++ >= 1200) {
                return;
            }
        } else {
            this.resetUpdateEntityTick();
        }

        this.theProfiler.startSection("entities");
        this.theProfiler.startSection("global");
        // Sponge start
        this.timings.entityTick.startTiming();
        co.aikar.timings.TimingHistory.entityTicks += this.loadedEntityList.size();
        // Sponge end
        for (int i = 0; i < this.weatherEffects.size(); ++i)
        {
            net.minecraft.entity.Entity entity = this.weatherEffects.get(i);
            IMixinEntity spongeEntity = (IMixinEntity) entity; // Sponge
            try
            {
                ++entity.ticksExisted;
                // Sponge start - handle tracking and timings
                boolean captureBlocks = this.causeTracker.isCapturingBlocks();
                this.causeTracker.setCaptureBlocks(true);
                this.causeTracker.preTrackEntity(spongeEntity);
                spongeEntity.getTimingsHandler().startTiming();
                entity.onUpdate();
                spongeEntity.getTimingsHandler().stopTiming();
                updateRotation(entity);
                SpongeCommonEventFactory.handleEntityMovement(entity);
                this.causeTracker.postTrackEntity();
                this.causeTracker.setCaptureBlocks(captureBlocks);
                // Sponge end
            }
            catch (Throwable throwable2)
            {
                spongeEntity.getTimingsHandler().stopTiming(); // Sponge
                CrashReport crashreport = CrashReport.makeCrashReport(throwable2, "Ticking entity");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Entity being ticked");

                if (entity == null)
                {
                    crashreportcategory.addCrashSection("Entity", "~~NULL~~");
                }
                else
                {
                    entity.addEntityCrashInfo(crashreportcategory);
                }

                throw new ReportedException(crashreport);
            }

            if (entity.isDead)
            {
                this.weatherEffects.remove(i--);
            }
        }
        // Sponge start
        this.timings.entityTick.stopTiming();
        this.timings.entityRemoval.startTiming();
        // Sponge end
        this.theProfiler.endStartSection("remove");
        this.loadedEntityList.removeAll(this.unloadedEntityList);

        for (int k = 0; k < this.unloadedEntityList.size(); ++k)
        {
            net.minecraft.entity.Entity entity1 = this.unloadedEntityList.get(k);
            int j = entity1.chunkCoordX;
            int l1 = entity1.chunkCoordZ;

            if (entity1.addedToChunk && this.isChunkLoaded(j, l1, true))
            {
                this.getChunkFromChunkCoords(j, l1).removeEntity(entity1);
            }
        }

        for (int l = 0; l < this.unloadedEntityList.size(); ++l)
        {
            this.onEntityRemoved(this.unloadedEntityList.get(l));
        }

        this.unloadedEntityList.clear();
        this.theProfiler.endStartSection("regular");
        this.timings.entityRemoval.stopTiming(); // Sponge

        for (int i1 = 0; i1 < this.loadedEntityList.size(); ++i1)
        {
            net.minecraft.entity.Entity entity2 = this.loadedEntityList.get(i1);
            IMixinEntity spongeEntity = (IMixinEntity) entity2; // Sponge
            if (entity2.ridingEntity != null)
            {
                if (!entity2.ridingEntity.isDead && entity2.ridingEntity.riddenByEntity == entity2)
                {
                    continue;
                }

                entity2.ridingEntity.riddenByEntity = null;
                entity2.ridingEntity = null;
            }

            this.theProfiler.startSection("tick");
            this.timings.entityTick.startTiming();
            if (!entity2.isDead)
            {
                try
                {
                    spongeEntity.getTimingsHandler().startTiming(); // Sponge
                    this.updateEntity(entity2);
                    spongeEntity.getTimingsHandler().stopTiming(); // Sponge
                }
                catch (Throwable throwable1)
                {
                    spongeEntity.getTimingsHandler().stopTiming(); // Sponge
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable1, "Ticking entity");
                    CrashReportCategory crashreportcategory2 = crashreport1.makeCategory("Entity being ticked");
                    entity2.addEntityCrashInfo(crashreportcategory2);
                    throw new ReportedException(crashreport1);
                }
            }
            this.timings.entityTick.stopTiming(); // Sponge
            this.theProfiler.endSection();
            this.theProfiler.startSection("remove");
            this.timings.entityRemoval.startTiming(); // Sponge
            if (entity2.isDead)
            {
                int k1 = entity2.chunkCoordX;
                int i2 = entity2.chunkCoordZ;

                if (entity2.addedToChunk && this.isChunkLoaded(k1, i2, true))
                {
                    this.getChunkFromChunkCoords(k1, i2).removeEntity(entity2);
                }

                this.loadedEntityList.remove(i1--);
                this.onEntityRemoved(entity2);
            }

            this.timings.entityRemoval.stopTiming();
            this.theProfiler.endSection();
        }

        this.theProfiler.endStartSection("blockEntities");
        // Sponge start - moved up to clean up tile entities before ticking
        this.timings.tileEntityRemoval.startTiming();
        if (!this.tileEntitiesToBeRemoved.isEmpty())
        {
            this.tickableTileEntities.removeAll(this.tileEntitiesToBeRemoved);
            this.loadedTileEntityList.removeAll(this.tileEntitiesToBeRemoved);
            this.tileEntitiesToBeRemoved.clear();
        }
        this.timings.tileEntityRemoval.stopTiming();
        // Sponge end

        this.processingLoadedTiles = true;
        Iterator<net.minecraft.tileentity.TileEntity> iterator = this.tickableTileEntities.iterator();

        while (iterator.hasNext())
        {
            this.timings.tileEntityTick.startTiming(); // Sponge
            net.minecraft.tileentity.TileEntity tileentity = iterator.next();
            IMixinTileEntity spongeTile = (IMixinTileEntity) tileentity;
            if (!tileentity.isInvalid() && tileentity.hasWorldObj())
            {
                BlockPos blockpos = tileentity.getPos();

                if (this.isBlockLoaded(blockpos) && this.worldBorder.contains(blockpos))
                {
                    try
                    {
                        // Sponge start - handle captures and timings
                        spongeTile.getTimingsHandler().startTiming();
                        boolean captureBlocks = this.causeTracker.isCapturingBlocks();
                        this.causeTracker.setCaptureBlocks(true);
                        this.causeTracker.preTrackTileEntity((TileEntity) tileentity);
                        ((ITickable)tileentity).update();
                        this.causeTracker.postTrackTileEntity();
                        this.causeTracker.setCaptureBlocks(captureBlocks);
                        spongeTile.getTimingsHandler().stopTiming();
                        // Sponge end
                    }
                    catch (Throwable throwable)
                    {
                        spongeTile.getTimingsHandler().stopTiming(); // Sponge
                        CrashReport crashreport2 = CrashReport.makeCrashReport(throwable, "Ticking block entity");
                        CrashReportCategory crashreportcategory1 = crashreport2.makeCategory("Block entity being ticked");
                        tileentity.addInfoToCrashReport(crashreportcategory1);
                        throw new ReportedException(crashreport2);
                    }
                }
            }
            // Sponge start
            this.timings.tileEntityTick.stopTiming();
            this.timings.tileEntityRemoval.startTiming();
            // Sponge end
            if (tileentity.isInvalid())
            {
                iterator.remove();
                this.loadedTileEntityList.remove(tileentity);

                if (this.isBlockLoaded(tileentity.getPos()))
                {
                    this.getChunkFromBlockCoords(tileentity.getPos()).removeTileEntity(tileentity.getPos());
                }
            }
            this.timings.tileEntityRemoval.stopTiming(); // Sponge
        }

        this.processingLoadedTiles = false;
        this.timings.tileEntityPending.startTiming(); // Sponge
        this.theProfiler.endStartSection("pendingBlockEntities");

        if (!this.addedTileEntityList.isEmpty())
        {
            for (int j1 = 0; j1 < this.addedTileEntityList.size(); ++j1)
            {
                net.minecraft.tileentity.TileEntity tileentity1 = this.addedTileEntityList.get(j1);

                if (!tileentity1.isInvalid())
                {
                    if (!this.loadedTileEntityList.contains(tileentity1))
                    {
                        this.addTileEntity(tileentity1);
                    }

                    if (this.isBlockLoaded(tileentity1.getPos()))
                    {
                        this.getChunkFromBlockCoords(tileentity1.getPos()).addTileEntity(tileentity1.getPos(), tileentity1);
                    }

                    this.markBlockForUpdate(tileentity1.getPos());
                }
            }

            this.addedTileEntityList.clear();
        }
        this.timings.tileEntityPending.stopTiming(); // Sponge
        TimingHistory.tileEntityTicks += this.loadedTileEntityList.size(); // Sponge
        this.theProfiler.endSection();
        this.theProfiler.endSection();
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
        if (causeTracker.isIgnoringCaptures()) {
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
        this.isCapturingBlocks = this.causeTracker.isCapturingBlocks();
        this.causeTracker.setCaptureBlocks(false);
    }

    @Inject(method = "newExplosion", at = @At(value = "RETURN"))
    public void onExplosionReturn(net.minecraft.entity.Entity entityIn, double x, double y, double z, float strength, boolean isFlaming, boolean isSmoking, CallbackInfoReturnable<net.minecraft.world.Explosion> cir) {
        this.processingExplosion = false;
        this.causeTracker.setCaptureBlocks(this.isCapturingBlocks);
    }

    @Override
    public WorldStorage getWorldStorage() {
        return (WorldStorage) ((WorldServer) (Object) this).theChunkProviderServer;
    }

    @Override
    public PortalAgent getPortalAgent() {
        return (PortalAgent) this.worldTeleporter;
    }

    /**************************** EFFECT ****************************************/

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
        this.playSoundEffect(position.getX(), position.getY(), position.getZ(), sound.getId(), (float) Math.max(minVolume, volume), (float) pitch);
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

        List<Packet<?>> packets = SpongeParticleHelper.toPackets((SpongeParticleEffect) particleEffect, position);

        if (!packets.isEmpty()) {
            ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();

            double x = position.getX();
            double y = position.getY();
            double z = position.getZ();

            for (Packet<?> packet : packets) {
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
    public long getRunningDuration() {
        return this.worldInfo.getWorldTotalTime() - this.weatherStartTime;
    }

    @Override
    public void setWeather(Weather weather) {
        if (weather.equals(Weathers.CLEAR)) {
            this.setWeather(weather, (300 + this.rand.nextInt(600)) * 20);
        } else {
            this.setWeather(weather, 0);
        }
    }

    @Override
    public void setWeather(Weather weather, long duration) {
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

    @Inject(method = "updateWeather", at = @At(value = "RETURN"))
    public void onUpdateWeatherReturn(CallbackInfo ci) {
        Weather weather = getWeather();
        int duration = (int) getRemainingDuration();
        if (this.prevWeather != weather && duration > 0) {
            ChangeWorldWeatherEvent event = SpongeEventFactory.createChangeWorldWeatherEvent(Cause.of(NamedCause.source(this)), duration, duration,
                    weather, weather, this.prevWeather, this);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                this.setWeather(this.prevWeather);
            } else {
                this.setWeather(event.getWeather(), event.getDuration());
                this.prevWeather = event.getWeather();
                this.weatherStartTime = this.worldInfo.getWorldTotalTime();
            }
        }
    }

    @Override
    public long getWeatherStartTime() {
        return this.weatherStartTime;
    }

    @Override
    public void setWeatherStartTime(long weatherStartTime) {
        this.weatherStartTime = weatherStartTime;
    }

    @Override
    public void setActiveConfig(SpongeConfig<?> config) {
        this.activeConfig = config;
        // update cached settings
        this.chunkGCLoadThreshold = this.activeConfig.getConfig().getWorld().getChunkLoadThreadhold();
        this.chunkGCTickInterval = this.activeConfig.getConfig().getWorld().getTickInterval();
        this.chunkUnloadDelay = this.activeConfig.getConfig().getWorld().getChunkUnloadDelay() * 1000;
        this.weatherIceAndSnowEnabled = this.activeConfig.getConfig().getWorld().getWeatherIceAndSnow();
        this.weatherThunderEnabled = this.activeConfig.getConfig().getWorld().getWeatherThunder();
        if (this.getChunkProvider() != null) {
            final IMixinChunkProviderServer mixinChunkProvider = (IMixinChunkProviderServer) this.getChunkProvider();
            final int maxChunkUnloads = this.activeConfig.getConfig().getWorld().getMaxChunkUnloads();
            mixinChunkProvider.setMaxChunkUnloads(maxChunkUnloads < 1 ? 1 : maxChunkUnloads);
            ((ChunkProviderServer) this.getChunkProvider()).chunkLoadOverride = !this.activeConfig.getConfig().getWorld().getDenyChunkRequests();
            for (net.minecraft.entity.Entity entity : this.loadedEntityList) {
                if (entity instanceof IModData) {
                    IModData spongeEntity = (IModData) entity;
                    spongeEntity.requiresCacheRefresh(true);
                }
            }
        }
    }

    @Override
    public int getChunkGCTickInterval() {
        return this.chunkGCTickInterval;
    }

    @Override
    public long getChunkUnloadDelay() {
        return this.chunkUnloadDelay;
    }
}
