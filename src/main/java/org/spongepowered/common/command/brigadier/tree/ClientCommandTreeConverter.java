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
package org.spongepowered.common.command.brigadier.tree;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandBox;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.BooleanArgument;
import net.minecraft.commands.arguments.DoubleArgument;
import net.minecraft.commands.arguments.FloatArgument;
import net.minecraft.commands.arguments.IntegerArgument;
import net.minecraft.commands.arguments.LongArgument;
import net.minecraft.commands.arguments.SerializableArgument;
import net.minecraft.commands.arguments.StringArgument;
import net.minecraft.commands.suggestions.AskServer;
import net.minecraft.commands.suggestions.CustomSuggestion;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.server.permissions.PermissionProviderCheck;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.function.Predicate;

/**
 * Turns the tree Sponge resolved for one player into what the commands packet
 * can carry: serializable argument types, serializable suggestions and
 * requirements the packet knows how to read. The tree only holds what the
 * player may use, so every requirement passes.
 */
public final class ClientCommandTreeConverter extends CommandBox.NodeConverter<CommandSourceStack, SuggestionContext> {

    private static final Command<SuggestionContext> EXECUTABLE = context -> 0;

    @Override
    protected Predicate<SuggestionContext> convert(final Predicate<CommandSourceStack> requirement) {
        return PermissionProviderCheck.ALWAYS_PASS.cast();
    }

    @Override
    protected Command<SuggestionContext> convert(final Command<CommandSourceStack> command) {
        return ClientCommandTreeConverter.EXECUTABLE;
    }

    @Override
    protected @Nullable RedirectModifier<SuggestionContext> convert(final RedirectModifier<CommandSourceStack> redirectModifier) {
        return null;
    }

    @Override
    protected SuggestionProvider<SuggestionContext> convert(final SuggestionProvider<CommandSourceStack> customSuggestions) {
        if (customSuggestions instanceof final CustomSuggestion.BrigadierWrapper<?> wrapper) {
            return wrapper.cast();
        }
        // Anything of ours can only be answered by the server
        return AskServer.INSTANCE.toBrigadier();
    }

    @Override
    protected ArgumentType<?> convert(final ArgumentType<?> type) {
        return switch (type) {
            case final SerializableArgument.BrigadierWrapper<?> wrapper -> wrapper;
            case final BoolArgumentType bool -> BooleanArgument.bool();
            case final IntegerArgumentType integer -> IntegerArgument.integer(integer.getMinimum(), integer.getMaximum());
            case final LongArgumentType longArg -> LongArgument.longArg(longArg.getMinimum(), longArg.getMaximum());
            case final FloatArgumentType floatArg -> FloatArgument.floatArg(floatArg.getMinimum(), floatArg.getMaximum());
            case final DoubleArgumentType doubleArg -> DoubleArgument.doubleArg(doubleArg.getMinimum(), doubleArg.getMaximum());
            case final StringArgumentType string -> switch (string.getType()) {
                case SINGLE_WORD -> StringArgument.word();
                case QUOTABLE_PHRASE -> StringArgument.string();
                case GREEDY_PHRASE -> StringArgument.greedyString();
            };
            // A type the client has no codec for
            default -> StringArgument.string();
        };
    }
}
