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
package org.spongepowered.common.command.parameter.subcommand;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.common.util.Preconditions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SpongeSubcommandParameterBuilder implements Parameter.Subcommand.Builder {

    private final Set<String> aliases = new HashSet<>();
    private Command.@Nullable Parameterized command;

    @Override
    public Parameter.Subcommand.@NonNull Builder addAlias(final @NonNull String alias) {
        this.aliases.add(alias);
        return this;
    }

    public SpongeSubcommandParameterBuilder aliases(final @NonNull Collection<String> alias) {
        this.aliases.addAll(alias);
        return this;
    }

    @Override
    public Parameter.Subcommand.@NonNull Builder subcommand(final Command.@NonNull Parameterized command) {
        this.command = command;
        return this;
    }

    @Override
    public Parameter.@NonNull Subcommand build() {
        Preconditions.checkState(!this.aliases.isEmpty(), "At least one alias must be specified.");
        Preconditions.checkState(this.command != null, "A Command.Parameterized must be supplied.");
        return new SpongeSubcommandParameter(ImmutableSet.copyOf(this.aliases), this.command);
    }

    @Override
    public Parameter.Subcommand.@NonNull Builder reset() {
        this.aliases.clear();
        this.command = null;
        return this;
    }

}
