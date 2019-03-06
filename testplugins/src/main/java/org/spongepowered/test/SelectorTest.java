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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.selector.Argument;
import org.spongepowered.api.text.selector.ArgumentTypes;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

/*
 * The Selector test provides the command `/test-selector` and prints out
 * the number of entities detected along with their positions and distance
 * from the selector origin
 */
@Plugin(id = "selector-test", name = "Selector Test", description = "test selector", version = "0.0.0")
public class SelectorTest {

    private static final String RAW_KEY = "raw";
    private static final String ORIGIN_KEY = "origin";
    private static final String ARG_KEY = "selector";
    private static final Text NOTHING_SELECTED = Text.of(TextColors.RED, "Nothing was selected");

    // Just a command for this one, no toggling required
    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                        CommandSpec.builder()
                            .arguments(new SelectorArgument())
                            .executor((source, context) -> {
                                Collection<Entity> selectedEntities = context.getAll(ARG_KEY);
                                if (selectedEntities.isEmpty()) {
                                    source.sendMessage(NOTHING_SELECTED);
                                    return CommandResult.empty();
                                }

                                long selectedPlayers = selectedEntities.stream()
                                    .filter(x -> x instanceof Player)
                                    .count();

                                PaginationList.Builder paginationBuilder = Sponge.getServiceManager().provideUnchecked(PaginationService.class)
                                        .builder()
                                        .title(Text.of(selectedEntities.size(), " entities / ", selectedPlayers, " players"));
                                Vector3d currentPosition = context.requireOne(ORIGIN_KEY);
                                World world;

                                if (source instanceof Locatable) {
                                    world = ((Locatable) source).getWorld();
                                } else {
                                    world = Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorld().get().getUniqueId()).get();
                                }

                                List<Text> contents = new ArrayList<>();
                                for (Entity entity : selectedEntities) {
                                    String distance;
                                    if (world.equals(entity.getWorld())) {
                                        distance = String.format("%.2f", currentPosition.distance(entity.getLocation().getPosition()));
                                    } else {
                                        distance = "n/a";
                                    }

                                    boolean isPlayer = entity instanceof Player;
                                    contents.add(
                                            Text.of(
                                                isPlayer ? TextColors.YELLOW : TextColors.GRAY,
                                                entity.getWorld().getName(),
                                                " [", entity.getLocation().getBlockX(), ", ",
                                                entity.getLocation().getBlockY(), ", ",
                                                entity.getLocation().getBlockZ(), "] (d: ", distance, ") -> ",
                                                isPlayer ? ((Player) entity).getName() : entity.getType().getName()));
                                }
                                paginationBuilder.contents(contents).sendTo(source);
                                return CommandResult.success();
                            })
                            .build(),
                        "test-selector"
                );
    }

    // Selector arg
    private static class SelectorArgument extends CommandElement {

        protected SelectorArgument() {
            super(Text.of(ARG_KEY));
        }

        @Override
        public void parse(final CommandSource source, final CommandArgs args, final CommandContext context) throws ArgumentParseException {
            String arg = args.next();
            if (arg.startsWith("@")) {
                context.putArg(RAW_KEY, arg);
                Selector selector = Selector.parse(arg);

                int x = selector.getArgument(ArgumentTypes.POSITION.x()).map(Argument::getValue)
                        .orElseGet(() -> source instanceof Locatable ? ((Locatable) source).getLocation().getBlockX() : 0);
                int y = selector.getArgument(ArgumentTypes.POSITION.y()).map(Argument::getValue)
                        .orElseGet(() -> source instanceof Locatable ? ((Locatable) source).getLocation().getBlockY() : 0);
                int z = selector.getArgument(ArgumentTypes.POSITION.z()).map(Argument::getValue)
                        .orElseGet(() -> source instanceof Locatable ? ((Locatable) source).getLocation().getBlockZ() : 0);

                context.putArg(ORIGIN_KEY, new Vector3d(x, y, z));

                // order is preserved
                selector.resolve(source).forEach(entity -> context.putArg(ARG_KEY, entity));
                return;
            }

            throw args.createError(Text.of("This command must be run with a selector!"));
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            String arg = args.next();
            if (arg.startsWith("@")) {
                return Selector.parse(arg).resolve(source);
            }

            throw args.createError(Text.of("This command must be run with a selector!"));
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return ImmutableList.of();
        }
    }
}
