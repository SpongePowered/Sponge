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
package org.spongepowered.common.command.parameter.managed.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.common.command.brigadier.argument.StandardArgumentParser;

import java.util.Objects;

public final class SpongeNumberRangeBuilder<T extends Number> implements VariableValueParameters.NumberRangeBuilder<T> {

    private static final Definition<Double> DOUBLE =
            new Definition<Double>() {
                @Override
                public boolean validate(final Double min, final Double max) {
                    return min <= max;
                }

                @Override
                public ArgumentType<Double> createArgumentType(final Double min, final Double max) {
                    return DoubleArgumentType.doubleArg(min, max);
                }
            };
    private static final Definition<Float> FLOAT =
            new Definition<Float>() {
                @Override
                public boolean validate(final Float min, final Float max) {
                    return min <= max;
                }

                @Override
                public ArgumentType<Float> createArgumentType(final Float min, final Float max) {
                    return FloatArgumentType.floatArg(min, max);
                }
            };
    private static final Definition<Integer> INTEGER =
            new Definition<Integer>() {
                @Override
                public boolean validate(final Integer min, final Integer max) {
                    return min <= max;
                }

                @Override
                public ArgumentType<Integer> createArgumentType(final Integer min, final Integer max) {
                    return IntegerArgumentType.integer(min, max);
                }
            };
    private static final Definition<Long> LONG =
            new Definition<Long>() {
                @Override
                public boolean validate(final Long min, final Long max) {
                    return min <= max;
                }

                @Override
                public ArgumentType<Long> createArgumentType(final Long min, final Long max) {
                    return LongArgumentType.longArg(min, max);
                }
            };

    public static SpongeNumberRangeBuilder<Double> doubleBuilder() {
        return new SpongeNumberRangeBuilder<>(Double.MIN_VALUE, Double.MAX_VALUE, SpongeNumberRangeBuilder.DOUBLE);
    }

    public static SpongeNumberRangeBuilder<Float> floatBuilder() {
        return new SpongeNumberRangeBuilder<>(Float.MIN_VALUE, Float.MAX_VALUE, SpongeNumberRangeBuilder.FLOAT);
    }

    public static SpongeNumberRangeBuilder<Integer> intBuilder() {
        return new SpongeNumberRangeBuilder<>(Integer.MIN_VALUE, Integer.MAX_VALUE, SpongeNumberRangeBuilder.INTEGER);
    }

    public static SpongeNumberRangeBuilder<Long> longBuilder() {
        return new SpongeNumberRangeBuilder<>(Long.MIN_VALUE, Long.MAX_VALUE, SpongeNumberRangeBuilder.LONG);
    }

    private final Definition<T> definition;
    private final T defaultMin;
    private final T defaultMax;
    private T min;
    private T max;

    private SpongeNumberRangeBuilder(final T min, final T max, final Definition<T> definition) {
        this.defaultMin = min;
        this.defaultMax = max;
        this.min = min;
        this.max = max;
        this.definition = definition;
    }

    @Override
    public VariableValueParameters.@NonNull NumberRangeBuilder<T> min(@NonNull final T min) {
        this.min = Objects.requireNonNull(min);
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull NumberRangeBuilder<T> max(@NonNull final T max) {
        this.max = Objects.requireNonNull(max);
        return this;
    }

    @Override
    @NonNull
    public ValueParameter<T> build() {
        if (!this.definition.validate(this.min, this.max)) {
            throw new IllegalStateException("Min must be smaller or equal to max!");
        }
        return StandardArgumentParser.createIdentity(this.definition.createArgumentType(this.min, this.max));
    }

    @Override
    public VariableValueParameters.@NonNull NumberRangeBuilder<T> reset() {
        this.min = this.defaultMin;
        this.max = this.defaultMax;
        return this;
    }

    public interface Definition<T extends Number> {

        boolean validate(T min, T max);

        ArgumentType<T> createArgumentType(T min, T max);

    }
}
