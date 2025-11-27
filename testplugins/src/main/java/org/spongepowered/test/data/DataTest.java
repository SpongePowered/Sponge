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
package org.spongepowered.test.data;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Optional;

@Plugin("datatest")
public final class DataTest  {
    private final PluginContainer plugin;

    @Inject
    public DataTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    private String mimic;

    @Listener
    private void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<String> mimicParameter = Parameter.string().key("mimic_username").optional().build();
        event.register(this.plugin, Command
                        .builder()
                        .addParameter(mimicParameter)
                        .executor(context -> {
                            final ServerPlayer player = context.cause().first(ServerPlayer.class).get();
                            final String mimicUsername = context.requireOne(mimicParameter);
                            this.setPlayerSkin(player, mimicUsername);
                            return CommandResult.success();
                        })
                        .build()
                , "mimic"
        );
        event.register(this.plugin, Command
                        .builder()
                        .addParameter(mimicParameter)
                        .executor(context -> {
                            final ServerPlayer player = context.cause().first(ServerPlayer.class).get();
                            this.mimic = context.requireOne(mimicParameter);
                            player.kick();
                            return CommandResult.success();
                        })
                        .build()
                , "mimickick"
        );
    }

    @Listener
    public void onLogin(ServerSideConnectionEvent.Login event) {
        if (this.mimic != null) {
            this.setPlayerSkin(event.user(), this.mimic);
            this.mimic = null;
        }
    }

    private void setPlayerSkin(final DataHolder.Mutable player, final String mimicUsername) {
        final GameProfile profile = Sponge.server().gameProfileManager().profile(mimicUsername).join();
        final Optional<ProfileProperty> skinProperty =
                profile.properties().stream().filter(prop -> prop.name().equals(ProfileProperty.TEXTURES)).findFirst();
        player.offer(Keys.SKIN_PROFILE_PROPERTY, skinProperty.get());
    }
}
