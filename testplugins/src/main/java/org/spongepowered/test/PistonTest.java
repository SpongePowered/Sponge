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
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "pistontest", name = "Piston Test", description = "A plugin to test cancelling pistons", version = "0.0.0")
public class PistonTest {

    boolean cancelPistons = false;

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
            .description(Text.of("Flips Pistons on and off"))
            .executor((src, args) -> {
                cancelPistons = !cancelPistons;
                src.sendMessage(cancelPistons
                                ? Text.of(TextColors.RED, "Cancelling Pistons")
                                : Text.of(TextColors.GREEN, "No longer cancelling pistons"));
                return CommandResult.success();
            })
            .build(), "flipPistons");
    }

    @Listener
    @Exclude(ChangeBlockEvent.Post.class)
    public void onChangeBlock(ChangeBlockEvent event) {
        if (!cancelPistons) {
            return;
        }
        event.getTransactions().forEach(transaction -> {
            final BlockSnapshot original = transaction.getOriginal();
            final BlockState state = original.getState();
            final BlockType type = state.getType();
            if (type == BlockTypes.PISTON || type == BlockTypes.PISTON_EXTENSION || type == BlockTypes.PISTON_HEAD || type == BlockTypes.STICKY_PISTON) {
                event.setCancelled(true);
            }
        });

    }

}
