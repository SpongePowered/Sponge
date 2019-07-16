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
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Plugin(id = "invaliduuidtest", name="Invalid UUID Test", version = "0.0.0",
        description = "Ensures profile lookups don't crash when Mojang returns an invalid UUID.")
public class InvalidUuidTest {

    @Listener
    public void onInit(GameInitializationEvent e) {
        Sponge.getCommandManager().register(this, CommandSpec.builder().executor((src, args) -> {
            // This must NOT throw an exception. If no exception is thrown, the
            // test passes.
            Optional<User> user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get("_");
            if (user.isPresent()) {
                src.sendMessage(Text.of(TextColors.GOLD, "WARN: The username used in InvalidUuidTest is now assigned to a valid user."));
                src.sendMessage(Text.of(TextColors.GOLD, "This should be assigned to a user with an invalid UUID."));
                try {
                    src.sendMessage(Text.of(TextColors.GOLD, "Replace the username with one listed in ",
                            Text.of(TextActions.openUrl(new URL("https://bugs.mojang.com/browse/WEB-1290")),
                                    TextColors.AQUA, TextStyles.UNDERLINE, "WEB-1290"),
                            "."));
                } catch (MalformedURLException ex) {
                    throw new AssertionError(ex);
                }
            } else {
                src.sendMessage(Text.of(TextColors.GREEN, "Success"));
            }
            return CommandResult.success();
        }).build(), "invaliduuidtest");
    }
}
