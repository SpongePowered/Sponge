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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.List;

/**
 * Regression tests for console completion. The console completer prepends a
 * synthetic leading '/', skips it before parsing and calls the no-argument
 * completion overload; the cursor must still match the command text
 * (SpongePowered/Sponge#4140).
 */
@GameTestGroup("command")
public final class ConsoleCompletionGameTests {

    @GameTest(maxTicks = 20)
    public void consoleCompletesAfterCommandName(final GameTestHelper helper) {
        final CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
        final CommandDispatcher<CommandSourceStack> dispatcher =
                helper.getLevel().getServer().getCommands().getDispatcher();

        // Console shape: leading '/' is skipped before parse, so the cursor is 1.
        final StringReader reader = new StringReader("/time ");
        reader.setCursor(1);

        final ParseResults<CommandSourceStack> parse = dispatcher.parse(reader, source);
        final List<Suggestion> suggestions = dispatcher.getCompletionSuggestions(parse).join().getList();

        helper.assertTrue(suggestions.stream().anyMatch(suggestion -> "set".equals(suggestion.getText())),
                "Expected suggestions for [time ], got " + suggestions);
        helper.succeed();
    }

    @GameTest(maxTicks = 20)
    public void consoleCompletesPartialArgument(final GameTestHelper helper) {
        final CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
        final CommandDispatcher<CommandSourceStack> dispatcher =
                helper.getLevel().getServer().getCommands().getDispatcher();

        final StringReader reader = new StringReader("/time s");
        reader.setCursor(1);

        final ParseResults<CommandSourceStack> parse = dispatcher.parse(reader, source);
        final List<Suggestion> suggestions = dispatcher.getCompletionSuggestions(parse).join().getList();

        helper.assertTrue(suggestions.stream().anyMatch(suggestion -> "set".equals(suggestion.getText())),
                "Expected suggestion [set] for [time s], got " + suggestions);
        helper.succeed();
    }

    @GameTest(maxTicks = 20)
    public void normalCompletionIsUnaffected(final GameTestHelper helper) {
        final CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
        final CommandDispatcher<CommandSourceStack> dispatcher =
                helper.getLevel().getServer().getCommands().getDispatcher();

        // No synthetic slash: cursor 0, the non-console path.
        final ParseResults<CommandSourceStack> parse = dispatcher.parse(new StringReader("time s"), source);
        final List<Suggestion> suggestions = dispatcher.getCompletionSuggestions(parse).join().getList();

        helper.assertTrue(suggestions.stream().anyMatch(suggestion -> "set".equals(suggestion.getText())),
                "Expected suggestion [set] for [time s] without a slash, got " + suggestions);
        helper.succeed();
    }
}
