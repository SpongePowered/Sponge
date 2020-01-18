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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

@Plugin(id = "text-test", name = "Text Test", description = "Tests text related functions", version = "0.0.0")
public class TextTestPlugin {
    @Inject private PluginContainer container;

    @Listener
    public void onInit(GameInitializationEvent event) {

        Parameter.Value<Text> paramTest = Parameter.formattingCodeText().consumeAllRemaining().setKey("test").build();
        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .parameters(paramTest)
                        .setExecutor((ctx) -> {
                            ctx.getMessageReceiver().sendMessage(ctx.<Text>requireOne(paramTest));
                            return CommandResult.success();
                        }).build(),
                "test-text-ampersand");

        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .setExecutor((ctx) -> {
                            Text message = Text.of(
                                    TextColors.YELLOW, "This is ", TextColors.GOLD, TextStyles.BOLD, "BOLD GOLD"
                            );
                            ctx.getMessageReceiver().sendMessage(message);
                            return CommandResult.success();
                        }).build(),
                "test-text-message");
    }

}