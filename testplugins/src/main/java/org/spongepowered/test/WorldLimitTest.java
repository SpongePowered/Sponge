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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.math.vector.Vector3d;

@Plugin(id = "world-limit-test", name = "World Limit Test", description = "Tests on the edge of the world", version = "0.0.0")
public class WorldLimitTest {

    @Inject private PluginContainer container;
    @Listener
    public void onInit(GameInitializationEvent event) {
        Parameter.Value<Vector3d> paramCoord = Parameter.vector3d().setKey("coord").build();
        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .setExecutor((ctx) -> {
                            if (ctx.getSubject() instanceof Player) {
                                Vector3d coord = ctx.getOne(paramCoord).get();

                                ((Player) ctx.getSubject()).getWorld().setBlock(coord.toInt(), BlockTypes.GLOWSTONE.get().getDefaultState());
                            }
                            return CommandResult.success();
                        })
                        .parameters(paramCoord)
                        .build(),
                "setglowstone"
        );

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .setExecutor((ctx) -> {
                            if (!(ctx.getSubject() instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            Vector3d coord = ctx.<Vector3d>getOne(paramCoord).get();

                            BlockState block = ((Player) ctx.getSubject()).getWorld().getBlock(coord.getFloorX(), coord.getFloorY(), coord.getFloorZ());

                            ctx.getMessageReceiver().sendMessage(Text.of("That's a " + block));
                            return CommandResult.success();
                        })
                        .parameters(paramCoord)
                        .build(),
                "getblock"
        );

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .setExecutor((ctx) -> {
                            if (!(ctx.getSubject() instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            final Vector3d position = ctx.<Vector3d>getOne(paramCoord).get();
                            Player player = (Player) ctx.getSubject();
                            player.setLocation(Location.of(player.getWorld(), position));
                            return CommandResult.success();
                        })
                        .parameters(paramCoord)
                        .build(),
                "setlocation"
        );

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .setExecutor((ctx) -> {
                            if (!(ctx.getSubject() instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            Player player = (Player) ctx.getSubject();
                            player.transferToWorld(player.getWorld(), Vector3d.from(Double.MAX_VALUE, player.getPosition().getY(), Double.MAX_VALUE));

                            return CommandResult.success();
                        })
                        .build(),
                "tpmaxdouble"
        );

    }

}
