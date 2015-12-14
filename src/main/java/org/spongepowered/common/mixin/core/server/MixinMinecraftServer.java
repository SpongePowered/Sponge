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
import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.command.ICommandSender;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.world.ChunkTicketManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplFactory;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.SpongeConfig.WorldConfig;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.interfaces.IMixinWorldProvider;
import org.spongepowered.common.interfaces.IMixinWorldSettings;
import org.spongepowered.common.registry.type.world.DimensionRegistryModule;
import org.spongepowered.common.registry.type.world.WorldPropertyRegistryModule;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.text.sink.SpongeMessageSinkFactory;
import org.spongepowered.common.util.ServerUtils;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.SpongeDimensionType;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, ConsoleSource, IMixinSubject, IMixinCommandSource, IMixinCommandSender,
        IMixinMinecraftServer {

    @Shadow private static Logger logger;
    @Shadow private boolean enableBonusChest;
    @Shadow private boolean worldIsBeingDeleted;
    @Shadow private int tickCounter;
    @Shadow private String motd;
    @Shadow private ServerConfigurationManager serverConfigManager;
    @Shadow public WorldServer[] worldServers;
    @Shadow public Profiler theProfiler;
    @Shadow public long[] tickTimeArray;

    @Shadow public abstract void setDifficultyForAllWorlds(EnumDifficulty difficulty);
    @Shadow public abstract void addChatMessage(IChatComponent message);
    @Shadow public abstract void initiateShutdown();
    @Shadow public abstract boolean isServerInOnlineMode();
    @Shadow public abstract boolean isServerRunning();
    @Shadow public abstract boolean canStructuresSpawn();
    @Shadow public abstract boolean isHardcore();
    @Shadow public abstract boolean isSinglePlayer();
    @Shadow public abstract String getFolderName();
    @Shadow public abstract ServerConfigurationManager getConfigurationManager();
    @Shadow public abstract EnumDifficulty getDifficulty();
    @Shadow public abstract WorldSettings.GameType getGameType();
    @Shadow protected abstract void setUserMessage(String message);
    @Shadow protected abstract void outputPercentRemaining(String message, int percent);
    @Shadow protected abstract void clearCurrentTask();
    @Shadow protected abstract void convertMapIfNeeded(String worldNameIn);
    @Shadow protected abstract void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn);

    private ResourcePack resourcePack;
    private boolean enableSaving = true;
    private GameProfileManager profileManager = new SpongeProfileManager();

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
        return WorldPropertyRegistryModule.getInstance().getWorldProperties(worldName);
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return WorldPropertyRegistryModule.getInstance().getAllWorldProperties();
    }

    @Override
    public MessageSink getBroadcastSink() {
        return SpongeMessageSinkFactory.TO_ALL;
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.empty();
    }

    @Override
    public boolean hasWhitelist() {
        return this.serverConfigManager.isWhiteListEnabled();
    }

    @Override
    public void setHasWhitelist(boolean enabled) {
        this.serverConfigManager.setWhiteListEnabled(enabled);
    }

    @Override
    public boolean getOnlineMode() {
        return isServerInOnlineMode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Player> getOnlinePlayers() {
        if (getConfigurationManager() == null || getConfigurationManager().playerEntityList == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List<Player>) getConfigurationManager().playerEntityList);
    }

    @Override
    public Optional<Player> getPlayer(UUID uniqueId) {
        if (getConfigurationManager() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) getConfigurationManager().getPlayerByUUID(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        if (getConfigurationManager() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((Player) getConfigurationManager().getPlayerByUsername(name));
    }

    @SuppressWarnings("deprecation")
    @Override
    public Text getMotd() {
        return Texts.legacy().fromUnchecked(this.motd);
    }

    @Override
    public int getMaxPlayers() {
        if (getConfigurationManager() == null) {
            return 0;
        }
        return getConfigurationManager().getMaxPlayers();
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
    protected void loadAllWorlds(String overworldFolder, String worldName, long seed, WorldType type, String generator) {
        this.convertMapIfNeeded(overworldFolder);
        this.setUserMessage("menu.loadingLevel");

        List<Integer> idList = new LinkedList<>(Arrays.asList(DimensionManager.getStaticDimensionIDs()));
        idList.remove(Integer.valueOf(0));
        idList.add(0, 0); // load overworld first
        for (int dim : idList) {
            WorldProvider provider = WorldProvider.getProviderForDimension(dim);
            String worldFolder;
            String levelName;
            if (dim == 0) {
                worldFolder = overworldFolder;
                levelName = worldName;
            } else {
                worldFolder = DimensionRegistryModule.getInstance().getWorldFolder(dim);
                if (worldFolder != null) {
                    final Optional<World> optWorld = getWorld(worldFolder);
                    if (optWorld.isPresent()) {
                        continue; // world is already loaded
                    }
                } else {
                    worldFolder = ((IMixinWorldProvider) provider).getSaveFolder();
                    DimensionRegistryModule.getInstance().registerWorldDimensionId(dim, worldFolder);
                }
                levelName = worldFolder;
            }

            SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(((Dimension) provider).getType().getId(), worldFolder);
            if (!activeConfig.getConfig().getWorld().isWorldEnabled()) {
                SpongeImpl.getLogger().info("World {} with dimension ID {} is disabled! Skipping world load...", worldFolder, dim);
                continue;
            }

            WorldInfo worldInfo;
            WorldSettings newWorldSettings ;
            AnvilSaveHandler worldsavehandler;

            if (SpongeImpl.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
                worldsavehandler =
                        new AnvilSaveHandler(dim == 0 ? SpongeImpl.getGame().getSavesDirectory().toFile() :
                                             SpongeImpl.getGame().getSavesDirectory().resolve(getFolderName()).toFile(), worldFolder, true);
                if (dim == 0) {
                    // overworld uses the client set world name
                    if (WorldPropertyRegistryModule.getInstance().isWorldRegistered(worldFolder)) {
                        worldInfo = (WorldInfo) WorldPropertyRegistryModule.getInstance().getWorldProperties(worldFolder).get();
                    } else {
                        worldInfo = (WorldInfo) WorldPropertyRegistryModule.getInstance().getWorldProperties(worldName).get(); // client copied world
                    }
                } else {
                    worldInfo = worldsavehandler.loadWorldInfo();
                }
            } else {
                worldsavehandler = new AnvilSaveHandler(new File(dim == 0 ? "." : getFolderName()), worldFolder, true);
                worldInfo = worldsavehandler.loadWorldInfo();
            }

            if (worldInfo == null) {
                newWorldSettings = new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
                newWorldSettings.setWorldName(worldFolder);

                if (this.enableBonusChest) {
                    newWorldSettings.enableBonusChest();
                }

                worldInfo = new WorldInfo(newWorldSettings, worldFolder);
                ((IMixinWorldInfo) worldInfo).setUUID(UUID.randomUUID());
                if (dim == 0 || dim == -1 || dim == 1) {// if vanilla dimension
                    if (dim != 0) {
                        ((WorldProperties) worldInfo).setGeneratorType(GeneratorTypes.DEFAULT);
                    }
                }
            } else {
                if (((WorldProperties) worldInfo).getUniqueId() == null || ((WorldProperties) worldInfo).getUniqueId().equals
                        (UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
                    ((IMixinWorldInfo) worldInfo).setUUID(UUID.randomUUID());

                    if (dim == 0 || dim == -1 || dim == 1) {// if vanilla dimension
                        if (dim != 0) {
                            ((WorldProperties) worldInfo).setGeneratorType(GeneratorTypes.DEFAULT);
                        }
                    }
                }
                worldInfo.setWorldName(levelName);
                newWorldSettings = new WorldSettings(worldInfo);
            }

            if (dim == 0) {
                this.setResourcePackFromWorld(this.getFolderName(), worldsavehandler);
            }

            ((IMixinWorldInfo) worldInfo).setDimensionId(dim);
            ((IMixinWorldInfo) worldInfo).setDimensionType(((Dimension) provider).getType());
            UUID uuid = ((WorldProperties) worldInfo).getUniqueId();
            DimensionRegistryModule.getInstance().registerWorldUniqueId(uuid, worldFolder);
            WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);
            SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldEvent(SpongeImpl.getGame(), Cause.of(NamedCause.source(this)),
                (WorldCreationSettings)(Object) newWorldSettings, (WorldProperties) worldInfo));
            final WorldServer world = (WorldServer) new WorldServer((MinecraftServer) (Object) this, worldsavehandler, worldInfo, dim,
                    this.theProfiler).init();

            world.initialize(newWorldSettings);
            world.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, world));

            if (!this.isSinglePlayer()) {
                world.getWorldInfo().setGameType(this.getGameType());
            }

            SpongeImpl.postEvent(SpongeImplFactory.createLoadWorldEvent(SpongeImpl.getGame(), (World) world));
        }

        this.serverConfigManager.setPlayerManager(new WorldServer[]{DimensionManager.getWorldFromDimId(0)});
        this.setDifficultyForAllWorlds(this.getDifficulty());
        this.initialWorldChunkLoad();
    }

    @Overwrite
    protected void initialWorldChunkLoad() {
        for (WorldServer worldserver : DimensionManager.getWorlds()) {
            WorldProperties worldProperties = ((World) worldserver).getProperties();
            if (worldProperties.doesKeepSpawnLoaded()) {
                prepareSpawnArea(worldserver);
            }
        }

        this.clearCurrentTask();
    }

    protected void prepareSpawnArea(WorldServer world) {
        int i = 0;
        this.setUserMessage("menu.generatingTerrain");
        logger.info("Preparing start region for level " + world.provider.getDimensionId());
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
                world.theChunkProviderServer.loadChunk(blockpos.getX() + k >> 4, blockpos.getZ() + l >> 4);
            }
        }

        this.clearCurrentTask();
    }

    @Override
    public Optional<World> loadWorld(UUID uuid) {
        String worldFolder = DimensionRegistryModule.getInstance().getWorldFolder(uuid);
        if (worldFolder != null) {
            return loadWorld(worldFolder);
        }
        return Optional.empty();
    }

    @Override
    public Optional<World> loadWorld(WorldProperties properties) {
        if (properties == null) {
            return Optional.empty();
        }
        return loadWorld(properties.getWorldName());
    }

    @Override
    public Optional<World> loadWorld(String worldName) {
        final Optional<World> optExisting = getWorld(worldName);
        if (optExisting.isPresent()) {
            return optExisting;
        }

        File file = new File(getFolderName(), worldName);

        if ((file.exists()) && (!file.isDirectory())) {
            throw new IllegalArgumentException("File exists with the name '" + worldName + "' and isn't a folder");
        }

        int dim;
        WorldInfo worldInfo = null;
        AnvilSaveHandler savehandler = getHandler(worldName);
        Optional<WorldProperties> worldProperties = WorldPropertyRegistryModule.getInstance().getWorldProperties(worldName);
        if (worldProperties.isPresent()) {
            worldInfo = (WorldInfo) worldProperties.get();
        } else {
            worldInfo = savehandler.loadWorldInfo();
        }

        if (worldInfo != null) {
            // check if enabled
            if (!((WorldProperties) worldInfo).isEnabled()) {
                SpongeImpl.getLogger().error("Unable to load world " + worldName + ". World is disabled!");
                return Optional.empty();
            }

            dim = ((IMixinWorldInfo) worldInfo).getDimensionId();
            if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
                DimensionManager
                        .registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
            }
            if (DimensionRegistryModule.getInstance().getWorldFolder(dim) == null) {
                DimensionRegistryModule.getInstance().registerWorldDimensionId(dim, worldName);
            }
            if (!WorldPropertyRegistryModule.getInstance().isWorldRegistered(worldName)) {
                WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);
            }
        } else {
            return Optional.empty(); // no world data found
        }

        WorldSettings settings = new WorldSettings(worldInfo);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }

        WorldServer world = (WorldServer) new WorldServer((MinecraftServer) (Object) this, savehandler, worldInfo, dim, this.theProfiler).init();

        world.initialize(settings);
        ((IMixinWorldProvider) world.provider).setDimension(dim);

        world.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, world));
        SpongeImpl.postEvent(SpongeImplFactory.createLoadWorldEvent(SpongeImpl.getGame(), (World) world));
        if (!isSinglePlayer()) {
            world.getWorldInfo().setGameType(getGameType());
        }
        this.setDifficultyForAllWorlds(this.getDifficulty());
        if (((WorldProperties) worldInfo).doesKeepSpawnLoaded()) {
            this.prepareSpawnArea(world);
        }

        return Optional.of((World) world);
    }

    @Override
    public Optional<WorldProperties> createWorld(WorldCreationSettings settings) {
        String worldName = settings.getWorldName();
        final Optional<World> optExisting = getWorld(worldName);
        if (optExisting.isPresent()) {
            return Optional.of(optExisting.get().getProperties());
        }

        int dim;
        AnvilSaveHandler savehandler;
        if (SpongeImpl.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
            savehandler =
                    new AnvilSaveHandler(new File(SpongeImpl.getGame().getSavesDirectory() + File.separator + getFolderName()), worldName,
                                         true);

        } else {
            savehandler = new AnvilSaveHandler(new File(getFolderName()), worldName, true);
        }

        WorldConfig worldConfig =
                new SpongeConfig<SpongeConfig.WorldConfig>(SpongeConfig.Type.WORLD, SpongeImpl.getSpongeConfigDir().resolve("worlds")
                        .resolve(settings.getDimensionType().getId()).resolve(worldName).resolve("world.conf"),
                        SpongeImpl.ECOSYSTEM_ID).getConfig();
        WorldInfo worldInfo = savehandler.loadWorldInfo();

        if (worldInfo != null) {
            ((IMixinWorldInfo) worldInfo).setWorldConfig(worldConfig);
            if (!WorldPropertyRegistryModule.getInstance().isWorldRegistered(((WorldProperties) worldInfo).getUniqueId())) {
                WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);
                return Optional.of((WorldProperties) worldInfo);
            } else {
                return WorldPropertyRegistryModule.getInstance().getWorldProperties(((WorldProperties) worldInfo).getUniqueId());
            }
        }

        if (((IMixinWorldSettings)settings).getDimensionId() != 0) {
            dim = ((IMixinWorldSettings)settings).getDimensionId();
        } else {
            dim = DimensionManager.getNextFreeDimId();
        }
        worldInfo = new WorldInfo((WorldSettings) (Object) settings, settings.getWorldName());
        UUID uuid = UUID.randomUUID();
        ((IMixinWorldInfo) worldInfo).setWorldConfig(worldConfig);
        ((IMixinWorldInfo) worldInfo).setUUID(uuid);
        ((IMixinWorldInfo) worldInfo).setDimensionId(dim);
        ((IMixinWorldInfo) worldInfo).setDimensionType(settings.getDimensionType());
        ((IMixinWorldInfo) worldInfo).setIsMod(((IMixinWorldSettings)settings).getIsMod());
        ((WorldProperties) worldInfo).setKeepSpawnLoaded(settings.doesKeepSpawnLoaded());
        ((WorldProperties) worldInfo).setLoadOnStartup(settings.loadOnStartup());
        ((WorldProperties) worldInfo).setEnabled(settings.isEnabled());
        ((WorldProperties) worldInfo).setGeneratorType(settings.getGeneratorType());
        ((WorldProperties) worldInfo).setGeneratorModifiers(settings.getGeneratorModifiers());
        WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);
        DimensionRegistryModule.getInstance().registerWorldDimensionId(dim, worldName);
        DimensionRegistryModule.getInstance().registerWorldUniqueId(uuid, worldName);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }
        savehandler.saveWorldInfoWithPlayer(worldInfo, getConfigurationManager().getHostPlayerData());

        SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldEvent(SpongeImpl.getGame(), Cause.of(NamedCause.source(this)), settings,
            (WorldProperties) worldInfo));
        return Optional.of((WorldProperties) worldInfo);
    }

    @Override
    public boolean unloadWorld(World world) {
        int dim = ((net.minecraft.world.World) world).provider.getDimensionId();
        if (DimensionManager.getWorldFromDimId(dim) != null) {
            return DimensionManager.unloadWorldFromDimId(dim);
        }
        return false;
    }

    @Override
    public Collection<World> getWorlds() {
        List<World> worlds = new ArrayList<>();
        for (WorldServer worldServer : DimensionManager.getWorlds()) {
            worlds.add((World) worldServer);
        }
        return worlds;
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
        if (DimensionManager.getWorldFromDimId(0) != null) {
            return Optional.of(((World) DimensionManager.getWorldFromDimId(0)).getProperties());
        }
        return Optional.empty();
    }

    @Override
    public Collection<WorldProperties> getUnloadedWorlds() {
        File rootDir = DimensionManager.getCurrentSaveRootDirectory();
        if (rootDir == null) {
            return Collections.emptyList();
        }
        List<WorldProperties> worlds = Lists.newArrayList();
        for (File f : rootDir.listFiles()) {
            if (f.isDirectory()) {
                if (this.getWorld(f.getName()).isPresent()) {
                    continue;
                }
                File level = new File(f, "level.dat");
                File levelSponge = new File(f, "level_sponge.dat");
                if (level.isFile() && levelSponge.isFile()) {
                    WorldInfo info = getHandler(f.getName()).loadWorldInfo();
                    if (info != null) {
                        worlds.add((WorldProperties) info);
                    }
                }
            }
        }
        return worlds;
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(UUID uniqueId) {
        return WorldPropertyRegistryModule.getInstance().getWorldProperties(uniqueId);
    }

    @Override
    public ListenableFuture<Optional<WorldProperties>> copyWorld(WorldProperties worldProperties, String copyName) {
        return ServerUtils.copyWorld((MinecraftServer) (Object) this, checkNotNull(worldProperties, "worldProperties"),
                checkNotNull(copyName, "copyName"));
    }

    @Override
    public Optional<WorldProperties> renameWorld(WorldProperties worldProperties, String newName) {
        checkNotNull(newName, "newName");
        checkState(DimensionManager.getWorldFromDimId(((IMixinWorldInfo) checkNotNull(worldProperties, "worldProperties"))
                .getDimensionId()) == null, "World still loaded");
        File rootDir = DimensionManager.getCurrentSaveRootDirectory();
        if (rootDir == null) {
            return Optional.empty();
        }
        File oldDir = new File(rootDir, worldProperties.getWorldName());
        File newDir = new File(rootDir, newName);
        if (newDir.exists()) {
            return Optional.empty();
        }
        if (!oldDir.renameTo(newDir)) {
            return Optional.empty();
        }
        WorldInfo info = new WorldInfo((WorldInfo) worldProperties);
        info.setWorldName(newName);
        getHandler(newName).saveWorldInfo(info);
        return Optional.of((WorldProperties) info);
    }

    @Override
    public ListenableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
        return ServerUtils.deleteWorld(checkNotNull(worldProperties, "worldProperties"));
    }

    @Override
    public boolean saveWorldProperties(WorldProperties properties) {
        WorldServer world = DimensionManager.getWorldFromDimId(((IMixinWorldInfo) checkNotNull(properties, "properties")).getDimensionId());
        if (world != null) {
            world.getSaveHandler().saveWorldInfo(world.getWorldInfo());
        } else {
            getHandler(properties.getWorldName()).saveWorldInfo((WorldInfo) properties);
        }
        // No return values or exceptions so can only assume true.
        return true;
    }

    @Override
    public ChunkTicketManager getChunkTicketManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameProfileManager getGameProfileManager() {
        return this.profileManager;
    }

    @Override
    public AnvilSaveHandler getHandler(String worldName) {
        if (SpongeImpl.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
            return new AnvilSaveHandler(new File(SpongeImpl.getGame().getSavesDirectory() + File.separator + getFolderName()), worldName,
                                        true);
        } else {
            return new AnvilSaveHandler(new File(getFolderName()), worldName, true);
        }
    }

    @Overwrite
    public WorldServer worldServerForDimension(int dim) {
        WorldServer ret = DimensionManager.getWorldFromDimId(dim);
        if (ret == null) {
            DimensionManager.initDimension(dim);
            ret = DimensionManager.getWorldFromDimId(dim);
        }
        return ret;
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
}
