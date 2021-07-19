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
package org.spongepowered.vanilla.launch.event;

import org.apache.logging.log4j.Logger;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.jvm.JVMPluginContainer;
import org.spongepowered.plugin.jvm.locator.JVMPluginResource;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * A plugin container that provides access to a plugin's Lookup instance.
 */
public class ModuleAwareJVMPluginContainer extends JVMPluginContainer {

    private MethodHandles.Lookup lookup;

    public ModuleAwareJVMPluginContainer(final PluginCandidate<JVMPluginResource> candidate) {
        super(candidate);
    }

    public ModuleAwareJVMPluginContainer(final PluginCandidate<JVMPluginResource> candidate, final Logger logger) {
        super(candidate, logger);
    }

    /**
     * {@return A lookup with access to internals of the module containing this plugin}
     */
    MethodHandles.Lookup lookup() {
        return this.lookup;
    }

    public void initializeLookup(final MethodHandles.Lookup lookup) {
        if (this.lookup != null) {
            throw new RuntimeException(String.format("Attempt made to set the lookup for plugin container '%s' twice!",
                this.metadata().id()));
        }
        this.lookup = Objects.requireNonNull(lookup);
    }
}
