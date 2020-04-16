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
package org.spongepowered.server;

import static com.google.common.base.Preconditions.checkState;
import static net.minecraft.server.MinecraftServer.USER_CACHE_FILE;
import static org.spongepowered.server.launch.VanillaCommandLine.BONUS_CHEST;
import static org.spongepowered.server.launch.VanillaCommandLine.PORT;
import static org.spongepowered.server.launch.VanillaCommandLine.WORLD_DIR;
import static org.spongepowered.server.launch.VanillaCommandLine.WORLD_NAME;
import static org.spongepowered.server.launch.VanillaLaunch.Environment.DEVELOPMENT;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import joptsimple.OptionSet;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.datafix.DataFixesManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.common.SpongeBootstrap;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.SpongeInternalListeners;
import org.spongepowered.common.bridge.command.ServerCommandManagerBridge;
import org.spongepowered.common.entity.ai.SpongeEntityAICommonSuperclass;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.inject.SpongeGuice;
import org.spongepowered.common.inject.SpongeModule;
import org.spongepowered.common.network.message.SpongeMessageHandler;
import org.spongepowered.common.registry.SpongeGameRegistry;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.service.permission.SpongeContextCalculator;
import org.spongepowered.common.service.permission.SpongePermissionService;
import org.spongepowered.common.service.sql.SqlServiceImpl;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.storage.SpongePlayerDataHandler;
import org.spongepowered.server.inject.SpongeVanillaModule;
import org.spongepowered.server.launch.VanillaCommandLine;
import org.spongepowered.server.launch.VanillaLaunch;
import org.spongepowered.server.launch.plugin.PluginSource;
import org.spongepowered.server.accessor.server.MinecraftServerAccessor_Vanilla;
import org.spongepowered.server.plugin.MetaPluginContainer;
import org.spongepowered.server.plugin.MetadataContainer;
import org.spongepowered.server.plugin.MinecraftPluginContainer;
import org.spongepowered.server.plugin.SpongeCommonContainer;
import org.spongepowered.server.plugin.VanillaPluginManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.Optional;
import java.util.UUID;

@Singleton
public final class SpongeVanilla extends MetaPluginContainer {

    private final Logger logger;
    private final Game game;
    private final SpongeGameRegistry registry;

    @Inject
    public SpongeVanilla(MetadataContainer metadata, Logger logger, Game game, SpongeGameRegistry registry) {
        super(metadata.get(SpongeImplHooks.getImplementationId(), "SpongeVanilla"), PluginSource.find(SpongeVanilla.class));

        this.logger = logger;
        this.game = game;
        this.registry = registry;

        this.logger.info("This server is running {} version {}", getName(), getVersion().orElse("unknown"));
    }

