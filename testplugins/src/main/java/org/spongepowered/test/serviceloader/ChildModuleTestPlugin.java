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
package org.spongepowered.test.serviceloader;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("childmoduletestplugin")
public final class ChildModuleTestPlugin implements ChildModuleTestPluginService, CombinableTestPluginService {

    private final ChildModuleTestPluginService.External external;

    @Inject
    public ChildModuleTestPlugin(final ChildModuleTestPluginService.External external) {
        this.external = external;
    }

    @Override
    public External external() {
        return this.external;
    }

    private static final class ExternalImpl implements ChildModuleTestPluginService.External {
    }

    public static final class Module extends AbstractModule {

        @Override
        protected void configure() {
            Multibinder.newSetBinder(this.binder(), CombinableTestPluginService.class).addBinding().to(ChildModuleTestPlugin.class);
            this.bind(ChildModuleTestPluginService.class).to(ChildModuleTestPlugin.class);
            this.bind(ChildModuleTestPluginService.External.class).toInstance(new ExternalImpl());
        }
    }
}
