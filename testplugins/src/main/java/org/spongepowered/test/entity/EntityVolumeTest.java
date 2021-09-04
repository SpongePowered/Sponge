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
package org.spongepowered.test.entity;

import com.google.inject.Inject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Collection;

@Plugin("entityvolumetest")
public class EntityVolumeTest {

    private final PluginContainer plugin;
    private final Logger logger;

    @Inject
    public EntityVolumeTest(final PluginContainer plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        Command.Parameterized command = Command.builder()
                .executor((ctx) -> {
                    final Object root = ctx.cause().root();
                    if (!(root instanceof Locatable)) {
                        throw new CommandException(Component.text("You must be locatable to use this command!"));
                    }
                    final Audience audience = ctx.cause().audience();

                    final ServerLocation serverLocation = ((Locatable) root).serverLocation();
                    final WorldChunk chunk = serverLocation.world().chunk(serverLocation.chunkPosition());

                    final Collection<? extends Entity> chunkEntities = chunk.entities();
                    final Collection<? extends Entity> worldEntities = serverLocation.world().entities();

                    final boolean worldContainsChunkEntities = serverLocation.world().entities().containsAll(chunkEntities);
                    audience.sendMessage(testResult("World contains chunk entities test", worldContainsChunkEntities));

                    final boolean worldContainsMoreEntitiesThanChunk = worldEntities.size() > chunkEntities.size();
                    audience.sendMessage(testResult("World contains more entities than chunk test", worldContainsMoreEntitiesThanChunk)
                            .append(Component.text(" (World " + worldEntities.size() + " vs Chunk " + chunkEntities.size() + ")")));

                    final boolean chunkEntitiesIsSameAsAABB = chunk.entities(AABB.of(chunk.min(), chunk.max())).equals(chunkEntities);
                    audience.sendMessage(testResult(".entities is the same as AABB of chunk", chunkEntitiesIsSameAsAABB));

                    audience.sendMessage(Component.text("See console for a list of all entities."));

                    this.logger.info(chunkEntities.size() + " entities in chunk " + chunk.chunkPosition() + ":\n" + chunkEntities);
                    this.logger.info("---------");
                    this.logger.info(worldEntities.size() + " entities in world " + serverLocation.world().properties().key() + ":\n" + worldEntities);

                    return CommandResult.success();
                })
                .build();
        event.register(this.plugin, command, "checkentitymethods");
    }

    private static Component testResult(final String message, final boolean success) {
        final Component result =  success ? Component.text("Succeeded", NamedTextColor.GREEN) : Component.text("Failed", NamedTextColor.RED);
        return Component.text(message).append(Component.text(": ")).append(result);
    }
}