    public void preInitialize() throws Exception {
        this.logger.info("Loading Sponge...");

        // Pre-initialize registry
        this.registry.preRegistryInit();
        PhaseTracker.SERVER.init(); // Needs to occur after the game registry registers all the builders.

        this.game.getEventManager().registerListeners(this, SpongeInternalListeners.getInstance());
        SpongeBootstrap.initializeServices();
        SpongeBootstrap.initializeCommands();
        for (EntityTypeRegistryModule.FutureRegistration registration : EntityTypeRegistryModule.getInstance().getCustomEntities()) {
            try {
                // Workaround until we can have static mixin accessors.
                final String registerName = SpongeImplHooks.isDeobfuscatedEnvironment() ? "register" : "func_191303_a";
                Method register = EntityList.class.getDeclaredMethod(registerName, int.class, String.class, Class.class, String.class);
                register.setAccessible(true);
                register.invoke(null, registration.id, registration.name.toString(), registration.type, registration.oldName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.logger.info("Loading plugins...");
        ((VanillaPluginManager) this.game.getPluginManager()).loadPlugins();
        SpongeImpl.postState(GameState.CONSTRUCTION, SpongeEventFactory.createGameConstructionEvent(Sponge.getCauseStackManager().getCurrentCause()));
        SpongeImpl.getLogger().info("Initializing plugins...");
        SpongeImpl.postState(GameState.PRE_INITIALIZATION, SpongeEventFactory.createGamePreInitializationEvent(Sponge.getCauseStackManager().getCurrentCause()));
        this.registry.preInit();

        checkState(Class.forName("org.spongepowered.api.entity.ai.task.AbstractAITask").getSuperclass()
                .equals(SpongeEntityAICommonSuperclass.class));

        SpongeInternalListeners.getInstance().registerServiceCallback(PermissionService.class,
                input -> input.registerContextCalculator(new SpongeContextCalculator()));

        SpongeHooks.enableThreadContentionMonitoring();

        SpongeMessageHandler.init();
    }

    public void initialize() {
        this.registry.init();

        if (!this.game.getServiceManager().provide(PermissionService.class).isPresent()) {
            SpongePermissionService service = new SpongePermissionService(this.game);
            this.game.getServiceManager().setProvider(this, PermissionService.class, service);
        }

        SpongeImpl.postState(GameState.INITIALIZATION, SpongeEventFactory.createGameInitializationEvent(Sponge.getCauseStackManager().getCurrentCause()));

        this.registry.postInit();
        SpongeImpl.getConfigSaveManager().flush();

        SpongeImpl.postState(GameState.POST_INITIALIZATION, SpongeEventFactory.createGamePostInitializationEvent(Sponge.getCauseStackManager().getCurrentCause()));

        this.logger.info("Successfully loaded and initialized plugins.");

        SpongeImpl.postState(GameState.LOAD_COMPLETE, SpongeEventFactory.createGameLoadCompleteEvent(Sponge.getCauseStackManager().getCurrentCause()));
    }

    public void onServerAboutToStart() {
        ((ServerCommandManagerBridge) SpongeImpl.getServer().getCommandManager()).bridge$registerEarlyCommands(this.game);
        SpongeImpl.postState(GameState.SERVER_ABOUT_TO_START, SpongeEventFactory.createGameAboutToStartServerEvent(Sponge.getCauseStackManager().getCurrentCause()));
    }

    public void onServerStarting() {
        SpongeImpl.postState(GameState.SERVER_STARTING, SpongeEventFactory.createGameStartingServerEvent(Sponge.getCauseStackManager().getCurrentCause()));
        SpongeImpl.getConfigSaveManager().flush();
        SpongeImpl.postState(GameState.SERVER_STARTED, SpongeEventFactory.createGameStartedServerEvent(Sponge.getCauseStackManager().getCurrentCause()));
        ((ServerCommandManagerBridge) SpongeImpl.getServer().getCommandManager()).bridge$registerLowPriorityCommands(this.game);
        SpongePlayerDataHandler.init();
    }

    public void onServerStopping() {
        SpongeImpl.postState(GameState.SERVER_STOPPING, SpongeEventFactory.createGameStoppingServerEvent(Sponge.getCauseStackManager().getCurrentCause()));
    }

    public void onServerStopped() throws IOException {
        SpongeImpl.postState(GameState.SERVER_STOPPED, SpongeEventFactory.createGameStoppedServerEvent(Sponge.getCauseStackManager().getCurrentCause()));
        ((SqlServiceImpl) this.game.getServiceManager().provideUnchecked(SqlService.class)).close();
        SpongeImpl.getConfigSaveManager().flush();
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.of(this);
    }

    @SuppressWarnings("ConstantConditions")
    private static void start(String[] args) {
        // Attempt to load metadata
        MetadataContainer metadata = MetadataContainer.load();

        // Register Minecraft plugin container
        MinecraftPluginContainer.register();
        SpongeCommonContainer.register(metadata);

        OptionSet options = VanillaCommandLine.parse(args);

        // Note: This launches the server instead of MinecraftServer.main
        // Keep command line options up-to-date with Vanilla

        Bootstrap.register();

        File worldDir = options.has(WORLD_DIR) ? options.valueOf(WORLD_DIR) : new File(".");

        YggdrasilAuthenticationService authenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
        MinecraftSessionService sessionService = authenticationService.createMinecraftSessionService();
        GameProfileRepository profileRepository = authenticationService.createProfileRepository();
        PlayerProfileCache profileCache = new PlayerProfileCache(profileRepository, new File(worldDir, USER_CACHE_FILE.getName()));

        DedicatedServer server = new DedicatedServer(worldDir, DataFixesManager.createFixer(),
                authenticationService, sessionService, profileRepository, profileCache);

        // We force-load NetHandlerPlayServer here.
        // Otherwise, VanillaChannelRegistrar causes it to be loaded from
        // within the Guice injector (see SpongeVanillaModule), thus swallowing
        // any Mixin exception that occurs.
        //
        // See https://github.com/SpongePowered/SpongeVanilla/issues/235 for a more
        // in-depth explanation
        NetHandlerPlayServer.class.getName();

        final Stage stage = SpongeGuice.getInjectorStage(VanillaLaunch.ENVIRONMENT == DEVELOPMENT ? Stage.DEVELOPMENT : Stage.PRODUCTION);
        SpongeImpl.getLogger().debug("Creating injector in stage '{}'", stage);
        Guice.createInjector(stage, new SpongeModule(), new SpongeVanillaModule(server, metadata));

        if (options.has(WORLD_NAME)) {
            server.setFolderName(options.valueOf(WORLD_NAME));
        }

        if (options.has(PORT)) {
            server.setServerPort(options.valueOf(PORT));
        }

        if (options.has(BONUS_CHEST)) {
            server.canCreateBonusChest(true);
        }

        server.startServerThread();
        Runtime.getRuntime().addShutdownHook(new Thread(((MinecraftServerAccessor_Vanilla) server)::accessor$stopServer, "Server Shutdown Thread"));
    }

    public static void main(String[] args) {
        try {
            start(args);
        } catch (Exception e) {
            SpongeImpl.getLogger().fatal("Failed to start the Minecraft server", e);
            System.exit(1);
        }
    }

}
