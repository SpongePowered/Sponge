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
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.command.ICommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedDataStorage;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.SerializationBehaviors;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldArchetype;
import org.spongepowered.api.world.chunk.ChunkTicketManager;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Intrinsic;
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
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.CauseTrackerCrashHandler;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.world.task.CopyWorldTask;
import org.spongepowered.common.world.task.DeleteWorldTask;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, ConsoleSource, IMixinSubject, IMixinCommandSource, IMixinCommandSender,
        IMixinMinecraftServer {

    @Shadow @Final private static Logger LOGGER;

    @Shadow @Final public Profiler profiler;
    @Shadow @Final public long[] tickTimeArray;
    @Shadow @Final private DataFixer dataFixer;

    @Shadow public Thread serverThread;

    @Shadow private boolean serverStopped;
    @Shadow private int tickCounter;
    @Shadow private String motd;

    @Shadow public abstract void sendMessage(ITextComponent message);
    @Shadow public abstract void initiateShutdown();
    @Shadow public abstract boolean isServerInOnlineMode();
    @Shadow public abstract boolean isServerRunning();
    @Shadow public abstract String getFolderName();
    @Shadow public abstract PlayerList getPlayerList();
    @Shadow public abstract EnumDifficulty getDifficulty();
    @Shadow protected abstract void clearCurrentTask();
    @Shadow public abstract int getMaxPlayerIdleMinutes();
    @Shadow public abstract void shadow$setPlayerIdleTimeout(int timeout);
    @Shadow public abstract boolean isDedicatedServer();
    @Shadow protected abstract void setUserMessage(ITextComponent p_200245_1_);
    @Shadow protected abstract void setCurrentTaskAndPercentDone(ITextComponent p_200250_1_, int p_200250_2_);
    @Shadow public abstract boolean allowSpawnMonsters();
    @Shadow public abstract boolean getCanSpawnAnimals();

    private ResourcePack resourcePack;
    private boolean enableSaving = true;
    private GameProfileManager profileManager;
    private MessageChannel broadcastChannel = MessageChannel.TO_ALL;

    @Override
    public ChunkLayout getChunkLayout() {
        return SpongeChunkLayout.instance;
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
        return this.getPlayerList().whiteListEnforced;
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
        if (getPlayerList() == null || getPlayerList().getPlayers() == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List) getPlayerList().getPlayers());
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
    public ICommandSource asICommandSender() {
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

    /**
     * @author blood - December 23rd, 2015
     * @author Zidane - March 13th, 2016
     * @author Zidane - Feburary 18th, 2019
     *
     * @reason Sponge re-writes this method because we take control of loading existing Sponge dimensions, migrate old worlds to our standard, and
     * configuration checks.
     * @reason Update to Minecraft 1.13
     */
    @Overwrite
    public void loadAllWorlds(String saveFolder, String worldFolder, long seed, WorldType type, JsonElement generatorOptions) {
        this.getWorldLoader().loadKnownWorlds(saveFolder, worldFolder, seed, type, generatorOptions);
    }

    /**
     * @author Zidane - March 13th, 2016
     * @author Zidane - Feburary 18th, 2019
     *
     * @reason Sponge has a config option for determining if we'll generate spawn on server start. I enforce that here.
     * @reason Update to Minecraft 1.13
     */
    @Overwrite
    public void initialWorldChunkLoad(WorldSavedDataStorage overworldStorage) {
        for (WorldServer world: this.getWorldLoader().getWorlds()) {
            if (world.dimension.getType() == DimensionType.OVERWORLD) {
                this.prepareSpawnArea(overworldStorage, world);
            } else {
                this.prepareSpawnArea(new WorldSavedDataStorage(world.getSaveHandler()), world);
            }
        }
        this.clearCurrentTask();
    }

    @Override
    public void prepareSpawnArea(WorldSavedDataStorage storage, WorldServer world) {
        this.setUserMessage(new TextComponentTranslation("menu.generatingTerrain"));
        LOGGER.info("Preparing start region for dimension " + DimensionType.getKey(world.dimension.getType()));
        BlockPos blockpos = world.getSpawnPoint();
        List<ChunkPos> list = Lists.newArrayList();
        Set<ChunkPos> set = Sets.newConcurrentHashSet();
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (int j1 = -192; j1 <= 192 && this.isServerRunning(); j1 += 16) {
            for (int k1 = -192; k1 <= 192 && this.isServerRunning(); k1 += 16) {
                list.add(new ChunkPos(blockpos.getX() + j1 >> 4, blockpos.getZ() + k1 >> 4));
            }

            // Sponge Start - Load chunks for world, not just overworld
            CompletableFuture<?> completablefuture = world.getChunkProvider().loadChunks(list, (p_201701_1_) -> {
                set.add(p_201701_1_.getPos());
            });
            // Sponge End

            while (!completablefuture.isDone()) {
                try {
                    completablefuture.get(1L, TimeUnit.SECONDS);
                } catch (InterruptedException interruptedexception) {
                    throw new RuntimeException(interruptedexception);
                } catch (ExecutionException executionexception) {
                    if (executionexception.getCause() instanceof RuntimeException) {
                        throw (RuntimeException) executionexception.getCause();
                    }

                    throw new RuntimeException(executionexception.getCause());
                } catch (TimeoutException var22) {
                    this.setCurrentTaskAndPercentDone(new TextComponentTranslation("menu.preparingSpawn"), set.size() * 100 / 625);
                }
            }

            this.setCurrentTaskAndPercentDone(new TextComponentTranslation("menu.preparingSpawn"), set.size() * 100 / 625);
        }

        LOGGER.info("Time elapsed: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        // Sponge Start - Get forced chunks for passed in world
        ForcedChunksSaveData forcedchunkssavedata = storage.get(world.dimension.getType(), ForcedChunksSaveData::new, "chunks");

        if (forcedchunkssavedata != null) {
            LongIterator longiterator = forcedchunkssavedata.getChunks().iterator();

            while (longiterator.hasNext()) {
                this.setCurrentTaskAndPercentDone(new TextComponentTranslation("menu.loadingForcedChunks", world.dimension.getType()),
                    forcedchunkssavedata.getChunks().size() * 100 / 625);
                long l1 = longiterator.nextLong();
                ChunkPos chunkpos = new ChunkPos(l1);
                world.getChunkProvider().getChunk(chunkpos.x, chunkpos.z, true, true);
            }
        // Sponge End
        }

        this.clearCurrentTask();
    }

    @Override
    public Optional<World> getWorld(String folderName) {
        return (Optional<World>) (Object) this.getWorldLoader().getWorld(folderName);
    }

    @Override
    public Collection<World> getWorlds() {
        return (Collection<World>) (Object) this.getWorldLoader().getWorlds();
    }

    @Override
    public Optional<World> loadWorld(UUID uniqueId) {
        return (Optional<World>) (Object) this.getWorldLoader().loadWorld(uniqueId);
    }

    @Override
    public Optional<World> loadWorld(WorldProperties properties) {
        return (Optional<World>) (Object) this.getWorldLoader().loadWorld((WorldInfo) properties);
    }

    @Override
    public Optional<World> loadWorld(String folderName) {
        return (Optional<World>) (Object) this.getWorldLoader().loadWorld(folderName);
    }

    @Override
    public Optional<WorldProperties> createWorldProperties(String folderName, WorldArchetype archetype) {
        return (Optional<WorldProperties>) (Object) this.getWorldLoader().createWorldInfo(folderName, archetype);
    }

    @Override
    public boolean unloadWorld(World world) {
        return this.getWorldLoader().unloadWorld((WorldServer) world, false);
    }

    @Override
    public Optional<World> getWorld(UUID uniqueId) {
        return (Optional<World>) (Object) this.getWorldLoader().getWorld(uniqueId);
    }

    @Override
    public Optional<WorldProperties> getDefaultWorld() {
        return this.getWorldLoader().getWorld(DimensionType.OVERWORLD).map(world -> (WorldProperties) world.getWorldInfo());
    }

    @Override
    public String getDefaultWorldName() {
        checkState(this.getFolderName() != null, "Attempt made to grab the save folder too early!");
        return this.getFolderName();
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(UUID uniqueId) {
        return (Optional<WorldProperties>) (Object) this.getWorldLoader().getWorldInfo(uniqueId);
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return (Optional<WorldProperties>) (Object) this.getWorldLoader().getWorldInfo(worldName);
    }

    @Override
    public boolean saveWorldProperties(WorldProperties properties) {
        // TODO (1.13) - Zidane

        return false;
    }

    @Override
    public Collection<WorldProperties> getUnloadedWorlds() {
        return (Collection<WorldProperties>) (Object) this.getWorldLoader().getUnloadedWorldInfos();
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return (Collection<WorldProperties>) (Object) this.getWorldLoader().getWorldInfos();
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(String folderName, String copyName) {
        checkNotNull(folderName);
        checkNotNull(copyName);

        final Path saveFolder = Paths.get(this.getFolderName());

        final WorldInfo info = this.getWorldLoader().getWorldInfo(folderName).orElseGet(() -> {
            // We don't know of this info, could have been copied in. For performance, cache the world data
            final Path path = saveFolder.resolve(folderName);
            if (Files.notExists(path)) {
                return null;
            }

            final ISaveHandler handler = new AnvilSaveHandler(saveFolder.toFile(), folderName, (MinecraftServer) (Object) this, this.dataFixer);
            final WorldInfo foundInfo = handler.loadWorldInfo();
            this.getWorldLoader().registerWorldInfo(folderName, foundInfo);
            return foundInfo;
        });

        if (info == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        final WorldServer world = this.getWorldLoader().getWorld(folderName).orElse(null);

        final CompletableFuture<Optional<WorldInfo>> future = SpongeImpl.getAsyncScheduler().submit(new CopyWorldTask(this.getWorldLoader(),
            saveFolder, info, world, copyName));
        if (world != null) {
            future.thenAccept(result -> {
                ((IMixinMinecraftServer) SpongeImpl.getServer()).setSaveEnabled(true);

                result.ifPresent(copyInfo -> {
                    ((IMixinWorldInfo) copyInfo).setDimensionType(null);
                    ((IMixinWorldInfo) copyInfo).setUniqueId(null);
                    this.getWorldLoader().registerWorldInfo(copyName, copyInfo);
                });
            });
        } else {
            future.thenAccept(result -> result.ifPresent(copyInfo -> {
                ((IMixinWorldInfo) copyInfo).setDimensionType(null);
                ((IMixinWorldInfo) copyInfo).setUniqueId(null);
                this.getWorldLoader().registerWorldInfo(copyName, copyInfo);
            }));
        }

        return (CompletableFuture<Optional<WorldProperties>>) (Object) future;
    }

    @Override
    public Optional<WorldProperties> renameWorld(String oldFolderName, String newFolderName) {
        checkNotNull(oldFolderName);
        checkNotNull(newFolderName);

        final Path saveFolder = Paths.get(this.getFolderName());
        final Path oldWorldFolder = saveFolder.resolve(oldFolderName);
        final Path newWorldFolder = oldWorldFolder.resolveSibling(newFolderName);

        if (Files.exists(newWorldFolder)) {
            return Optional.empty();
        }

        final WorldServer world = this.getWorldLoader().getWorld(oldFolderName).orElse(null);
        WorldInfo info = null;

        if (world != null) {
            if (!this.getWorldLoader().unloadWorld(world, false)) {
                return Optional.empty();
            }

            info = world.getWorldInfo();
        }

        try {
            Files.move(oldWorldFolder, newWorldFolder);
        } catch (IOException e) {
            return Optional.empty();
        }

        if (info != null) {
            this.getWorldLoader().unregisterWorldInfo(info);
        } else {
            this.getWorldLoader().unregisterWorldInfo(oldFolderName);
        }

        final ISaveHandler handler = new AnvilSaveHandler(saveFolder.toFile(), newFolderName, (MinecraftServer) (Object) this, this.dataFixer);
        info = handler.loadWorldInfo();

        if (info != null) {
            ((IMixinWorldInfo) info).createConfig();
            this.getWorldLoader().registerWorldInfo(newFolderName, info);
        }

        return Optional.ofNullable((WorldProperties) info);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(String folderName) {
        checkNotNull(folderName);

        final Path saveFolder = Paths.get(this.getFolderName());
        final WorldServer world = this.getWorldLoader().getWorld(folderName).orElse(null);

        if (world == null) {
            return SpongeImpl.getAsyncScheduler().submit(new DeleteWorldTask(this.getWorldLoader(), saveFolder, folderName));
        }

        if (!this.getWorldLoader().unloadWorld(folderName, false)) {
            return CompletableFuture.completedFuture(false);
        }

        return SpongeImpl.getAsyncScheduler().submit(new DeleteWorldTask(this.getWorldLoader(), saveFolder, folderName));
    }

    /**
     * @author blood - June 2nd, 2016
     * @author Zidane - Feburary 18th, 2019
     *
     * @reason To allow per-world auto-save tick intervals or disable auto-saving entirely
     * @reason Update to Minecraft 1.13
     */
    @Overwrite
    public void saveAllWorlds(boolean isSilent) {
        if (!this.enableSaving) {
            return;
        }

        for (WorldServer world : this.getWorldLoader().getWorlds()) {
            if (!world.disableLevelSaving) {
                // Sponge start - check auto save interval in world config
                if (this.isDedicatedServer() && this.isServerRunning()) {
                    final IMixinWorldServer spongeWorld = (IMixinWorldServer) world;
                    final int autoSaveInterval = spongeWorld.getActiveConfig().getConfig().getWorld().getAutoSaveInterval();
                    final boolean logAutoSave = !isSilent && spongeWorld.getActiveConfig().getConfig().getLogging().worldAutoSaveLogging();
                    if (autoSaveInterval <= 0
                        || ((WorldProperties) world.getWorldInfo()).getSerializationBehavior() != SerializationBehaviors.AUTOMATIC) {
                        if (logAutoSave) {
                            LOGGER.warn("Auto-saving has been disabled for level \'" + world.getWorldInfo().getWorldName() + "\'/"
                                + world.getDimension().getType() + ". "
                                + "No chunk data will be auto-saved - to re-enable auto-saving set 'auto-save-interval' to a value greater than"
                                + " zero in the corresponding world config.");
                        }
                        continue;
                    }
                    if (this.tickCounter % autoSaveInterval != 0) {
                        continue;
                    }
                    if (logAutoSave) {
                        LOGGER.info("Auto-saving chunks for level \'" + world.getWorldInfo().getWorldName() + "\'/"
                            + world.dimension.getType());
                    }
                } else if (!isSilent) {
                    LOGGER.info("Saving chunks for level \'" + world.getWorldInfo().getWorldName() + "\'/"
                        + world.dimension.getType());
                }
                // Sponge end

                try {
                    world.saveAllChunks(true, null);
                } catch (SessionLockException sessionlockexception) {
                    LOGGER.warn(sessionlockexception.getMessage());
                }
            }
        }
    }
    /**
     * @author Zidane - Feburary 18th, 2019
     *
     * @reason Honor the server difficulty as well as difficulties set via mods/plugins
     */
    @Overwrite
    public void setDifficultyForAllWorlds(EnumDifficulty difficulty) {
        final EnumDifficulty serverDifficulty = SpongeImpl.getServer().getDifficulty();

        for (WorldServer world : this.getWorldLoader().getWorlds()) {
            this.adjustWorldForDifficulty(world, ((IMixinWorldInfo) world.getWorldInfo()).hasCustomDifficulty() ? world.getWorldInfo()
                .getDifficulty() : serverDifficulty, false);
        }
    }

    @Override
    public void adjustWorldForDifficulty(WorldServer world, EnumDifficulty difficulty, boolean isCustom) {
        if (world.getWorldInfo().isHardcore()) {
            difficulty = EnumDifficulty.HARD;
            world.setAllowedSpawnTypes(true, true);
        } else if (SpongeImpl.getServer().isSinglePlayer()) {
            world.setAllowedSpawnTypes(world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
        } else {
            world.setAllowedSpawnTypes(this.allowSpawnMonsters(), this.getCanSpawnAnimals());
        }

        if (!isCustom) {
            ((IMixinWorldInfo) world.getWorldInfo()).forceSetDifficulty(difficulty);
        } else {
            world.getWorldInfo().setDifficulty(difficulty);
        }
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

    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        return this.getWorldLoader().getWorld(DimensionType.OVERWORLD).map(world -> (Scoreboard) world.getScoreboard());
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
        if (SpongeCommonEventFactory.lastAnimationPlayer != null) {
            EntityPlayerMP player = SpongeCommonEventFactory.lastAnimationPlayer.get();
            if (player != null && lastAnimTick != lastPrimaryTick && lastAnimTick != lastSecondaryTick && lastAnimTick != 0 && lastAnimTick - lastPrimaryTick > 3 && lastAnimTick - lastSecondaryTick > 3) {
                BlockSnapshot blockSnapshot = BlockSnapshot.NONE;

                final RayTraceResult result = SpongeImplHooks.rayTraceEyes(player, SpongeImplHooks.getBlockReachDistance(player) + 1);
                // Hit non-air block
                if (result != null && result.getBlockPos() != null) {
                    return;
                }

                Sponge.getCauseStackManager().pushCause(player);
                Sponge.getCauseStackManager().addContext(EventContextKeys.OWNER, (User) player);
                Sponge.getCauseStackManager().addContext(EventContextKeys.NOTIFIER, (User) player);
                if (!player.getHeldItemMainhand().isEmpty() && SpongeCommonEventFactory.callInteractItemEventPrimary(player, player.getHeldItemMainhand(), EnumHand.MAIN_HAND, null, blockSnapshot).isCancelled()) {
                    SpongeCommonEventFactory.lastAnimationPacketTick = 0;
                    Sponge.getCauseStackManager().popCause();
                    return;

                }

                SpongeCommonEventFactory.callInteractBlockEventPrimary(player, player.getHeldItemMainhand(), EnumHand.MAIN_HAND, null);

                Sponge.getCauseStackManager().popCause();
            }
        }
        SpongeCommonEventFactory.lastAnimationPacketTick = 0;
        TimingsManager.FULL_SERVER_TICK.stopTiming();
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

    @Inject(method = "stopServer", at = @At(value = "HEAD"), cancellable = true)
    public void onStopServer(CallbackInfo ci) {
        // If the server is already stopping, don't allow stopServer to be called off the main thread
        // (from the shutdown handler thread in MinecraftServer)
        if ((Sponge.isServerAvailable() && !((MinecraftServer) Sponge.getServer()).isServerRunning() && !Sponge.getServer().onMainThread())) {
            ci.cancel();
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

    @Override
    public boolean onMainThread() {
        return this.serverThread == Thread.currentThread();
    }

    @Redirect(method = "callFromMainThread", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Callable;call()Ljava/lang/Object;", remap = false))
    public Object onCall(Callable<?> callable) throws Exception {
        // This method can be called async while server is stopping
        if (this.serverStopped && !SpongeImplHooks.isMainThread()) {
            return callable.call();
        }

        Object value;
        try (BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(callable)) {
            context.buildAndSwitch();
            value = callable.call();
        } catch (Exception e) {
            throw e;
        }
        return value;
    }

    @Redirect(method = "updateTimeLightAndEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;runTask(Ljava/util/concurrent/FutureTask;Lorg/apache/logging/log4j/Logger;)Ljava/lang/Object;"))
    private Object onRun(FutureTask<?> task, Logger logger) {
        return SpongeImplHooks.onUtilRunTask(task, logger);
    }

    @Override
    public DataFixer getDataFixer() {
        return this.dataFixer;
    }

    @Inject(method = "addServerInfoToCrashReport", at = @At("RETURN"), cancellable = true)
    private void onCrashReport(CrashReport report, CallbackInfoReturnable<CrashReport> cir) {
        report.makeCategory("Sponge PhaseTracker").addDetail("Phase Stack", CauseTrackerCrashHandler.INSTANCE);
        cir.setReturnValue(report);
    }
}
