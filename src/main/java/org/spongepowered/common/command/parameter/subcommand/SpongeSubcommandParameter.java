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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.Set;

public final class SpongeSubcommandParameter implements Parameter.Subcommand {

    private final Set<String> aliases;
    private final Command.Parameterized command;

    public SpongeSubcommandParameter(@NonNull final Set<String> aliases, final Command.@NonNull Parameterized command) {
        this.aliases = aliases;
        this.command = command;
    }

    // A subcommand is always considered optional.
    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return this.command.isTerminal();
    }

    @Override
    public Command.@NonNull Parameterized getCommand() {
        return this.command;
    }

    @Override
    @NonNull
    public Set<String> getAliases() {
        return ImmutableSet.copyOf(this.aliases);
    }

}
