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
package org.spongepowered.test;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Client;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.ProvideServiceEvent;
import org.spongepowered.api.event.lifecycle.RegisterBuilderEvent;
import org.spongepowered.api.event.lifecycle.RegisterCatalogEvent;
import org.spongepowered.api.event.lifecycle.RegisterCatalogRegistryEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.RegisterFactoryEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.whitelist.WhitelistService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin("test")
public final class TestPlugin {

    private final Logger logger;
    private final PluginContainer plugin;

    @Inject
    public TestPlugin(final Logger logger, final PluginContainer plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    @Listener
    public void onConstruct(final ConstructPluginEvent event) {
        this.logger.info(event);
    }

    @Listener
    public void onRegisterFactory(final RegisterFactoryEvent event) {
        this.logger.info(event);
    }

    @Listener
    public void onRegisterBuilder(final RegisterBuilderEvent event) {
        this.logger.info(event);
    }

    @Listener
    public void onProvideService(final ProvideServiceEvent<WhitelistService> event) {
        this.logger.info(event);
        event.suggest(TestWhitelistService::new);
    }

    @Listener
    public void onRegisterCatalogRegistry(final RegisterCatalogRegistryEvent event) {
        this.logger.info(event);
        event.register(TestType.class, ResourceKey.of(this.plugin, "test_type"), () -> Sets.newHashSet(new TestType(ResourceKey.of(this.plugin, "a"))));
    }

    @Listener
    public void onRegisterTestType(final RegisterCatalogEvent<TestType> event) {
        this.logger.info(event);
        event.register(new TestType(ResourceKey.of(this.plugin, "b")));
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> playerKey = Parameter.playerOrSource().setKey("player").build();
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(playerKey)
                        .setExecutor(context -> {
                            final ServerPlayer player = context.requireOne(playerKey);
                            this.logger.info(player.getName());
                            return CommandResult.success();
                        })
                        .build(),
                "getplayer");

        final Parameter.Value<String> playerParameterKey = Parameter.string().setKey("name").optional().build();
        event.register(
                this.plugin,
                Command.builder()
                        .parameter(playerParameterKey)
                        .setExecutor(context -> {
                            final Optional<String> result = context.getOne(playerParameterKey);
                            final Collection<GameProfile> collection;
                            if (result.isPresent()) {
                                // check to see if the string matches
                                collection = Sponge.getGame().getServer().getUserManager()
                                                .streamOfMatches(result.get().toLowerCase(Locale.ROOT))
                                                .collect(Collectors.toList());
                            } else {
                                collection = Sponge.getGame().getServer().getUserManager()
                                        .streamAll()
                                        .collect(Collectors.toList());
                            }
                            collection.forEach(x -> this.logger.info(
                                    "GameProfile - UUID: {}, Name - {}", x.getUniqueId().toString(), x.getName().orElse("---")));
                            return CommandResult.success();
                        })
                        .build(),
                "checkuser"
        );
    }

    @Listener
    public void onStartingServer(final StartingEngineEvent<Server> event) {
        this.logger.info("Starting engine '{}'", event.getEngine());
    }

    @Listener
    public void onStartingClient(final StartingEngineEvent<Client> event) {
        this.logger.info("Starting engine '{}'", event.getEngine());
    }

    @Listener
    public void onStartedServer(final StartedEngineEvent<Server> event) {
        this.logger.info("Started engine '{}'", event.getEngine());
    }

    @Listener
    public void onStartedClient(final StartedEngineEvent<Client> event) {
        this.logger.info("Started engine '{}'", event.getEngine());
    }

    @Listener
    public void onLoadedGame(final LoadedGameEvent event) {
        this.logger.info(event);
    }

    @Listener
    public void onStoppingServer(final StoppingEngineEvent<Server> event) {
        this.logger.info("Stopping engine '{}'", event.getEngine());
    }

    @Listener
    public void onStoppingClient(final StoppingEngineEvent<Client> event) {
        this.logger.info("Stopping engine '{}'", event.getEngine());
    }
}
