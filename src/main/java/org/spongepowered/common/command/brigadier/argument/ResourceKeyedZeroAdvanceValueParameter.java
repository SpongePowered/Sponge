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
package org.spongepowered.common.command.brigadier.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.parameter.managed.clientcompletion.ClientCompletionType;
import org.spongepowered.common.command.parameter.managed.clientcompletion.SpongeClientCompletionType;
import org.spongepowered.common.util.Constants;

import java.util.Collections;
import java.util.List;

public abstract class ResourceKeyedZeroAdvanceValueParameter<T> extends ResourceKeyedArgumentValueParser<T> {

    private static final List<ClientCompletionType> NONE_KEY = Collections.singletonList(SpongeClientCompletionType.NONE);

    public ResourceKeyedZeroAdvanceValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    public final List<CommandCompletion> complete(final @NonNull CommandCause cause, final @NonNull String currentInput) {
        return Collections.emptyList();
    }

    @Override
    public final @NonNull List<ClientCompletionType> clientCompletionType() {
        return ResourceKeyedZeroAdvanceValueParameter.NONE_KEY;
    }

    @Override
    public final @NonNull List<ArgumentType<?>> getClientCompletionArgumentType() {
        // need this as a dummy
        return Collections.singletonList(Constants.Command.STANDARD_STRING_ARGUMENT_TYPE);
    }

    @Override
    public boolean doesNotRead() {
        return true;
    }

}
