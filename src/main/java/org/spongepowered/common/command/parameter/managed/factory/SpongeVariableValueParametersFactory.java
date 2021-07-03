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
package org.spongepowered.common.command.parameter.managed.factory;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.chat.TextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.command.brigadier.argument.StandardArgumentParser;
import org.spongepowered.common.command.parameter.managed.builder.SpongeDynamicChoicesBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeLiteralBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeNumberRangeBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeRegistryEntryParameterBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeStaticChoicesBuilder;
import org.spongepowered.common.command.parameter.managed.standard.SpongeChoicesValueParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class SpongeVariableValueParametersFactory implements VariableValueParameters.Factory {

    @Override
    public <T extends Enum<T>> @NonNull ValueParameter<T> createEnumParameter(final @NonNull Class<T> enumClass) {
        final Map<String, Supplier<? extends T>> choices = new HashMap<>();
        for (final T e :  enumClass.getEnumConstants()) {
            choices.put(e.name().toLowerCase(), () -> e);
        }
        return new SpongeChoicesValueParameter<>(Collections.unmodifiableMap(choices), true, true);
    }

    @Override
    public <T> VariableValueParameters.@NonNull StaticChoicesBuilder<T> createStaticChoicesBuilder(final @NonNull Class<T> returnType) {
        return new SpongeStaticChoicesBuilder<>();
    }

    @Override
    public <T> VariableValueParameters.@NonNull DynamicChoicesBuilder<T> createDynamicChoicesBuilder(final @NonNull Class<T> returnType) {
        return new SpongeDynamicChoicesBuilder<>();
    }

    @Override
    public <T> VariableValueParameters.@NonNull RegistryEntryBuilder<T> createRegistryEntryBuilder(final @NonNull RegistryType<T> registryKey) {
        return new SpongeRegistryEntryParameterBuilder<>(registryKey);
    }

    @Override
    public <T> VariableValueParameters.@NonNull RegistryEntryBuilder<T> createRegistryEntryBuilder(
            final @NonNull List<Function<CommandContext, @Nullable RegistryHolder>> holderProviders,
            final @NonNull RegistryType<T> type) {
        final VariableValueParameters.RegistryEntryBuilder<T> builder = this.createRegistryEntryBuilder(type);
        holderProviders.forEach(builder::addHolderFunction);
        return builder;
    }

    @Override
    public <T> VariableValueParameters.@NonNull RegistryEntryBuilder<T> createRegistryEntryBuilder(
            final @NonNull DefaultedRegistryType<T> type) {
        final VariableValueParameters.RegistryEntryBuilder<T> builder = new SpongeRegistryEntryParameterBuilder<>(type);
        builder.addHolderFunction(x -> type.defaultHolder().get());
        return builder;
    }

    @Override
    public <T> VariableValueParameters.@NonNull LiteralBuilder<T> createLiteralBuilder(final @NonNull Class<T> returnType) {
        return new SpongeLiteralBuilder<>();
    }

    @Override
    public VariableValueParameters.@NonNull NumberRangeBuilder<@NonNull Integer> createIntegerNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.intBuilder();
    }

    @Override
    public VariableValueParameters.@NonNull NumberRangeBuilder<@NonNull Float> createFloatNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.floatBuilder();
    }

    @Override
    public VariableValueParameters.@NonNull NumberRangeBuilder<@NonNull Double> createDoubleNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.doubleBuilder();
    }

    @Override
    public VariableValueParameters.@NonNull NumberRangeBuilder<@NonNull Long> createLongNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.longBuilder();
    }

    @Override
    public @NonNull ValueParameter<String> createValidatedStringParameter(final @NonNull Pattern pattern) {
        Objects.requireNonNull(pattern);

        return StandardArgumentParser.createConverter(StringArgumentType.string(), (reader, contextBuilder, input) -> {
            if (pattern.matcher(input).matches()) {
                return input;
            }
            throw new SimpleCommandExceptionType(
                    new TextComponent("Input \"" + input + "\" does not match required pattern \"" + pattern.pattern() + "\""))
                    .createWithContext(reader);
        });
    }

}
