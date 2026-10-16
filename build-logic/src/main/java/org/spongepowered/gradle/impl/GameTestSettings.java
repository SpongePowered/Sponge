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
package org.spongepowered.gradle.impl;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * How a platform boots its game test server, for {@link SpongeImplementationExtension#gameTests}.
 * Each platform reads these from its own run configuration, which build-logic can't see.
 */
public abstract class GameTestSettings {

    private final List<Object> dependencies = new ArrayList<>();

    /** Names the root project's task that runs the common gametests on this platform: {@code gameTest<name>} */
    public abstract Property<String> getPlatformName();

    /** The game test server run's program arguments, handed to the platform test boot */
    public abstract ListProperty<String> getBootArguments();

    /** The game test server run's JVM arguments */
    public abstract ListProperty<String> getJvmArguments();

    /** The class loader the game layer is defined by, which JaCoCo's agent must leave to Sponge's own instrumentation */
    public abstract Property<String> getGameClassLoader();

    public abstract MapProperty<String, String> getSystemProperties();

    public void systemProperty(final String name, final String value) {
        this.getSystemProperties().put(name, value);
    }

    /** Tasks the game test server run needs before it can boot, such as preparing its arguments */
    public void dependsOn(final Object... tasks) {
        this.dependencies.addAll(Arrays.asList(tasks));
    }

    List<Object> dependencies() {
        return this.dependencies;
    }
}
