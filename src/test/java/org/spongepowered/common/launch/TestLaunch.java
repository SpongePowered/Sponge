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
package org.spongepowered.common.launch;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.serialization.Lifecycle;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.api.Server;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.launch.inject.TestModule;
import org.spongepowered.common.server.BootstrapProperties;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.vanilla.launch.VanillaBaseLaunch;
import org.spongepowered.vanilla.launch.plugin.VanillaBasePluginManager;

import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestLaunch extends VanillaBaseLaunch {

    protected TestLaunch(final PluginPlatform pluginPlatform) {
        super(pluginPlatform, new VanillaBasePluginManager());
    }

    @Override
    public boolean dedicatedServer() {
        return true;
    }

    @Override
    public Stage injectionStage() {
        return Stage.DEVELOPMENT;
    }

    @Override
    public PluginContainer platformPlugin() {
        return null;
    }

    @Override
    public Injector createInjector() {
        return Guice.createInjector(this.injectionStage(), new SpongeCommonModule(), new TestModule());
    }

    /**
     * A simplified version of org.spongepowered.vanilla.launch.VanillaBootstrap#perform.
     * Lifecycle events are removed because there is no plugin that will listen to them.
     */
    private void bootstrapSponge() {
        // Sponge init
        final SpongeLifecycle lifecycle = this.createInjector().getInstance(SpongeLifecycle.class);
        this.setLifecycle(lifecycle);
        lifecycle.establishFactories();
        lifecycle.establishBuilders();
        lifecycle.establishGameServices();
        lifecycle.establishDataKeyListeners();

        // Minecraft server init
        try {
            createMinecraftServer();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create minecraft server", e);
        }
    }

    /**
     * A simplified version of {@link net.minecraft.server.Main#main} and {@link org.spongepowered.common.mixin.core.server.MainMixin}.
     * Creates a {@link DedicatedServer} but doesn't start it.
     * Lifecycle events are removed because there is no plugin that will listen to them.
     *
     * @throws Exception if anything fails to load
     */
    private static void createMinecraftServer() throws Exception {
        CrashReport.preload();
        Bootstrap.bootStrap();
        Bootstrap.validate();

        // This simplified startup avoids as much as possible writing to the disk but there are still a few files
        final Path serverDir = AppLaunch.pluginPlatform().baseDirectory();

        final RegistryAccess.RegistryHolder registries = RegistryAccess.builtin();
        final Path propertiesFile = Paths.get("server.properties");
        final DedicatedServerSettings settings = new DedicatedServerSettings(registries, propertiesFile);
        final DedicatedServerProperties properties = settings.getProperties();

        // Sponge
        BootstrapProperties.init(properties.worldGenSettings, properties.gamemode, properties.difficulty,
                properties.pvp, properties.hardcore, true, properties.viewDistance, registries);

        final YggdrasilAuthenticationService authService = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
        final MinecraftSessionService sessionService = authService.createMinecraftSessionService();
        final GameProfileRepository profileRepo = authService.createProfileRepository();
        final GameProfileCache profileCache = new GameProfileCache(profileRepo, serverDir.resolve(MinecraftServer.USERID_CACHE_FILE.getName()).toFile());

        final LevelStorageSource storageSource = LevelStorageSource.createDefault(serverDir);
        final LevelStorageSource.LevelStorageAccess mainStorageAccess = storageSource.createAccess(properties.levelName);

        // Sponge
        final org.spongepowered.common.launch.Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishGlobalRegistries();
        lifecycle.establishDataProviders();

        final PackRepository packRepo = new PackRepository(new ServerPacksSource(), new FolderRepositorySource(mainStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
        final DataPackConfig dataPackConfig = MinecraftServer.configurePackRepository(packRepo, DataPackConfig.DEFAULT, true);

        final ServerResources resources = ServerResources.loadResources(packRepo.openAllSelected(), Commands.CommandSelection.DEDICATED,
                properties.functionPermissionLevel, Util.backgroundExecutor(), Runnable::run).get();
        resources.updateGlobals();

        final RegistryReadOps<Tag> worldSettingsAdapter = RegistryReadOps.create(NbtOps.INSTANCE, resources.getResourceManager(), registries);

        // Sponge
        BootstrapProperties.worldSettingsAdapter(worldSettingsAdapter);
        BootstrapProperties.setIsNewLevel(true);

        final LevelSettings levelSettings = new LevelSettings(properties.levelName, properties.gamemode, properties.hardcore,
                properties.difficulty, false, new GameRules(), dataPackConfig);
        final WorldData worldData = new PrimaryLevelData(levelSettings, properties.worldGenSettings, Lifecycle.stable());

        final DedicatedServer server = new DedicatedServer(Thread.currentThread(), registries, mainStorageAccess, packRepo, resources, worldData,
                settings, DataFixers.getDataFixer(), sessionService, profileRepo, profileCache, LoggerChunkProgressListener::new);

        // Sponge
        SpongeCommon.game().setServer((Server) server);
    }

    public static void launch() {
        final TestLaunch launch = new TestLaunch(AppLaunch.pluginPlatform());
        Launch.setInstance(launch);
        launch.launchPlatform(new String[0]);
    }

    @Override
    protected void performBootstrap(String[] args) {
        this.bootstrapSponge();
    }
}
