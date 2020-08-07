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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.text.StringTextComponent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.common.command.brigadier.argument.StandardArgumentParser;
import org.spongepowered.common.command.parameter.managed.builder.SpongeCatalogedElementParameterBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeDynamicChoicesBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeLiteralBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeNumberRangeBuilder;
import org.spongepowered.common.command.parameter.managed.builder.SpongeStaticChoicesBuilder;
import org.spongepowered.common.command.parameter.managed.standard.SpongeChoicesValueParameter;

import java.util.function.Supplier;
import java.util.regex.Pattern;

public final class SpongeVariableValueParameterBuilderFactory implements VariableValueParameters.Factory {

    public static final SpongeVariableValueParameterBuilderFactory INSTANCE = new SpongeVariableValueParameterBuilderFactory();

    private SpongeVariableValueParameterBuilderFactory() {
    }

    @Override
    @NonNull
    public <T extends Enum<T>> ValueParameter<T> createEnumParameter(@NonNull final Class<T> enumClass) {
        final ImmutableMap.Builder<String, Supplier<? extends T>> choices = ImmutableMap.builder();
        for (final T e :  enumClass.getEnumConstants()) {
            choices.put(e.name().toLowerCase(), () -> e);
        }
        return new SpongeChoicesValueParameter<>(choices.build(), true, true);
    }

    @Override
    public <T> VariableValueParameters.@NonNull StaticChoicesBuilder<T> createStaticChoicesBuilder(@NonNull final Class<T> returnType) {
        return new SpongeStaticChoicesBuilder<>();
    }

    @Override
    public <T> VariableValueParameters.@NonNull DynamicChoicesBuilder<T> createDynamicChoicesBuilder(@NonNull final Class<T> returnType) {
        return new SpongeDynamicChoicesBuilder<>();
    }

    @Override
    public <T extends CatalogType> VariableValueParameters.@NonNull CatalogedTypeBuilder<T> createCatalogedTypesBuilder(@NonNull final Class<T> returnType) {
        return new SpongeCatalogedElementParameterBuilder<>(returnType);
    }

    @Override
    public <T> VariableValueParameters.@NonNull LiteralBuilder<T> createLiteralBuilder(@NonNull final Class<T> returnType) {
        return new SpongeLiteralBuilder<>();
    }

    @Override
    public VariableValueParameters.NumberRangeBuilder<Integer> createIntegerNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.intBuilder();
    }

    @Override
    public VariableValueParameters.NumberRangeBuilder<Float> createFloatNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.floatBuilder();
    }

    @Override
    public VariableValueParameters.NumberRangeBuilder<Double> createDoubleNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.doubleBuilder();
    }

    @Override
    public VariableValueParameters.NumberRangeBuilder<Long> createLongNumberRangeBuilder() {
        return SpongeNumberRangeBuilder.longBuilder();
    }

    @Override
    public ValueParameter<String> createValidatedStringParameter(@NonNull final Pattern pattern) {
        Preconditions.checkNotNull(pattern, "Pattern must not be null.");
        return StandardArgumentParser.createConverter(StringArgumentType.string(), (reader, contextBuilder, input) -> {
            if (pattern.matcher(input).matches()) {
                return input;
            }
            throw new SimpleCommandExceptionType(
                    new StringTextComponent("Input \"" + input + "\" does not match required pattern \"" + pattern.pattern() + "\""))
                    .createWithContext(reader);
        });
    }

}
