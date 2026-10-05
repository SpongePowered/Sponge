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
package org.spongepowered.common;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.WorldDataConfiguration;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Client;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryRoots;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.common.bridge.core.WritableRegistryBridge;
import org.spongepowered.common.bridge.server.packs.resources.ResourceManagerBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.event.lifecycle.AbstractRegisterRegistryEvent;
import org.spongepowered.common.event.lifecycle.AbstractRegisterRegistryValueEvent;
import org.spongepowered.common.event.lifecycle.FreezeRegistryEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterBuilderEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterChannelEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterDataEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterFactoryEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterTagEventImpl;
import org.spongepowered.common.event.manager.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.launch.plugin.SpongePluginContainer;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.profile.SpongeGameProfileManager;
import org.spongepowered.common.registry.SpongeBuilderProvider;
import org.spongepowered.common.registry.SpongeFactoryProvider;
import org.spongepowered.common.registry.SpongeRegistries;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.scheduler.AsyncScheduler;
import org.spongepowered.common.service.SpongeServiceProvider;
import org.spongepowered.common.service.server.SpongeServerScopedServiceProvider;
import org.spongepowered.common.service.server.permission.SpongeContextCalculator;
import org.spongepowered.common.util.ExecutorUtil;
import org.spongepowered.common.world.server.SpongeWorldManager;
import org.spongepowered.plugin.PluginContainer;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public final class SpongeLifecycle implements Lifecycle {

    private final Game game;
    private final Injector injector;
    private FeatureFlagSet featureFlags;

    @Inject
    public SpongeLifecycle(final Game game, final Injector injector) {
        this.game = game;
        this.injector = injector;
    }

    @Override
    public Injector platformInjector() {
        return this.injector;
    }

    @Override
    public void establishFactories() {
        ((SpongeFactoryProvider) this.game.factoryProvider()).registerDefaultFactories();
    }

    @Override
    public void establishBuilders() {
        ((SpongeBuilderProvider) this.game.builderProvider()).registerDefaultBuilders();
        ((SpongeDataManager) this.game.dataManager()).registerDefaultBuilders();
    }

    @Override
    public void callRegisterFactoryEvent() {
        this.game.eventManager().post(new RegisterFactoryEventImpl(Cause.of(EventContext.empty(), this.game), this.game));
    }

    @Override
    public void callRegisterBuilderEvent() {
        this.game.eventManager().post(new RegisterBuilderEventImpl(Cause.of(EventContext.empty(), this.game), this.game));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void establishEarlyGlobalRegistries() {
        final SpongeRegistryHolder holder = (SpongeRegistryHolder) this.game;
        holder.setRootMinecraftRegistry((Registry<Registry<?>>) BuiltInRegistries.REGISTRY);
        holder.streamRegistries().forEach(r -> ((WritableRegistryBridge<?>) r).bridge$setRegistryHolder(this.game));

        SpongeRegistries.registerEarlyGlobalRegistries(holder);
    }

    @Override
    public void establishGlobalRegistries() {
        final SpongeRegistryHolder holder = (SpongeRegistryHolder) this.game;

        // Plugin registries
        this.game.eventManager().post(new AbstractRegisterRegistryEvent.GameScopedImpl(Cause.of(EventContext.empty(), this.game), this.game));

        // Freeze Sponge Root - Registries are now available
        holder.registryHolder().freezeSpongeRootRegistry();

        final Map<RegistryType<?>, org.spongepowered.api.registry.Registry<?>> map =
                holder.streamRegistries().collect(Collectors.toMap(org.spongepowered.api.registry.Registry::type, Function.identity()));

        this.game.eventManager().post(new AbstractRegisterRegistryValueEvent.GameScopedImpl(Cause.of(EventContext.empty(), this.game), this.game, map));
        map.values().forEach(r -> ((WritableRegistryBridge<?>) r).bridge$markEventCalled());
        holder.registryHolder().freezeSpongeDynamicRegistries(false);
    }

    public void endEstablishGlobalRegistries() {
        ((SpongeRegistryHolder) this.game).registryHolder().freezeSpongeDynamicRegistries(true);

        this.game.eventManager().post(new FreezeRegistryEventImpl.PostImpl.GameImpl(Cause.of(EventContext.empty(), this.game), this.game));
    }

    @Override
    public void callRegisterDataEvent() {
        this.game.eventManager().post(new RegisterDataEventImpl(Cause.of(EventContext.empty(), Sponge.game()), Sponge.game(),
            (SpongeDataManager) this.game.dataManager()));
    }

    @Override
    public void establishDataProviders() {
        ((SpongeDataManager) this.game.dataManager()).registerDefaultProviders();
    }

    @Override
    public void establishDataKeyListeners() {
        ((SpongeDataManager) this.game.dataManager()).registerKeyListeners();
    }

    @Override
    public void callRegisterChannelEvent() {
        this.game.eventManager().post(new RegisterChannelEventImpl(Cause.of(EventContext.empty(), this.game), this.game,
                (SpongeChannelManager) this.game.channelManager()));
    }

    @Override
    public void establishGameServices() {
        ((SpongeServiceProvider) this.game.serviceProvider()).init();
    }

    @Override
    public void establishServerServices(final ResourceManager resourceManager, final PermissionLevel functionsPermissionLevel) {
        final SpongeServerScopedServiceProvider serviceProvider = new SpongeServerScopedServiceProvider(this.game, this.injector, functionsPermissionLevel);
        serviceProvider.init();
        ((ResourceManagerBridge) resourceManager).bridge$services(serviceProvider);
    }

    @Override
    public void establishServerFeatures() {
        // In 26.1, the service provider may not yet be available during ServerAboutToStartEvent
        // on the integrated server path (NeoForge/Forge). The service provider is set in
        // MinecraftServer.<init> TAIL from the WorldStem's resource manager, but on integrated
        // servers the event can fire before the services propagate. Guard against this.
        final var serviceProvider = Sponge.server().serviceProvider();
        if (serviceProvider != null) {
            serviceProvider.contextService().registerContextCalculator(new SpongeContextCalculator());
        }
        // Yes this looks odd but prevents having to do sided lifecycle solely to always point at the Server
        ((SpongeServer) this.game.server()).getUsernameCache().load();
    }

    // Methods are in order of the SpongeCommon lifecycle

    @Override
    public void callConstructEvent() {
        for (final PluginContainer plugin : this.game.pluginManager().plugins()) {
            ((SpongeEventManager) this.game.eventManager()).postToPlugin(SpongeEventFactory.createConstructPluginEvent(Cause.of(EventContext.empty(),
                    this.game), this.game, plugin), plugin);
        }
    }

    @Override
    public void beginEstablishServerRegistries(final RegistryHolder server) {
        SpongeRegistries.registerServerRegistries((SpongeRegistryHolder) server, this.featureFlags);

        ((SpongeRegistryHolder) server).registryHolder().featureFlagSet(this.featureFlags);

        this.game.eventManager().post(
            AbstractRegisterRegistryEvent.EngineScopedImpl.server(Cause.of(EventContext.empty(), this.game), this.game, server));

        ((SpongeRegistryHolder) server).registryHolder().freezeSpongeRootRegistry();

        this.processServerRegistries(server, server.streamRegistries(RegistryRoots.SPONGE)
            .filter(r -> !r.type().equals(RegistryTypes.ADVANCEMENT) && !r.type().equals(RegistryTypes.RECIPE)));
    }

    @Override
    public void processServerRegistries(final RegistryHolder server, final Stream<? extends org.spongepowered.api.registry.Registry<?>> registries) {
        try {
            PhaseTracker.getInstance().startupRegistryHolder(server);
            final Map<RegistryType<?>, org.spongepowered.api.registry.Registry<?>> map =
                    registries.collect(Collectors.toMap(org.spongepowered.api.registry.Registry::type, Function.identity()));
            if (!map.isEmpty()) {
                map.values().forEach(r -> ((WritableRegistryBridge<?>) r).bridge$setRegistryHolder(server));
                this.game.eventManager().post(AbstractRegisterRegistryValueEvent.EngineScopedImpl.server(Cause.of(EventContext.empty(), this.game), this.game, server, map));
                map.values().forEach(r -> ((WritableRegistryBridge<?>) r).bridge$markEventCalled());
                ((SpongeRegistryHolder) server).registryHolder().freezeSpongeDynamicRegistries(false);
            }
        } finally {
            PhaseTracker.getInstance().startupRegistryHolder(null);
        }
    }

    @Override
    public void endEstablishServerRegistries(final RegistryHolder server) {
        ((SpongeRegistryHolder) server).registryHolder().freezeSpongeDynamicRegistries(true);
    }

    @Override
    public void establishClientRegistries(final Client client) {
        this.game.eventManager().post(
            AbstractRegisterRegistryEvent.EngineScopedImpl.client(Cause.of(EventContext.empty(), this.game), this.game, client));

        ((SpongeRegistryHolder) client).registryHolder().freezeSpongeRootRegistry();

        this.game.eventManager().post(AbstractRegisterRegistryValueEvent.EngineScopedImpl.client(Cause.of(EventContext.empty(), this.game),
                this.game, client, client.streamRegistries(RegistryRoots.SPONGE).collect(Collectors.toMap(org.spongepowered.api.registry.Registry::type, Function.identity()))));

        ((SpongeRegistryHolder) client).registryHolder().freezeSpongeDynamicRegistries(true);

        this.game.eventManager().post(FreezeRegistryEventImpl.PostImpl.EngineImpl.client(Cause.of(EventContext.empty(), this.game), this.game, client));
    }

    @Override
    public void callStartingEngineEvent(final Engine engine) {
        final boolean server = engine instanceof Server;
        for (final PluginContainer container : this.game.pluginManager().plugins()) {
            if (container instanceof SpongePluginContainer spongeContainer) {
                try {
                    spongeContainer.loadSidedEntrypoints(server);
                } catch (final Exception e) {
                    PluginPlatform.LOGGER.error("Failed to load {} sided entrypoints of plugin {}", server ? "server" : "client", container.metadata().id(), e);
                }
            }
        }

        this.game.eventManager().post(SpongeEventFactory.createStartingEngineEvent(PhaseTracker.getInstance().currentCause(),
                engine, this.game, (TypeToken<Engine>) TypeToken.get(engine.getClass())));
    }

    @Override
    public void callStartedEngineEvent(final Engine engine) {
        this.game.eventManager().post(SpongeEventFactory.createStartedEngineEvent(PhaseTracker.getInstance().currentCause(),
                engine, this.game, (TypeToken<Engine>) TypeToken.get(engine.getClass())));
    }

    @Override
    public void callLoadedGameEvent() {
        this.game.eventManager().post(SpongeEventFactory.createLoadedGameEvent(PhaseTracker.getInstance().currentCause(), this.game));
    }

    @Override
    public void callStoppingEngineEvent(final Engine engine) {
        if (engine instanceof SpongeServer) {
            ExecutorUtil.serverManagedBlock((MinecraftServer) this.game.server(), ((SpongeWorldManager) this.game.server().worldManager()).close());
        }
        this.game.eventManager().post(SpongeEventFactory.createStoppingEngineEvent(PhaseTracker.getInstance().currentCause(),
                engine, this.game, (TypeToken<Engine>) TypeToken.get(engine.getClass())));
        if (engine instanceof SpongeServer) {
            final @Nullable SpongeGameProfileManager profileManager = ((SpongeServer) engine).gameProfileManagerIfPresent();
            if (profileManager != null) {
                profileManager.close();
            }
        }
    }

    @Override
    public void callStoppedGameEvent() {
        // Call an event for plugins to shut down any thread pools
        this.game.eventManager().post(SpongeEventFactory.createStoppedGameEvent(PhaseTracker.getInstance().currentCause(), this.game));

        // Then shut down our own thread pool
        ((AsyncScheduler) this.game.asyncScheduler()).close();
    }

    @Override
    public void setWorldDataConfiguration(final WorldDataConfiguration config) {
        this.featureFlags = config.enabledFeatures();
    }

    @Override
    public void establishTags(final ResourceManager resourceManager) {
        final RegisterTagEventImpl event = new RegisterTagEventImpl(Cause.of(EventContext.empty(), this.game), this.game);
        this.game.eventManager().post(event);
        ((ResourceManagerBridge) resourceManager).bridge$pluginProvidedTags(event.tags());
    }
}
