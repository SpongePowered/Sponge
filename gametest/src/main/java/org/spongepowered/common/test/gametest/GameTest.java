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

import org.junit.platform.commons.annotation.Testable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A gametest: an instance method taking a {@link net.minecraft.gametest.framework.GameTestHelper},
 * in a class listed with {@link SpongeGameTests}. Its id is
 * {@code sponge:<group>/<method_name>}, see {@link GameTestGroup}.
 *
 * <p>{@link Testable} gives the method a run icon in IDEs, which run it through
 * {@link SpongeGameTestEngine}.</p>
 */
@Testable
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GameTest {

    /** A template at {@code data/<namespace>/structure/<path>.nbt} */
    String structure() default "minecraft:empty";

    int maxTicks() default 20;

    int setupTicks() default 0;

    /** False for a known issue: it still runs and is reported, but does not fail the run */
    boolean required() default true;

    /**
     * Also fails if anything in the structure changes, or an entity spawns in it, during the 20 ticks after it passes:
     * for a test whose point is that nothing is left pending, such as a cancelled push leaving no queued block event.
     * Only checked when run through JUnit.
     */
    boolean settle() default false;

    /**
     * Leaves out the barrier ceiling vanilla puts over the structure while the test runs. Removing barriers updates
     * their neighbors, so a test that leaves a block out of step on purpose, such as a piston whose push was
     * cancelled, needs this to keep it that way once the test is over.
     */
    boolean skyAccess() default false;

    /**
     * Needs the Minecraft client in the same JVM, as a {@code gameTestClient} run has: a screen or widget test.
     * Skipped by the dedicated and game test servers, so it never runs through {@code gameTest} or JUnit.
     */
    boolean client() default false;
}
