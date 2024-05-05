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
import org.spongepowered.common.util.Preconditions;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public final class SpongeFlagBuilder implements Flag.Builder {

    private @Nullable Parameter parameter;
    private final Set<String> aliases = new HashSet<>();
    private final Set<String> keys = new HashSet<>();
    private Predicate<CommandCause> requirement = cause -> true;

    @Override
    public Flag.@NonNull Builder alias(final @NonNull String alias) {
        Objects.requireNonNull(alias, "The alias cannot be null.");
        if (alias.startsWith("-")) {
            throw new IllegalArgumentException("Flag '" + alias + "' cannot start with a dash");
        }
        if (alias.contains(" ")) {
            throw new IllegalArgumentException("Flag '" + alias + "' cannot contain whitespace");
        }
        if (alias.length() == 1) {
            this.aliases.add("-" + alias.toLowerCase(Locale.ROOT));
        } else {
            this.aliases.add("--" + alias.toLowerCase(Locale.ROOT));
        }
        this.keys.add(alias.toLowerCase(Locale.ROOT));
        return this;
    }

    @Override
    public Flag.@NonNull Builder aliases(final @NonNull Iterable<String> aliases) {
        for (final String alias : aliases) {
            this.alias(alias);
        }
        return this;
    }

    @Override
    public Flag.@NonNull Builder setPermission(final @Nullable String permission) {
        if (permission == null) {
            return this.setRequirement(null);
        } else {
            return this.setRequirement(cause -> cause.hasPermission(permission));
        }
    }

    @Override
    public Flag.@NonNull Builder setRequirement(final @Nullable Predicate<CommandCause> requirement) {
        if (requirement == null) {
            this.requirement = cause -> true;
        } else {
            this.requirement = requirement;
        }
        return this;
    }

    @Override
    public Flag.@NonNull Builder setParameter(final @Nullable Parameter parameter) {
        this.parameter = parameter;
        return this;
    }

    @Override
    public @NonNull Flag build() throws IllegalStateException {
        Preconditions.checkState(!this.aliases.isEmpty(), "Aliases cannot be empty.");
        return new SpongeFlag(
                ImmutableSet.copyOf(this.keys),
                ImmutableSet.copyOf(this.aliases),
                this.requirement,
                this.parameter
        );
    }

    @Override
    public Flag.@NonNull Builder reset() {
        this.aliases.clear();
        this.requirement = cause -> true;
        this.parameter = null;
        return this;
    }

}
