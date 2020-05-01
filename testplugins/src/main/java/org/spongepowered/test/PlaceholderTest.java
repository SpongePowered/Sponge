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
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;
import org.spongepowered.api.text.placeholder.PlaceholderText;
import org.spongepowered.api.text.Text;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.inject.Inject;

@Plugin(id = "placeholdertest", name = "Placeholder Test", description = "A plugin to test placeholders", version = "0.0.0")
public class PlaceholderTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z");
    private final PluginContainer pluginContainer;
    private final Text placeholderKey = Text.of("Placeholder");
    private final Text argumentStringKey = Text.of("Argument String");

    @Inject
    public PlaceholderTest(PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    // Plugins providing placeholders will do this
    @Listener
    public void onRegistration(GameRegistryEvent.Register<PlaceholderParser> event) {
        event.register(new PlaceholderParser() {
            @Override
            public Text parse(PlaceholderContext placeholderContext) {
                if (placeholderContext.getArgumentString().map(x -> x.equalsIgnoreCase("UTC")).isPresent()) {
                    return Text.of(OffsetDateTime.now(ZoneOffset.UTC).format(FORMATTER));
                }
                return Text.of(OffsetDateTime.now().format(FORMATTER));
            }

            @Override
            public String getId() {
                return "placeholdertest:currenttime";
            }

            @Override
            public String getName() {
                return "Placeholder Test";
            }
        });
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(
                this.pluginContainer,
                CommandSpec.builder()
                    .arguments(
                            GenericArguments.catalogedElement(this.placeholderKey, PlaceholderParser.class),
                            GenericArguments.optional(GenericArguments.remainingRawJoinedStrings(this.argumentStringKey))
                    )
                    .executor((source, context) -> {
                        // Plugins using placeholders will do this
                        final PlaceholderParser parser = context.requireOne(this.placeholderKey);
                        final Optional<String> args = context.getOne(this.argumentStringKey);

                        final PlaceholderContext.Builder builder = PlaceholderContext.builder().setAssociatedObject(source);
                        args.ifPresent(builder::setArgumentString);
                        final PlaceholderText builderText = PlaceholderText.builder().setContext(builder.build()).setParser(parser).build();

                        source.sendMessage(Text.of("Result: ", builderText));
                        return CommandResult.success();
                    }).build(),
                "placeholder"
        );
    }

}
