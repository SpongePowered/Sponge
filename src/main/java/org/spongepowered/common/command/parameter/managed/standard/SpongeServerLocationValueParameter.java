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

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.command.SpongeCommandCompletion;
import org.spongepowered.common.command.brigadier.argument.ResourceKeyedArgumentValueParser;
import org.spongepowered.common.command.brigadier.argument.ComplexSuggestionNodeProvider;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SpongeServerLocationValueParameter extends ResourceKeyedArgumentValueParser<ServerLocation> implements ComplexSuggestionNodeProvider {

    private static final Vec3Argument VEC_3_ARGUMENT = Vec3Argument.vec3(false);
    private static final Pattern STARTS_WITH_NUMBER = Pattern.compile("^\\s*((-)?[0-9]|~|\\^)");

    public SpongeServerLocationValueParameter(final ResourceKey key) {
        super(key);
    }

    @Override
    public List<CommandCompletion> complete(final @NonNull CommandCause cause, final @NonNull String currentInput) {
        return this.complete(currentInput).map(SpongeCommandCompletion::new).collect(Collectors.toList());
    }

    private Stream<String> complete(final String currentInput) {
        return SpongeCommon.getGame().server().worldManager().worlds()
                .stream()
                .map(ServerWorld::key)
                .map(ResourceKey::formatted)
                .filter(x -> x.startsWith(currentInput));
    }

    @Override
    public @NonNull Optional<? extends ServerLocation> parseValue(
            final @NonNull CommandCause cause, final ArgumentReader.@NonNull Mutable reader) throws ArgumentParseException {
        final ArgumentReader.Immutable state = reader.immutable();
        ServerWorld serverWorld;
        try {
            final ResourceKey resourceLocation = reader.parseResourceKey("minecraft");
            serverWorld = SpongeCommon.getGame().server().worldManager()
                    .world(resourceLocation)
                    .orElseThrow(() -> reader.createException(
                            Component.text("Could not get world with key \"" + resourceLocation.toString() + "\"")));
        } catch (final ArgumentParseException e) {
            final Optional<ServerLocation> location = cause.location();
            if (location.isPresent()) {
                // do this as late as possible to prevent expense of regex.
                if (!SpongeServerLocationValueParameter.STARTS_WITH_NUMBER.matcher(state.remaining()).find()) {
                    throw e;
                }
                serverWorld = location.get().world();
            } else {
                throw e;
            }
            reader.setState(state);
        }

        try {
            reader.skipWhitespace();
            final Vec3 vec3d = SpongeServerLocationValueParameter.VEC_3_ARGUMENT.parse((StringReader) reader).getPosition((CommandSourceStack) cause);
            return Optional.of(serverWorld.location(VecHelper.toVector3d(vec3d)));
        } catch (final CommandSyntaxException e) {
            throw reader.createException(Component.text(e.getMessage()));
        }
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return ImmutableList.of(Constants.Command.RESOURCE_LOCATION_TYPE, Vec3Argument.vec3());
    }

    @Override
    public CommandNode<SharedSuggestionProvider> createSuggestions(final CommandNode<SharedSuggestionProvider> rootNode, final String key,
            final boolean isTerminal,
            final Consumer<List<CommandNode<SharedSuggestionProvider>>> firstNodes,
            final Consumer<CommandNode<SharedSuggestionProvider>> redirectionNodes,
            final boolean allowCustomSuggestionsOnTheFirstElement) {

        final RequiredArgumentBuilder<SharedSuggestionProvider, ResourceLocation> firstNode =
                RequiredArgumentBuilder.argument(key, Constants.Command.RESOURCE_LOCATION_TYPE);
        if (allowCustomSuggestionsOnTheFirstElement) {
            firstNode.suggests((context, builder) -> {
                this.complete("").forEach(builder::suggest);
                return builder.buildFuture();
            });
        }
        final RequiredArgumentBuilder<SharedSuggestionProvider, Coordinates> secondNode =
                RequiredArgumentBuilder.argument(key + "_pos", Vec3Argument.vec3());
        if (isTerminal) {
            secondNode.executes(x -> 1);
        }
        final CommandNode<SharedSuggestionProvider> second = secondNode.build();
        firstNode.then(second);
        final CommandNode<SharedSuggestionProvider> first = firstNode.build();
        redirectionNodes.accept(second);
        rootNode.addChild(first);
        rootNode.addChild(second);
        final List<CommandNode<SharedSuggestionProvider>> nodesToAttach = new ArrayList<>();
        nodesToAttach.add(first);
        nodesToAttach.add(second);
        firstNodes.accept(nodesToAttach);
        return second;
    }

}
