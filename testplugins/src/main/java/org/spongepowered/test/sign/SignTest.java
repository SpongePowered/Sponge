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
package org.spongepowered.test.sign;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("signtest")
public class SignTest {

    private final PluginContainer pluginContainer;

    @Inject
    public SignTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Listener
    public void onSignChange(ChangeSignEvent event) {
        event.cause().first(ServerPlayer.class).ifPresent(player -> {
            player.sendMessage(Component.text("Changing Sign").append(Component.newline())
                    .append(event.isFrontSide() ? Component.text("Front") : Component.text("Back")).append(Component.newline())
                    .append(event.text().get(0)).append(Component.newline())
                    .append(event.text().get(1)).append(Component.newline())
                    .append(event.text().get(2)).append(Component.newline())
                    .append(event.text().get(3)).append(Component.newline())
            );
        });
    }


}
