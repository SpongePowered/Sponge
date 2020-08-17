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
package org.spongepowered.changeblock;

import com.google.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("changeblocktest")
public class ChangeBlockTest implements LoadableModule {

    private static final Marker marker = MarkerManager.getMarker("CHANGEBLOCK");

    @Inject Logger pluginLogger;
    @Inject PluginContainer pluginContainer;

    private boolean cancelAll = false;

    @Override
    public void enable(CommandContext ctx) {
        Sponge.getEventManager().registerListeners(this.pluginContainer, new ChangeBlockListener());
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.pluginContainer, Command.builder()
            .setExecutor(context -> {
                this.cancelAll = !this.cancelAll;
                final TextComponent newState = TextComponent.of(this.cancelAll ? "ON" : "OFF", this.cancelAll ? NamedTextColor.GREEN : NamedTextColor.RED);
                context.sendMessage(TextComponent.of("Turning Block Changes: ").append(newState));
                return CommandResult.success();
            })
            .build(),
            "toggleBlockChanges"
        );
    }


    private class ChangeBlockListener {
        @Listener
        public void onChangeBlock(final ChangeBlockEvent.Post post) {
            final Logger pluginLogger = ChangeBlockTest.this.pluginLogger;
            pluginLogger.log(Level.FATAL, ChangeBlockTest.marker, "/*************");
            pluginLogger.log(Level.FATAL, ChangeBlockTest.marker, "/* ChangeBlockEvent");
            pluginLogger.log(Level.FATAL, ChangeBlockTest.marker, "/");
            pluginLogger.log(Level.FATAL, ChangeBlockTest.marker, "/ Cause:");
            for (final Object o : post.getCause()) {
                pluginLogger.log(Level.FATAL, ChangeBlockTest.marker, "/ - " + o);
            }
            pluginLogger.log(Level.FATAL, ChangeBlockTest.marker, "/");
            if (ChangeBlockTest.this.cancelAll && post.getCause().containsType(BlockSnapshot.class)) {
                post.setCancelled(true);
            }
        }

    }
}
