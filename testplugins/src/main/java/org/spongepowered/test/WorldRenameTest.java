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

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.WorldArchetypes;
import org.spongepowered.api.world.server.WorldRegistration;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

// Allows for testing the rename of a world.
// Includes commands to load, unload, rename and create worlds.
@Plugin(id = "world-rename-test", name = "World Rename Test", description = "Tests renaming of worlds", version = "0.0.0")
public class WorldRenameTest {

    @Inject private PluginContainer container;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Parameter.Value<WorldProperties> paramWorld = Parameter.worldProperties().setKey("world").build();
        Parameter.Value<String> paramName = Parameter.string().setKey("new name").build();

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                    .parameters(paramWorld)
                    .setExecutor((context) -> {
                        Sponge.getServer().getWorldManager().unloadWorld(Sponge.getServer().getWorldManager().getWorld(context.getOne(paramWorld).get().getUniqueId())
                                .orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "That world doesn't seem to be loaded."))));
                        return CommandResult.success();
                    }).build(), "unloadworld");

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .parameters(new WorldParameter())
                        .setExecutor((context) -> {
                            Sponge.getServer().getWorldManager().unloadWorld(Sponge.getServer().getWorldManager().getWorld(context.getOne(paramWorld).get().getUniqueId())
                                    .orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "That world doesn't seem to be loaded."))));
                            return CommandResult.success();
                        }).build(), "loadworld");

        Sponge.getCommandManager().register(this,
                Command.builder()
                        .parameters(new WorldParameter(), paramName)
                        .setExecutor((context) -> {
                            // Name clashes should be handled by the impl.
                            WorldProperties newProperties = Sponge.getServer().getWorldManager()
                                    .renameWorld(context.getOne(paramWorld).get().getName(), context.getOne(paramName).get())
                                    .orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "The world was not renamed.")));

                            context.getMessageReceiver().sendMessage(Text.of("The world was renamed to " + newProperties.getName()));
                            return CommandResult.success();
                        }).build(), "renameworld");

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .parameters(paramName)
                        .setExecutor((context) -> {
                            try {
                                WorldRegistration registration = WorldRegistration.builder().directoryName(context.getOne(paramName).get()).build();
                                Sponge.getServer().getWorldManager().createProperties(registration, WorldArchetypes.OVERWORLD.get());
                            } catch (IOException e) {
                                throw new CommandException(Text.of(TextColors.RED, "Could not create the world."), e);
                            }

                            context.getMessageReceiver().sendMessage(Text.of("The world was created"));
                            return CommandResult.success();
                        }).build(), "createworld");

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .parameters(new WorldParameter(x -> true))
                        .setExecutor((context) -> {
                            WorldProperties wp = context.<WorldProperties>getOne(paramWorld).get();
                            context.getMessageReceiver().sendMessage(Text.of("World: ", wp.getName(), " - UUID: ", wp.getUniqueId(), " - Dim ID:",
                                    wp.getAdditionalProperties().getInt(DataQuery.of("SpongeData", "dimensionId")).map(Object::toString)
                                            .orElse("unknown")));
                            return CommandResult.success();
                        }).build(), "worldinfo");

    }

    @NonnullByDefault
    static class WorldParameter extends CommandElement {

        private final Predicate<WorldProperties> worldPropertiesPredicate;

        WorldParameter() {
            this(wp -> !Sponge.getServer().getWorldManager().getWorld(wp.getUniqueId()).isPresent());
        }

        WorldParameter(Predicate<WorldProperties> worldPropertiesPredicate) {
            super(WorldRenameTest.worldKey);
            this.worldPropertiesPredicate = worldPropertiesPredicate;
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            WorldProperties worldProperties = Sponge.getServer().getWorldProperties(args.next())
                    .orElseThrow(() -> args.createError(Text.of("Cannot find the world")));
            if (this.worldPropertiesPredicate.test(worldProperties)) {
                throw args.createError(Text.of("The world is currently loaded."));
            }

            return worldProperties;
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            Stream<String> tabComplete = Sponge.getServer().getAllWorldProperties().stream()
                    .filter(this.worldPropertiesPredicate)
                    .map(x -> x.getWorldName().toLowerCase());

            try {
                final String input = args.peek().toLowerCase();
                return tabComplete.filter(x -> x.startsWith(input)).collect(Collectors.toList());
            } catch (ArgumentParseException ignored) {
                return tabComplete.collect(Collectors.toList());
            }
        }
    }
}
