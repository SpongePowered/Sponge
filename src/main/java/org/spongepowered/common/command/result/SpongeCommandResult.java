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

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.OptionalInt;

import javax.annotation.Nullable;

public class SpongeCommandResult implements CommandResult {

    public static final SpongeCommandResult EMPTY = new SpongeCommandResult(null, null, null, null, null, null);
    public static final SpongeCommandResult SUCCESS = new SpongeCommandResult(1, null, null, null, null, null);

    @Nullable private final Integer successCount;
    @Nullable private final Integer affectedBlocks;
    @Nullable private final Integer affectedEntities;
    @Nullable private final Integer affectedItems;
    @Nullable private final Integer queryResult;
    @Nullable private final Text errorMessage;

    public SpongeCommandResult(@Nullable Integer successCount, @Nullable Integer affectedBlocks, @Nullable Integer affectedEntities,
            @Nullable Integer affectedItems, @Nullable Integer queryResult, @Nullable Text errorMessage) {
        this.successCount = successCount;
        this.affectedBlocks = affectedBlocks;
        this.affectedEntities = affectedEntities;
        this.affectedItems = affectedItems;
        this.queryResult = queryResult;
        this.errorMessage = errorMessage;
    }

    @Override
    public OptionalInt successCount() {
        return fromInteger(this.successCount);
    }

    @Override
    public OptionalInt affectedBlocks() {
        return fromInteger(this.affectedBlocks);
    }

    @Override
    public OptionalInt affectedEntities() {
        return fromInteger(this.affectedEntities);
    }

    @Override
    public OptionalInt affectedItems() {
        return fromInteger(this.affectedItems);
    }

    @Override
    public OptionalInt queryResult() {
        return fromInteger(this.queryResult);
    }

    @Override
    public Optional<Text> getErrorMessage() {
        return Optional.ofNullable(this.errorMessage);
    }

    private static OptionalInt fromInteger(@Nullable Integer integer) {
        if (integer == null) {
            return OptionalInt.empty();
        }

        return OptionalInt.of(integer);
    }

}
