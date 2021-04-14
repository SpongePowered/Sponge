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
package org.spongepowered.common.command.parameter.managed.standard;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.common.command.brigadier.argument.ResourceKeyedArgumentValueParser;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class SpongeGameProfileValueParameter extends ResourceKeyedArgumentValueParser.ClientNativeCompletions<GameProfile> {

    private final GameProfileArgument argument = GameProfileArgument.gameProfile();

    public SpongeGameProfileValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(
            final com.mojang.brigadier.context.CommandContext<?> context,
            final SuggestionsBuilder builder) {

        return this.argument.listSuggestions(context, builder);
    }

    @Override
    public Optional<? extends GameProfile> parseValue(
            final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable reader) throws ArgumentParseException {
        try {
            final Collection<com.mojang.authlib.GameProfile> profileCollection =
                    this.argument.parse((StringReader) reader).getNames((CommandSourceStack) cause);
            if (profileCollection.size() == 1) {
                return Optional.of(SpongeGameProfile.of(profileCollection.iterator().next()));
            } else if (profileCollection.isEmpty()) {
                throw reader.createException(Component.text("No game profiles were selected."));
            } else {
                throw reader.createException(Component.text("Many game profiles were selected when only one was requested."));
            }
        } catch (final CommandSyntaxException e) {
            throw new ArgumentParseException(Component.text(e.getMessage()), e, reader.input(), reader.cursor());
        }
    }

}
