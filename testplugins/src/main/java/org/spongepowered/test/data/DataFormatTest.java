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
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.StringDataFormat;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.util.HashMap;

@Plugin("dataformattest")
public class DataFormatTest {

    private final PluginContainer plugin;

    @Inject
    public DataFormatTest(final PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Parameterized> event) {
        // /dataformattest snbt {id:"stone",Count:3}
        event.register(this.plugin, Command.builder()
                        .executionRequirements(cc -> cc.first(ServerPlayer.class).isPresent())
                        .addParameters(Parameter.choices(StringDataFormat.class, new HashMap<String, StringDataFormat>() {{
                                    put("hocon", DataFormats.HOCON.get());
                                    put("json", DataFormats.JSON.get());
                                    put("snbt", DataFormats.SNBT.get());
                                }}).key("format").build(),
                                Parameter.remainingJoinedStrings().key("value").build())
                        .executor(context -> {
                            final StringDataFormat format = context.requireOne(Parameter.key("format", StringDataFormat.class));
                            final String value = context.requireOne(Parameter.key("value", String.class));
                            try {
                                context.cause().audience().sendMessage(Component.text(format.write(format.read(value))));
                            } catch (final IOException e) {
                                throw new CommandException(Component.text("Serialize of input failed"), e);
                            }
                            return CommandResult.success();
                        })
                        .build()
                , "dataformattest");
    }
}
