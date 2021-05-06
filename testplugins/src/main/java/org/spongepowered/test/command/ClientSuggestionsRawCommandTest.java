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
package org.spongepowered.test.command;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.registrar.tree.CommandTreeNodeTypes;
import org.spongepowered.api.command.registrar.tree.CommandCompletionProviders;
import org.spongepowered.api.command.registrar.tree.CommandTreeNode;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClientSuggestionsRawCommandTest implements Command.Raw {

    @Override
    public @NonNull CommandResult process(@NotNull final CommandCause cause, final ArgumentReader.@NonNull Mutable arguments) throws CommandException {
        cause.sendMessage(Identity.nil(), Component.text(arguments.remaining()));
        return CommandResult.success();
    }

    @Override
    public List<CommandCompletion> complete(@NonNull final CommandCause cause, final ArgumentReader.@NonNull Mutable arguments) throws CommandException {
        // This should not get hit
        return Collections.emptyList();
    }

    @Override
    public boolean canExecute(@NonNull final CommandCause cause) {
        return true;
    }

    @Override
    public @NonNull Optional<Component> shortDescription(@NonNull final CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Component> extendedDescription(@NonNull final CommandCause cause) {
        return Optional.empty();
    }

    @Override
    public @NonNull Component usage(@NonNull final CommandCause cause) {
        return Component.empty();
    }

    @Override
    public CommandTreeNode.Root commandTree() {
        return CommandTreeNode.root()
                .child("s1", CommandTreeNodeTypes.RESOURCE_LOCATION.get().createNode()
                .executable()
                .completions(CommandCompletionProviders.ALL_RECIPES));
    }

}
