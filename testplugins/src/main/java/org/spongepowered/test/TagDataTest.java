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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Plugin(id = "tagdatatest", name = "Tag Data Test", description = "A plugin to test scoreboard tag data.", version = "0.0.0")
public final class TagDataTest {

    @Listener
    public void onGamePreInitialization(final GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments(GenericArguments.onlyOne(GenericArguments.entityOrSource(Text.of("entity"))),
                                GenericArguments.allOf(GenericArguments.string(Text.of("tags"))))
                        .executor((src, args) -> {
                            final Optional<Entity> entity = args.getOne("entity");

                            if (!entity.isPresent()) {
                                throw new CommandException(Text.of(TextColors.RED, "You must specify an entity!"));
                            }

                            final Collection<String> tags = args.getAll("tags");

                            if (tags.isEmpty()) {
                                throw new CommandException(Text.of(TextColors.RED, "You must specify a tag to set."));
                            }

                            entity.get().offer(Keys.TAGS, new HashSet<>(tags));

                            src.sendMessage(Text.of(TextColors.GREEN, "Successfully set the tags of the targeted entity to:"));
                            tags.forEach(t -> src.sendMessage(Text.of(TextColors.DARK_GREEN, t)));

                            return CommandResult.success();
                        })
                        .build(),
                "settagstest");

        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments(GenericArguments.onlyOne(GenericArguments.entityOrSource(Text.of("entity"))))
                        .executor((src, args) -> {
                            final Optional<Entity> entity = args.getOne("entity");

                            if (!entity.isPresent()) {
                                throw new CommandException(Text.of(TextColors.RED, "You must specify an entity!"));
                            }

                            final Set<String> tags = entity.get().get(Keys.TAGS).orElse(ImmutableSet.of());

                            if (tags.isEmpty()) {
                                src.sendMessage(Text.of(TextColors.GREEN, "This entity has no tags currently."));
                            } else {
                                PaginationList.builder()
                                        .title(Text.of(TextColors.GREEN, "Tags of Targeted Entity"))
                                        .padding(Text.of(TextColors.DARK_GREEN, "="))
                                        .contents(tags.stream().map(Text::of).collect(ImmutableList.toImmutableList()))
                                        .sendTo(src);
                            }

                            return CommandResult.success();
                        })
                        .build(),
                "gettagstest");
    }

}
