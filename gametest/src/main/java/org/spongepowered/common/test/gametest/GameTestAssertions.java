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
package org.spongepowered.common.test.gametest;

import net.minecraft.gametest.framework.GameTestHelper;

/**
 * {@link GameTestHelper}'s assertions with plain text messages. Which type vanilla takes for a message differs between
 * Minecraft versions, so a test that asserts through these is written the same way on every version.
 */
public final class GameTestAssertions {

    public static void assertTrue(final GameTestHelper helper, final boolean condition, final String message) {
        helper.assertTrue(condition, message);
    }

    public static void assertFalse(final GameTestHelper helper, final boolean condition, final String message) {
        helper.assertFalse(condition, message);
    }

    /** Fails unless {@code actual} equals {@code expected}; {@code name} says what the value is */
    public static <N> void assertValueEqual(final GameTestHelper helper, final N actual, final N expected, final String name) {
        helper.assertValueEqual(actual, expected, name);
    }

    private GameTestAssertions() {
    }
}
