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
package org.spongepowered.common.command.result;

import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.CommandResult;

public final class SpongeCommandResultBuilder implements CommandResult.Builder {

    private int result;
    private boolean isSuccess = true;
    private @Nullable Component errorMessage;

    @Override
    public CommandResult.@NonNull Builder result(final int result) {
        this.result = result;
        return this;
    }

    @Override
    public CommandResult.@NonNull Builder error(final @Nullable Component errorMessage) {
        this.errorMessage = errorMessage;
        this.isSuccess = false;
        return this;
    }

    @Override
    public @NonNull CommandResult build() {
        return new SpongeCommandResult(this.isSuccess, this.result, this.errorMessage);
    }

    @Override
    public CommandResult.@NonNull Builder reset() {
        this.result = 0;
        this.errorMessage = null;
        this.isSuccess = true;
        return this;
    }

}
