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
package org.spongepowered.common.command.exception;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.brigadier.context.SpongeCommandContext;

public final class SpongeCommandSyntaxException extends CommandSyntaxException {

    private static final Component ERROR_MESSAGE = Component.text("Error running command: ", NamedTextColor.RED);

    private final CommandException innerException;
    private final SpongeCommandContext commandContext;

    public SpongeCommandSyntaxException(final CommandException exception, final SpongeCommandContext commandContext) {
        super(new SimpleCommandExceptionType(SpongeAdventure.asVanilla(exception.componentMessage())), SpongeAdventure.asVanilla(exception.componentMessage()));
        this.innerException = exception;
        this.commandContext = commandContext;
    }

    public SpongeCommandSyntaxException(final CommandException exception, final SpongeCommandContext commandContext, final String command, final int cursor) {
        super(new SimpleCommandExceptionType(SpongeAdventure.asVanilla(exception.componentMessage())), SpongeAdventure.asVanilla(exception.componentMessage()), command, cursor);
        this.innerException = exception;
        this.commandContext = commandContext;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // Don't gather stacktrace, we are just wrapping the existing exception
        return this;
    }

    @Override
    public synchronized CommandException getCause() {
        return this.innerException;
    }

    public SpongeCommandContext getCommandContext() {
        return this.commandContext;
    }

    public Component getComponentMessage() {
        final Component message = this.innerException.componentMessage();
        return SpongeCommandSyntaxException.ERROR_MESSAGE.append(message == null ? Component.text("unknown") : message);
    }

    @Override
    public String getMessage() {
        return PlainTextComponentSerializer.plainText().serialize(this.getComponentMessage());
    }

}
