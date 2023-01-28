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
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.serialization.Lifecycle;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Services;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.api.Server;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.SpongeLifecycle;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.launch.inject.TestModule;
import org.spongepowered.common.world.server.SpongeWorldManager;
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
        SharedConstants.tryDetectVersion();

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

        final Path propertiesFile = Paths.get("server.properties");
        final DedicatedServerSettings settings = new DedicatedServerSettings(propertiesFile);
        final DedicatedServerProperties props = settings.getProperties();

        final Services services = Services.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), serverDir.toFile());
        final LevelStorageSource storageSource = LevelStorageSource.createDefault(serverDir);
        final LevelStorageSource.LevelStorageAccess storageAccess = storageSource.createAccess(props.levelName);

        // Sponge
        final org.spongepowered.common.launch.Lifecycle lifecycle = Launch.instance().lifecycle();
        lifecycle.establishDataProviders();

        final PackRepository packRepo = ServerPacksSource.createPackRepository(storageAccess.getLevelPath(LevelResource.DATAPACK_DIR));
        final WorldDataConfiguration dataConfig = new WorldDataConfiguration(props.initialDataPackConfiguration, FeatureFlags.DEFAULT_FLAGS);
        final WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepo, dataConfig, false, true);
        final WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.DEDICATED, props.functionPermissionLevel);

        final WorldStem worldStem = Util.blockUntilDone((executor) -> {
            return WorldLoader.load(initConfig, (context) -> {
                final Registry<LevelStem> stemRegistry = context.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
                final RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, context.datapackWorldgen());

                // Sponge
                SpongeWorldManager.bootstrapOps = ops;

                final LevelSettings levelSettings = new LevelSettings(props.levelName, props.gamemode, props.hardcore, props.difficulty, false,
                        new GameRules(), context.dataConfiguration());
                final WorldOptions worldOptions = props.worldOptions;
                final WorldDimensions worldDimensionsBuilder = props.createDimensions(context.datapackWorldgen());

                final WorldDimensions.Complete worldDimensions = worldDimensionsBuilder.bake(stemRegistry);
                final Lifecycle serializationLifecycle = worldDimensions.lifecycle().add(context.datapackWorldgen().allRegistriesLifecycle());
                return new WorldLoader.DataLoadOutput<>(new PrimaryLevelData(levelSettings, worldOptions, worldDimensions.specialWorldProperty(),
                        serializationLifecycle), worldDimensions.dimensionsRegistryAccess());
            }, WorldStem::new, Util.backgroundExecutor(), executor);
        }).get();

        final DedicatedServer server = new DedicatedServer(Thread.currentThread(), storageAccess, packRepo, worldStem,
                settings, DataFixers.getDataFixer(), services, LoggerChunkProgressListener::new);

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
