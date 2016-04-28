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
package org.spongepowered.common.command;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.annotation.Command;
import org.spongepowered.api.command.annotation.Permission;
import org.spongepowered.api.command.annotation.PlainDescription;
import org.spongepowered.api.command.annotation.TranslatableDescription;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

/**
 * Wrapper around the Command annotation.
 */
public class SpongeAnnotationCommand implements CommandCallable {

    private final Command command;
    private final Optional<Permission> permissions;

    private Optional<Text> description = Optional.empty();
    private Optional<Text> usage = Optional.empty();
    private Optional<Text> help = Optional.empty();

    public SpongeAnnotationCommand(Command command,
            Optional<PlainDescription> plainDescription, Optional<TranslatableDescription> translatableDescription,
            Optional<Permission> permissions) {
        this.command = command;
        this.permissions = permissions;
        if (plainDescription.isPresent()) {
            this.description = Optional.of(Text.of(plainDescription.get().description()));
            this.usage = Optional.of(Text.of(plainDescription.get().usage()));
            this.help = Optional.of(Text.of(plainDescription.get().help()));
        }
        // TODO: translatable permission
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        return null;
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return null;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        if (this.permissions.isPresent()) {
            for (String permission : this.permissions.get().value()) {
                if (!source.hasPermission(permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return this.description;
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return this.help;
    }

    @Override
    public Text getUsage(CommandSource source) {
        if (this.usage.isPresent()) {
            return this.usage.get();
        }
        return Text.EMPTY;
    }
}
