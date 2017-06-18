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

import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tests for child commands.
 */
@RunWith(LaunchWrapperTestRunner.class)
public class ChildCommandsTest {

    @Test
    public void testSimpleChildCommand() throws CommandException {
        final AtomicBoolean childExecuted = new AtomicBoolean();
        final Command spec = Command.builder()
                .children(ImmutableMap.<List<String>, Command>of(ImmutableList.of("child"), Command.builder()
                        .setExecutor((cause, src, args) -> {
                            childExecuted.set(true);
                            return CommandResult.builder().successCount(1).build();
                        }).build())).build();
        final SpongeDispatcher execute = new SpongeDispatcher();
        execute.register(spec, "parent");
        CommandSource source = Mockito.mock(CommandSource.class);
        execute.process(Cause.builder().append(source).build(EventContext.empty()), "parent child");

        assertTrue(childExecuted.get());
    }
}
