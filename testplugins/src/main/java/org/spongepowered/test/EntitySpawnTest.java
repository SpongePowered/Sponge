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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Plugin(id = "entityspawntest", name = "EntitySpawnTest", version = "0.1.0", description = "Tests entity spawning.")
public final class EntitySpawnTest {

    @Listener
    public void onInitialization(final GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                Command.builder()
                        .parameter(Parameter.catalogedElement(EntityType.class).setKey("type").allOf().build())
                        .targetedExecutor((cause, player, args) -> {
                            final List<EntityType> types = new ArrayList<>(args.getAll("type"));

                            final int size = types.size();

                            if (size == 0) {
                                throw new CommandException(Text.of(TextColors.RED, "You must specify at least one entity type to spawn any."));
                            }

                            final Location location = player.getLocation();

                            if (size == 1) {
                                boolean failed = false;
                                final EntityType type = types.get(0);
                                final Entity entity = location.createEntity(type);
                                try {
                                    if (location.getExtent().spawnEntity(entity)) {
                                        player.sendMessage(Text.of(TextColors.GOLD, "You have successfully spawned a ",
                                                TextColors.DARK_GREEN, entity.getTranslation()));
                                    } else {
                                        failed = true;
                                    }
                                } catch (Exception e) {
                                    failed = true;
                                }
                                if (failed) {
                                    throw new CommandException(Text.of(TextColors.RED, "You have failed to spawn a " + type.getId()));
                                }
                            } else {
                                player.sendMessage(Text.of(TextColors.GOLD, "You have spawned the following entities:"));
                                location.getExtent()
                                        .spawnEntities(types.stream().map(location::createEntity).collect(Collectors.toList()))
                                        .forEach(e -> player.sendMessage(Text.of(TextColors.DARK_GREEN, e.getTranslation())));
                            }

                            return CommandResult.success();
                        }, Player.class)
                        .build(),
                "spawnspongeentity");
    }

}
