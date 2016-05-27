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
import com.google.common.collect.Lists;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.*;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
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
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
import org.spongepowered.common.interfaces.world.IMixinWorldProvider;
import org.spongepowered.common.interfaces.world.IMixinWorldSettings;
import org.spongepowered.common.profile.SpongeProfileManager;
import org.spongepowered.common.registry.type.world.DimensionRegistryModule;
import org.spongepowered.common.registry.type.world.WorldPropertyRegistryModule;
import org.spongepowered.common.resourcepack.SpongeResourcePack;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.ServerUtils;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.SpongeDimensionType;
import org.spongepowered.common.world.WorldMigrator;
import org.spongepowered.common.world.storage.SpongeChunkLayout;
import org.spongepowered.common.world.storage.WorldServerMultiAdapterWorldInfo;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
@Implements(@Interface(iface = Server.class, prefix = "server$"))
public abstract class MixinMinecraftServer implements Server, ConsoleSource, IMixinSubject, IMixinCommandSource, IMixinCommandSender,
        IMixinMinecraftServer {

    @Shadow @Final private static Logger logger;
    @Shadow @Final public Profiler theProfiler;
    @Shadow @Final public long[] tickTimeArray;
    @Shadow private boolean enableBonusChest;
    @Shadow private boolean worldIsBeingDeleted;
    @Shadow private int tickCounter;
    @Shadow private String motd;
    @Shadow private ServerConfigurationManager serverConfigManager;
    @Shadow public WorldServer[] worldServers;

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
    @Shadow public abstract boolean getAllowNether();
    @Shadow public abstract int getMaxPlayerIdleMinutes();
    @Shadow public abstract void shadow$setPlayerIdleTimeout(int timeout);

    private ResourcePack resourcePack;
    private boolean enableSaving = true;
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
        return WorldPropertyRegistryModule.getInstance().getWorldProperties(worldName);
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return WorldPropertyRegistryModule.getInstance().getAllWorldProperties();
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<Player> getOnlinePlayers() {
        if (getConfigurationManager() == null || getConfigurationManager().getPlayerList() == null) {
            return ImmutableList.of();
        }
        return ImmutableList.copyOf((List) getConfigurationManager().getPlayerList());
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

    @Override
    public Text getMotd() {
        return SpongeTexts.fromLegacy(this.motd);
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

    /**
     * @author Zidane - June 15th, 2015
     * @author blood - December 23rd, 2015
     *
     * @reason Add multiworld support
     */
    @Overwrite
    protected void loadAllWorlds(String overworldFolder, String worldName, long seed, WorldType type, String generatorOptions) {
        StaticMixinHelper.convertingMapFormat = true;
        this.convertMapIfNeeded(overworldFolder);
        StaticMixinHelper.convertingMapFormat = false;
        this.setUserMessage("menu.loadingLevel");

        if (!getAllowNether()) {
            SpongeImpl.getLogger().warn("Multi-world capability has been disabled via [allow-nether] in [server.properties]. All "
                    + "dimensions besides [{}] will be skipped.", overworldFolder);
        }

        WorldMigrator.migrateWorldsTo(SpongeImpl.getGame().getSavesDirectory().resolve(overworldFolder));

        registerExistingSpongeDimensions();

        final List<Integer> idList = new LinkedList<>();
        if (getAllowNether()) {
            idList.addAll(Arrays.asList(DimensionManager.getStaticDimensionIDs()));
            // Ensure that we'll load in Vanilla order then plugins
            idList.remove(Integer.valueOf(0));
            idList.add(0, 0);
            if (idList.remove(Integer.valueOf(-1))) {
                idList.add(1, -1);
            }
            if (idList.remove(Integer.valueOf(1))) {
                idList.add(2, 1);
            }
        } else {
            idList.add(0, 0);
        }

        for (int dim : idList) {
            WorldProvider provider = WorldProvider.getProviderForDimension(dim);
            String worldFolder;
            if (dim == 0) {
                worldFolder = overworldFolder;
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
            }

            if (dim != 0) {
                final SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(((Dimension) provider).getType().getId(), worldFolder);
                if (activeConfig.getType().equals(SpongeConfig.Type.WORLD) || activeConfig.getType().equals(SpongeConfig.Type.DIMENSION)) {
                    if (!activeConfig.getConfig().getWorld().isWorldEnabled()) {
                        SpongeImpl.getLogger().warn("World [{}] (DIM{}) is disabled. World will not be loaded...", worldFolder, dim);
                        continue;
                    }
                }
            }

            final AnvilSaveHandler worldsavehandler = new AnvilSaveHandler(dim == 0 ? SpongeImpl.getGame().getSavesDirectory().toFile() :
                            SpongeImpl.getGame().getSavesDirectory().resolve(getFolderName()).toFile(), worldFolder, true);
            WorldInfo worldInfo;
            WorldSettings worldSettings;

            if (SpongeImpl.getGame().getPlatform().getType() == Platform.Type.CLIENT && dim == 0) {
                // overworld uses the client set world name
                if (WorldPropertyRegistryModule.getInstance().isWorldRegistered(worldFolder)) {
                    worldInfo = (WorldInfo) WorldPropertyRegistryModule.getInstance().getWorldProperties(worldFolder).get();
                } else {
                    worldInfo = (WorldInfo) WorldPropertyRegistryModule.getInstance().getWorldProperties(worldName).get(); // client copied world
                }
            } else {
                worldInfo = worldsavehandler.loadWorldInfo();
            }

            if (worldInfo == null) {
                if (SpongeImpl.getGame().getPlatform().getType().isClient()) {
                    final WorldServer overworld = worldServerForDimension(0);
                    worldSettings =
                            new WorldSettings(seed, this.getGameType(), overworld.getWorldInfo().isMapFeaturesEnabled(), this.isHardcore(), type);
                } else {
                    worldSettings =
                            new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
                }
                worldSettings.setWorldName(generatorOptions); // setGeneratorOptions
                ((IMixinWorldSettings) (Object) worldSettings).setActualWorldName(worldFolder);

                if (this.enableBonusChest) {
                    worldSettings.enableBonusChest();
                }

                ((IMixinWorldSettings)(Object) worldSettings).setDimensionId(dim);
                ((IMixinWorldSettings)(Object) worldSettings).setDimensionType(((Dimension) provider).getType());

                worldInfo = new WorldInfo(worldSettings, worldFolder);
                ((IMixinWorldInfo) worldInfo).setUUID(UUID.randomUUID());

                SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldEvent(Cause.of(NamedCause.source(this)),
                        (WorldCreationSettings)(Object) worldSettings, (WorldProperties) worldInfo));

            } else {
                setUuidForProperties((WorldProperties) worldInfo);
                ((IMixinWorldInfo) worldInfo).setDimensionId(dim);
                ((IMixinWorldInfo) worldInfo).setDimensionType(((Dimension) provider).getType());
                worldSettings = new WorldSettings(worldInfo);
            }

            if (dim == 0) {
                this.setResourcePackFromWorld(this.getFolderName(), worldsavehandler);
            }

            ((IMixinWorldInfo) worldInfo).createWorldConfig();
            DimensionRegistryModule.getInstance().registerWorldUniqueId(((WorldProperties) worldInfo).getUniqueId(), worldFolder);
            WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) worldInfo);


            if (!((WorldProperties) worldInfo).loadOnStartup()) {
                SpongeImpl.getLogger().warn("World [{}] (DIM{}) is set to not load on startup. To load it later, enable [load-on-startup] in config "
                        + "or use a plugin", worldFolder, dim);
                continue;
            }

            final WorldServer worldServer;
            if (dim == 0) {
                worldServer = (WorldServer) new WorldServer((MinecraftServer) (Object) this, worldsavehandler, worldInfo, dim,
                        this.theProfiler).init();
            } else {
                worldServer = (WorldServer) new WorldServerMulti((MinecraftServer) (Object) this, new WorldServerMultiAdapterWorldInfo(worldsavehandler, worldInfo), dim, DimensionManager.getWorldFromDimId(0), this.theProfiler).init();
            }

            worldServer.initialize(worldSettings);
            worldServer.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, worldServer));

            // This code changes from Mojang's to account for per-world API-set GameModes.
            if (!this.isSinglePlayer() && worldServer.getWorldInfo().getGameType().equals(WorldSettings.GameType.NOT_SET)) {
                worldServer.getWorldInfo().setGameType(this.getGameType());
            }

            SpongeImpl.postEvent(SpongeImplHooks.createLoadWorldEvent((World) worldServer));
        }

        this.serverConfigManager.setPlayerManager(new WorldServer[]{DimensionManager.getWorldFromDimId(0)});
        this.setDifficultyForAllWorlds(this.getDifficulty());
        this.initialWorldChunkLoad();
    }

    /**
     * Handles registering existing Sponge dimensions that are not the root dimension (known as overworld).
     */
    private void registerExistingSpongeDimensions() {
        final File[] worldCandidateFiles = DimensionManager.getCurrentSaveRootDirectory().listFiles();
        if (worldCandidateFiles == null) {
            return;
        }

        // Skip other dimensions if multi-world is turned off.
        if (!getAllowNether()) {
            return;
        }

        for (File worldCandidateFile : worldCandidateFiles) {
            final File levelData = new File(worldCandidateFile, "level_sponge.dat");

            // This method only handles registering existing Sponge worlds (and in directories for that matter).
            if (!worldCandidateFile.isDirectory() || !levelData.exists()) {
                continue;
            }

            NBTTagCompound compound;
            try {
                compound = CompressedStreamTools.readCompressed(new FileInputStream(levelData));
            } catch (IOException e) {
                SpongeImpl.getLogger().error("Failed loading Sponge data for World [{}]}. This is a critical "
                        + "problem and should be reported to Sponge ASAP.", worldCandidateFile.getName(), e);
                continue;
            }

            if (compound.hasKey(NbtDataUtil.SPONGE_DATA)) {
                final NBTTagCompound spongeData = compound.getCompoundTag(NbtDataUtil.SPONGE_DATA);

                if (!spongeData.hasKey(NbtDataUtil.DIMENSION_ID)) {
                    SpongeImpl.getLogger().error("World [{}] has no dimension id. This is a critical error and should be reported to Sponge ASAP.",
                            worldCandidateFile.getName());
                    continue;
                }

                final int dimensionId = spongeData.getInteger(NbtDataUtil.DIMENSION_ID);

                // Nether and The End are handled above
                if (dimensionId == -1 || dimensionId == 1) {
                    continue;
                }

                String dimensionType = "overworld";

                if (spongeData.hasKey(NbtDataUtil.DIMENSION_TYPE)) {
                    dimensionType = spongeData.getString(NbtDataUtil.DIMENSION_TYPE);

                    // Temporary fix for old data, remove in future build
                    if (dimensionType.equalsIgnoreCase("net.minecraft.world.WorldProviderSurface")) {
                        dimensionType = "overworld";
                    } else if (dimensionType.equalsIgnoreCase("net.minecraft.world.WorldProviderHell")) {
                        dimensionType = "nether";
                    } else if (dimensionType.equalsIgnoreCase("net.minecraft.world.WorldProviderEnd")) {
                        dimensionType = "the_end";
                    }
                } else {
                    SpongeImpl.getLogger().warn("World [{}] (DIM{}) has no specified dimension type. Defaulting to [overworld]...",
                            worldCandidateFile.getName(), dimensionId);
                }

                spongeData.setString(NbtDataUtil.DIMENSION_TYPE, dimensionType);
                final SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(dimensionType, worldCandidateFile.getName());

                if (activeConfig.getType().equals(SpongeConfig.Type.WORLD) || activeConfig.getType().equals(SpongeConfig.Type.DIMENSION)) {
                    if (!activeConfig.getConfig().getWorld().isWorldEnabled()) {
                        SpongeImpl.getLogger().warn("World [{}] (DIM{}) is disabled. World will not be registered...",
                                worldCandidateFile.getName(), dimensionId);
                        continue;
                    }
                }

                if (spongeData.hasKey(NbtDataUtil.WORLD_UUID_MOST) && spongeData.hasKey(NbtDataUtil.WORLD_UUID_LEAST)) {
                    final UUID uuid = new UUID(spongeData.getLong(NbtDataUtil.WORLD_UUID_MOST), spongeData.getLong(NbtDataUtil.WORLD_UUID_LEAST));
                    DimensionRegistryModule.getInstance().registerWorldUniqueId(uuid, worldCandidateFile.getName());
                } else {
                    SpongeImpl.getLogger().error("World [{}] (DIM{}) has no valid unique identifier. This is a critical error and should be reported"
                            + "to Sponge ASAP.", worldCandidateFile.getName(), dimensionId);
                    continue;
                }

                DimensionRegistryModule.getInstance().getAll().forEach(type -> {
                    if (type.getId().equalsIgnoreCase(spongeData.getString(NbtDataUtil.DIMENSION_TYPE))) {
                        DimensionRegistryModule.getInstance().registerWorldDimensionId(dimensionId, worldCandidateFile.getName());
                        if (!DimensionManager.isDimensionRegistered(dimensionId)) {
                            DimensionManager.registerDimension(dimensionId,
                                    ((SpongeDimensionType) type).getDimensionTypeId());
                        }
                    }
                });
            }
        }
    }

    /**
     * @author Zidane - June 15th, 2015
     * @author blood - December 23rd, 2015
     *
     * @reason Add multiworld support
     */
    @Overwrite
    protected void initialWorldChunkLoad() {
        final List<WorldServer> worldServers = new LinkedList<>(Arrays.asList(DimensionManager.getWorlds()));
        final WorldServer overworld = DimensionManager.getWorldFromDimId(0);

        if (getAllowNether()) {
            worldServers.remove(DimensionManager.getWorldFromDimId(0));
            worldServers.add(0, overworld);

            final WorldServer the_end = DimensionManager.getWorldFromDimId(1);

            if (worldServers.remove(the_end)) {
                worldServers.add(1, the_end);
            }

            final WorldServer nether = DimensionManager.getWorldFromDimId(-1);

            if (worldServers.remove(nether)) {
                worldServers.add(1, nether);
            }
        } else {
            worldServers.add(0, overworld);
        }

        for (WorldServer worldServer : worldServers) {
            final SpongeConfig<?> activeConfig = SpongeHooks.getActiveConfig(worldServer);

            if (activeConfig.getType().equals(SpongeConfig.Type.WORLD) || activeConfig.getType().equals(SpongeConfig.Type.DIMENSION)) {
                if (!activeConfig.getConfig().getWorld().getGenerateSpawnOnLoad()) {
                    continue;
                }
            }

            this.prepareSpawnArea(worldServer);
        }

        this.clearCurrentTask();
    }

    protected void prepareSpawnArea(WorldServer world) {
        ((IMixinWorld) world).getCauseTracker().setCapturingTerrainGen(true);
        int i = 0;
        this.setUserMessage("menu.generatingTerrain");
        logger.info("Preparing start region for level {} ({})", world.provider.getDimensionId(), ((World) world).getName());
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
        ((IMixinWorld) world).getCauseTracker().setCapturingTerrainGen(false);
    }

    @Override
    public Optional<World> loadWorld(UUID uuid) {
        checkNotNull(uuid);
        final String worldFolder = DimensionRegistryModule.getInstance().getWorldFolder(uuid);
        if (worldFolder == null) {
            return Optional.empty();
        }
        return loadWorld(worldFolder);
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
            if (DimensionRegistryModule.getInstance().getWorldFolder(dim) == null) {
                DimensionRegistryModule.getInstance().registerWorldDimensionId(dim, worldName);
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

        final WorldServer worldServer = (WorldServer) new WorldServerMulti((MinecraftServer) (Object) this, new WorldServerMultiAdapterWorldInfo
                (savehandler, worldInfo), dim, DimensionManager.getWorldFromDimId(0), this.theProfiler).init();

        worldServer.initialize(settings);
        ((IMixinWorldProvider) worldServer.provider).setDimension(dim);

        worldServer.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, worldServer));
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

        if (!getAllowNether()) {
            SpongeImpl.getLogger().error("Unable to create world " + worldName + ". Multiworld is disabled via allow-nether.");
            return Optional.empty();
        }

        AnvilSaveHandler savehandler;
        if (SpongeImpl.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
            savehandler =
                    new AnvilSaveHandler(new File(SpongeImpl.getGame().getSavesDirectory() + File.separator + getFolderName()), worldName,
                                         true);

        } else {
            savehandler = new AnvilSaveHandler(new File(getFolderName()), worldName, true);
        }

        WorldInfo worldInfo = savehandler.loadWorldInfo();

        if (worldInfo != null) {
            worldInfo.setWorldName(worldName);
            if (WorldPropertyRegistryModule.getInstance().isWorldRegistered(((WorldProperties) worldInfo).getWorldName())) {
                return Optional.of((WorldProperties) worldInfo);
            }
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
        DimensionRegistryModule.getInstance().registerWorldDimensionId(dim, worldName);
        DimensionRegistryModule.getInstance().registerWorldUniqueId(uuid, worldName);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }
        savehandler.saveWorldInfoWithPlayer(worldInfo, getConfigurationManager().getHostPlayerData());

        SpongeImpl.postEvent(SpongeEventFactory.createConstructWorldEvent(Cause.of(NamedCause.source(this)), settings,
            (WorldProperties) worldInfo));
        return Optional.of((WorldProperties) worldInfo);
    }

    private UUID setUuidForProperties(WorldProperties properties) {
        checkNotNull(properties);

        UUID uuid;
        if (properties.getUniqueId() == null || properties.getUniqueId().equals
                (StaticMixinHelper.INVALID_WORLD_UUID)) {
            // Check if Bukkit's uid.dat file is here and use it
            final Path uidPath = SpongeImpl.getGame().getSavesDirectory().resolve(properties.getWorldName()).resolve("uid.dat");
            if (!Files.exists(uidPath)) {
                uuid = UUID.randomUUID();
            } else {
                DataInputStream dis;

                try {
                    dis = new DataInputStream(Files.newInputStream(uidPath));
                    uuid = new UUID(dis.readLong(), dis.readLong());
                } catch (IOException e) {
                    SpongeImpl.getLogger().error("World folder [{}] has an existing Bukkit unique identifier for it but we encountered issues "
                                    + "parsing the file. We will have to use a new unique id. Please report this to Sponge ASAP.", properties.getWorldName(),
                            e);
                    uuid = UUID.randomUUID();
                }
            }
        } else {
            uuid = properties.getUniqueId();
        }

        ((IMixinWorldInfo) properties).setUUID(uuid);
        return uuid;
    }

    @Override
    public boolean unloadWorld(World world) {
        checkNotNull(world);
        int dim = ((net.minecraft.world.World) world).provider.getDimensionId();
        if (DimensionManager.getWorldFromDimId(dim) != null) {
            final WorldServer worldServer = (WorldServer) world;
            if (!worldServer.playerEntities.isEmpty()) {
                return false;
            }

            Sponge.getEventManager().post(SpongeEventFactory.createUnloadWorldEvent(Cause.of(NamedCause.source(this)), world));

            try {
                worldServer.saveAllChunks(true, null);
                worldServer.flush();

                Sponge.getEventManager().post(SpongeEventFactory.createSaveWorldEvent(Cause.of(NamedCause.source(this)), world));
            } catch (MinecraftException e) {
                e.printStackTrace();
            }
            finally {
                DimensionManager.setWorld(dim, null);
            }

            return true;
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
    public String getDefaultWorldName() {
        return getFolderName();
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
    public CompletableFuture<Optional<WorldProperties>> copyWorld(WorldProperties worldProperties, String copyName) {
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
        WorldPropertyRegistryModule.getInstance().unregister(worldProperties);
        WorldInfo info = new WorldInfo((WorldInfo) worldProperties);
        info.setWorldName(newName);
        ((IMixinWorldInfo) info).createWorldConfig();
        getHandler(newName).saveWorldInfo(info);
        WorldPropertyRegistryModule.getInstance().registerWorldProperties((WorldProperties) info);
        return Optional.of((WorldProperties) info);
    }

    @Override
    public CompletableFuture<Boolean> deleteWorld(WorldProperties worldProperties) {
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
        if (this.profileManager == null) {
            this.profileManager = new SpongeProfileManager();
        }
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

    /**
     * @author Zidane - June 13th, 2015
     *
     * @reason Use our DimensionManager to get dimensions since
     * we support multiple dimensions with the same dimension type.
     *
     * @param dim The dimension id
     * @return The world server
     */
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

    @Override
    public Optional<Scoreboard> getServerScoreboard() {
        WorldServer world = DimensionManager.getWorldFromDimId(0);
        if (world != null) {
            return Optional.of((Scoreboard) world.getScoreboard());
        }
        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    @Inject(method = "getTabCompletions", at = @At(value = "RETURN", ordinal = 1), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onTabCompleteChat(ICommandSender sender, String input, BlockPos pos, CallbackInfoReturnable<List> cir,
            List<String> list, String astring[], String s) {
        TabCompleteEvent.Chat event = SpongeEventFactory.createTabCompleteEventChat(Cause.source(sender).build(),
                ImmutableList.copyOf(list), list, input);
        Sponge.getEventManager().post(event);
        if (event.isCancelled()) {
            cir.setReturnValue(new ArrayList<>());
        } else {
            cir.setReturnValue(event.getTabCompletions());
        }
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
        int lastAnimTick = StaticMixinHelper.lastAnimationPacketTick;
        int lastPrimaryTick = StaticMixinHelper.lastPrimaryPacketTick;
        int lastSecondaryTick = StaticMixinHelper.lastSecondaryPacketTick;
        EntityPlayerMP player = StaticMixinHelper.lastAnimationPlayer;
        if (player != null && lastAnimTick != lastPrimaryTick && lastAnimTick != lastSecondaryTick && lastAnimTick != 0 && lastAnimTick - lastPrimaryTick > 3 && lastAnimTick - lastSecondaryTick > 3) {
            InteractBlockEvent.Primary event = SpongeEventFactory.createInteractBlockEventPrimary(Cause.of(NamedCause.source(player)), Optional.empty(), BlockSnapshot.NONE, Direction.NONE);
            Sponge.getEventManager().post(event);
        }
        StaticMixinHelper.lastAnimationPacketTick = 0;
        TimingsManager.FULL_SERVER_TICK.stopTiming();
    }

    @Override
    public int getPlayerIdleTimeout() {
        return this.getMaxPlayerIdleMinutes();
    }

    @Intrinsic
    public void server$setPlayerIdleTimeout(int timeout) {
        this.shadow$setPlayerIdleTimeout(timeout);
    }

}
