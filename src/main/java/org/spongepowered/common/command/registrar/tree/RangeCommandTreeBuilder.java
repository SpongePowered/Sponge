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
package org.spongepowered.common.command.registrar.tree;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.network.PacketBuffer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.registrar.tree.ClientCompletionKey;
import org.spongepowered.api.command.registrar.tree.CommandTreeBuilder;

import java.util.Optional;
import java.util.function.BiConsumer;

public class RangeCommandTreeBuilder<T extends Number>
        extends ArgumentCommandTreeBuilder<CommandTreeBuilder.Range<T>> implements CommandTreeBuilder.Range<T> {

    private static final String MIN_PROPERTY = "min";
    private static final String MAX_PROPERTY = "max";

    private final BiConsumer<PacketBuffer, RangeCommandTreeBuilder<T>> packetWriter;
    @Nullable private T min;
    @Nullable private T max;

    public RangeCommandTreeBuilder(final ClientCompletionKey<Range<T>> parameterType, final BiConsumer<PacketBuffer, RangeCommandTreeBuilder<T>> packetWriter) {
        super(parameterType);
        this.packetWriter = packetWriter;
    }

    @Override
    @NonNull
    public Range<T> min(@Nullable final T min) {
        this.min = min;
        return this.getThis();
    }

    @Override
    @NonNull
    public Range<T> max(@Nullable final T max) {
        this.max = max;
        return this.getThis();
    }

    public Optional<T> getMin() {
        return Optional.ofNullable(this.min);
    }

    public Optional<T> getMax() {
        return Optional.ofNullable(this.max);
    }

    @Override protected ArgumentType<?> getArgumentType() {
        return null;
    }
}
