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
package org.spongepowered.test.humantest;

import com.google.inject.Inject;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

@Plugin("humantest")
public final class HumanTest {

    private final PluginContainer plugin;

    @Inject
    public HumanTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<ServerPlayer> playerParameter = Parameter.playerOrSource().setKey("player").build();
        final Parameter.Value<String> nameParameter = Parameter.string().setKey("name").build();
        final Parameter.Value<String> mimicParameter = Parameter.string().setKey("mimic_username").build();

        event.register(this.plugin, Command
                    .builder()
                    .parameter(playerParameter)
                    .parameter(nameParameter)
                    .parameter(mimicParameter)
                    .setPermission(this.plugin.getMetadata().getId() + ".command.human.create")
                    .setExecutor(context -> {
                        final ServerPlayer player = context.requireOne(playerParameter);
                        final String name = context.requireOne(nameParameter);
                        final String mimicUsername = context.getOne(mimicParameter).orElse(name);
                        final Human human = player.getWorld().createEntity(EntityTypes.HUMAN.get(), player.getPosition());
                        human.offer(Keys.DISPLAY_NAME, TextComponent.of(name));
                        human.useSkinFor(mimicUsername);
                        final boolean result = player.getWorld().spawnEntity(human);
                        return result ? CommandResult.success() : CommandResult.error(TextComponent.of("Failed to spawn the human!"));
                    })
                    .build()
            , "ch", "createhuman"
        );
    }
}
