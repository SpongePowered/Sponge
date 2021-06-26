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
package org.spongepowered.common.command.parameter.managed.standard;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.argument.ResourceKeyedArgumentValueParser;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SpongePluginContainerValueParameter extends ResourceKeyedArgumentValueParser<PluginContainer> {

    public SpongePluginContainerValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull CommandCause context, final String currentInput) {
        return Launch.instance().pluginManager().plugins().stream()
                .filter(x -> x.metadata().id().startsWith(currentInput))
                .map(entry -> {
                    final Component tooltip = Component.text(entry.metadata().name().orElse(entry.metadata().id()));
                    return new SpongeCommandCompletion(entry.metadata().id(), tooltip);
                })
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull Optional<? extends PluginContainer> parseValue(
            final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable reader) throws ArgumentParseException {

        final String id = reader.parseString();
        final Optional<PluginContainer> container = Launch.instance().pluginManager().plugin(id);
        if (container.isPresent()) {
            return container;
        }

        throw reader.createException(Component.text("Could not find plugin with ID \"" + id + "\""));
    }

}
