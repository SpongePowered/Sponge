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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.leangen.geantyref.TypeToken;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.Flag;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;
import org.spongepowered.common.test.gametest.SpongeGameTests;

/**
 * Regression tests for <a href="https://github.com/SpongePowered/Sponge/issues/3281">GH-3281</a>
 * and <a href="https://github.com/SpongePowered/Sponge/issues/4335">GH-4335</a>: a command that
 * ends at a flag which has no associated parameter must be a complete command, while a flag with
 * a required associated parameter must not become complete on the flag alone.
 *
 * <p>The commands are registered through {@link RegisterCommandEvent}, as a plugin would register
 * them, so the tests exercise the same command tree construction path as a real command.</p>
 */
@GameTestGroup("command")
public final class FlagAloneGameTests {

    private static final String PLAIN_FLAG = "spongefix-flag-plain";
    private static final String PLAIN_FLAG_REQUIRED = "spongefix-flag-required";
    private static final String FLAG_PARAM_REQUIRED = "spongefix-flagparam-required";
    private static final String PLAIN_FLAG_OPTIONAL = "spongefix-flag-optional";
    private static final String FLAG_PARAM_OPTIONAL = "spongefix-flagparam-optional";

    public FlagAloneGameTests() {
        // Gametest classes are created before the server starts, which fires the event.
        SpongeGameTests.listen(new TypeToken<RegisterCommandEvent<Command.Parameterized>>() {}, Order.DEFAULT, event -> {
            event.register(Launch.instance().commonPlugin(), FlagAloneGameTests.plainFlagOnly(), PLAIN_FLAG);
            event.register(Launch.instance().commonPlugin(), FlagAloneGameTests.plainFlagRequiredParameter(), PLAIN_FLAG_REQUIRED);
            event.register(Launch.instance().commonPlugin(), FlagAloneGameTests.flagWithRequiredParameter(), FLAG_PARAM_REQUIRED);
            event.register(Launch.instance().commonPlugin(), FlagAloneGameTests.plainFlagOptionalParameter(), PLAIN_FLAG_OPTIONAL);
            event.register(Launch.instance().commonPlugin(), FlagAloneGameTests.flagWithOptionalParameter(), FLAG_PARAM_OPTIONAL);
        });
    }

    @GameTest(maxTicks = 40)
    public void plainFlagAloneCompletesCommand(final GameTestHelper helper) {
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.PLAIN_FLAG + " -a", true);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void plainFlagWithRequiredParameter(final GameTestHelper helper) {
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.PLAIN_FLAG_REQUIRED, false);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.PLAIN_FLAG_REQUIRED + " -a", false);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.PLAIN_FLAG_REQUIRED + " -a value", true);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void flagWithRequiredParameter(final GameTestHelper helper) {
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.FLAG_PARAM_REQUIRED + " -b", false);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.FLAG_PARAM_REQUIRED + " -b bvalue", false);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.FLAG_PARAM_REQUIRED + " -b bvalue value", true);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void plainFlagWithOptionalParameter(final GameTestHelper helper) {
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.PLAIN_FLAG_OPTIONAL, true);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.PLAIN_FLAG_OPTIONAL + " -a", true);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.PLAIN_FLAG_OPTIONAL + " -a value", true);
        helper.succeed();
    }

    @GameTest(maxTicks = 40)
    public void flagWithOptionalParameter(final GameTestHelper helper) {
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.FLAG_PARAM_OPTIONAL, true);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.FLAG_PARAM_OPTIONAL + " -b", true);
        FlagAloneGameTests.assertComplete(helper, FlagAloneGameTests.FLAG_PARAM_OPTIONAL + " -b bvalue", true);
        helper.succeed();
    }

    private static void assertComplete(final GameTestHelper helper, final String input, final boolean expected) {
        final MinecraftServer server = helper.getLevel().getServer();
        final boolean complete;
        try {
            final ParseResults<CommandSourceStack> results = server.getCommands().getDispatcher()
                .parse(input, server.createCommandSourceStack());
            complete = results.getContext().getCommand() != null;
        } catch (final CommandSyntaxException e) {
            complete = false;
        }
        helper.assertTrue(complete == expected,
            "expected '" + input + "' to be " + (expected ? "a complete command" : "incomplete"));
    }

    private static Command.Parameterized plainFlagOnly() {
        return Command.builder()
            .addFlag(Flag.of("a"))
            .terminal(true)
            .executor(context -> CommandResult.success())
            .build();
    }

    private static Command.Parameterized plainFlagRequiredParameter() {
        return Command.builder()
            .addFlag(Flag.of("a"))
            .addParameter(Parameter.string().key("value").build())
            .executor(context -> CommandResult.success())
            .build();
    }

    private static Command.Parameterized flagWithRequiredParameter() {
        return Command.builder()
            .addFlag(Flag.of(Parameter.string().key("flagValue").build(), "b"))
            .addParameter(Parameter.string().key("value").build())
            .executor(context -> CommandResult.success())
            .build();
    }

    private static Command.Parameterized plainFlagOptionalParameter() {
        return Command.builder()
            .addFlag(Flag.of("a"))
            .addParameter(Parameter.string().key("value").optional().build())
            .executor(context -> CommandResult.success())
            .build();
    }

    private static Command.Parameterized flagWithOptionalParameter() {
        return Command.builder()
            .addFlag(Flag.of(Parameter.string().key("flagValue").optional().build(), "b"))
            .addParameter(Parameter.string().key("value").optional().build())
            .executor(context -> CommandResult.success())
            .build();
    }
}
