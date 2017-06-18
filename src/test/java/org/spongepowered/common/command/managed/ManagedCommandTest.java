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
package org.spongepowered.common.command.managed;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.common.command.dispatcher.SpongeDispatcher;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

/**
 * Test for basic command creation.
 */
@RunWith(LaunchWrapperTestRunner.class)
public class ManagedCommandTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public void testNoArgsFunctional() throws CommandException {
        Command cmd = new SpongeCommandBuilder()
                .setExecutor((src, args) -> CommandResult.empty())
                .build();

        final SpongeDispatcher dispatcher = new SpongeDispatcher();
        dispatcher.register(cmd, "cmd");
        CommandSource source = Mockito.mock(CommandSource.class);
        dispatcher.process(Cause.builder().append(source).build(EventContext.empty()),  "cmd");
    }

    @Test
    public void testExecutorRequired() {
        this.expected.expect(IllegalStateException.class);
        this.expected.expectMessage("The command must have an executor or at least one child command.");
        new SpongeCommandBuilder().build();

    }
}
