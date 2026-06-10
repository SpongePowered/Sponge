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
package org.spongepowered.vanilla.applaunch.plugin.discovery;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.securemodules.SecureModuleClassLoader;
import net.minecraftforge.securemodules.SecureModuleFinder;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.common.applaunch.plugin.discovery.SpongeJVMPluginResource;
import org.spongepowered.plugin.Environment;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class VanillaPluginDiscovery extends PluginDiscovery {
    private ModuleLayer currentLayer;
    private ClassLoader currentLoader;

    public VanillaPluginDiscovery(Environment environment) {
        super(environment);
        this.currentLayer = getClass().getModule().getLayer();
        this.currentLoader = getClass().getClassLoader();
    }

    @Override
    protected <T> ServiceLoader<T> newServiceLoader(final Class<T> serviceClass) {
        return ServiceLoader.load(this.currentLayer, serviceClass);
    }

    @Override
    protected void appendDiscoveryServices(final List<SpongeJVMPluginResource> resources, final int batch) {
        final ModuleLayer parentLayer = this.currentLayer;
        final ClassLoader parentLoader = this.currentLoader;

        final List<SecureJar> jars = new ArrayList<>();
        final List<String> moduleNames = new ArrayList<>();

        for (final SpongeJVMPluginResource resource : resources) {
            final SecureJar jar = ((SecureJarPluginResource) resource).jar();
            jars.add(jar);
            moduleNames.add(jar.name());
        }

        final Configuration config = parentLayer.configuration().resolveAndBind(SecureModuleFinder.of(jars), ModuleFinder.of(), moduleNames);
        final ClassLoader loader = new SecureModuleClassLoader("SPONGE-DISCOVERY-BATCH-" + batch, null, config, List.of(parentLayer), List.of(parentLoader));

        this.currentLayer = this.currentLayer.defineModulesWithOneLoader(config, loader);
        this.currentLoader = loader;

        this.environment.logger().debug("Built new service layer {} on top of {}.", loader.getName(), parentLoader.getName());
    }
}
