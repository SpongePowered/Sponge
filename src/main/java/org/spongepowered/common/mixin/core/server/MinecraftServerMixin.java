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
package org.spongepowered.common.mixin.core.server;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.concurrent.RecursiveEventLoop;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.chunk.listener.IChunkStatusListenerFactory;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraft.world.storage.SessionLockException;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.server.WorldManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTrackerCrashHandler;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.general.MapConversionContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.generation.GenericGenerationContext;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.relocate.co.aikar.timings.TimingsManager;
import org.spongepowered.common.resourcepack.SpongeResourcePack;

import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.annotation.Nullable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends RecursiveEventLoop<TickDelayedTask> implements MinecraftServerBridge, SubjectBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final protected IChunkStatusListenerFactory chunkStatusListenerFactory;
    @Shadow private long serverTime;
    @Shadow private int tickCounter;
    @Shadow protected abstract void shadow$convertMapIfNeeded(String directoryName);
    @Shadow protected abstract void shadow$setUserMessage(ITextComponent translationKey);
    @Shadow public abstract Difficulty shadow$getDifficulty();
    @Shadow protected abstract void shadow$runScheduledTasks();
    @Shadow public abstract boolean shadow$isDedicatedServer();
    @Shadow public abstract boolean shadow$isServerRunning();
    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract Iterable<ServerWorld> shadow$getWorlds();
    @Shadow public abstract boolean shadow$isServerStopped();

    @Nullable private ResourcePack impl$resourcePack;
    private boolean impl$enableSaving = true;

    public MinecraftServerMixin(String name) {
        super(name);
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_SYSTEM;
    }

    @Override
    public Tristate bridge$permDefault(String permission) {
        return Tristate.TRUE;
    }

    /**
     * @author Zidane - Minecraft 1.14.4
     * @reason Sponge rewrites the method to use the Sponge {@link WorldManager} to load worlds,
     * migrating old worlds, upgrading worlds to our standard, and configuration loading.
     */
    @Overwrite
    public void loadAllWorlds(String directoryName, String levelName, long seed, WorldType type, JsonElement generatorOptions) {
        try (final MapConversionContext context = GeneralPhase.State.MAP_CONVERSION.createPhaseContext()
            .source(this)
            .world(directoryName)) {
            context.buildAndSwitch();
            this.shadow$convertMapIfNeeded(directoryName);
        }

        this.shadow$setUserMessage(new TranslationTextComponent("menu.loadingLevel"));

        SpongeImpl.getWorldManager().loadAllWorlds((MinecraftServer) (Object) this, directoryName, levelName, seed, type, generatorOptions);

        this.setDifficultyForAllWorlds(this.shadow$getDifficulty(), true);
    }

    @Override
    public void bridge$loadInitialChunks(ServerWorld world) {

        final WorldInfoBridge infoBridge = (WorldInfoBridge) world.getWorldInfo();
        if (!infoBridge.bridge$isValid() || !infoBridge.bridge$doesGenerateSpawnOnLoad()) {
            return;
        }

        try (final GenerationContext<GenericGenerationContext> context = GenerationPhase.State.TERRAIN_GENERATION.createPhaseContext()
            .source(world)
            .world( world)) {
            context.buildAndSwitch();

            final IChunkStatusListener chunkStatusListener = this.chunkStatusListenerFactory.create(11);

            this.shadow$setUserMessage(new TranslationTextComponent("menu.generatingTerrain"));
            LOGGER.info("Preparing start region for world '[{}}]'...", ((DimensionTypeBridge) world.dimension.getType()).bridge$getKey());
            final BlockPos blockpos = world.getSpawnPoint();
            chunkStatusListener.start(new ChunkPos(blockpos));
            final ServerChunkProvider serverChunkProvider = world.getChunkProvider();
            serverChunkProvider.getLightManager().func_215598_a(500);
            this.serverTime = Util.milliTime();
            serverChunkProvider.func_217228_a(TicketType.START, new ChunkPos(blockpos), 11, Unit.INSTANCE);

            while (serverChunkProvider.func_217229_b() != 441) {
                this.serverTime = Util.milliTime() + 10L;
                this.shadow$runScheduledTasks();
            }

            this.serverTime = Util.milliTime() + 10L;
            this.shadow$runScheduledTasks();

            final ForcedChunksSaveData forcedChunksData = world.getSavedData().get(ForcedChunksSaveData::new, "chunks");

            if (forcedChunksData != null) {
                final LongIterator longiterator = forcedChunksData.getChunks().iterator();

                while (longiterator.hasNext()) {
                    final long i = longiterator.nextLong();
                    final ChunkPos chunkpos = new ChunkPos(i);
                    serverChunkProvider.forceChunk(chunkpos, true);
                }
            }

            this.serverTime = Util.milliTime() + 10L;
            this.shadow$runScheduledTasks();
            chunkStatusListener.stop();
            serverChunkProvider.getLightManager().func_215598_a(5);
        }
    }

    @Inject(method = "setResourcePack(Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD") )
    private void impl$createSpongeResourcePackWrapper(String url, String hash, CallbackInfo ci) {
        if (url.length() == 0) {
            this.impl$resourcePack = null;
        } else {
            try {
                this.impl$resourcePack = SpongeResourcePack.create(url, hash);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public ResourcePack bridge$getResourcePack() {
        return this.impl$resourcePack;
    }

    @Override
    public void bridge$setSaveEnabled(boolean enabled) {
        this.impl$enableSaving = enabled;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void impl$onServerTickStart(CallbackInfo ci) {
        TimingsManager.FULL_SERVER_TICK.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void impl$completeTickCheckAnimationAndPhaseTracker(CallbackInfo ci) {
        final int lastAnimTick = SpongeCommonEventFactory.lastAnimationPacketTick;
        final int lastPrimaryTick = SpongeCommonEventFactory.lastPrimaryPacketTick;
        final int lastSecondaryTick = SpongeCommonEventFactory.lastSecondaryPacketTick;
        if (SpongeCommonEventFactory.lastAnimationPlayer != null) {
            final ServerPlayerEntity player = SpongeCommonEventFactory.lastAnimationPlayer.get();
            if (player != null && lastAnimTick != 0 && lastAnimTick - lastPrimaryTick > 3 && lastAnimTick - lastSecondaryTick > 3) {
                final BlockSnapshot blockSnapshot = BlockSnapshot.NONE;

                final RayTraceResult result = SpongeImplHooks.rayTraceEyes(player, SpongeImplHooks.getBlockReachDistance(player) + 1);

                // Hit non-air block
                if (result instanceof BlockRayTraceResult) {
                    return;
                }

                if (!player.getHeldItemMainhand().isEmpty() && SpongeCommonEventFactory.callInteractItemEventPrimary(player, player.getHeldItemMainhand(), Hand.MAIN_HAND, null, blockSnapshot).isCancelled()) {
                    SpongeCommonEventFactory.lastAnimationPacketTick = 0;
                    SpongeCommonEventFactory.lastAnimationPlayer = null;
                    return;
                }

                SpongeCommonEventFactory.callInteractBlockEventPrimary(player, player.getHeldItemMainhand(), Hand.MAIN_HAND, null);
            }
            SpongeCommonEventFactory.lastAnimationPlayer = null;
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = 0;

        PhaseTracker.getInstance().ensureEmpty();

        TimingsManager.FULL_SERVER_TICK.stopTiming();
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 900))
    private int getSaveTickInterval(int tickInterval) {
        if (!this.shadow$isDedicatedServer()) {
            return tickInterval;
        } else if (!this.shadow$isServerRunning()) {
            // Don't autosave while server is stopping
            return this.tickCounter + 1;
        }

        final int autoPlayerSaveInterval = SpongeImpl.getGlobalConfigAdapter().getConfig().getWorld().getAutoPlayerSaveInterval();
        if (autoPlayerSaveInterval > 0 && (this.tickCounter % autoPlayerSaveInterval == 0)) {
            this.shadow$getPlayerList().saveAllPlayerData();
        }

        this.save(true, true, false);
        // force check to fail as we handle everything above
        return this.tickCounter + 1;
    }

    /**
     * @author Zidane - Minecraft 1.14.4
     * @reason To allow per-world auto-save tick intervals or disable auto-saving entirely
     */
    @Overwrite
    public boolean save(boolean suppressLog, boolean flush, boolean forced) {
        if (!this.impl$enableSaving) {
            return false;
        }

        for (final ServerWorld world : this.shadow$getWorlds()) {
            final SerializationBehavior serializationBehavior = ((WorldInfoBridge) world.getWorldInfo()).bridge$getSerializationBehavior();
            final boolean save = serializationBehavior != SerializationBehaviors.NONE;
            boolean log = !suppressLog;

            if (save) {
                // Sponge start - check auto save interval in world config
                if (this.shadow$isDedicatedServer() && this.shadow$isServerRunning()) {
                    final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
                    final int autoSaveInterval = configAdapter.getConfig().getWorld().getAutoSaveInterval();
                    if (log) {
                        log = configAdapter.getConfig().getLogging().logWorldAutomaticSaving();
                    }
                    if (autoSaveInterval <= 0 || serializationBehavior != SerializationBehaviors.AUTOMATIC) {
                        if (log) {
                            LOGGER.warn("Auto-saving has been disabled for world \'" + world.getWorldInfo().getWorldName() + "\'/"
                                    + ((DimensionTypeBridge) world.dimension.getType()).bridge$getKey() + ". "
                                    + "No chunk data will be auto-saved - to re-enable auto-saving set 'auto-save-interval' to a value greater than"
                                    + " zero in the corresponding serverWorld config.");
                        }
                        continue;
                    }
                    if (this.tickCounter % autoSaveInterval != 0) {
                        continue;
                    }
                    if (log) {
                        LOGGER.info("Auto-saving chunks for world \'" + world.getWorldInfo().getWorldName() + "\'/"
                                + ((DimensionTypeBridge) world.dimension.getType()).bridge$getKey());
                    }
                } else if (log) {
                    LOGGER.info("Saving chunks for world \'" + world.getWorldInfo().getWorldName() + "\'/"
                        + ((DimensionTypeBridge) world.dimension.getType()).bridge$getKey());
                }

                try {
                    ((ServerWorldBridge) world).bridge$saveChunksAndProperties(null, flush, world.disableLevelSaving && !forced);
                } catch (SessionLockException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Inject(method = "stopServer", at = @At(value = "HEAD"), cancellable = true)
    private void impl$dontExecuteServerStopOffThread(CallbackInfo ci) {
        // If the server is already stopping, don't allow stopServer to be called off the main thread
        // (from the shutdown handler thread in MinecraftServer)
        if ((Sponge.isServerAvailable() && !((MinecraftServer) Sponge.getServer()).isServerRunning() && !Sponge.getServer().onMainThread())) {
            ci.cancel();
        }
    }

    @Redirect(method = "callFromMainThread",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/Callable;call()Ljava/lang/Object;",
            remap = false))
    private Object impl$callOnMainThreadWithPhaseState(Callable<?> callable) throws Exception {
        // This method can be called async while server is stopping
        if (this.shadow$isServerStopped() && !SpongeImplHooks.isMainThread()) {
            return callable.call();
        }

        final Object value;
        try (final BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(callable)) {
            context.buildAndSwitch();
            value = callable.call();
        }
        return value;
    }

    @Nullable
    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;runTask(Ljava/util/concurrent/FutureTask;Lorg/apache/logging/log4j/Logger;)Ljava/lang/Object;"))
    private Object impl$trackUtilTaskRun(FutureTask<?> task, Logger logger) {
        return SpongeImplHooks.onUtilRunTask(task, logger);
    }

    @Inject(method = "addServerInfoToCrashReport", at = @At("RETURN"), cancellable = true)
    private void impl$addPhaseTrackerToCrashReport(CrashReport report, CallbackInfoReturnable<CrashReport> cir) {
        report.makeCategory("Sponge PhaseTracker").addDetail("Phase Stack", CauseTrackerCrashHandler.INSTANCE);
        cir.setReturnValue(report);
    }

    /**
     * @author Zidane - Minecraft 1.14.4
     * @reason Set the difficulty without marking as custom
     */
    @Overwrite
    public void setDifficultyForAllWorlds(Difficulty difficulty, boolean forceDifficulty) {
        for (ServerWorld world : this.shadow$getWorlds()) {
            this.bridge$updateWorldForDifficulty(world, difficulty, false);
        }
    }
}
