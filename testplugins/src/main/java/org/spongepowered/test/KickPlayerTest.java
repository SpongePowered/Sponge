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
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("kickplayertest")
public final class KickPlayerTest implements LoadableModule {

    private final PluginContainer pluginContainer;
    private final KickListener kickListener;
    private boolean isEnabled = false;

    @Inject
    public KickPlayerTest(final PluginContainer pluginContainer, final Logger logger) {
        this.pluginContainer = pluginContainer;
        this.kickListener =  new KickListener(logger);
    }

    @Listener
    public void onInit(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> playerParameter = Parameter.playerOrTarget().setKey("player").build();
        final Parameter.Value<Component> messageParameter = Parameter.jsonText().setKey("message").optional().build();
        event.register(this.pluginContainer, Command
            .builder()
            .parameter(playerParameter)
            .parameter(messageParameter)
            .setExecutor(context -> {
                final ServerPlayer player = context.requireOne(playerParameter);

                return CommandResult.builder().setResult(
                    context.getOne(messageParameter)
                        .map(player::kick)
                        .orElseGet(player::kick) ? 1 : 0)
                    .build();
            })
            .build(), "kickplayer", "kick"
        );
    }

    @Override
    public void enable(final CommandContext ctx) {
        if (!this.isEnabled) {
            Sponge.getEventManager().registerListeners(this.pluginContainer, this.kickListener);
            this.isEnabled = true;
        }
    }

    @Override
    public void disable(final CommandContext ctx) {
        if (this.isEnabled) {
            Sponge.getEventManager().unregisterListeners(this.kickListener);
            this.isEnabled = false;
        }
    }

    public static final class KickListener {

        private final Logger logger;

        KickListener(final Logger logger) {
            this.logger = logger;
        }

        @Listener
        public void onKick(final KickPlayerEvent event) {
            event.setMessage(Component.text("[intercepted] " + event.getMessage()));
            this.logger.info("Kick intercepted.");
        }

    }

}
