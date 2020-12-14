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
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.command.brigadier.argument.CatalogedArgumentParser;
import org.spongepowered.common.command.brigadier.argument.ComplexSuggestionNodeProvider;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SpongeServerLocationValueParameter extends CatalogedArgumentParser<ServerLocation> implements ComplexSuggestionNodeProvider {

    private static final Vec3Argument VEC_3_ARGUMENT = Vec3Argument.vec3(false);
    private static final Pattern STARTS_WITH_NUMBER = Pattern.compile("^\\s*((-)?[0-9]|~|\\^)");
    private final boolean selectAllWorlds;

    public SpongeServerLocationValueParameter(final ResourceKey key, final boolean selectAllWorlds) {
        super(key);
        this.selectAllWorlds = selectAllWorlds;
    }

    @Override
    @NonNull
    public List<String> complete(@NonNull final CommandContext context, @NonNull final String currentInput) {
        return this.complete(currentInput);
    }

    private List<String> complete(final String currentInput) {
        return SpongeCommon.getGame().getServer().getWorldManager().getAllProperties()
                .stream()
                .filter(x -> this.selectAllWorlds || x.getWorld().isPresent())
                .map(WorldProperties::getKey)
                .map(ResourceKey::getFormatted)
                .filter(x -> x.startsWith(currentInput))
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public Optional<? extends ServerLocation> getValue(
            final Parameter.@NonNull Key<? super ServerLocation> parameterKey,
            final ArgumentReader.@NonNull Mutable reader,
            final CommandContext.@NonNull Builder context) throws ArgumentParseException {
        final ArgumentReader.Immutable state = reader.getImmutable();
        WorldProperties worldProperties;
        try {
            final ResourceKey resourceLocation = reader.parseResourceKey("minecraft");
            worldProperties = SpongeCommon.getGame().getServer().getWorldManager()
                    .getProperties(resourceLocation).filter(x -> this.selectAllWorlds || x.getWorld().isPresent())
                    .orElseThrow(() -> reader.createException(
                            Component.text("Could not get world with key \"" + resourceLocation.toString() + "\"")));
        } catch (final ArgumentParseException e) {
            final Optional<ServerLocation> location = context.getCause().getLocation();
            if (location.isPresent()) {
                // do this as late as possible to prevent expense of regex.
                if (!SpongeServerLocationValueParameter.STARTS_WITH_NUMBER.matcher(state.getRemaining()).find()) {
                    throw e;
                }
                worldProperties = location.get().getWorld().getProperties();
            } else {
                throw e;
            }
            reader.setState(state);
        }

        try {
            reader.skipWhitespace();
            final Vector3d vec3d = SpongeServerLocationValueParameter.VEC_3_ARGUMENT.parse((StringReader) reader).getPosition((CommandSource) context.getCause());
            final ResourceKey key = worldProperties.getKey();
            return Optional.of(
                    worldProperties.getWorld()
                            .map(x -> x.getLocation(vec3d.x, vec3d.y, vec3d.z))
                            .orElseGet(() -> ServerLocation.of(key, vec3d.x, vec3d.y, vec3d.z)));
        } catch (final CommandSyntaxException e) {
            throw reader.createException(Component.text(e.getMessage()));
        }
    }

    @Override
    public List<ArgumentType<?>> getClientCompletionArgumentType() {
        return ImmutableList.of(Constants.Command.RESOURCE_LOCATION_TYPE, Vec3Argument.vec3());
    }

    @Override
    public CommandNode<ISuggestionProvider> createSuggestions(final CommandNode<ISuggestionProvider> rootNode, final String key,
            final boolean isTerminal,
            final Consumer<List<CommandNode<ISuggestionProvider>>> firstNodes,
            final Consumer<CommandNode<ISuggestionProvider>> redirectionNodes,
            final boolean allowCustomSuggestionsOnTheFirstElement) {

        final RequiredArgumentBuilder<ISuggestionProvider, ResourceLocation> firstNode =
                RequiredArgumentBuilder.argument(key, Constants.Command.RESOURCE_LOCATION_TYPE);
        if (allowCustomSuggestionsOnTheFirstElement) {
            firstNode.suggests((context, builder) -> {
                this.complete("").forEach(builder::suggest);
                return builder.buildFuture();
            });
        }
        final RequiredArgumentBuilder<ISuggestionProvider, ILocationArgument> secondNode =
                RequiredArgumentBuilder.argument(key + "_pos", Vec3Argument.vec3());
        if (isTerminal) {
            secondNode.executes(x -> 1);
        }
        final CommandNode<ISuggestionProvider> second = secondNode.build();
        firstNode.then(second);
        final CommandNode<ISuggestionProvider> first = firstNode.build();
        redirectionNodes.accept(second);
        rootNode.addChild(first);
        rootNode.addChild(second);
        final List<CommandNode<ISuggestionProvider>> nodesToAttach = new ArrayList<>();
        nodesToAttach.add(first);
        nodesToAttach.add(second);
        firstNodes.accept(nodesToAttach);
        return second;
    }

}
