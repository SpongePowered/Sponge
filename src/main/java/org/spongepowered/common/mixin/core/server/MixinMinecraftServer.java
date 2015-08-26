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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.command.ICommandSender;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.sink.MessageSink;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.ConsoleSource;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.event.SpongeImplEventFactory;
import org.spongepowered.common.interfaces.IMixinCommandSender;
import org.spongepowered.common.interfaces.IMixinCommandSource;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.interfaces.IMixinWorldInfo;
import org.spongepowered.common.interfaces.IMixinWorldProvider;
import org.spongepowered.common.interfaces.IMixinWorldSettings;
import org.spongepowered.common.text.sink.SpongeMessageSinkFactory;
import org.spongepowered.common.world.DimensionManager;
import org.spongepowered.common.world.SpongeDimensionType;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@NonnullByDefault
@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements Server, ConsoleSource, IMixinSubject, IMixinCommandSource, IMixinCommandSender,
        IMixinMinecraftServer {

    @Shadow private String motd;
    @Shadow private static Logger logger;
    @Shadow private ServerConfigurationManager serverConfigManager;
    @Shadow private int tickCounter;
    @Shadow public abstract EnumDifficulty getDifficulty();
    @Shadow public abstract ServerConfigurationManager getConfigurationManager();
    @Shadow public abstract void addChatMessage(IChatComponent message);
    @Shadow public abstract boolean isServerInOnlineMode();
    @Shadow public abstract void initiateShutdown();
    @Shadow public abstract boolean isServerRunning();
    @Shadow protected abstract void setUserMessage(String message);
    @Shadow protected abstract void outputPercentRemaining(String message, int percent);
    @Shadow protected abstract void clearCurrentTask();
    @Shadow public WorldServer[] worldServers;
    @Shadow public Profiler theProfiler;
    @Shadow private boolean enableBonusChest;
    @Shadow private boolean worldIsBeingDeleted;
    @Shadow public abstract boolean canStructuresSpawn();
    @Shadow public abstract boolean isHardcore();
    @Shadow public abstract boolean isSinglePlayer();
    @Shadow public abstract String getFolderName();
    @Shadow public abstract WorldSettings.GameType getGameType();
    @Shadow public abstract void setDifficultyForAllWorlds(EnumDifficulty difficulty);
    @Shadow protected abstract void convertMapIfNeeded(String worldNameIn);
    @Shadow protected abstract void setResourcePackFromWorld(String worldNameIn, ISaveHandler saveHandlerIn);

    @Shadow
    @SideOnly(Side.SERVER)
    public abstract String getServerHostname();

    @Shadow
    @SideOnly(Side.SERVER)
    public abstract int getPort();

    @Override
    public Optional<World> getWorld(String worldName) {
        for (World world : getWorlds()) {
            if (world.getName().equals(worldName)) {
                return Optional.of(world);
            }
        }
        return Optional.absent();
    }

    @Override
    public ChunkLayout getChunkLayout() {
        return SpongeChunkLayout.instance;
    }

    @Override
    public Optional<WorldProperties> getWorldProperties(String worldName) {
        return Sponge.getSpongeRegistry().getWorldProperties(worldName);
    }

    @Override
    public Collection<WorldProperties> getAllWorldProperties() {
        return Sponge.getSpongeRegistry().getAllWorldProperties();
    }

    @Override
    public MessageSink getBroadcastSink() {
        return SpongeMessageSinkFactory.TO_ALL;
    }

    @Override
    public Optional<InetSocketAddress> getBoundAddress() {
        return Optional.fromNullable(new InetSocketAddress(getServerHostname(), getPort()));
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
            return Optional.absent();
        }
        return Optional.fromNullable((Player) getConfigurationManager().getPlayerByUUID(uniqueId));
    }

    @Override
    public Optional<Player> getPlayer(String name) {
        if (getConfigurationManager() == null) {
            return Optional.absent();
        }
        return Optional.fromNullable((Player) getConfigurationManager().getPlayerByUsername(name));
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

    @Overwrite
    protected void loadAllWorlds(String overworldFolder, String unused, long seed, WorldType type, String generator) {
        this.convertMapIfNeeded(overworldFolder);
        this.setUserMessage("menu.loadingLevel");

        List<Integer> idList = new LinkedList<Integer>(Arrays.asList(DimensionManager.getStaticDimensionIDs()));
        idList.remove(Integer.valueOf(0));
        idList.add(0, 0); // load overworld first
        for (int dim : idList) {
            WorldProvider provider = WorldProvider.getProviderForDimension(dim);
            String worldFolder;
            if (dim == 0) {
                worldFolder = overworldFolder;
            } else {
                worldFolder = Sponge.getSpongeRegistry().getWorldFolder(dim);
                if (worldFolder != null) {
                    final Optional<World> optWorld = getWorld(worldFolder);
                    if (optWorld.isPresent()) {
                        continue; // world is already loaded
                    }
                } else {
                    worldFolder = ((IMixinWorldProvider) provider).getSaveFolder();
                    Sponge.getSpongeRegistry().registerWorldDimensionId(dim, worldFolder);
                }
            }

            WorldInfo worldInfo;
            WorldSettings newWorldSettings ;
            AnvilSaveHandler worldsavehandler;

            if (Sponge.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
                worldsavehandler =
                        new AnvilSaveHandler(dim == 0 ? Sponge.getGame().getSavesDirectory() :
                                new File(Sponge.getGame().getSavesDirectory() + File.separator + getFolderName()), worldFolder, true);
                if (dim == 0) {
                    worldInfo = (WorldInfo)Sponge.getSpongeRegistry().getWorldProperties(worldFolder).get();
                } else {
                    worldInfo = worldsavehandler.loadWorldInfo();
                }
            } else {
                worldsavehandler = new AnvilSaveHandler(new File(dim == 0 ? "." : getFolderName()), worldFolder, true);
                worldInfo = worldsavehandler.loadWorldInfo();
            }

            if (worldInfo == null) {
                newWorldSettings = new WorldSettings(seed, this.getGameType(), this.canStructuresSpawn(), this.isHardcore(), type);
                newWorldSettings.setWorldName(generator);

                if (this.enableBonusChest) {
                    newWorldSettings.enableBonusChest();
                }

                worldInfo = new WorldInfo(newWorldSettings, worldFolder);
                ((IMixinWorldInfo) worldInfo).setUUID(UUID.randomUUID());
                if (dim == 0 || dim == -1 || dim == 1) {// if vanilla dimension
                    ((WorldProperties) worldInfo).setKeepSpawnLoaded(true);
                    ((WorldProperties) worldInfo).setLoadOnStartup(true);
                    ((WorldProperties) worldInfo).setEnabled(true);
                    if (dim != 0) {
                        ((WorldProperties) worldInfo).setGeneratorType(GeneratorTypes.DEFAULT);
                    }
                    Sponge.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
                }
            } else {
                worldInfo.setWorldName(worldFolder);
                newWorldSettings = new WorldSettings(worldInfo);
            }
            if (dim == 0) {
                this.setResourcePackFromWorld(this.getFolderName(), worldsavehandler);
            }

            ((IMixinWorldInfo) worldInfo).setDimensionId(dim);
            ((IMixinWorldInfo) worldInfo).setDimensionType(((Dimension) provider).getType());
            UUID uuid = ((WorldProperties) worldInfo).getUniqueId();
            Sponge.getSpongeRegistry().registerWorldUniqueId(uuid, worldFolder);
            final WorldServer world = (WorldServer) new WorldServer((MinecraftServer) (Object) this, worldsavehandler, worldInfo, dim,
                    this.theProfiler).init();

            world.initialize(newWorldSettings);
            world.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, world));

            if (!this.isSinglePlayer()) {
                world.getWorldInfo().setGameType(this.getGameType());
            }
            Sponge.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
            Sponge.getGame().getEventManager().post(SpongeImplEventFactory.createServerLoadWorld(Sponge.getGame(), (org.spongepowered.api.world.World)
                    world));
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
        String worldFolder = Sponge.getSpongeRegistry().getWorldFolder(uuid);
        if (worldFolder != null) {
            return loadWorld(worldFolder);
        }
        return Optional.absent();
    }

    @Override
    public Optional<World> loadWorld(WorldProperties properties) {
        if (properties == null) {
            return Optional.absent();
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
        Optional<WorldProperties> worldProperties = Sponge.getSpongeRegistry().getWorldProperties(worldName);
        if (worldProperties.isPresent()) {
            worldInfo = (WorldInfo) worldProperties.get();
        } else {
            worldInfo = savehandler.loadWorldInfo();
        }

        if (worldInfo != null) {
            // check if enabled
            if (!((WorldProperties) worldInfo).isEnabled()) {
                Sponge.getLogger().error("Unable to load world " + worldName + ". World is disabled!");
                return Optional.absent();
            }

            dim = ((IMixinWorldInfo) worldInfo).getDimensionId();
            if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
                DimensionManager
                        .registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
            }
            if (Sponge.getSpongeRegistry().getWorldFolder(dim) == null) {
                Sponge.getSpongeRegistry().registerWorldDimensionId(dim, worldName);
            }
            if (!Sponge.getSpongeRegistry().getWorldProperties(worldName).isPresent()) {
                Sponge.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
            }
        } else {
            return Optional.absent(); // no world data found
        }

        WorldSettings settings = new WorldSettings(worldInfo);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }

        WorldServer world = (WorldServer) new WorldServer((MinecraftServer) (Object) this, savehandler, worldInfo, dim, this.theProfiler).init();

        world.initialize(settings);
        ((IMixinWorldProvider) world.provider).setDimension(dim);

        world.addWorldAccess(new WorldManager((MinecraftServer) (Object) this, world));
        Sponge.getGame().getEventManager().post(SpongeImplEventFactory.createServerLoadWorld(Sponge.getGame(), (World) world));
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
        if (Sponge.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
            savehandler =
                    new AnvilSaveHandler(new File(Sponge.getGame().getSavesDirectory() + File.separator + getFolderName()), worldName,
                            true);

        } else {
            savehandler = new AnvilSaveHandler(new File(getFolderName()), worldName, true);
        }
        WorldInfo worldInfo = savehandler.loadWorldInfo();

        if (worldInfo != null) {
            if (!Sponge.getSpongeRegistry().getWorldProperties(((WorldProperties) worldInfo).getUniqueId()).isPresent()) {
                Sponge.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
                return Optional.of((WorldProperties) worldInfo);
            } else {
                return Sponge.getSpongeRegistry().getWorldProperties(((WorldProperties) worldInfo).getUniqueId());
            }
        }

        if (((IMixinWorldSettings)settings).getDimensionId() != 0) {
            dim = ((IMixinWorldSettings)settings).getDimensionId();
        } else {
            dim = DimensionManager.getNextFreeDimId();
        }
        worldInfo = new WorldInfo((WorldSettings) (Object) settings, settings.getWorldName());
        UUID uuid = UUID.randomUUID();
        ((IMixinWorldInfo) worldInfo).setUUID(uuid);
        ((IMixinWorldInfo) worldInfo).setDimensionId(dim);
        ((IMixinWorldInfo) worldInfo).setDimensionType(settings.getDimensionType());
        ((IMixinWorldInfo) worldInfo).setIsMod(((IMixinWorldSettings)settings).getIsMod());
        ((WorldProperties) worldInfo).setKeepSpawnLoaded(settings.doesKeepSpawnLoaded());
        ((WorldProperties) worldInfo).setLoadOnStartup(settings.loadOnStartup());
        ((WorldProperties) worldInfo).setEnabled(settings.isEnabled());
        ((WorldProperties) worldInfo).setGeneratorType(settings.getGeneratorType());
        ((WorldProperties) worldInfo).setGeneratorModifiers(settings.getGeneratorModifiers());
        Sponge.getSpongeRegistry().registerWorldProperties((WorldProperties) worldInfo);
        Sponge.getSpongeRegistry().registerWorldDimensionId(dim, worldName);
        Sponge.getSpongeRegistry().registerWorldUniqueId(uuid, worldName);

        if (!DimensionManager.isDimensionRegistered(dim)) { // handle reloads properly
            DimensionManager.registerDimension(dim, ((SpongeDimensionType) ((WorldProperties) worldInfo).getDimensionType()).getDimensionTypeId());
        }
        savehandler.saveWorldInfoWithPlayer(worldInfo, getConfigurationManager().getHostPlayerData());

        Sponge.getGame().getEventManager().post(SpongeEventFactory.createServerCreateWorld(Sponge.getGame(), (WorldProperties)
                worldInfo, settings));
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
        List<World> worlds = new ArrayList<World>();
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
        return Optional.absent();
    }

    @Override
    public Optional<WorldProperties> getDefaultWorld() {
        if (DimensionManager.getWorldFromDimId(0) != null) {
            return Optional.of(((World) DimensionManager.getWorldFromDimId(0)).getProperties());
        }
        return Optional.absent();
    }

    @Override
    public AnvilSaveHandler getHandler(String worldName) {
        if (Sponge.getGame().getPlatform().getType() == Platform.Type.CLIENT) {
            return new AnvilSaveHandler(new File(Sponge.getGame().getSavesDirectory() + File.separator + getFolderName()), worldName,
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
}
