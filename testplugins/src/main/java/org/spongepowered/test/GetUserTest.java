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
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

/*
 * Tests getting a user from a string, ensuring that an empty optional one time is an empty optional all the time.
 */
@Plugin(id = "get_user", name = "Get User Test", description = "Test getting a user from a string", version = "0.0.0")
public class GetUserTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager()
                .register(this, CommandSpec.builder()
                        .arguments(GenericArguments.string(Text.of("name")))
                        .executor((source, context) -> {
                            Optional<User> user =
                                    Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(context.<String>requireOne("name"));
                            if (user.isPresent()) {
                                source.sendMessage(Text.of("The user exists"));
                                source.sendMessage(Text.of(user.get()));
                            } else {
                                source.sendMessage(Text.of("The user does not exist"));
                            }

                            return CommandResult.success();
                        }).build(),
                        "getuser"
                );

        Sponge.getCommandManager()
                .register(this, CommandSpec.builder()
                                .arguments(GenericArguments.string(Text.of("name")))
                                .executor((source, context) -> {
                                    GameProfile profile;
                                    try {
                                         profile = Sponge.getServer().getGameProfileManager().get(context.<String>requireOne("name")).get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        source.sendMessage(Text.of("The game profile probably doesn't exist"));
                                        return CommandResult.empty();
                                    }

                                    User user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).getOrCreate(profile);
                                    source.sendMessage(Text.of("The user exists"));
                                    source.sendMessage(Text.of(user));
                                    return CommandResult.success();
                                }).build(),
                        "getorcreateuser"
                );
    }
}
