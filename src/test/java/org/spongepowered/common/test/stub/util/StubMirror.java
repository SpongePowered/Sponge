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
package org.spongepowered.common.test.stub.util;

import org.spongepowered.api.util.mirror.Mirror;
import org.spongepowered.common.test.stub.StubKey;
import org.spongepowered.common.test.stub.registry.StubbedRegistry;

public enum StubMirror implements Mirror {
    NONE,
    LEFT_RIGHT,
    FRONT_BACK;

    public static void registerDefaults(final StubbedRegistry<Mirror> registry) {
        registry.register(new StubKey("sponge", "none"), StubMirror.NONE);
        registry.register(new StubKey("sponge", "left_right"), StubMirror.LEFT_RIGHT);
        registry.register(new StubKey("sponge", "front_back"), StubMirror.FRONT_BACK);
    }

}
