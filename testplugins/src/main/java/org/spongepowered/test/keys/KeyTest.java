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
package org.spongepowered.test.keys;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("key_test")
public class KeyTest implements LoadableModule {

    private final PluginContainer container;

    @Inject
    public KeyTest(final PluginContainer container) {
        this.container = container;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.container, new HealthListener());
    }

    @Override
    public void disable(final CommandContext ctx) {
        Sponge.eventManager().unregisterListeners(this.container);
    }

    public static class HealthListener {

        @Listener
        public void onHealthChange(
            final ChangeDataHolderEvent.ValueChange event, @Getter("targetHolder") final Player player
        ) {
            event.originalChanges().successfulValue(Keys.HEALTH)
                .ifPresent(newVal ->
                    player.sendMessage(Identity.nil(), Component.text()
                        .append(Component.text("[", NamedTextColor.GOLD))
                        .append(Component.text(newVal.get(), NamedTextColor.RED))
                        .append(Component.text("/", NamedTextColor.DARK_GREEN))
                        .append(Component.text(20, NamedTextColor.GREEN))
                        .append(Component.text("]", NamedTextColor.GOLD))
                        .append(Component.text(" Health changed cause", NamedTextColor.BLUE)
                            .hoverEvent(Component.text(event.cause().all().toString())))
                        .build()
                    ));
        }
    }

}
