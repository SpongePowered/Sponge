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
package org.spongepowered.common.test.gametest.command;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.common.command.brigadier.dispatcher.SpongeCommandDispatcher;
import org.spongepowered.common.command.manager.SpongeCommandManager;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.List;

/**
 * Exercises the suggestion-mode parse path used by the NeoForge console
 * completion mixin (SpongePowered/Sponge#4140).
 */
@GameTestGroup("command")
public final class SuggestionCompletionGameTests {

    @GameTest(maxTicks = 20)
    public void suggestionParseCompletesAfterCommandName(final GameTestHelper helper) {
        final CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
        final SpongeCommandDispatcher dispatcher =
                SpongeCommandManager.get(helper.getLevel().getServer()).getDispatcher();

        final ParseResults<CommandSourceStack> parse = dispatcher.parse("time ", source, true);
        final List<Suggestion> suggestions = dispatcher.getCompletionSuggestions(parse).join().getList();

        helper.assertTrue(suggestions.stream().anyMatch(suggestion -> "set".equals(suggestion.getText())),
                "Expected suggestions for [time ] from the suggestion parse, got " + suggestions);
        helper.succeed();
    }

    @GameTest(maxTicks = 20)
    public void suggestionParseCompletesPartialArgument(final GameTestHelper helper) {
        final CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
        final SpongeCommandDispatcher dispatcher =
                SpongeCommandManager.get(helper.getLevel().getServer()).getDispatcher();

        final ParseResults<CommandSourceStack> parse = dispatcher.parse("time s", source, true);
        final List<Suggestion> suggestions = dispatcher.getCompletionSuggestions(parse).join().getList();

        helper.assertTrue(suggestions.stream().anyMatch(suggestion -> "set".equals(suggestion.getText())),
                "Expected suggestion [set] for [time s] from the suggestion parse, got " + suggestions);
        helper.succeed();
    }
}