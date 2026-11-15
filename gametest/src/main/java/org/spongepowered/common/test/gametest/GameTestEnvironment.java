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

import org.spongepowered.api.world.server.ServerWorld;

/**
 * What the world needs to be like while a group's tests run: set up before the first of them starts and torn down
 * once the last is done, so the tests can take it as given. Named by {@link GameTestGroup#environment()}, any number
 * of groups may share one, and it needs a no-argument constructor.
 *
 * <p>How it is applied depends on the Minecraft version, never on the tests: a {@link GameRulesEnvironment} is
 * vanilla's own game rules environment where vanilla has test environments, and any other is run around the tests'
 * batch.</p>
 */
public interface GameTestEnvironment {

    void setup(ServerWorld world);

    default void teardown(final ServerWorld world) {
    }

    /** Leaves the world as it is: the environment of a group that names none */
    final class None implements GameTestEnvironment {

        @Override
        public void setup(final ServerWorld world) {
        }
    }
}
