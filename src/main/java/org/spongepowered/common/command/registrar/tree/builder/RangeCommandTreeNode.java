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
package org.spongepowered.common.command.registrar.tree.builder;

import com.mojang.brigadier.arguments.ArgumentType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeType;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

import java.util.Optional;
import java.util.function.BiFunction;

public final class RangeCommandTreeNode<T extends Number>
        extends ArgumentCommandTreeNode<CommandTreeNode.Range<T>> implements CommandTreeNode.Range<T> {

    private @Nullable T min;
    private @Nullable T max;
    private final T defaultMin;
    private final T defaultMax;
    private final BiFunction<T, T, ArgumentType<?>> typeCreator;

    public RangeCommandTreeNode(
            final CommandTreeNodeType<Range<T>> parameterType,
            final BiFunction<T, T, ArgumentType<?>> typeCreator,
            final T defaultMin,
            final T defaultMax) {
        super(parameterType);
        this.typeCreator = typeCreator;
        this.defaultMax = defaultMax;
        this.defaultMin = defaultMin;
    }

    @Override
    public @NonNull Range<T> min(final @Nullable T min) {
        this.min = min;
        return this.getThis();
    }

    @Override
    public @NonNull Range<T> max(final @Nullable T max) {
        this.max = max;
        return this.getThis();
    }

    public Optional<T> getMin() {
        return Optional.ofNullable(this.min);
    }

    public Optional<T> getMax() {
        return Optional.ofNullable(this.max);
    }

    @Override
    protected ArgumentType<?> getArgumentType() {
        return this.typeCreator.apply(this.getMin().orElse(this.defaultMin), this.getMax().orElse(this.defaultMax));
    }

}
