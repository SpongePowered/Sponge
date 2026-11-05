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
package org.spongepowered.common.plugin.discovery.spi;

import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.discovery.PluginResourceLocator;
import org.spongepowered.plugin.discovery.UnknownResourceStrategy;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/**
 * WARNING: This class may exist in multiple classloaders.
 */
public class DummyResourceLocator implements PluginResourceLocator {
    public static final Path UNIONED_JAR = Path.of("dummy", "meta.jar");
    public static final List<Path> UNION = List.of(Path.of("dummy", "classes"), DummyResourceLocator.UNIONED_JAR);

    @Override
    public Collection<Result> locatePluginResources(final Environment environment) {
        // Union plus one of its jars alone, as in a gametest run
        return List.of(
            new Result(new DummyPluginResource(), UnknownResourceStrategy.IGNORE),
            new Result(new DummyPluginResource(List.of(DummyResourceLocator.UNIONED_JAR)), UnknownResourceStrategy.IGNORE),
            new Result(new DummyPluginResource(DummyResourceLocator.UNION), UnknownResourceStrategy.IGNORE)
        );
    }

    @Override
    public String name() {
        return "dummy_locator";
    }
}
