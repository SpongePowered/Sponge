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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;

public class BehindCommandTestPlugin {

    @Inject
    private PluginContainer container;

    protected boolean enabled;

    protected CommandSpec.Builder baseCommand;

    @Listener(order = Order.FIRST)
    public void onPreInit(final GamePreInitializationEvent event) {
        this.baseCommand = CommandSpec.builder().child(CommandSpec.builder()
                        .arguments()
                        .executor((src, args) -> {
                            this.enabled = !this.enabled;

                            if (this.enabled) {
                                Sponge.getEventManager().registerListeners(this, this);
                                src.sendMessage(Text.of(TextColors.DARK_GREEN, "You have enabled event testing for " + container.getName() + "."));
                            } else {
                                Sponge.getEventManager().unregisterListeners(this);
                                src.sendMessage(Text.of(TextColors.DARK_GREEN, "You have disabled event testing for " + container.getName() + "."));
                            }

                            return CommandResult.success();
                        })
                        .build(),
                "enable");
    }

    @Listener(order = Order.LAST)
    public void onInit(final GameStartedServerEvent event) {
        Sponge.getEventManager().unregisterListeners(this);

        Sponge.getCommandManager().register(this, this.baseCommand.build(), this.container.getId());
    }

}
