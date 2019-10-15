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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
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
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.command.SpongeCommandManager;
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
import org.spongepowered.common.mixin.core.world.storage.WorldInfoMixin;
import org.spongepowered.common.relocate.co.aikar.timings.TimingsManager;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.annotation.Nullable;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements SubjectBridge, CommandSourceBridge, CommandSenderBridge,
    MinecraftServerBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final public Profiler profiler;
    @Shadow private boolean serverStopped;
    @Shadow private int tickCounter;
    @Shadow public WorldServer[] worlds;

    @Shadow public abstract void sendMessage(ITextComponent message);
    @Shadow public abstract boolean isServerRunning();
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow public abstract EnumDifficulty getDifficulty();
    @Shadow public abstract GameType getGameType();
    @Shadow protected abstract void setUserMessage(String message);
    @Shadow protected abstract void outputPercentRemaining(String message, int percent);
    @Shadow protected abstract void clearCurrentTask();
    @Shadow protected abstract void convertMapIfNeeded(String worldNameIn);
    @Shadow public abstract boolean isDedicatedServer();
    @Shadow public abstract String shadow$getName();
    @Shadow public abstract PlayerProfileCache getPlayerProfileCache();

    @Nullable private List<String> impl$currentTabCompletionOptions;
    @Nullable private ResourcePack impl$resourcePack;
    private boolean impl$enableSaving = true;

    @Override
    public String bridge$getIdentifier() {
        return shadow$getName();
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_SYSTEM;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.TRUE;
    }

    @Override
    public ICommandSender bridge$asICommandSender() {
        return (MinecraftServer) (Object) this;
    }

    @Override
    public CommandSource bridge$asCommandSource() {
        return (CommandSource) this;
    }

    /**
     * @author blood - December 23rd, 2015
     * @author Zidane - March 13th, 2016
     * @author gabizou - April 22nd, 2019 - Minecraft 1.12.2
     *
     * @reason Sponge rewrites the method to use the Sponge {@link WorldManager} to load worlds,
     * migrating old worlds, upgrading worlds to our standard, and configuration loading. Also
     * validates that the {@link WorldInfoMixin onConstruction} will not be doing anything
     * silly during map conversions.
     */
    @Overwrite
    public void loadAllWorlds(final String overworldFolder, final String worldName, final long seed, final WorldType type,
        final String generatorOptions) {
        try (final MapConversionContext context = GeneralPhase.State.MAP_CONVERSION.createPhaseContext()
            .source(this)
            .world(overworldFolder)) {
            context.buildAndSwitch();
            this.convertMapIfNeeded(overworldFolder);
        }
        this.setUserMessage("menu.loadingLevel");

        WorldManager.loadAllWorlds(seed, type, generatorOptions);

        this.getPlayerList().setPlayerManager(this.worlds);
        this.setDifficultyForAllWorlds(this.getDifficulty());
    }

    /**
     * @author Zidane - March 13th, 2016
     *
     * @reason Sponge has a config option for determining if we'll
     * generate spawn on server start. I enforce that here.
     */
    @Overwrite
    public void initialWorldChunkLoad() {
        for (final WorldServer worldServer: this.worlds) {
            this.bridge$prepareSpawnArea(worldServer);
        }
        this.clearCurrentTask();
    }

    @Override
    public void bridge$prepareSpawnArea(final WorldServer worldServer) {
        final WorldProperties worldProperties = (WorldProperties) worldServer.getWorldInfo();
        if (!((WorldInfoBridge) worldProperties).bridge$isValid() || !worldProperties.doesGenerateSpawnOnLoad()) {
            return;
        }

        final ChunkProviderServerBridge chunkProviderServer = (ChunkProviderServerBridge) worldServer.getChunkProvider();
        chunkProviderServer.bridge$setForceChunkRequests(true);

        try (final GenerationContext<GenericGenerationContext> context = GenerationPhase.State.TERRAIN_GENERATION.createPhaseContext()
            .source(worldServer)
            .world( worldServer)) {
            context.buildAndSwitch();
            int i = 0;
            this.setUserMessage("menu.generatingTerrain");
            LOGGER.info("Preparing start region for world {} ({}/{})", worldServer.getWorldInfo().getWorldName(),
                ((DimensionType) (Object) worldServer.provider.getDimensionType()).getId(), ((WorldServerBridge) worldServer).bridge$getDimensionId());
            final BlockPos blockpos = worldServer.getSpawnPoint();
            long j = MinecraftServer.getCurrentTimeMillis();
            for (int k = -192; k <= 192 && this.isServerRunning(); k += 16) {
                for (int l = -192; l <= 192 && this.isServerRunning(); l += 16) {
                    final long i1 = MinecraftServer.getCurrentTimeMillis();

                    if (i1 - j > 1000L) {
                        this.outputPercentRemaining("Preparing spawn area", i * 100 / 625);
                        j = i1;
                    }

                    ++i;
                    worldServer.getChunkProvider().provideChunk(blockpos.getX() + k >> 4, blockpos.getZ() + l >> 4);
                }
            }
            this.clearCurrentTask();
        }
        chunkProviderServer.bridge$setForceChunkRequests(false);
    }

    @Inject(method = "setResourcePack(Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD") )
    private void impl$updateResourcePack(final String url, final String hash, final CallbackInfo ci) {
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
    public void bridge$setSaveEnabled(final boolean enabled) {
        this.impl$enableSaving = enabled;
    }

    @Redirect(method = "getTabCompletions", at = @At(value = "INVOKE",
            target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;", remap = false))
    private ArrayList<String> impl$useSpongeTabCompletionList() {
        final ArrayList<String> list = new ArrayList<>();
        this.impl$currentTabCompletionOptions = list;
        return list;
    }

    @Inject(method = "getTabCompletions", at = @At(value = "RETURN", ordinal = 0))
    private void impl$throwEventForTabCompletion(final ICommandSender sender, final String input, final BlockPos pos, final boolean usingBlock,
            final CallbackInfoReturnable<List<String>> cir) {

        final List<String> completions = checkNotNull(this.impl$currentTabCompletionOptions, "currentTabCompletionOptions");
        this.impl$currentTabCompletionOptions = null;

        Sponge.getCauseStackManager().pushCause(sender);
        final TabCompleteEvent.Chat event = SpongeEventFactory.createTabCompleteEventChat(Sponge.getCauseStackManager().getCurrentCause(),
                ImmutableList.copyOf(completions), completions, input, Optional.ofNullable(getTarget(sender, pos)), usingBlock);
        Sponge.getEventManager().post(event);
        Sponge.getCauseStackManager().popCause();
        if (event.isCancelled()) {
            completions.clear();
        }
    }

    @Redirect(method = "getTabCompletions",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/command/ICommandManager;getTabCompletions(Lnet/minecraft/command/ICommandSender;Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;"))
    private List<String> impl$useSpongeCommandManagerForSuggestions(
        final ICommandManager manager, final ICommandSender sender, final String input, @Nullable final BlockPos pos, final ICommandSender sender_,
        final String input_, final BlockPos pos_, final boolean hasTargetBlock) {
        return ((SpongeCommandManager) SpongeImpl.getGame().getCommandManager()).getSuggestions((CommandSource) sender, input, getTarget(sender, pos), hasTargetBlock);
    }

    @Nullable
    private static Location<World> getTarget(final ICommandSender sender, @Nullable final BlockPos pos) {
        @Nullable Location<World> targetPos = null;
        if (pos != null) {
            targetPos = new Location<>((World) sender.getEntityWorld(), VecHelper.toVector3i(pos));
        }
        return targetPos;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void onServerTickStart(final CallbackInfo ci) {
        TimingsManager.FULL_SERVER_TICK.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void impl$copmleteTickCheckAnimationAndPhaseTracker(final CallbackInfo ci) {
        final int lastAnimTick = SpongeCommonEventFactory.lastAnimationPacketTick;
        final int lastPrimaryTick = SpongeCommonEventFactory.lastPrimaryPacketTick;
        final int lastSecondaryTick = SpongeCommonEventFactory.lastSecondaryPacketTick;
        if (SpongeCommonEventFactory.lastAnimationPlayer != null) {
            final EntityPlayerMP player = SpongeCommonEventFactory.lastAnimationPlayer.get();
            if (player != null && lastAnimTick != 0 && lastAnimTick - lastPrimaryTick > 3 && lastAnimTick - lastSecondaryTick > 3) {
                final BlockSnapshot blockSnapshot = BlockSnapshot.NONE;

                final RayTraceResult result = SpongeImplHooks.rayTraceEyes(player, SpongeImplHooks.getBlockReachDistance(player) + 1);
                // Hit non-air block
                if (result != null && result.getBlockPos() != null) {
                    return;
                }

                if (!player.getHeldItemMainhand().isEmpty() && SpongeCommonEventFactory.callInteractItemEventPrimary(player, player.getHeldItemMainhand(), EnumHand.MAIN_HAND, null, blockSnapshot).isCancelled()) {
                    SpongeCommonEventFactory.lastAnimationPacketTick = 0;
                    SpongeCommonEventFactory.lastAnimationPlayer = null;
                    return;
                }

                SpongeCommonEventFactory.callInteractBlockEventPrimary(player, player.getHeldItemMainhand(), EnumHand.MAIN_HAND, null);
            }
            SpongeCommonEventFactory.lastAnimationPlayer = null;
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = 0;

        PhaseTracker.getInstance().ensureEmpty();

        TimingsManager.FULL_SERVER_TICK.stopTiming();
    }

    @Nullable private Integer dimensionId;

    @Redirect(method = "addServerStatsToSnooper",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/WorldServer;provider:Lnet/minecraft/world/WorldProvider;",
            opcode = Opcodes.GETFIELD))
    private WorldProvider impl$getWorldProviderAndMaybeSetDimensionId(final WorldServer world) {
        //noinspection ConstantConditions
        if (((WorldBridge) world).bridge$isFake() || world.getWorldInfo() == null) {
            // Return overworld provider
            return ((net.minecraft.world.World) Sponge.getServer().getWorlds().iterator().next()).provider;
        }
        this.dimensionId = ((WorldServerBridge) world).bridge$getDimensionId();
        return world.provider;
    }

    @Redirect(method = "addServerStatsToSnooper",
        at = @At(value = "INVOKE", target = "Ljava/lang/Integer;valueOf(I)Ljava/lang/Integer;", ordinal = 5))
    @Nullable private Integer onValueOfInteger(final int original) {
        return this.dimensionId;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 900))
    private int getSaveTickInterval(final int tickInterval) {
        if (!isDedicatedServer()) {
            return tickInterval;
        } else if (!this.isServerRunning()) {
            // Don't autosave while server is stopping
            return this.tickCounter + 1;
        }

        final int autoPlayerSaveInterval = SpongeImpl.getGlobalConfigAdapter().getConfig().getWorld().getAutoPlayerSaveInterval();
        if (autoPlayerSaveInterval > 0 && (this.tickCounter % autoPlayerSaveInterval == 0)) {
            this.getPlayerList().saveAllPlayerData();
        }

        this.saveAllWorlds(true);
        // force check to fail as we handle everything above
        return this.tickCounter + 1;
    }

    /**
     * @author blood - June 2nd, 2016
     *
     * @reason To allow per-world auto-save tick intervals or disable auto-saving entirely
     *
     * @param dontLog Whether to log during saving
     */
    @Overwrite
    public void saveAllWorlds(final boolean dontLog) {
        if (!this.impl$enableSaving) {
            return;
        }
        for (final WorldServer world : this.worlds) {
            final boolean save = world.getChunkProvider().canSave() && ((WorldProperties) world.getWorldInfo()).getSerializationBehavior() != SerializationBehaviors.NONE;
            boolean log = !dontLog;

            if (save) {
                // Sponge start - check auto save interval in world config
                if (this.isDedicatedServer() && this.isServerRunning()) {
                    final SpongeConfig<WorldConfig> configAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
                    final int autoSaveInterval = configAdapter.getConfig().getWorld().getAutoSaveInterval();
                    if (log) {
                        log = configAdapter.getConfig().getLogging().logWorldAutomaticSaving();
                    }
                    if (autoSaveInterval <= 0
                            || ((WorldProperties) world.getWorldInfo()).getSerializationBehavior() != SerializationBehaviors.AUTOMATIC) {
                        if (log) {
                            LOGGER.warn("Auto-saving has been disabled for level \'" + world.getWorldInfo().getWorldName() + "\'/"
                                    + world.provider.getDimensionType().getName() + ". "
                                    + "No chunk data will be auto-saved - to re-enable auto-saving set 'auto-save-interval' to a value greater than"
                                    + " zero in the corresponding world config.");
                        }
                        continue;
                    }
                    if (this.tickCounter % autoSaveInterval != 0) {
                        continue;
                    }
                    if (log) {
                        LOGGER.info("Auto-saving chunks for level \'" + world.getWorldInfo().getWorldName() + "\'/"
                                + ((WorldServerBridge) world).bridge$getDimensionId());
                    }
                } else if (log) {
                    LOGGER.info("Saving chunks for level \'" + world.getWorldInfo().getWorldName() + "\'/"
                        + ((WorldServerBridge) world).bridge$getDimensionId());
                }

                // Sponge end
                try {
                    WorldManager.saveWorld(world, false);
                } catch (MinecraftException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Inject(method = "stopServer", at = @At(value = "HEAD"), cancellable = true)
    private void onStopServer(final CallbackInfo ci) {
        // If the server is already stopping, don't allow stopServer to be called off the main thread
        // (from the shutdown handler thread in MinecraftServer)
        if ((Sponge.isServerAvailable() && !((MinecraftServer) Sponge.getServer()).isServerRunning() && !Sponge.getServer().isMainThread())) {
            ci.cancel();
        }
    }

    /**
     * @author Zidane - June 2nd
     * @reason Tells the server to use our WorldManager instead of the arrays, this will
     * work in Forge as well as our WorldManagement system is intended to work with Forge
     * modded worlds.
     *
     * @param dimensionId The dimension id requested
     * @return The world server, or else the overworld.
     */
    @Overwrite
    public WorldServer getWorld(final int dimensionId) {
        return WorldManager.getWorldByDimensionId(dimensionId)
                .orElse(WorldManager.getWorldByDimensionId(0)
                        .orElseThrow(() -> new RuntimeException("Attempt made to get world before overworld is loaded!")
                        )
                );
    }

    @Redirect(method = "callFromMainThread",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/Callable;call()Ljava/lang/Object;",
            remap = false))
    private Object impl$callOnMainThreadWithPhaseState(final Callable<?> callable) throws Exception {
        // This method can be called async while server is stopping
        if (this.serverStopped && !SpongeImplHooks.isMainThread()) {
            return callable.call();
        }

        final Object value;
        try (final BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(callable)) {
            context.buildAndSwitch();
            value = callable.call();
        } catch (Exception e) {
            throw e;
        }
        return value;
    }

    @Nullable
    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;runTask(Ljava/util/concurrent/FutureTask;Lorg/apache/logging/log4j/Logger;)Ljava/lang/Object;"))
    private Object onRun(final FutureTask<?> task, final Logger logger) {
        return SpongeImplHooks.onUtilRunTask(task, logger);
    }

    @Inject(method = "addServerInfoToCrashReport", at = @At("RETURN"), cancellable = true)
    private void onCrashReport(final CrashReport report, final CallbackInfoReturnable<CrashReport> cir) {
        report.makeCategory("Sponge PhaseTracker").addDetail("Phase Stack", CauseTrackerCrashHandler.INSTANCE);
        cir.setReturnValue(report);
    }

    /**
     * @author unknown
     * @reason uses the world manager to update.
     */
    @Overwrite
    public void setDifficultyForAllWorlds(final EnumDifficulty difficulty) {
        WorldManager.updateServerDifficulty();
    }
}
