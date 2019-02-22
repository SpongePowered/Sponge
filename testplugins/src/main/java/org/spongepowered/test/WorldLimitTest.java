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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "world-limit-test", name = "World Limit Test", description = "Tests on the edge of the world", version = "0.0.0")
public class WorldLimitTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .executor((src, args) -> {
                            if (src instanceof Player) {
                                Vector3d coord = args.<Vector3d>getOne("coord").get();

                                ((Player) src).getWorld().setBlock(coord.toInt(), BlockTypes.GLOWSTONE.getDefaultState());
                            }
                            return CommandResult.success();
                        })
                        .arguments(GenericArguments.vector3d(Text.of("coord")))
                        .build(),
                "setglowstone"
        );

        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!(src instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            Vector3d coord = args.<Vector3d>getOne("coord").get();

                            BlockState block = ((Player) src).getWorld().getBlock(coord.getFloorX(), coord.getFloorY(), coord.getFloorZ());

                            src.sendMessage(Text.of("That's a " + block));
                            return CommandResult.success();
                        })
                        .arguments(GenericArguments.vector3d(Text.of("coord")))
                        .build(),
                "getblock"
        );

        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!(src instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            final Vector3d position = args.<Vector3d>getOne("coord").get();
                            ((Player) src).setLocation(((Player) src).getLocation().setPosition(position));
                            return CommandResult.success();
                        })
                        .arguments(GenericArguments.vector3d(Text.of("coord")))
                        .build(),
                "setlocation"
        );

        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!(src instanceof Player)) {
                                throw new CommandException(Text.of(TextColors.RED, "You must be a player to use this command!"));
                            }
                            ((Player) src).transferToWorld(((Player) src).getWorld(), Vector3d.from(Double.MAX_VALUE, ((Player) src).getPosition().getY(), Double.MAX_VALUE));

                            return CommandResult.success();
                        })
                        .build(),
                "tpmaxdouble"
        );

    }

}
