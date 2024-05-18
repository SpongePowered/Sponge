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
import org.checkerframework.checker.nullness.qual.Nullable;
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
import org.spongepowered.api.world.server.TicketType;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Plugin("chunkmanagertest")
public final class ChunkManagerTest implements LoadableModule {

    private final PluginContainer pluginContainer;
    private final ChunkListener listener;

    private Map<Vector3i, Set<Ticket<Vector3i>>> ticketsMap = new HashMap<>();

    @Inject
    public ChunkManagerTest(final PluginContainer pluginContainer, final Logger logger) {
        this.pluginContainer = pluginContainer;
        this.listener = new ChunkListener(logger);
    }

    @Listener
    private void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.pluginContainer,
                Command.builder()
                        .addChild(this.registerTicketCommand(), "register")
                        .addChild(this.unregisterTicketCommand(), "unregister")
                        .build(),
                "chunkticket");
    }

    private Command.Parameterized registerTicketCommand() {
        final Parameter.Value<ServerLocation> serverLocationParameter = Parameter.location().key("position").build();
        final Parameter.Value<Integer> timeParameter = Parameter.integerNumber().key("time").optional().build();

        return Command.builder()
                .addParameter(serverLocationParameter)
                .addParameter(timeParameter)
                .executor(context -> {
                    final ServerLocation location = context.requireOne(serverLocationParameter);
                    final Optional<Integer> time = context.one(timeParameter);

                    final TicketType<Vector3i> ticketType = TicketType.<Vector3i>builder()
                            .name("chunkManagerTest")
                            .comparator(Vector3i::compareTo)
                            .lifetime(time.map(Ticks::of).orElse(Ticks.infinite()))
                            .build();

                    final ChunkManager manager = location.world().chunkManager();
                    final Optional<Ticket<Vector3i>> optionalTicket = manager
                            .requestTicket(ticketType, location.chunkPosition(), location.chunkPosition(), 5);
                    if (optionalTicket.isPresent()) {
                        final Ticket<Vector3i> ticket = optionalTicket.get();
                        final Set<Ticket<Vector3i>> tickets = this.ticketsMap.computeIfAbsent(location.chunkPosition(), k -> new HashSet<>());
                        tickets.add(ticket);
                        context.sendMessage(Identity.nil(), LinearComponents.linear(
                                Component.text("Ticket registered. Lifetime - "),
                                Component.text(manager.timeLeft(ticket).ticks())));

                        context.sendMessage(Identity.nil(), LinearComponents.linear(
                                Component.text("Ticket validity check: "),
                                Component.text(manager.valid(ticket))
                        ));

                        // now find the ticket
                        if (manager.findTickets(ticketType).contains(ticket)) {
                            context.sendMessage(Identity.nil(),
                                    Component.text().content("Ticket was found in the chunk manager").color(NamedTextColor.GREEN).build());
                        } else {
                            context.sendMessage(Identity.nil(),
                                    Component.text().content("Ticket was not found in the chunk manager").color(NamedTextColor.RED).build());
                        }

                        time.ifPresentOrElse(t -> {
                            Sponge.server().scheduler().submit(Task.builder()
                                    .plugin(this.pluginContainer)
                                    .delay(Ticks.of(t + 1))
                                    .execute(() -> context.sendMessage(Identity.nil(), LinearComponents.linear(
                                            Component.text("Ticket validity check (1 tick before expiration): "),
                                            Component.text(manager.valid(ticket))
                                    ))).build());

                            Sponge.server().scheduler().submit(Task.builder()
                                    .plugin(this.pluginContainer)
                                    .delay(Ticks.of(t + 2))
                                    .execute(() -> {
                                        tickets.remove(ticket);
                                        if (tickets.isEmpty()) {
                                            this.ticketsMap.remove(location.chunkPosition(), tickets);
                                        }
                                        context.sendMessage(Identity.nil(), LinearComponents.linear(
                                            Component.text("Ticket validity check (After expiration): "),
                                            Component.text(manager.valid(ticket))));
                                    }).build());
                        }, () -> Sponge.server().scheduler().submit(Task.builder()
                                .plugin(this.pluginContainer)
                                .interval(Ticks.of(20))
                                .execute(t -> {
                                    context.sendMessage(Identity.nil(), LinearComponents.linear(
                                        Component.text("Ticket validity check (Repeating 20 ticks): "),
                                        Component.text(manager.valid(ticket))));
                                    if (!manager.valid(ticket)) {
                                        t.cancel();
                                    }
                                }).build()));
                    } else {
                        context.sendMessage(Identity.nil(), Component.text("Ticket was not registered."));
                    }
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized unregisterTicketCommand() {
        final Parameter.Value<ServerLocation> serverLocationParameter = Parameter.location().key("position").build();

        return Command.builder()
                .addParameter(serverLocationParameter)
                .executor(context -> {
                    final ServerLocation location = context.requireOne(serverLocationParameter);
                    final @Nullable Set<Ticket<Vector3i>> tickets = this.ticketsMap.remove(location.chunkPosition());
                    if (tickets != null) {
                        final ChunkManager manager = location.world().chunkManager();
                        tickets.forEach(manager::releaseTicket);
                        context.sendMessage(Identity.nil(), Component.text("Removed tickets.", NamedTextColor.GREEN));
                    } else {
                        context.sendMessage(Identity.nil(), Component.text("No valid tickets.", NamedTextColor.RED));
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

    static class ChunkListener {

        private final Logger logger;

        public ChunkListener(final Logger logger) {
            this.logger = logger;
        }

        @Listener
        private void onChunkLoad(final ChunkEvent.Load event) {
            this.logger.info("Chunk load: {}, {}", event.worldKey(), event.chunkPosition());
        }

    }
}
