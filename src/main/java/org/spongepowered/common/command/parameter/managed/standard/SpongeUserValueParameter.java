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
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.user.UserManager;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.argument.ResourceKeyedArgumentValueParser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SpongeUserValueParameter extends ResourceKeyedArgumentValueParser<User> {

    private final EntityArgument selectorArgumentType = EntityArgument.player();

    public SpongeUserValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull CommandCause cause, final @NonNull String currentInput) {
        return Sponge.server().userManager().streamOfMatches(currentInput).filter(GameProfile::hasName)
                .map(x -> x.name().map(SpongeCommandCompletion::new).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull Optional<? extends User> parseValue(
            final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable reader)
            throws ArgumentParseException {
        final String peek = reader.peekString();
        if (peek.startsWith("@")) {
            try {
                final ServerPlayer entity =
                        (ServerPlayer) (this.selectorArgumentType.parse((StringReader) reader)
                                .findSingleEntity(((CommandSourceStack) cause)));
                return Optional.of(entity.user());
            } catch (final CommandSyntaxException e) {
                throw reader.createException(Component.text(e.getContext()));
            }
        }

        final UserManager userManager = SpongeCommon.getGame().server().userManager();
        Optional<User> user;
        try {
            final UUID uuid = UUID.fromString(reader.parseString());
            user = userManager.find(uuid);
        } catch (final Exception e) {
            // if no UUID, get the name. We've already advanced the reader at this point.
            user = userManager.find(peek);
        }

        if (user.isPresent()) {
            return user;
        }

        throw reader.createException(Component.text("Could not find user with user name \"" + peek + "\""));
    }

}
