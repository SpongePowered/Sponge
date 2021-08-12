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

import co.aikar.timings.TimingsFactory;
import co.aikar.timings.sponge.SpongeTimingsFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.leangen.geantyref.TypeToken;
import net.minecraft.core.Registry;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Client;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.common.applaunch.plugin.DummyPluginContainer;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.datapack.SpongeDataPackManager;
import org.spongepowered.common.event.lifecycle.AbstractRegisterRegistryEvent;
import org.spongepowered.common.event.lifecycle.AbstractRegisterRegistryValueEvent;
import org.spongepowered.common.event.lifecycle.RegisterBuilderEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterChannelEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterDataEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterFactoryEventImpl;
import org.spongepowered.common.event.manager.SpongeEventManager;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.launch.Lifecycle;
import org.spongepowered.common.network.channel.SpongeChannelManager;
import org.spongepowered.common.profile.SpongeGameProfileManager;
import org.spongepowered.common.registry.SpongeBuilderProvider;
import org.spongepowered.common.registry.SpongeFactoryProvider;
import org.spongepowered.common.registry.SpongeRegistries;
import org.spongepowered.common.registry.SpongeRegistryHolder;
import org.spongepowered.common.scheduler.AsyncScheduler;
import org.spongepowered.common.service.SpongeServiceProvider;
import org.spongepowered.common.service.server.permission.SpongeContextCalculator;
import org.spongepowered.plugin.PluginContainer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

@Singleton
public final class SpongeLifecycle implements Lifecycle {

    private final Game game;
    private final Injector injector;

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

    @Override
    public void establishGlobalRegistries() {
        final SpongeRegistryHolder holder = (SpongeRegistryHolder) this.game.registries();
        // Need to do this here to prevent classloading Registry too early...
        holder.setRootMinecraftRegistry((Registry<Registry<?>>) Registry.REGISTRY);

        SpongeRegistries.registerGlobalRegistries((SpongeRegistryHolder) this.game.registries());

        this.game.eventManager().post(new AbstractRegisterRegistryEvent.GameScopedImpl(Cause.of(EventContext.empty(), this.game), this.game));
        this.game.eventManager().post(new AbstractRegisterRegistryValueEvent.GameScopedImpl(Cause.of(EventContext.empty(), this.game), this.game));
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
    public void callRegisterDataPackValueEvent(final Path dataPacksDirectory) {
        SpongeDataPackManager.INSTANCE.callRegisterDataPackValueEvents(dataPacksDirectory);
    }

    @Override
    public void callRegisterChannelEvent() {
        this.game.eventManager().post(new RegisterChannelEventImpl(Cause.of(EventContext.empty(), this.game), this.game,
                (SpongeChannelManager) this.game.channelManager()));
    }

    @Override
    public void initTimings() {
        ((SpongeTimingsFactory) this.game.factoryProvider().provide(TimingsFactory.class)).init();
    }

    @Override
    public void establishGameServices() {
        ((SpongeServiceProvider) this.game.serviceProvider()).init();
    }

    @Override
    public void establishServerServices() {
        ((MinecraftServerBridge) this.game.server()).bridge$initServices(this.game, this.injector);
    }

    @Override
    public void establishServerFeatures() {
        Sponge.server().serviceProvider().contextService().registerContextCalculator(new SpongeContextCalculator());
        // Yes this looks odd but prevents having to do sided lifecycle solely to always point at the Server
        ((SpongeServer) this.game.server()).getUsernameCache().load();
    }

    // Methods are in order of the SpongeCommon lifecycle

    @Override
    public void callConstructEvent() {
        for (final PluginContainer plugin : this.filterInternalPlugins(this.game.pluginManager().plugins())) {
            ((SpongeEventManager) this.game.eventManager()).postToPlugin(SpongeEventFactory.createConstructPluginEvent(Cause.of(EventContext.empty(),
                    this.game), this.game, plugin), plugin);
        }
    }

    @Override
    public void establishServerRegistries(final Server server) {
        SpongeRegistries.registerServerRegistries((SpongeRegistryHolder) server.registries());

        this.game.eventManager().post(new AbstractRegisterRegistryEvent.EngineScopedImpl<>(Cause.of(EventContext.empty(), this.game), this.game,
         server));

        this.game.eventManager().post(new AbstractRegisterRegistryValueEvent.EngineScopedImpl<>(Cause.of(EventContext.empty(), this.game),
                this.game, server));
    }

    @Override
    public void establishClientRegistries(final Client client) {
        this.game.eventManager().post(new AbstractRegisterRegistryEvent.EngineScopedImpl<>(Cause.of(EventContext.empty(), this.game), this.game,
                client));

        this.game.eventManager().post(new AbstractRegisterRegistryValueEvent.EngineScopedImpl<>(Cause.of(EventContext.empty(), this.game),
                this.game, client));
    }

    @Override
    public void callStartingEngineEvent(final Engine engine) {
        this.game.eventManager().post(SpongeEventFactory.createStartingEngineEvent(PhaseTracker.getCauseStackManager().currentCause(),
                engine, this.game, (TypeToken<Engine>) TypeToken.get(engine.getClass())));
    }

    @Override
    public void callStartedEngineEvent(final Engine engine) {
        this.game.eventManager().post(SpongeEventFactory.createStartedEngineEvent(PhaseTracker.getCauseStackManager().currentCause(),
                engine, this.game, (TypeToken<Engine>) TypeToken.get(engine.getClass())));
    }

    @Override
    public void callLoadedGameEvent() {
        this.game.eventManager().post(SpongeEventFactory.createLoadedGameEvent(PhaseTracker.getCauseStackManager().currentCause(), this.game));
    }

    @Override
    public void callStoppingEngineEvent(final Engine engine) {
        this.game.eventManager().post(SpongeEventFactory.createStoppingEngineEvent(PhaseTracker.getCauseStackManager().currentCause(),
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
        this.game.eventManager().post(SpongeEventFactory.createStoppedGameEvent(PhaseTracker.getCauseStackManager().currentCause(), this.game));

        // Then shut down our own thread pool
        ((AsyncScheduler) this.game.asyncScheduler()).close();
    }

    private Collection<PluginContainer> filterInternalPlugins(final Collection<PluginContainer> plugins) {
        return plugins
                .stream()
                .filter(plugin -> !(plugin instanceof DummyPluginContainer))
                .collect(Collectors.toList());
    }
}
