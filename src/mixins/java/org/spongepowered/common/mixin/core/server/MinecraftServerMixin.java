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

import com.google.inject.Injector;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectProxy;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.ServerFunctionManagerAccessor;
import org.spongepowered.common.adventure.NativeComponentRenderer;
import org.spongepowered.common.bridge.commands.CommandSourceBridge;
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.network.chat.SpongeChatDecorator;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.server.players.GameProfileCacheBridge;
import org.spongepowered.common.bridge.server.players.PlayerListBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.bridge.world.level.storage.ServerLevelDataBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.event.lifecycle.FreezeRegistryEventImpl;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.config.common.AutoSaveOptions;
import org.spongepowered.common.launch.config.core.SpongeConfigs;
import org.spongepowered.common.registry.RegistryHolderLogic;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.service.server.SpongeServerScopedServiceProvider;
import org.spongepowered.common.util.AutoSaveMapQueue;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.io.IOException;
import java.net.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements SpongeServer, MinecraftServerBridge, CommandSourceProviderBridge, SubjectProxy,
    CommandSourceBridge {

    // @formatter:off
    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int tickCount;
    @Shadow @Final private Thread serverThread;
    @Shadow @Final private ServerFunctionManager functionManager;

    @Shadow public abstract CommandSourceStack shadow$createCommandSourceStack();
    @Shadow public abstract Iterable<ServerLevel> shadow$getAllLevels();
    @Shadow public abstract boolean shadow$isDedicatedServer();
    @Shadow public abstract boolean shadow$isRunning();
    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract PackRepository shadow$getPackRepository();
    @Shadow public abstract RegistryAccess.Frozen shadow$registryAccess();
    @Shadow public abstract GameProfileCache shadow$getProfileCache();
    @Shadow public abstract CompletableFuture<Void> shadow$reloadResources(final Collection<String> $$0);
    @Shadow public abstract WorldData shadow$getWorldData();
    @Shadow public abstract boolean shadow$haveTime();
    @Shadow private volatile boolean isSaving;
    @Shadow public abstract ResourceManager shadow$getResourceManager();
    @Shadow @Nullable public abstract ServerLevel shadow$getLevel(ResourceKey<Level> $$0);
    // @formatter:on

    private final ChatDecorator impl$spongeDecorator = new SpongeChatDecorator();
    private @Nullable SpongeServerScopedServiceProvider impl$serviceProvider;
    protected @Nullable ResourcePackRequest impl$resourcePack;
    private final BlockableEventLoop<Runnable> impl$spongeMainThreadExecutor = new BlockableEventLoop<>("Sponge") {

        //Used to schedule internal Sponge tasks to the main thread
        //that could be joined on the main thread. Avoiding using the
        //MinecraftServer Executor to prevent changes in timings.

        @Override
        public Runnable wrapRunnable(Runnable runnable) {
            return runnable;
        }

        @Override
        protected boolean shouldRun(@NonNull Runnable runnable) {
            return MinecraftServerMixin.this.shadow$haveTime();
        }

        @Override
        protected @NonNull Thread getRunningThread() {
            return MinecraftServerMixin.this.serverThread;
        }
    };
    private RegistryHolderLogic impl$registryHolder;

    private final AutoSaveMapQueue<org.spongepowered.api.ResourceKey> impl$worldConfigSaveQueue = new AutoSaveMapQueue<>((k, log) -> {
        final @Nullable ServerLevel level = this.shadow$getLevel(SpongeWorldManager.createRegistryKey(k));
        if (level != null) {
            this.impl$saveWorldConfig(level, SpongeGameConfigs.getForWorld(level), log);
            return true;
        }
        return false;
    });

    private final AutoSaveMapQueue<org.spongepowered.api.ResourceKey> impl$worldSaveQueue = new AutoSaveMapQueue<>((k, log) -> {
        final @Nullable ServerLevel level = this.shadow$getLevel(SpongeWorldManager.createRegistryKey(k));
        if (level != null) {
            if (log) {
                MinecraftServerMixin.LOGGER.info("Saving chunks for level '{}'/{}", level, level.dimension().location());
            }
            level.save(null, false, level.noSave);
            return true;
        }
        return false;
    });

    @Override
    public Subject subject() {
        return SpongeCommon.game().systemSubject();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setThreadOnServerPhaseTracker(
        Thread thread, LevelStorageSource.LevelStorageAccess storageAccess, PackRepository packRepo,
        WorldStem stem, Proxy proxy, DataFixer fixer,
        Services services, ChunkProgressListenerFactory progress, CallbackInfo ci
    ) {
        try {
            PhaseTracker.getServerInstanceExplicitly().setThread(thread);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not initialize the server PhaseTracker!");
        }
    }

    @Override
    public ResourcePackRequest bridge$getResourcePack() {
        return this.impl$resourcePack;
    }

    @Inject(method = "tickServer", at = @At(value = "HEAD"))
    private void impl$onServerTickStart(final CallbackInfo ci) {
        this.scheduler().tick();
    }

    @Override
    public CommandSourceStack bridge$getCommandSource(final Cause cause) {
        return this.shadow$createCommandSourceStack();
    }

    // The Audience of the Server is actually a Forwarding Audience - so any message sent to
    // the server will be sent to everyone connected. We therefore need to make sure we send
    // things to the right place. We consider anything done by the server as being done by the
    // system subject
    @Override
    public void bridge$addToCauseStack(final CauseStackManager.StackFrame frame) {
        frame.pushCause(Sponge.systemSubject());
    }

    @Inject(method = "createLevels", at = @At(value = "INVOKE", ordinal = 0,
        target = "Lnet/minecraft/server/level/ServerLevel;<init>(Lnet/minecraft/server/MinecraftServer;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lnet/minecraft/world/level/storage/ServerLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/LevelStem;Lnet/minecraft/server/level/progress/ChunkProgressListener;ZJLjava/util/List;ZLnet/minecraft/world/RandomSequences;)V"))
    private void impl$onCreateDefaultLevel(final CallbackInfo ci, @Local final ServerLevelData levelData, @Local final LevelStem levelStem) {
        ((PrimaryLevelDataBridge) levelData).bridge$populateFromLevelStem(levelStem);
        ((ServerLevelDataBridge) levelData).bridge$spongeData().setKey(DefaultWorldKeys.DEFAULT);
    }

    @Inject(method = "createLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;isInitialized()Z"))
    private void impl$onInitDefaultLevel(final CallbackInfo ci, @Local final ServerLevelData levelData, @Local final ServerLevel level) {
        final boolean initialized = levelData.isInitialized();
        final LoadWorldEvent loadWorldEvent = SpongeEventFactory.createLoadWorldEvent(PhaseTracker.getInstance().currentCause(), (ServerWorld) level, initialized);
        SpongeCommon.post(loadWorldEvent);
    }

    @Redirect(method = "createLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;entrySet()Ljava/util/Set;"))
    private Set<?> impl$onCreateOtherLevels(final Registry<?> stemRegistry) {
        this.worldManager().createNonDefaultLevels();
        return Set.of(); // prevent vanilla code
    }

    @WrapMethod(method = "setInitialSpawn")
    private static void impl$wrapSetInitialSpawn(final ServerLevel level, final ServerLevelData levelData, final boolean generateBonusChest, final boolean debugWorld, final Operation<Void> original) {
        try (final var state = GenerationPhase.State.TERRAIN_GENERATION.createPhaseContext(PhaseTracker.getInstance())) {
            state.buildAndSwitch();
            original.call(level, levelData, generateBonusChest, debugWorld);
        }
    }

    /**
     * @author Yeregorix
     * @reason Multi world.
     */
    @Overwrite
    private void prepareLevels(final ChunkProgressListener progressListener) {
        this.worldManager().prepareLevels();
    }

    /**
     * @author Zidane
     * @reason Apply our branding
     */
    @DontObfuscate
    @Overwrite
    public String getServerModName() {
        return "sponge";
    }

    @Inject(method = "stopServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;saveAllChunks(ZZZ)Z"))
    private void impl$callUnloadWorldEvents(final CallbackInfo ci) {
        for(final ServerLevel level : this.shadow$getAllLevels()) {
            final UnloadWorldEvent unloadWorldEvent = SpongeEventFactory.createUnloadWorldEvent(PhaseTracker.getInstance().currentCause(), (ServerWorld) level);
            SpongeCommon.post(unloadWorldEvent);
        }
    }

    @Inject(method = "stopServer", at = @At(value = "TAIL"))
    private void impl$closeLevelSaveForOtherWorlds(final CallbackInfo ci) {
        for (final Map.Entry<ResourceKey<Level>, ServerLevel> entry : this.levels.entrySet()) {
            if (entry.getKey() == Level.OVERWORLD) {
                continue;
            }

            final LevelStorageSource.LevelStorageAccess levelSave = ((ServerLevelBridge) entry.getValue()).bridge$getLevelSave();
            try {
                levelSave.close();
            } catch (final IOException e) {
                MinecraftServerMixin.LOGGER.error("Failed to unlock level {}", levelSave.getLevelId(), e);
            }
        }
    }

    /**
     * Render localized/formatted chat components
     *
     * @param input original component
     */
    @Inject(method = "sendSystemMessage", at = @At("HEAD"), cancellable = true)
    private void impl$useTranslatingLogger(final Component input, final CallbackInfo ci) {
        MinecraftServerMixin.LOGGER.info(NativeComponentRenderer.apply(input, Locale.getDefault(), Sponge.game().systemSubject()).getString());
        ci.cancel();
    }

    @ModifyConstant(method = "tickServer",
        slice = @Slice(
            to = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;autoSave()V", ordinal = 1),
            from = @At(value = "FIELD", target = "Lnet/minecraft/server/MinecraftServer;ticksUntilAutosave:I", ordinal = 0)
        ),
        constant = @Constant(intValue = 0, ordinal = 0, expandZeroConditions = Constant.Condition.LESS_THAN_OR_EQUAL_TO_ZERO))
    private int impl$getSaveTickInterval(final int zero) {
        if (!this.shadow$isDedicatedServer()) {
            return zero;
        } else if (!this.shadow$isRunning()) {
            // Don't autosave while server is stopping
            return Integer.MIN_VALUE;
        }

        final AutoSaveOptions autoPlayerSave = SpongeConfigs.getCommon().get().world.playerAutoSave;
        if (autoPlayerSave.interval > 0 && (this.tickCount % autoPlayerSave.interval == 0)) {
            this.isSaving = true;
            if (autoPlayerSave.batchInterval <= 0) {
                if (autoPlayerSave.log) {
                    MinecraftServerMixin.LOGGER.info("Starting to save player data");
                }
                this.shadow$getPlayerList().saveAll();
                if (autoPlayerSave.log) {
                    MinecraftServerMixin.LOGGER.info("All player data has been saved");
                }
            } else {
                ((PlayerListBridge) this.shadow$getPlayerList()).bridge$saveAll(autoPlayerSave.batchInterval, Math.max(1, autoPlayerSave.batchAmount), autoPlayerSave.log);
            }
            this.isSaving = false;
        }

        this.isSaving = true;
        this.saveAllChunks(true, false, false);
        this.isSaving = false;

        // force check to fail as we handle everything above
        return Integer.MIN_VALUE;
    }

    /**
     * @author Zidane - November, 24th 2020 - Minecraft 1.15
     * @reason To allow per-world auto-save tick intervals or disable auto-saving entirely
     */
    @Overwrite
    public boolean saveAllChunks(final boolean suppressLog, final boolean flush, final boolean isForced) {
        boolean result = false;

        for (final ServerLevel level : this.shadow$getAllLevels()) {
            // Sponge start - use our own config
            final SerializationBehavior serializationBehavior = ((ServerLevelDataBridge) level.getLevelData()).bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
            final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.getForWorld(level);
            final AutoSaveOptions configAutoSave = configAdapter.get().world.configAutoSave;

            // If the server isn't running or we hit save interval or this was triggered
            // by a command, save our configs
            if (!this.shadow$isRunning() || isForced || (configAutoSave.interval > 0 && this.tickCount % configAutoSave.interval == 0)) {
                if (flush || configAutoSave.batchInterval == 0) {
                    this.impl$worldConfigSaveQueue.remove(((ServerWorld) level).key());
                    this.impl$saveWorldConfig(level, configAdapter, configAutoSave.log);
                } else {
                    this.impl$worldConfigSaveQueue.add(configAutoSave, ((ServerWorld) level).key());
                }
            }

            final boolean canSaveAtAll = serializationBehavior != SerializationBehavior.NONE;

            // This world is set to not save of any time, no reason to check the auto-save/etc, skip it
            if (!canSaveAtAll) {
                continue;
            }

            final AutoSaveOptions autoSave = configAdapter.get().world.autoSave;

            // Only run auto-save skipping if the server is still running and the save is not forced
            if (this.bridge$performAutosaveChecks() && !isForced) {

                // Do not process properties or chunks if the world is not set to do so unless the server is shutting down
                if (autoSave.interval <= 0 || serializationBehavior != SerializationBehavior.AUTOMATIC) {
                    continue;
                }

                // Now check the interval vs the tick counter and skip it
                if (this.tickCount % autoSave.interval != 0) {
                    continue;
                }

                if (!flush && autoSave.batchInterval > 0) {
                    this.impl$worldSaveQueue.add(autoSave, ((ServerWorld) level).key());
                    result = true;
                    continue;
                }
            }

            this.impl$worldSaveQueue.remove(((ServerWorld) level).key());
            // Sponge end

            if (autoSave.log) {
                LOGGER.info("Saving chunks for level '{}'/{}", level, level.dimension().location());
            }

            level.save(null, flush, level.noSave && !isForced);
            result = true;
        }

        // Sponge start - We do per-world WorldInfo/WorldBorders/BossBars
//        ServerLevel var2 = this.overworld();
//        ServerLevelData var3 = this.worldData.overworldData();
//        var3.setWorldBorder(var2.getWorldBorder().createSettings());
//        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save(this.registryAccess()));
//        this.storageSource.saveDataTag(this.registryHolder, this.worldData, this.shadow$getPlayerList().getSingleplayerData());
        // Sponge end

        // Sponge start
        // Save the usercache.json file every 10 minutes or if forced to
        if (isForced || this.tickCount % 6000 == 0) {
            // We want to save the username cache json, as we normally bypass it.
            final GameProfileCache profileCache = this.shadow$getProfileCache();
            ((GameProfileCacheBridge) profileCache).bridge$setCanSave(true);
            profileCache.save();
            ((GameProfileCacheBridge) profileCache).bridge$setCanSave(false);
        }
        // Sponge end

        if (flush) {
            for (final ServerLevel level : this.shadow$getAllLevels()) {
                // Sponge start - use our own config
                final InheritableConfigHandle<WorldConfig> configAdapter = SpongeGameConfigs.getForWorld(level);
                final boolean log = configAdapter.get().world.autoSave.log;
                // Sponge end

                if (log) {
                    LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", level.getChunkSource().chunkMap.getStorageName());
                }
            }

            LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }

        this.impl$worldConfigSaveQueue.drain();
        this.impl$worldSaveQueue.drain();

        return result;
    }

    private void impl$saveWorldConfig(final ServerLevel level, final InheritableConfigHandle<WorldConfig> configAdapter, final boolean log) {
        if (log) {
            MinecraftServerMixin.LOGGER.info("Saving world configuration file for level {}", level);
        }
        configAdapter.save();
        if (log) {
            MinecraftServerMixin.LOGGER.info("Saved world configuration file for level {}", level);
        }
    }

    /**
     * @author Zidane
     * @reason Set the difficulty without marking as custom
     */
    @Overwrite
    public void setDifficulty(final Difficulty difficulty, final boolean force) {
        for (final ServerLevel level : this.shadow$getAllLevels()) {
            if (level.getLevelData() instanceof PrimaryLevelData levelData && (force || !levelData.isDifficultyLocked())) {
                // Don't allow vanilla forcing the difficulty at launch set ours if we have a custom one
                if (!((PrimaryLevelDataBridge) levelData).bridge$customDifficulty()) {
                    ((PrimaryLevelDataBridge) levelData).bridge$forceSetDifficulty(difficulty);
                }
            }
        }
    }

    @Override
    public void bridge$initServices(final Game game, final Injector injector) {
        if (this.impl$serviceProvider == null) {
            this.impl$serviceProvider = new SpongeServerScopedServiceProvider(this, game, injector);
            this.impl$serviceProvider.init();

            Util.blockUntilDone(e ->
                SimpleReloadInstance.create(this.shadow$getResourceManager(), List.of(((ServerFunctionManagerAccessor) this.functionManager).accessor$library()),
                    Util.backgroundExecutor(), e, CompletableFuture.completedFuture(Unit.INSTANCE), MinecraftServerMixin.LOGGER.isDebugEnabled()).done());
        }
    }

    @Override
    public SpongeServerScopedServiceProvider bridge$getServiceProvider() {
        return this.impl$serviceProvider;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

    @Inject(method = "getChatDecorator", at = @At("RETURN"), cancellable = true)
    private void impl$redirectChatDecorator(final CallbackInfoReturnable<ChatDecorator> cir) {
        if (cir.getReturnValue() == ChatDecorator.PLAIN) {
            cir.setReturnValue(this.impl$spongeDecorator);
        }
    }

    @Override
    public BlockableEventLoop<Runnable> bridge$spongeMainThreadExecutor() {
        return this.impl$spongeMainThreadExecutor;
    }

    @Inject(method = "pollTaskInternal", at = @At("HEAD"), cancellable = true)
    private void impl$pollSpongeTasks(final CallbackInfoReturnable<Boolean> cir) {
        //Pool our tasks first to try to have small impact on timings
        if (this.impl$spongeMainThreadExecutor.pollTask()) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void bridge$reloadServerRegistries(final RegistryHolder holder) {
        ((SpongeRegistryHolder) holder).setRootMinecraftRegistry(this.shadow$registryAccess());
        Launch.instance().lifecycle().beginEstablishServerRegistries(holder);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void impl$onInit(final CallbackInfo ci, final @Local(argsOnly = true) WorldStem levelStem) {
        this.bridge$reloadedServerRegistries(((SpongeRegistryHolder) levelStem.resourceManager()).registryHolder());
    }

    @Override
    public void bridge$reloadedServerRegistries(final RegistryHolderLogic holder) {
        this.impl$registryHolder = holder;
        Sponge.game().eventManager().post(FreezeRegistryEventImpl.PostImpl.EngineImpl.server(Cause.of(EventContext.empty(), Sponge.game()), Sponge.game(), holder));
    }

    @Override
    public RegistryHolderLogic bridge$registryHolder() {
        return this.impl$registryHolder;
    }
}
