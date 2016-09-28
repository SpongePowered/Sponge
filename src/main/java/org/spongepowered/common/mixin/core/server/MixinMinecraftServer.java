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
import static com.google.common.base.Preconditions.checkState;

import co.aikar.timings.TimingsManager;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveHandler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.world.WorldManager;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, ConsoleSource, IMixinSubject, IMixinCommandSource, IMixinCommandSender,
        IMixinMinecraftServer {

    @Shadow @Final private static Logger LOG;
    @Shadow @Final public Profiler theProfiler;
    @Shadow @Final public long[] tickTimeArray;
    @Shadow private boolean enableBonusChest;
    @Shadow private int tickCounter;
    @Shadow private String motd;
    @Shadow public WorldServer[] worldServers;

    @Shadow public abstract void setDifficultyForAllWorlds(EnumDifficulty difficulty);
    @Shadow public abstract void addChatMessage(ITextComponent message);
    @Shadow public abstract void initiateShutdown();
    @Shadow public abstract boolean isServerInOnlineMode();
    @Shadow public abstract boolean isServerRunning();
    @Shadow public abstract boolean canStructuresSpawn();
    @Shadow public abstract boolean isHardcore();
    @Shadow public abstract boolean isSinglePlayer();
    @Shadow public abstract String getFolderName();
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow public abstract EnumDifficulty getDifficulty();
    @Shadow public abstract GameType getGameType();
    @Shadow protected abstract void setUserMessage(String message);
    @Shadow protected abstract void outputPercentRemaining(String message, int percent);
    @Shadow protected abstract void clearCurrentTask();
    @Shadow protected abstract void convertMapIfNeeded(String worldNameIn);
    @Shadow public abstract void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn);
    @Shadow public abstract boolean getAllowNether();
    @Shadow public abstract DataFixer getDataFixer();
    @Shadow public abstract int getMaxPlayerIdleMinutes();
    @Shadow public abstract void shadow$setPlayerIdleTimeout(int timeout);
    @Shadow public abstract boolean isDedicatedServer();

    private ResourcePack resourcePack;
    private boolean enableSaving = true;
    private GameProfileManager profileManager;
    private MessageChannel broadcastChannel = MessageChannel.TO_ALL;

    @SuppressWarnings("unchecked")
    @Override
    public Optional<World> getWorld(String worldName) {
        return (Optional<World>) (Object) WorldManager.getWorld(worldName);
    }

    @Override
    public ChunkLayout getChunkLayout() {
        return SpongeChunkLayout.instance;
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return WorldManager.getWorldProperties(worldName);
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return WorldManager.getAllWorldProperties();
    }

    @Override
    public MessageChannel getBroadcastChannel() {
        return this.broadcastChannel;
    }

    @Override
    public void setBroadcastChannel(MessageChannel channel) {
        this.broadcastChannel = checkNotNull(channel, "channel");
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.empty();
    }

    @Override
    public boolean hasWhitelist() {
        return this.getPlayerList().isWhiteListEnabled();
    }

    @Override
    public void setHasWhitelist(boolean enabled) {
        this.getPlayerList().setWhiteListEnabled(enabled);
    }

    @Override
    public boolean getOnlineMode() {
        return isServerInOnlineMode();
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Player> getOnlinePlayers() {
        if (getPlayerList() == null || getPlayerList().getPlayerList() == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List) getPlayerList().getPlayerList());
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        if (getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) getPlayerList().getPlayerByUUID(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        if (getPlayerList() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) getPlayerList().getPlayerByUsername(name));
    }

    @Override
    public Text getMotd() {
        return SpongeTexts.fromLegacy(this.motd);
    }

    @Override
    public int getMaxPlayers() {
        if (getPlayerList() == null) {
            return 0;
        }
        return getPlayerList().getMaxPlayers();
    }

    @Override
    public int getRunningTimeTicks() {
        return this.tickCounter;
    }

    @Override
    public double getTicksPerSecond() {
        double nanoSPerTick = MathHelper.average(this.tickTimeArray);
        // Cap at 20 TPS
        return 1000 / Math.max(50, nanoSPerTick / 1000000);
    }

    @Override
    public String getIdentifier() {
        return getName();
    }

    @Override
    public String getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_SYSTEM;
    }

    @Override
    public Tristate permDefault(String permission) {
        return Tristate.TRUE;
    }

    @Override
    public ConsoleSource getConsole() {
        return this;
    }

    @Override
    public ICommandSender asICommandSender() {
        return (MinecraftServer) (Object) this;
    }

    @Override
    public CommandSource asCommandSource() {
        return this;
    }

    @Override
    public void shutdown() {
        initiateShutdown();
    }

    @Override
    public void shutdown(Text kickMessage) {
        for (Player player : getOnlinePlayers()) {
            player.kick(kickMessage);
        }

        initiateShutdown();
    }

    @Inject(method = "stopServer()V", at = @At("HEAD"))
    public void onServerStopping(CallbackInfo ci) {
        ((MinecraftServer) (Object) this).getPlayerProfileCache().save();
    }

    /**
     * @author blood - December 23rd, 2015
     * @author Zidane - March 13th, 2016
     *
     * Sponge re-writes this method because we take control of loading existing Sponge dimensions, migrate old worlds to our standard, and
     * configuration checks.
     * @reason Add multiworld support
     */
    @Overwrite
    protected void loadAllWorlds(String overworldFolder, String worldName, long seed, WorldType type, String generatorOptions) {
        SpongeCommonEventFactory.convertingMapFormat = true;
        this.convertMapIfNeeded(overworldFolder);
        SpongeCommonEventFactory.convertingMapFormat = false;
        this.setUserMessage("menu.loadingLevel");

        WorldManager.loadAllWorlds(worldName, seed, type, generatorOptions);

        this.getPlayerList().setPlayerManager(new WorldServer[]{WorldManager.getWorldByDimensionId(0).get()});
        this.setDifficultyForAllWorlds(this.getDifficulty());
        this.initialWorldChunkLoad();
    }

    /**
     * @author Zidane - March 13th, 2016
     *
     * @reason Sponge has a config option for determining if we'll
     * generate spawn on server start. I enforce that here.
     */
    @Overwrite
    protected void initialWorldChunkLoad() {
        for (WorldServer worldServer: this.worldServers) {
            this.prepareSpawnArea(worldServer);
        }
        this.clearCurrentTask();
    }

    @Override
    public void prepareSpawnArea(WorldServer worldServer) {
        if (!((WorldProperties) worldServer.getWorldInfo()).doesGenerateSpawnOnLoad()) {
            return;
        }

        final CauseTracker causeTracker = ((IMixinWorldServer) worldServer).getCauseTracker();
        if (CauseTracker.ENABLED) {
            causeTracker.switchToPhase(GenerationPhase.State.TERRAIN_GENERATION, PhaseContext.start()
                    .add(NamedCause.source(worldServer))
                    .addCaptures()
                    .complete());
        }
        int i = 0;
        this.setUserMessage("menu.generatingTerrain");
        LOG.info("Preparing start region for level {} ({})", ((IMixinWorldServer) worldServer).getDimensionId(), ((World) worldServer).getName());
        BlockPos blockpos = worldServer.getSpawnPoint();
        long j = MinecraftServer.getCurrentTimeMillis();
        for (int k = -192; k <= 192 && this.isServerRunning(); k += 16) {
            for (int l = -192; l <= 192 && this.isServerRunning(); l += 16) {
                long i1 = MinecraftServer.getCurrentTimeMillis();

                if (i1 - j > 1000L) {
                    this.outputPercentRemaining("Preparing spawn area", i * 100 / 625);
                    j = i1;
                }

                ++i;
                worldServer.getChunkProvider().provideChunk(blockpos.getX() + k >> 4, blockpos.getZ() + l >> 4);
            }
        }
        this.clearCurrentTask();
        if (CauseTracker.ENABLED) {
            causeTracker.completePhase();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Optional<World> loadWorld(UUID uuid) {
        return (Optional) WorldManager.loadWorld(uuid);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<World> loadWorld(WorldProperties properties) {
        return (Optional) WorldManager.loadWorld(properties);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<World> loadWorld(String worldName) {
        return (Optional) WorldManager.loadWorld(worldName);
    }

    @Override
    public WorldProperties createWorldProperties(String folderName, WorldArchetype archetype) {
        return WorldManager.createWorldProperties(folderName, archetype);
    }

    @Override
    public boolean unloadWorld(World world) {
        return WorldManager.unloadWorld((WorldServer) world, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<World> getWorlds() {
        return (Collection<World>) (Object) Collections.unmodifiableCollection(WorldManager.getWorlds());
    }

    @Override
    public Optional<World> getWorld(UUID uniqueId) {
        for (WorldServer worldserver : WorldManager.getWorlds()) {
            if (((World) worldserver).getUniqueId().equals(uniqueId)) {
                return Optional.of((World) worldserver);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<WorldProperties> getDefaultWorld() {
        return WorldManager.getWorldByDimensionId(0).map(worldServer -> ((World) worldServer).getProperties());
    }

    @Override
    public String getDefaultWorldName() {
        checkState(getFolderName() != null, "Attempt made to grab default world name too early!");
        return getFolderName();
    }

    @Override
    public Collection<WorldProperties> getUnloadedWorlds() {
        try {
            return WorldManager.getUnloadedWorlds();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(UUID uniqueId) {
        return WorldManager.getWorldProperties(uniqueId);
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(WorldProperties worldProperties, String copyName) {
        return WorldManager.copyWorld(worldProperties, copyName);
    }

    @Override
    public Optional<WorldProperties> renameWorld(WorldProperties worldProperties, String newName) {
        return WorldManager.renameWorld(worldProperties, newName);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
        return WorldManager.deleteWorld(worldProperties);
    }

    @Override
    public boolean saveWorldProperties(WorldProperties properties) {
        return WorldManager.saveWorldProperties(properties);
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameProfileManager getGameProfileManager() {
        if (this.profileManager == null) {
            this.profileManager = new SpongeProfileManager();
        }
        return this.profileManager;
    }

    @Override
    public Optional<ResourcePack> getDefaultResourcePack() {
        return Optional.ofNullable(this.resourcePack);
    }

    @Inject(method = "setResourcePack(Ljava/lang/String;Ljava/lang/String;)V", at = @At("HEAD") )
    public void onSetResourcePack(String url, String hash, CallbackInfo ci) {
        if (url.length() == 0) {
            this.resourcePack = null;
        } else {
            try {
                this.resourcePack = SpongeResourcePack.create(url, hash);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setSaveEnabled(boolean enabled) {
        this.enableSaving = enabled;
    }

    @Inject(method = "saveAllWorlds(Z)V", at = @At("HEAD"), cancellable = true)
    private void onSaveWorlds(boolean dontLog, CallbackInfo ci) {
        if (!this.enableSaving) {
            ci.cancel();
        }
    }

    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        return WorldManager.getWorldByDimensionId(0).map(worldServer -> (Scoreboard) worldServer.getScoreboard());
    }

    private void onTabCompleteChat(ICommandSender sender, String input, CallbackInfoReturnable<List<String>> cir, List<String> completions) {
        TabCompleteEvent.Chat event = SpongeEventFactory.createTabCompleteEventChat(Cause.source(sender).build(),
                ImmutableList.copyOf(completions), completions, input);
        Sponge.getEventManager().post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(new ArrayList<>());
        } else {
            cir.setReturnValue(event.getTabCompletions());
        }
    }

    @Surrogate
    private void onTabCompleteChat(ICommandSender sender, String input, BlockPos pos, boolean p_184104_4_, CallbackInfoReturnable<List<String>> cir,
            List<String> list, boolean flag, boolean flag1, List<String> list1) {
        onTabCompleteChat(sender, input, cir, list);
    }

    @Inject(method = "getTabCompletions", at = @At(value = "RETURN", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void onTabCompleteChat(ICommandSender sender, String input, BlockPos pos, boolean p_184104_4_, CallbackInfoReturnable<List<String>> cir,
            List<String> list, String[] a, String b, String[] c, int d, int e) {
        onTabCompleteChat(sender, input, cir, list);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    public void onServerTickStart(CallbackInfo ci) {
        TimingsManager.FULL_SERVER_TICK.startTiming();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    public void onServerTickEnd(CallbackInfo ci) {
        int lastAnimTick = SpongeCommonEventFactory.lastAnimationPacketTick;
        int lastPrimaryTick = SpongeCommonEventFactory.lastPrimaryPacketTick;
        int lastSecondaryTick = SpongeCommonEventFactory.lastSecondaryPacketTick;
        EntityPlayerMP player = SpongeCommonEventFactory.lastAnimationPlayer;
        if (player != null && lastAnimTick != lastPrimaryTick && lastAnimTick != lastSecondaryTick && lastAnimTick != 0 && lastAnimTick - lastPrimaryTick > 3 && lastAnimTick - lastSecondaryTick > 3) {
            InteractBlockEvent.Primary event = SpongeEventFactory.createInteractBlockEventPrimaryMainHand(Cause.of(NamedCause.source(player)), Optional.empty(), BlockSnapshot.NONE, Direction.NONE);
            Sponge.getEventManager().post(event);
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = 0;
        TimingsManager.FULL_SERVER_TICK.stopTiming();
    }

    private int dimensionId;

    @Redirect(method = "addServerStatsToSnooper", at = @At(value = "FIELD", target = "Lnet/minecraft/world/WorldServer;provider:Lnet/minecraft/world/WorldProvider;", opcode = Opcodes.GETFIELD))
    private WorldProvider onGetWorldProviderForSnooper(WorldServer world) {
        this.dimensionId = WorldManager.getDimensionId(world);
        return world.provider;
    }

    @ModifyArg(method = "addServerStatsToSnooper", at = @At(value = "INVOKE", target = "Ljava/lang/Integer;valueOf(I)Ljava/lang/Integer;", ordinal = 5))
    private int onValueOfInteger(int dimensionId) {
        return this.dimensionId;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 900))
    private int getSaveTickInterval(int tickInterval) {
        if (!isDedicatedServer()) {
            return tickInterval;
        } else if (!this.isServerRunning()) {
            // Don't autosave while server is stopping
            return this.tickCounter + 1;
        }

        int autoPlayerSaveInterval = SpongeImpl.getGlobalConfig().getConfig().getWorld().getAutoPlayerSaveInterval();
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
    protected void saveAllWorlds(boolean dontLog)
    {
        for (WorldServer worldserver : this.worldServers)
        {
            if (worldserver != null)
            {
                // Sponge start - check auto save interval in world config
                if (this.isDedicatedServer() && this.isServerRunning()) {
                    final IMixinWorldServer spongeWorld = (IMixinWorldServer) worldserver;
                    final int autoSaveInterval = spongeWorld.getActiveConfig().getConfig().getWorld().getAutoSaveInterval();
                    final boolean logAutoSave = spongeWorld.getActiveConfig().getConfig().getLogging().worldAutoSaveLogging();
                    if (autoSaveInterval <= 0 || ((WorldProperties) worldserver.getWorldInfo()).getSerializationBehavior() != SerializationBehaviors.AUTOMATIC) {
                        if (logAutoSave) {
                            LOG.warn("Auto-saving has been disabled for level \'" + worldserver.getWorldInfo().getWorldName() + "\'/"
                                        + worldserver.provider.getDimensionType().getName() + ". "
                                        + "No chunk data will be auto-saved - to re-enable auto-saving set 'auto-save-interval' to a value greater than zero in the corresponding world config.");
                        }
                        continue;
                    }
                    if (this.tickCounter % autoSaveInterval != 0) {
                        continue;
                    }
                    if (logAutoSave) {
                        LOG.info("Auto-saving chunks for level \'" + worldserver.getWorldInfo().getWorldName() + "\'/"
                                    + worldserver.provider.getDimensionType().getName());
                    }
                } else if (!dontLog) {
                    LOG.info("Saving chunks for level \'" + worldserver.getWorldInfo().getWorldName() + "\'/"
                                + worldserver.provider.getDimensionType().getName());
                }
                // Sponge end
                try {
                    WorldManager.saveWorld(worldserver, false);
                } catch (MinecraftException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public int getPlayerIdleTimeout() {
        return this.getMaxPlayerIdleMinutes();
    }

    @Intrinsic
    public void server$setPlayerIdleTimeout(int timeout) {
        this.shadow$setPlayerIdleTimeout(timeout);
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
    public WorldServer worldServerForDimension(int dimensionId) {
        return WorldManager.getWorldByDimensionId(dimensionId)
                .orElse(WorldManager.getWorldByDimensionId(0)
                        .orElseThrow(() -> new RuntimeException("Attempt made to get world before overworld is loaded!")
                        )
                );
    }
}
