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
package org.spongepowered.bootstrap.neoforge;

import cpw.mods.cl.JarModuleFinder;
import cpw.mods.cl.ModuleClassLoader;
import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.bootstrap.Bootstrap;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public class NeoForgeBootstrap extends Bootstrap<SecureJar> {
    private final String[] args;

    public NeoForgeBootstrap(final String[] args) {
        this.args = args;
    }

    @Override
    public String name() {
        return "NeoForge";
    }

    @Override
    protected SecureJar createJar(final Path[] paths) {
        return SecureJar.from(paths);
    }

    @Override
    protected String getModuleName(final SecureJar jar) {
        return jar.name();
    }

    @Override
    protected ModuleFinder createModuleFinder(final Collection<SecureJar> jars) {
        return JarModuleFinder.of(jars.toArray(SecureJar[]::new));
    }

    @Override
    protected ClassLoader createApplicationClassLoader(final Configuration config, final List<ModuleLayer> parentLayers, final ClassLoader parentLoader) {
        return new ModuleClassLoader("APP-BOOTSTRAP", config, parentLayers, parentLoader);
    }

    @Override
    protected void runApplication(final ModuleLayer layer) throws Exception {
        final Class<?> appClass = layer.findModule("cpw.mods.modlauncher").get().getClassLoader().loadClass("cpw.mods.modlauncher.Launcher");
        appClass.getDeclaredMethod("main", String[].class).invoke(null, (Object) this.args);
    }

    public static void main(final String[] args) throws Exception {
        new NeoForgeBootstrap(args).devBoot(true);
    }
}
