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

import co.aikar.timings.Timing;
import co.aikar.timings.sponge.ServerTimingsHandler;
import co.aikar.timings.sponge.TimingsManager;
import com.google.inject.Injector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectProxy;
import org.spongepowered.api.world.SerializationBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.commands.CommandSourceBridge;
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.server.players.GameProfileCacheBridge;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.datapack.SpongeDataPackManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.service.server.SpongeServerScopedServiceProvider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements SpongeServer, MinecraftServerBridge, CommandSourceProviderBridge, SubjectProxy,
    CommandSourceBridge {

    // @formatter:off
    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow @Final private GameProfileCache profileCache;
    @Shadow @Final private static Logger LOGGER;
    @Shadow private int tickCount;
    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;

    @Shadow public abstract CommandSourceStack shadow$createCommandSourceStack();
    @Shadow public abstract Iterable<ServerLevel> shadow$getAllLevels();
    @Shadow public abstract boolean shadow$isDedicatedServer();
    @Shadow public abstract boolean shadow$isRunning();
    @Shadow public abstract PlayerList shadow$getPlayerList();
    @Shadow public abstract PackRepository shadow$getPackRepository();
    @Shadow protected abstract void shadow$detectBundledResources();

    @Shadow protected abstract void loadLevel();
    // @formatter:on

    private @Nullable SpongeServerScopedServiceProvider impl$serviceProvider;
    private @Nullable ResourcePack impl$resourcePack;
    private @Nullable ServerTimingsHandler impl$timingsHandler;

    @Override
    public Subject subject() {
        return SpongeCommon.game().systemSubject();
    }

    @Inject(method = "spin", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void impl$setThreadOnServerPhaseTracker(final Function<Thread, MinecraftServer> p_240784_0_,
                                                           final CallbackInfoReturnable<MinecraftServerMixin> cir,
                                                           final AtomicReference<MinecraftServer> atomicReference,
                                                           final Thread thread) {
        try {
            PhaseTracker.SERVER.setThread(thread);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Could not initialize the server PhaseTracker!");
        }
    }

    @Inject(method = "setResourcePack(Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD") )
    private void impl$createSpongeResourcePackWrapper(final String url, final String hash, final CallbackInfo ci) {
        if (url.length() == 0) {
            this.impl$resourcePack = null;
        } else {
            try {
                this.impl$resourcePack = SpongeResourcePack.create(url, hash);
            } catch (final URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ResourcePack bridge$getResourcePack() {
        return this.impl$resourcePack;
    }

    @Inject(method = "tickServer", at = @At(value = "HEAD"))
    private void impl$onServerTickStart(final CallbackInfo ci) {
        TimingsManager.FULL_SERVER_TICK.startTiming();
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void impl$tickServerScheduler(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
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

    @Inject(method = "tickServer", at = @At(value = "RETURN"))
    private void impl$completeTickCheckAnimation(final CallbackInfo ci) {
        TimingsManager.FULL_SERVER_TICK.stopTiming();
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
    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void impl$useTranslatingLogger(final Component input, final UUID sender, final CallbackInfo ci) {
        MinecraftServerMixin.LOGGER.info(input);
        ci.cancel();
    }

    @ModifyConstant(method = "tickServer", constant = @Constant(intValue = 6000, ordinal = 0))
    private int getSaveTickInterval(final int tickInterval) {
        if (!this.shadow$isDedicatedServer()) {
            return tickInterval;
        } else if (!this.shadow$isRunning()) {
            // Don't autosave while server is stopping
            return this.tickCount + 1;
        }

        final int autoPlayerSaveInterval = SpongeConfigs.getCommon().get().world.playerAutoSaveInterval;
        if (autoPlayerSaveInterval > 0 && (this.tickCount % autoPlayerSaveInterval == 0)) {
            this.shadow$getPlayerList().saveAll();
        }

        try (final Timing timing = this.bridge$timingsHandler().save.startTiming()) {
            this.saveAllChunks(true, false, false);
        }

        // force check to fail as we handle everything above
        return this.tickCount + 1;
    }

    /**
     * @author Zidane - November, 24th 2020 - Minecraft 1.15
     * @reason To allow per-world auto-save tick intervals or disable auto-saving entirely
     */
    @Overwrite
    public boolean saveAllChunks(final boolean suppressLog, final boolean flush, final boolean isForced) {
        for (final ServerLevel world : this.shadow$getAllLevels()) {
            final SerializationBehavior serializationBehavior = ((PrimaryLevelDataBridge) world.getLevelData()).bridge$serializationBehavior().orElse(SerializationBehavior.AUTOMATIC);
            boolean log = !suppressLog;

            // Not forced happens during ticks and when shutting down
            if (!isForced) {
                final InheritableConfigHandle<WorldConfig> adapter = ((PrimaryLevelDataBridge) world.getLevelData()).bridge$configAdapter();
                final int autoSaveInterval = adapter.get().world.autoSaveInterval;
                if (log) {
                    if (this.bridge$performAutosaveChecks()) {
                        log = adapter.get().world.logAutoSave;
                    }
                }

                // Not forced means this is an auto-save or a shut down, handle accordingly

                // If the server isn't running or we hit Vanilla's save interval, save our configs
                if (!this.shadow$isRunning() || this.tickCount % 6000 == 0) {
                    ((PrimaryLevelDataBridge) world.getLevelData()).bridge$configAdapter().save();
                }

                final boolean canSaveAtAll = serializationBehavior != SerializationBehavior.NONE;

                // This world is set to not save of any time, no reason to check the auto-save/etc, skip it
                if (!canSaveAtAll) {
                    continue;
                }

                // Only run auto-save skipping if the server is still running
                if (this.bridge$performAutosaveChecks()) {

                    // Do not process properties or chunks if the world is not set to do so unless the server is shutting down
                    if (autoSaveInterval <= 0 || serializationBehavior != SerializationBehavior.AUTOMATIC) {
                        continue;
                    }

                    // Now check the interval vs the tick counter and skip it
                    if (this.tickCount % autoSaveInterval != 0) {
                        continue;
                    }
                }

                world.save(null, false, world.noSave);

                if (log) {
                    if (this.bridge$performAutosaveChecks()) {
                        MinecraftServerMixin.LOGGER.info("Auto-saving data for world '{}'", ((org.spongepowered.api.world.server.ServerWorld) world).key());
                    } else {
                        MinecraftServerMixin.LOGGER.info("Saving data for world '{}'", ((org.spongepowered.api.world.server.ServerWorld) world).key());
                    }
                }
            // Forced happens during command
            } else {
                if (log) {
                    MinecraftServerMixin.LOGGER.info("Manually saving data for world '{}'", ((org.spongepowered.api.world.server.ServerWorld) world).key());
                }

                ((PrimaryLevelDataBridge) world.getLevelData()).bridge$configAdapter().save();

                world.save(null, false, world.noSave);
            }
        }

        // Save the usercache.json file every 10 minutes or if forced to
        if (isForced || this.tickCount % 6000 == 0) {
            // We want to save the username cache json, as we normally bypass it.
            ((GameProfileCacheBridge) this.profileCache).bridge$setCanSave(true);
            this.profileCache.save();
            ((GameProfileCacheBridge) this.profileCache).bridge$setCanSave(false);
        }
        return true;
    }

    /**
     * @author Zidane
     * @reason Set the difficulty without marking as custom
     */
    @Overwrite
    public void setDifficulty(final Difficulty difficulty, final boolean forceDifficulty) {
        for (final ServerLevel world : this.shadow$getAllLevels()) {
            this.bridge$setDifficulty(world, difficulty, forceDifficulty);
        }
    }

    @Override
    public void bridge$setDifficulty(final ServerLevel world, final Difficulty newDifficulty, final boolean forceDifficulty) {
        if (world.getLevelData().isDifficultyLocked() && !forceDifficulty) {
            return;
        }

        if (forceDifficulty) {
            // Don't allow vanilla forcing the difficulty at launch set ours if we have a custom one
            if (!((PrimaryLevelDataBridge) world.getLevelData()).bridge$customDifficulty()) {
                ((PrimaryLevelDataBridge) world.getLevelData()).bridge$forceSetDifficulty(newDifficulty);
            }
        } else {
            ((PrimaryLevelData) world.getLevelData()).setDifficulty(newDifficulty);
        }
    }

    @Override
    public ServerTimingsHandler bridge$timingsHandler() {
        if (this.impl$timingsHandler == null) {
            this.impl$timingsHandler = new ServerTimingsHandler((MinecraftServer) (Object) this);
        }

        return this.impl$timingsHandler;
    }

    @Override
    public void bridge$initServices(final Game game, final Injector injector) {
        if (this.impl$serviceProvider == null) {
            this.impl$serviceProvider = new SpongeServerScopedServiceProvider(this, game, injector);
            this.impl$serviceProvider.init();
        }
    }

    @Override
    public SpongeServerScopedServiceProvider bridge$getServiceProvider() {
        return this.impl$serviceProvider;
    }

    @Inject(method = "reloadResources", at = @At(value = "HEAD"))
    public void impl$reloadResources(final Collection<String> datapacksToLoad, final CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        SpongeDataPackManager.INSTANCE.callRegisterDataPackValueEvents(this.storageSource.getLevelPath(LevelResource.DATAPACK_DIR), datapacksToLoad);
        this.shadow$getPackRepository().reload();
    }

    @Inject(method = "reloadResources", at = @At(value = "RETURN"))
    public void impl$serializeDelayedDataPack(final Collection<String> datapacksToLoad, final CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        cir.getReturnValue().thenAccept(v -> {
            SpongeDataPackManager.INSTANCE.serializeDelayedDataPack(DataPackTypes.WORLD);
        });
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
