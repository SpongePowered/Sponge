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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.common.command.brigadier.SpongeParameterTranslator;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

import java.util.Collection;
import java.util.List;

/**
 * A command that ends at a plain flag (a flag with no associated parameter) must be
 * a complete command, see <a href="https://github.com/SpongePowered/Sponge/issues/3281">GH-3281</a>.
 */
@GameTestGroup("command")
public final class FlagAloneGameTests {

    @GameTest(maxTicks = 40)
    public void flagAloneCompletesCommand(final GameTestHelper helper) {
        final Command.Parameterized command = Command.builder()
            .addFlag(Flag.of("a"))
            .executor(context -> CommandResult.success())
            .build();
        final Collection<LiteralCommandNode<CommandSourceStack>> nodes =
            SpongeParameterTranslator.INSTANCE.createCommandTree(command, List.of("flagtest"));
        final LiteralCommandNode<CommandSourceStack> root = nodes.iterator().next();
        final CommandNode<CommandSourceStack> flag = root.getChild("-a");
        helper.assertTrue(flag != null, "the flag node -a was not created");
        helper.assertTrue(flag.getCommand() != null,
            "the flag node has no command, so '/flagtest -a' cannot be parsed as a complete command");
        final CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.getRoot().addChild(root);
        final ParseResults<CommandSourceStack> results = dispatcher.parse(
            new StringReader("flagtest -a"), helper.getLevel().getServer().createCommandSourceStack());
        helper.assertTrue(results.getExceptions().isEmpty(),
            "parsing '/flagtest -a' produced exceptions: " + results.getExceptions());
        helper.succeed();
    }
}
