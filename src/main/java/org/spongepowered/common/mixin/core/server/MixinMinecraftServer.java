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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.command.TabCompleteEvent;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.registry.type.world.DimensionTypeRegistryModule;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.ServerUtils;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.WorldMigrator;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, ConsoleSource, IMixinSubject, IMixinCommandSource, IMixinCommandSender,
        IMixinMinecraftServer {

    @Shadow @Final private static Logger logger;
    @Shadow @Final public Profiler theProfiler;
    @Shadow @Final public long[] tickTimeArray;
    @Shadow private boolean enableBonusChest;
    @Shadow private int tickCounter;
    @Shadow private String motd;

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
    @Shadow public abstract WorldSettings.GameType getGameType();
    @Shadow protected abstract void setUserMessage(String message);
    @Shadow protected abstract void outputPercentRemaining(String message, int percent);
    @Shadow protected abstract void clearCurrentTask();
    @Shadow protected abstract void convertMapIfNeeded(String worldNameIn);
    @Shadow protected abstract void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn);
    @Shadow public abstract boolean getAllowNether();
    @Shadow public abstract DataFixer getDataFixer();

    private ResourcePack resourcePack;
    private boolean enableSaving = true;
    private boolean preparingChunks = false;
    private GameProfileManager profileManager;
    private MessageChannel broadcastChannel = MessageChannel.TO_ALL;

    @Override
    public Optional<World> getWorld(String worldName) {
        for (World world : getWorlds()) {
            if (world.getName().equals(worldName)) {
                return Optional.of(world);
            }
        }
        return Optional.empty();
    }

    @Override
    public ChunkLayout getChunkLayout() {
        return SpongeChunkLayout.instance;
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return DimensionManager.getWorldProperties(worldName);
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return DimensionManager.getAllWorldProperties();
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
    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("deprecation")
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

    @Overwrite
    protected void loadAllWorlds(String overworldFolder, String worldName, long seed, WorldType type, String generatorOptions) {
        StaticMixinHelper.convertingMapFormat = true;
        this.convertMapIfNeeded(overworldFolder);
        StaticMixinHelper.convertingMapFormat = false;
        this.setUserMessage("menu.loadingLevel");

        if (!getAllowNether()) {
            SpongeImpl.getLogger().warn("Multi-world capability has been disabled via [allow-nether] in [server.properties]. All "
                    + "dimensions besides [{}] will be skipped.", overworldFolder);
        } else {
            final Path rootFolder = SpongeImpl.getGame().getSavesDirectory().resolve(overworldFolder);
            WorldMigrator.migrateWorldsTo(rootFolder);
            DimensionManager.loadAllWorlds(worldName, seed, type, generatorOptions);

            this.getPlayerList().setPlayerManager(new WorldServer[]{DimensionManager.getWorldByDimensionId(0).get()});
            this.setDifficultyForAllWorlds(this.getDifficulty());
            this.preparingChunks = true;
            this.initialWorldChunkLoad();
            this.preparingChunks = false;
        }
    }

    /**
     * @author Zidane
     * Sponge has a config option for determining if we'll generate spawn on server start. I enforce that here.
     */
    @Overwrite
    protected void initialWorldChunkLoad() {
        for (WorldServer worldServer : DimensionManager.getWorlds()) {
            final SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(worldServer);

            if (!activeConfig.getConfig().getWorld().getGenerateSpawnOnLoad()) {
                continue;
            }

            this.prepareSpawnArea(worldServer);
        }

        this.clearCurrentTask();
    }

    private void prepareSpawnArea(WorldServer world) {
        int i = 0;
        this.setUserMessage("menu.generatingTerrain");
        logger.info("Preparing start region for level {} ({})", ((IMixinWorld) world).getDimensionId(), ((World) world).getName());
        BlockPos blockpos = world.getSpawnPoint();
        long j = MinecraftServer.getCurrentTimeMillis();

        for (int k = -192; k <= 192 && this.isServerRunning(); k += 16) {
            for (int l = -192; l <= 192 && this.isServerRunning(); l += 16) {
                long i1 = MinecraftServer.getCurrentTimeMillis();

                if (i1 - j > 1000L) {
                    this.outputPercentRemaining("Preparing spawn area", i * 100 / 625);
                    j = i1;
                }

                ++i;
                world.getChunkProvider().provideChunk(blockpos.getX() + k >> 4, blockpos.getZ() + l >> 4);
            }
        }

        this.clearCurrentTask();
    }

    @Override
    public Optional<World> loadWorld(UUID uuid) {
        checkNotNull(uuid);
    }

    @Override
    public Optional<World> loadWorld(WorldProperties properties) {
        checkNotNull(properties);
        return loadWorld(properties.getWorldName());
    }

    @Override
    public Optional<World> loadWorld(String worldName) {
        final Optional<World> optExisting = getWorld(worldName);
        if (optExisting.isPresent()) {
            return optExisting;
        }

        if (!getAllowNether() && !worldName.equals(getFolderName())) {
            SpongeImpl.getLogger().error("Unable to load world " + worldName + ". Multi-world is disabled via allow-nether.");
            return Optional.empty();
        }

        final File file = new File(getFolderName(), worldName);
        if ((file.exists()) && (!file.isDirectory())) {
            throw new IllegalArgumentException("File exists with the name '" + worldName + "' and isn't a folder");
        }

        int dim;
        WorldInfo worldInfo;
        AnvilSaveHandler savehandler = getHandler(worldName);
        final Optional<WorldProperties> worldProperties = WorldPropertyRegistryModule.getInstance().getWorldProperties(worldName);
        if (worldProperties.isPresent()) {
            worldInfo = (WorldInfo) worldProperties.get();
        } else {
            worldInfo = savehandler.loadWorldInfo();
        }

        if (worldInfo != null) {
            ((IMixinWorldInfo) worldInfo).createWorldConfig();
            // check if enabled
            if (!((WorldProperties) worldInfo).isEnabled()) {
                SpongeImpl.getLogger().error("World [{}] cannot be loaded as it is disabled.", worldName);
                return Optional.empty();
            }

            dim = ((IMixinWorldInfo) worldInfo).getDimensionId();
            if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
                DimensionManager
                        .registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
            }
            if (DimensionTypeRegistryModule.getInstance().getWorldFolder(dim) == null) {
                DimensionTypeRegistryModule.getInstance().registerWorldDimensionId(dim, worldName);
            }
            if (!WorldPropertyRegistryModule.getInstance().isWorldRegistered(((WorldProperties) worldInfo).getUniqueId())) {
                WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);
            }
        } else {
            return Optional.empty(); // no world data found
        }

        WorldSettings settings = new WorldSettings(worldInfo);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }

        WorldServer worldServer = (WorldServer) new WorldServer((MinecraftServer) (Object) this, savehandler, worldInfo, dim, this.theProfiler).init();

        worldServer.initialize(settings);
        ((IMixinWorldProvider) worldServer.provider).setDimensionId(dim);

        worldServer.addEventListener(new WorldManager((MinecraftServer) (Object) this, worldServer));
        SpongeImpl.postEvent(SpongeImplHooks.createLoadWorldEvent((World) worldServer));
        if (!isSinglePlayer()) {
            worldServer.getWorldInfo().setGameType(getGameType());
        }
        this.setDifficultyForAllWorlds(this.getDifficulty());

        final SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(worldServer);
        if (activeConfig.getType().equals(SpongeConfig.Type.WORLD) || activeConfig.getType().equals(SpongeConfig.Type.DIMENSION)) {
            if (activeConfig.getConfig().getWorld().getGenerateSpawnOnLoad()) {
                this.prepareSpawnArea(worldServer);
            }
        } else {
            this.prepareSpawnArea(worldServer);
        }

        return Optional.of((World) worldServer);
    }

    @Override
    public Optional<WorldProperties> createWorldProperties(WorldCreationSettings settings) {
        final String worldName = ((IMixinWorldSettings) settings).getActualWorldName();
        final Optional<World> optExisting = getWorld(worldName);
        if (optExisting.isPresent()) {
            return Optional.of(optExisting.get().getProperties());
        }

        final ISaveHandler saveHandler = ((IMixinMinecraftServer) Sponge.getServer()).getHandler(worldName);
        WorldInfo worldInfo = saveHandler.loadWorldInfo();

        if (worldInfo != null) {
            final Optional<WorldProperties> optWorldProperties = DimensionManager.getWorldProperties(worldInfo.getWorldName());

        } else {
            worldInfo = new WorldInfo((WorldSettings) (Object) settings, worldName);
        }
        ((IMixinWorldInfo) worldInfo).createWorldConfig();

        if (((IMixinWorldSettings)settings).getDimensionId() == null || ((IMixinWorldSettings)settings).getDimensionId() == 0) {
            ((IMixinWorldInfo) worldInfo).setDimensionId(DimensionManager.getNextFreeDimId());
        }

        final int dim = ((IMixinWorldInfo) worldInfo).getDimensionId();
        final UUID uuid = setUuidForProperties((WorldProperties) worldInfo);
        ((IMixinWorldInfo) worldInfo).setDimensionType(settings.getDimensionType());
        ((IMixinWorldInfo) worldInfo).setIsMod(((IMixinWorldSettings)settings).getIsMod());
        ((WorldProperties) worldInfo).setGeneratorType(settings.getGeneratorType());
        ((WorldProperties) worldInfo).setGeneratorModifiers(settings.getGeneratorModifiers());
        ((IMixinWorldInfo) worldInfo).getWorldConfig().save();
        ((IMixinWorldInfo) worldInfo).getWorldConfig().reload();
        WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);
        DimensionTypeRegistryModule.getInstance().registerWorldDimensionId(dim, worldName);
        DimensionTypeRegistryModule.getInstance().registerWorldUniqueId(uuid, worldName);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }
        savehandler.saveWorldInfoWithPlayer(worldInfo, getPlayerList().getHostPlayerData());

        SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldEvent(Cause.of(NamedCause.source(this)), settings,
            (WorldProperties) worldInfo));
        return Optional.of((WorldProperties) worldInfo);
    }

    @Override
    public boolean unloadWorld(World world) {
        return DimensionManager.unloadWorld((WorldServer) world);
    }

    @Override
    public Collection<World> getWorlds() {
        Collections.unmodifiableCollection(DimensionManager.getWorlds());
    }

    @Override
    public Optional<World> getWorld(UUID uniqueId) {
        for (WorldServer worldserver : DimensionManager.getWorlds()) {
            if (((World) worldserver).getUniqueId().equals(uniqueId)) {
                return Optional.of((World) worldserver);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<WorldProperties> getDefaultWorld() {
        final Optional<WorldServer> optWorldServer = DimensionManager.getWorldByDimensionId(0);
        if (optWorldServer.isPresent()) {
            return Optional.of(((World) optWorldServer.get()).getProperties());
        }
        return Optional.empty();
    }

    @Override
    public String getDefaultWorldName() {
        return getFolderName();
    }

    @Override
    public Collection<WorldProperties> getUnloadedWorlds() {
        try {
            return DimensionManager.getUnloadedWorlds();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(UUID uniqueId) {
        return DimensionManager.getWorldProperties(uniqueId);
    }

    @Override
    public CompletableFuture<Optional<WorldProperties>> copyWorld(WorldProperties worldProperties, String copyName) {
        return DimensionManager.copyWorld(worldProperties, copyName);
    }

    @Override
    public Optional<WorldProperties> renameWorld(WorldProperties worldProperties, String newName) {
        return DimensionManager.renameWorld(worldProperties, newName);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
        return DimensionManager.deleteWorld(worldProperties);
    }

    @Override
    public boolean saveWorldProperties(WorldProperties properties) {
        return DimensionManager.saveWorldProperties(properties);
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
    public AnvilSaveHandler getHandler(String worldName) {
        if (SpongeImpl.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
            return new AnvilSaveHandler(new File(SpongeImpl.getGame().getSavesDirectory() + File.separator + getFolderName()), worldName,
                                        true, getDataFixer());
        } else {
            return new AnvilSaveHandler(new File(getFolderName()), worldName, true, getDataFixer());
        }
    }

    /**
     * @author Zidane
     * Vanilla simply returns worldServers[0]/[1]/[2] here. We change this to ask the {@link DimensionManager}.
     */
    @Overwrite
    public WorldServer worldServerForDimension(int dim) {
        return DimensionManager.getWorldByDimensionId(dim).orElse(null);
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

    @SuppressWarnings("unchecked")
    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        final Optional<WorldServer> optWorldServer = DimensionManager.getWorldByDimensionId(0);
        return optWorldServer.isPresent() ? Optional.of((Scoreboard) optWorldServer.get().getScoreboard()) : Optional.empty();
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

    @Override
    public boolean isPreparingChunks() {
        return this.preparingChunks;
    }
}
