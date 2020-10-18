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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.common.advancement.SpongeAdvancementProvider;
import org.spongepowered.common.bridge.server.MinecraftServerBridge;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.event.SpongeEventManager;
import org.spongepowered.common.event.lifecycle.RegisterBuilderEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterCatalogRegistryEventImpl;
import org.spongepowered.common.event.lifecycle.RegisterFactoryEventImpl;
import org.spongepowered.common.event.lifecycle.StartedEngineEventImpl;
import org.spongepowered.common.event.lifecycle.StartingEngineEventImpl;
import org.spongepowered.common.event.lifecycle.StoppingEngineEventImpl;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.recipe.SpongeRecipeProvider;
import org.spongepowered.common.launch.plugin.DummyPluginContainer;
import org.spongepowered.common.network.channel.SpongeChannelRegistry;
import org.spongepowered.common.registry.SpongeBuilderRegistry;
import org.spongepowered.common.registry.SpongeCatalogRegistry;
import org.spongepowered.common.registry.SpongeFactoryRegistry;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimingsFactory;
import org.spongepowered.common.service.SpongeServiceProvider;
import org.spongepowered.plugin.PluginContainer;

import java.util.Collection;
import java.util.stream.Collectors;

@Singleton
public final class SpongeLifecycle {

    private final Game game;
    private final Injector injector;

    @Inject
    public SpongeLifecycle(final Game game, final Injector injector) {
        this.game = game;
        this.injector = injector;
    }

    public void establishFactories() {
        ((SpongeFactoryRegistry) this.game.getRegistry().getFactoryRegistry()).registerDefaultFactories();
    }

    public void establishBuilders() {
        ((SpongeBuilderRegistry) this.game.getRegistry().getBuilderRegistry()).registerDefaultBuilders();
    }

    public void callRegisterFactoryEvent() {
        this.game.getEventManager().post(new RegisterFactoryEventImpl(Cause.of(EventContext.empty(), this.game), this.game));
    }

    public void callRegisterBuilderEvent() {
        this.game.getEventManager().post(new RegisterBuilderEventImpl(Cause.of(EventContext.empty(), this.game), this.game));
    }

    public void establishRegistries() {
        final SpongeCatalogRegistry spongeCatalogRegistry = (SpongeCatalogRegistry) this.game.getRegistry().getCatalogRegistry();

        spongeCatalogRegistry.registerDefaultRegistries();
        spongeCatalogRegistry.registerDefaultSuppliers();

        this.game.getEventManager().post(new RegisterCatalogRegistryEventImpl(Cause.of(EventContext.empty(), this.game), this.game));

        spongeCatalogRegistry.callRegisterCatalogEvents(Cause.of(EventContext.empty(), this.game), this.game);
    }

    public void establishDataPackRegistries() {
        final SpongeCatalogRegistry spongeCatalogRegistry = (SpongeCatalogRegistry) this.game.getRegistry().getCatalogRegistry();
        spongeCatalogRegistry.callDataPackRegisterCatalogEvents(Cause.of(EventContext.empty(), this.game), this.game);

        // After all plugins registered their recipes we serialize them
        SpongeRecipeProvider.registerRecipes(spongeCatalogRegistry.getRegistry(RecipeRegistration.class));
        SpongeAdvancementProvider.registerAdvancements(spongeCatalogRegistry.getRegistry(Advancement.class));

    }

    public void callRegisterChannelEvent() {
        ((SpongeChannelRegistry) this.game.getChannelRegistry()).postRegistryEvent();
    }

    public void initTimings() {
        SpongeTimingsFactory.INSTANCE.init();
    }

    public void establishGlobalServices() {
        ((SpongeServiceProvider) this.game.getServiceProvider()).init();
    }

    public void establishServerServices() {
        ((MinecraftServerBridge) this.game.getServer()).bridge$initServices(this.game, this.injector);
    }

    public void establishServerFeatures() {
        //Sponge.getSystemSubject().getContainingCollection();
        // Yes this looks odd but prevents having to do sided lifecycle solely to always point at the Server
        ((SpongeServer) this.game.getServer()).getUsernameCache().load();
    }

    public void establishCommands() {
        ((SpongeCommandManager) this.game.getCommandManager()).init();
    }

    public void registerPluginListeners() {
        for (final PluginContainer plugin : this.filterInternalPlugins(this.game.getPluginManager().getPlugins())) {
            this.game.getEventManager().registerListeners(plugin, plugin.getInstance());
        }
    }

    // Methods are in order of the SpongeCommon lifecycle

    public void callConstructEvent() {
        for (final PluginContainer plugin : this.filterInternalPlugins(this.game.getPluginManager().getPlugins())) {
            ((SpongeEventManager) this.game.getEventManager()).post(SpongeEventFactory.createConstructPluginEvent(Cause.of(EventContext.empty(),
                    this.game), this.game, plugin), plugin);
        }
    }

    public void callStartingEngineEvent(final Engine engine) {
        this.game.getEventManager().post(new StartingEngineEventImpl<>(PhaseTracker.getCauseStackManager().getCurrentCause(), this.game, engine, (TypeToken<Engine>) TypeToken.of(engine.getClass())));
    }

    public void callStartedEngineEvent(final Engine engine) {
        this.game.getEventManager().post(new StartedEngineEventImpl<>(PhaseTracker.getCauseStackManager().getCurrentCause(), this.game, engine, (TypeToken<Engine>) TypeToken.of(engine.getClass())));
    }

    public void callLoadedGameEvent() {
        this.game.getEventManager().post(SpongeEventFactory.createLoadedGameEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), this.game));
    }

    public void callStoppingEngineEvent(final Engine engine) {
        this.game.getEventManager().post(new StoppingEngineEventImpl<>(PhaseTracker.getCauseStackManager().getCurrentCause(), this.game, engine, (TypeToken<Engine>) TypeToken.of(engine.getClass())));
    }

    private Collection<PluginContainer> filterInternalPlugins(final Collection<PluginContainer> plugins) {
        return plugins
                .stream()
                .filter(plugin -> !(plugin instanceof DummyPluginContainer))
                .collect(Collectors.toList());
    }

    public void establishDataProviders() {
        SpongeDataManager.getInstance().registerDefaultProviders();
    }
}
