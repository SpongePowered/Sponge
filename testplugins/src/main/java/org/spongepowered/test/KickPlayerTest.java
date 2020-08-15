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
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.player.KickPlayerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@Plugin(id = "kickplayertest", name = "Kick Player Test", version = "1.0", description = "Tests kicking a player")
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
    public void onInit(final GameInitializationEvent event) {
        Sponge.getCommandManager().register(
                this.pluginContainer,
                CommandSpec.builder()
                    .arguments(
                            GenericArguments.player(Text.of("player")),
                            GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(Text.of("message")))
                    )
                    .executor((source, context) -> {
                        final Player player = context.requireOne("player");
                        if (context.hasAny("message")) {
                            player.kick(TextSerializers.FORMATTING_CODE.deserialize(context.requireOne("message")));
                        } else {
                            player.kick();
                        }
                        return CommandResult.success();
                    })
                    .build(),
                "kickplayer"
        );
    }

    @Override
    public void enable(final CommandSource src) {
        if (!this.isEnabled) {
            Sponge.getEventManager().registerListeners(this.pluginContainer, this.kickListener);
            this.isEnabled = true;
        }
    }

    @Override
    public void disable(final CommandSource src) {
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
            event.setMessage(Text.of("[intercepted] ", event.getMessage()));
            this.logger.info("Kick intercepted.");
        }

    }

}
