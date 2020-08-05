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

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.reference.ValueReference;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;

import javax.inject.Inject;

@Plugin(id = "text-test", name = "Text Test", description = "Tests text related functions", version = "0.0.0")
public class TextTestPlugin {

    @Inject
    private @DefaultConfig(sharedRoot = true) ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private ValueReference<TextTestConfig> config;

    @Listener
    public void onInit(GameInitializationEvent event) throws IOException, ObjectMappingException {
        this.config = this.configLoader.loadToReference().referenceTo(TextTestConfig.class);
        this.config.setAndSave(this.config.get());

        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments(GenericArguments.text(Text.of("test"), TextSerializers.FORMATTING_CODE, true))
                        .executor((src, args) -> {
                            src.sendMessage(args.<Text>requireOne("test"));
                            return CommandResult.success();
                        }).build(),
                "test-text-ampersand");

        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .executor((src, args) -> {
                            Text message = Text.of(
                                    TextColors.YELLOW, "This is ", TextColors.GOLD, TextStyles.BOLD, "BOLD GOLD"
                            );
                            src.sendMessage(message);
                            return CommandResult.success();
                        }).build(),
                "test-text-message");
    }

    @Listener
    public void playerConnect(ClientConnectionEvent.Join event) {
        final Text message = this.config.get().joinMessage;
        if (message != null) {
            event.getTargetEntity().sendMessage(message);
        }
    }

    @ConfigSerializable
    public static class TextTestConfig {
        @Setting
        private Text joinMessage = LiteralText.builder("Welcome to the test plugins!").color(TextColors.DARK_AQUA).build();
    }

}
