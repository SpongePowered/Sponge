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
package org.spongepowered.common.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to mark tests that ensure that a specific
 * Sponge issue has not regressed. These will usually, but not always,
 * be written using Mctester.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
public @interface RegressionTest {

    /**
     * A fully-qualified link to the GitHub issue being tested by this test.
     *
     * <p>Example: 'https://github.com/SpongePowered/SpongeCommon/issues/1945'</p>
     *
     * @return The URL of the issue
     */
    String ghIssue();

    /**
     * A fully qualified link to a commit related to the issue where possible
     * regressions took place as a byproduct to said commits.
     *
     * <p>Example: 'https://github.com/SpongePowered/SpongeCommon/commit/2e394dc71f03026f937ad332eab57020eb55e536'</p>
     *
     * @return The URL of the related commits
     */
    String[] relatedCommits() default {};

    /**
     * Any additional comments about the regresion issue that can be made, maybe outside
     * the javadoc comment of the overall test.
     *
     * @return The comment about the regression to avoid
     */
    String comment() default "";

}
