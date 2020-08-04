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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.arguments.ArgumentType;
import net.kyori.adventure.text.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.command.brigadier.argument.AbstractArgumentParser;
import org.spongepowered.common.util.Constants;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class SpongeCatalogedElementValueParameter<T extends CatalogType> extends AbstractArgumentParser<T> {

    private final List<String> prefixes;
    private final Class<T> catalogType;

    // As CatalogTypes are baked in at startup, we can calculate and cache them.
    @Nullable private List<String> completions = null;

    public SpongeCatalogedElementValueParameter(final List<String> prefixes, final Class<T> catalogType) {
        this.prefixes = prefixes;
        this.catalogType = catalogType;
    }

    @NonNull
    @Override
    public Optional<? extends T> getValue(final Parameter.@NonNull Key<? super T> parameterKey,
                                          final ArgumentReader.@NonNull Mutable reader,
                                          final CommandContext.@NonNull Builder context) throws ArgumentParseException {
        try {
            final ResourceKey resourceKey = reader.parseResourceKey();
            final Optional<T> result = SpongeCommon.getRegistry().getCatalogRegistry().get(this.catalogType, resourceKey);
            if (!result.isPresent()) {
                throw reader.createException(TextComponent.of("No " + this.catalogType.getSimpleName() + " with ID " + resourceKey.asString() + "exists"));
            }
            return result;
        } catch (final ArgumentParseException ex) {
            if (this.prefixes.isEmpty()) {
                throw ex;
            }

            final String check = reader.parseUnquotedString();
            for (final String prefix : this.prefixes) {
                final Optional<T> result = SpongeCommon.getRegistry().getCatalogRegistry().get(this.catalogType, ResourceKey.of(prefix, check));
                if (result.isPresent()) {
                    return result;
                }
            }

            final String ids = this.prefixes.stream().map(x -> x + ":" + check).collect(Collectors.joining(", "));
            throw reader.createException(
                    TextComponent.of("No " + this.catalogType.getSimpleName() + " with any of the following IDs exist: " + ids));
        }
    }

    @NonNull
    @Override
    public List<String> complete(@NonNull final CommandContext context) {
        if (this.completions == null) {
            final ImmutableList.Builder<String> entries = ImmutableList.builder();
            final Collection<T> catalogTypes = SpongeCommon.getRegistry().getCatalogRegistry().getAllOf(this.catalogType);
            for (final T catalogType : catalogTypes) {
                entries.add(catalogType.getKey().getFormatted());
                if (this.prefixes.contains(catalogType.getKey().getNamespace())) {
                    entries.add(catalogType.getKey().getValue());
                }
            }
            this.completions = entries.build();
        }
        return this.completions;
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return ImmutableList.of(Constants.Command.RESOURCE_LOCATION_TYPE);
    }
}
