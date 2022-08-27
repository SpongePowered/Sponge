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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.command.result.SpongeCommandResult;

/**
 * Used as a vehicle to transfer an error result down to the command
 * manager from a Brig supported layer.
 */
public final class SpongeCommandResultException extends CommandSyntaxException {

    private final static Component EMPTY = new TextComponent("");
    private final CommandResult result;

    public static SpongeCommandResultException createException(final CommandResult result) {
        return new SpongeCommandResultException(result, result.errorMessage().map(SpongeAdventure::asVanilla).orElse(SpongeCommandResultException.EMPTY));
    }

    private SpongeCommandResultException(final CommandResult result, final Component error) {
        super(new SimpleCommandExceptionType(error), error);
        this.result = result;
    }

    // We're never going to use this.
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public CommandResult result() {
        return this.result;
    }

}
