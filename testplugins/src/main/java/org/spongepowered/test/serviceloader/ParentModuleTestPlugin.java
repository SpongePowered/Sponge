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
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.Set;

@Plugin("parentmoduletestplugin")
public final class ParentModuleTestPlugin {

    @Inject
    public ParentModuleTestPlugin(final Logger logger, final ChildModuleTestPlugin childModulePlugin, final SecondChildModuleTestPlugin secondChildModulePlugin,
                                  final ChildModuleTestPluginService service, final ChildModuleTestPluginService.External external,
                                  final Set<CombinableTestPluginService> combinablePluginServices) {
        if (Sponge.pluginManager().plugin("childmoduletestplugin").get().instance() != childModulePlugin) {
            logger.error("Mismatched instance of the plugin childmoduletestplugin");
        }

        if (Sponge.pluginManager().plugin("secondchildmoduletestplugin").get().instance() != secondChildModulePlugin) {
            logger.error("Mismatched instance of the plugin secondChildModuleTestPlugin");
        }

        if (childModulePlugin != service) {
            logger.error("Mismatched instance of service component from plugin childmoduletestplugin");
        }

        if (childModulePlugin.external() != external) {
            logger.error("Mismatched instance of the external component from plugin childmoduletestplugin");
        }

        if (combinablePluginServices.size() != 2 || !combinablePluginServices.contains(childModulePlugin) || !combinablePluginServices.contains(secondChildModulePlugin)) {
            logger.error("Mismatched content of combinablePluginServices");
        }
    }

    public static final class Module extends AbstractModule {

        @Override
        protected void configure() {
            Multibinder.newSetBinder(this.binder(), CombinableTestPluginService.class);
        }
    }
}
