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
package org.spongepowered.common.applaunch.config.core;

import ninja.leaping.configurate.transformation.ConfigurationTransformation;

public interface Config {

    ConfigurationTransformation EMPTY_VERSIONED = ConfigurationTransformation.versionedBuilder().build();

    /**
     * Path array builder. Will be removed in configurate 4.0.
     *
     * @param path path
     * @return path
     */
    static Object[] path(Object... path) {
        return path;
    }

    /**
     * Get a transformation that will be applied to this configuration on load.
     *
     * <p>We assume this is a versioned transformation. Configurate 4.0 will be
     * able to enforce this.</p>
     *
     * <p>When determining versions, round to the nearest 100 and add 100 for
     * every major release. This allows for future minor releases to add
     * configuration changes in previous major releases.</p>
     *
     * @return A transformation instance
     */
    default ConfigurationTransformation getTransformation() {
        return EMPTY_VERSIONED;
    }
}
