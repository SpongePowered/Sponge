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

import com.mojang.brigadier.arguments.ArgumentType;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.argument.AbstractArgumentParser;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SpongeCatalogedElementValueParameter<T> extends AbstractArgumentParser<T> {

    private final List<String> prefixes;
    private final List<Function<CommandContext, @Nullable RegistryHolder>> registryHolderFunctions;
    private final RegistryType<? extends T> registryType;

    public SpongeCatalogedElementValueParameter(final List<String> prefixes,
            final List<Function<CommandContext, @Nullable RegistryHolder>> registryFunctions,
            final RegistryType<? extends T> registryType) {
        this.prefixes = prefixes;
        this.registryHolderFunctions = registryFunctions;
        this.registryType = registryType;
    }

    @Override
    public @NonNull Optional<? extends T> parseValue(final Parameter.@NonNull Key<? super T> parameterKey,
                                          final ArgumentReader.@NonNull Mutable reader,
                                          final CommandContext.@NonNull Builder context) throws ArgumentParseException {
        final List<Registry<? extends T>> registry = this.registryHolderFunctions.stream()
                .map(x -> this.retrieveRegistry(x, context))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (registry.isEmpty()) {
            throw reader.createException(Component.text("No registries associated with this parameter are active."));
        }
        final ArgumentReader.Immutable snapshot = reader.immutable();
        try {
            final ResourceKey resourceKey = reader.parseResourceKey();
            final Optional<? extends T> result = this.selectValue(registry, resourceKey);
            if (!result.isPresent()) {
                throw reader.createException(
                        Component.text("None of the selected registries contain the ID " + resourceKey.asString()));
            }
            return result;
        } catch (final ArgumentParseException ex) {
            if (this.prefixes.isEmpty()) {
                throw ex;
            }

            reader.setState(snapshot);
            final String check = reader.parseUnquotedString();
            for (final String prefix : this.prefixes) {
                final Optional<? extends T> result = this.selectValue(registry, ResourceKey.of(prefix, check));
                if (result.isPresent()) {
                    return result;
                }
            }

            final String ids = this.prefixes.stream().map(x -> x + ":" + check).collect(Collectors.joining(", "));
            throw reader.createException(
                    Component.text("None of the selected registries contain any of the following IDs: " + ids));
        }
    }

    @Override
    public @NonNull List<CommandCompletion> complete(final @NonNull CommandContext context, final @NonNull String currentInput) {
        final String lowerCase = currentInput.toLowerCase();
        return this.registryHolderFunctions.stream()
                .map(x -> this.retrieveRegistry(x, context))
                .filter(Objects::nonNull)
                .flatMap(Registry::streamEntries)
                .map(RegistryEntry::key)
                .map(x -> {
                    if (x.asString().startsWith(lowerCase)) {
                        return x.asString();
                    } else if (this.prefixes.contains(x.namespace()) && x.value().startsWith(lowerCase)) {
                        return x.value();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(SpongeCommandCompletion::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return Collections.singletonList(Constants.Command.RESOURCE_LOCATION_TYPE);
    }

    private Optional<? extends T> selectValue(final List<Registry<? extends T>> registry, final ResourceKey resourceKey) {
        return registry.stream()
                .map(x -> x.findValue(resourceKey).orElse(null))
                .filter(Objects::nonNull)
                .findFirst();
    }

    private @Nullable Registry<? extends T> retrieveRegistry(
            final Function<CommandContext, @Nullable RegistryHolder> func, final CommandContext context) {
        final RegistryHolder holder = func.apply(context);
        if (holder != null) {
            return holder.findRegistry(this.registryType).orElse(null);
        }
        return null;
    }
}
