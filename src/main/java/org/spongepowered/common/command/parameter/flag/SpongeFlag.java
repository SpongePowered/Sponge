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
package org.spongepowered.common.command.parameter.flag;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public final class SpongeFlag implements Flag {

    private final Set<String> keys;
    private final Set<String> aliases;
    private final Predicate<CommandCause> requirement;
    private final @Nullable Parameter associatedParameter;

    public SpongeFlag(
            final Set<String> keys,
            final Set<String> aliases,
            final Predicate<CommandCause> requirement,
            final @Nullable Parameter associatedParameter) {
        this.keys = keys;
        this.aliases = aliases;
        this.requirement = requirement;
        this.associatedParameter = associatedParameter;
    }

    @Override
    public @NonNull Collection<String> unprefixedAliases() {
        return ImmutableSet.copyOf(this.keys);
    }

    @Override
    public @NonNull Collection<String> aliases() {
        return ImmutableSet.copyOf(this.aliases);
    }

    @Override
    public @NonNull Predicate<CommandCause> requirement() {
        return this.requirement;
    }

    @Override
    public @NonNull Optional<Parameter> associatedParameter() {
        return Optional.ofNullable(this.associatedParameter);
    }

}
