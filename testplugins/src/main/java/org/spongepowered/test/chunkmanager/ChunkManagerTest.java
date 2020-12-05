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
package org.spongepowered.test.chunkmanager;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ChunkManager;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.Ticket;
import org.spongepowered.api.world.server.TicketTypes;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Optional;

@Plugin("chunkmanagertest")
public final class ChunkManagerTest implements LoadableModule {

    private final PluginContainer pluginContainer;
    private final ChunkListener listener;

    @Inject
    public ChunkManagerTest(final PluginContainer pluginContainer, final Logger logger) {
        this.pluginContainer = pluginContainer;
        this.listener = new ChunkListener(logger);
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.pluginContainer,
                Command.builder()
                       .addChild(this.registerTicketCommand(), "register")
                       .build(),
                "chunkticket");
    }

    private Command.Parameterized registerTicketCommand() {
        final Parameter.Value<ServerLocation> serverLocationParameter = Parameter.location().key("position").build();
        return Command.builder()
                .addParameter(serverLocationParameter)
                .executor(context -> {
                    final ServerLocation location = context.requireOne(serverLocationParameter);
                    final ChunkManager manager = location.world().chunkManager();
                    final Optional<Ticket<Vector3i>> optionalTicket = manager
                            .requestTicket(TicketTypes.STANDARD, location.chunkPosition(), location.chunkPosition(), 5);
                    if (optionalTicket.isPresent()) {
                        final Ticket<Vector3i> ticket = optionalTicket.get();
                        context.sendMessage(Identity.nil(), LinearComponents.linear(
                                Component.text("Ticket registered. Lifetime - "),
                                Component.text(manager.timeLeft(ticket).ticks())));

                        context.sendMessage(Identity.nil(), LinearComponents.linear(
                                Component.text("Ticket validity check: "),
                                Component.text(manager.valid(ticket))
                        ));

                        // now find the ticket
                        if (manager.findTickets(TicketTypes.STANDARD).contains(ticket)) {
                            context.sendMessage(Identity.nil(),
                                    Component.text().content("Ticket was found in the chunk manager").color(NamedTextColor.GREEN).build());
                        } else {
                            context.sendMessage(Identity.nil(),
                                    Component.text().content("Ticket was not found in the chunk manager").color(NamedTextColor.RED).build());
                        }

                        Sponge.server().scheduler().submit(Task.builder()
                                .plugin(this.pluginContainer)
                                .delay(Ticks.of(2))
                                .execute(() -> context.sendMessage(Identity.nil(), LinearComponents.linear(
                                        Component.text("Ticket validity check (after 2 ticks): "),
                                        Component.text(manager.valid(ticket))
                                ))).build());
                    } else {
                        context.sendMessage(Identity.nil(), Component.text("Ticket was not registered."));
                    }
                    return CommandResult.success();
                })
                .build();
    }

    @Override
    public void disable(final CommandContext ctx) {
        Sponge.eventManager().unregisterListeners(this.listener);
        ctx.sendMessage(Identity.nil(), Component.text("Disabled ChunkManagerTest listener"));
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.pluginContainer, this.listener);
        ctx.sendMessage(Identity.nil(), Component.text("Enabled ChunkManagerTest listener"));
    }

    public static class ChunkListener {

        private final Logger logger;

        public ChunkListener(final Logger logger) {
            this.logger = logger;
        }

        @Listener
        public void onChunkLoad(final ChunkEvent.Load event) {
            this.logger.info("Chunk load: {}, {}", event.chunkWorld(), event.chunkPosition());
        }

    }
}
