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
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "change_block_test", name = "ChangeBlock Listener Test", description = "log Change Block event", version = "0.0.0")
public class ChangeBlockTest implements LoadableModule {

    private final ChangeBlockListener listener = new ChangeBlockListener();

    @Inject private PluginContainer container;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .executor(((src, args) -> {
                    if (!(src instanceof Player)) {
                        throw new CommandException(Text.of(TextColors.RED, "Must be a player to use this command!"));
                    }
                    Sponge.getEventManager().registerListeners(this.container, this.listener);
                    ((Player) src).getLocation().sub(0, 1, 0).setBlock(BlockTypes.AIR.getDefaultState());
                    Sponge.getEventManager().unregisterListeners(this.listener);
                    return CommandResult.success();
                })).build(), "changeblocktest");
    }

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class ChangeBlockListener {
        @Listener
        @Exclude(ChangeBlockEvent.Post.class)
        public void onBlockChange(ChangeBlockEvent event, @First Player player) {
            // Technically this should print twice, once for the Break and once for the Post.
            System.err.println(event);
        }
    }

}
