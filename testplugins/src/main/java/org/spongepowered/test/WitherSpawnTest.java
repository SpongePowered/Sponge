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
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.stream.Collectors;

@Plugin(id = "witherspawntest", name = "Wither Spawn Test", description = "Log Wither Skele Spawn and generation", version = "0.0.0")
public class WitherSpawnTest implements LoadableModule {

    private final SpawnListener listener = new SpawnListener();

    @Inject private PluginContainer container;

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class SpawnListener {
        @Listener
        public void onSpawn(SpawnEntityEvent event) {
            final String entities = event.getEntities().stream()
                    .map(entity -> entity.getType().getId())
                    .collect(Collectors.joining(", "));
            Sponge.getServer().getBroadcastChannel().send(
                    Text.of(TextColors.GREEN, "Spawning: ", entities, " ", TextColors.WHITE, event.getCause())
            );
        }

        @Listener
        public void onConstruct(ConstructEntityEvent.Post event) {
            final String entity = event.getTargetType().getId();
            Sponge.getServer().getBroadcastChannel().send(
                    Text.of(TextColors.RED, "Constructing: ", TextColors.WHITE, entity)
            );
        }
    }
}
