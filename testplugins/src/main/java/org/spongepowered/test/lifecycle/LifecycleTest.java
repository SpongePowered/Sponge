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
package org.spongepowered.test.lifecycle;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.event.GenericEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.LifecycleEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("lifecycletest")
public class LifecycleTest implements LoadableModule {

    private static final boolean LOG_LIFECYCLE = Boolean.getBoolean("sponge.logLifecycle");

    private final Game game;
    private final PluginContainer container;
    private final Logger logger;
    private boolean registered;

    @Inject
    LifecycleTest(final Game game, final PluginContainer container, final Logger logger) {
        this.game = game;
        this.container = container;
        this.logger = logger;

        if (LifecycleTest.LOG_LIFECYCLE) {
            game.eventManager().registerListeners(this.container, new Listeners());
            this.registered = true;
        }
    }

    @Override
    public void enable(final CommandContext ctx) {
        if (!this.registered) {
            this.game.eventManager().registerListeners(this.container, new Listeners());
            this.registered = true;
            ctx.sendMessage(Identity.nil(), Component.text("Successfully registered lifecycle listener"));
        } else {
            ctx.sendMessage(Identity.nil(), Component.text("Listener already registered", NamedTextColor.RED));
        }
    }

    @Override
    public void disable(final CommandContext ctx) {
        if (this.registered) {
            this.game.eventManager().unregisterListeners(this.container);
            this.registered = false;
            ctx.sendMessage(Identity.nil(), Component.text("Successfully unregistered lifecycle listener"));
        } else {
            ctx.sendMessage(Identity.nil(), Component.text("Listener not registered", NamedTextColor.RED));
        }
    }

    public class Listeners {
        @Listener
        public void onLifecycle(final LifecycleEvent event) {
            LifecycleTest.this.logger.info(":: Lifecycle {}", () -> {
                final StringBuilder eventName = new StringBuilder(event.getClass().getSimpleName());
                Class<?> enclosing = event.getClass().getEnclosingClass();
                while (enclosing != null) {
                    eventName.insert(0, '$');
                    eventName.insert(0, enclosing.getSimpleName());
                    enclosing = enclosing.getEnclosingClass();
                }
                if (event instanceof GenericEvent) {
                    eventName.append('<')
                        .append(((GenericEvent<?>) event).paramType().getType().getTypeName())
                        .append('>');
                }
                return eventName;
            });
        }
    }

}
