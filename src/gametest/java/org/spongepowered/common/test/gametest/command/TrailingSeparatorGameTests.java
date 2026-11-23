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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTestHelper;
import org.spongepowered.common.test.gametest.GameTest;
import org.spongepowered.common.test.gametest.GameTestGroup;

/**
 * Checks that parsing keeps the trailing argument separator. Sponge used to
 * rebuild the reader from the split command and arguments, which dropped the
 * separator of inputs such as {@code "help "} and made the console unable to
 * complete (SpongePowered/Sponge#4235).
 */
@GameTestGroup("command")
public final class TrailingSeparatorGameTests {

    @GameTest(maxTicks = 20)
    public void parseKeepsTrailingSeparator(final GameTestHelper helper) {
        final CommandSourceStack source = helper.getLevel().getServer().createCommandSourceStack();
        final ParseResults<CommandSourceStack> results =
                helper.getLevel().getServer().getCommands().getDispatcher().parse("help ", source);

        helper.assertTrue("help ".equals(results.getReader().getString()),
                "Parsing dropped the trailing argument separator: expected [help ], got [" + results.getReader().getString() + "]");
        helper.succeed();
    }
}
