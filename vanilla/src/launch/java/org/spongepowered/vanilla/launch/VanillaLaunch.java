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
package org.spongepowered.vanilla.launch;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import org.spongepowered.common.inject.SpongeCommonModule;
import org.spongepowered.common.inject.SpongeModule;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.common.launch.plugin.DummyPluginContainer;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.locator.JVMPluginResourceLocatorService;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.util.PluginMetadataHelper;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;
import org.spongepowered.vanilla.launch.inject.SpongeVanillaModule;
import org.spongepowered.vanilla.launch.plugin.VanillaPluginManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

public abstract class VanillaLaunch extends Launch {

    private final Stage injectionStage;
    private final VanillaPluginManager pluginManager;
    private PluginContainer vanillaPlugin;

    protected VanillaLaunch(final VanillaPluginPlatform pluginPlatform, final Stage injectionStage) {
        super(pluginPlatform);
        this.injectionStage = injectionStage;
        this.pluginManager = new VanillaPluginManager();
    }

    @Override
    public final Stage injectionStage() {
        return this.injectionStage;
    }

    @Override
    public final void performLifecycle() {
        this.pluginManager().loadPlugins(this.pluginPlatform());
    }

    @Override
    public final PluginContainer platformPlugin() {
        if (this.vanillaPlugin == null) {
            this.vanillaPlugin = this.pluginManager().plugin("spongevanilla").orElse(null);

            if (this.vanillaPlugin == null) {
                throw new RuntimeException("Could not find the plugin representing SpongeVanilla, this is a serious issue!");
            }
        }

        return this.vanillaPlugin;
    }

    @Override
    public final VanillaPluginPlatform pluginPlatform() {
        return (VanillaPluginPlatform) this.pluginPlatform;
    }

    @Override
    public final VanillaPluginManager pluginManager() {
        return this.pluginManager;
    }

    @Override
    public Injector createInjector() {
        final List<Module> modules = Lists.newArrayList(
            new SpongeModule(),
            new SpongeCommonModule(),
            new SpongeVanillaModule()
        );
        return Guice.createInjector(this.injectionStage(), modules);
    }

    protected final void launchPlatform(final String[] args) {
        this.createPlatformPlugins();
        this.logger().info("Loading Sponge, please wait...");
        this.performBootstrap(args);
    }

    protected abstract void performBootstrap(final String[] args);

    protected final void createPlatformPlugins() {
        final Path gameDirectory = this.pluginPlatform.baseDirectory();

        try {
            // This is a bit nasty, but allows Sponge to detect builtin platform plugins when it's not the first entry on the classpath.
            final URL classUrl = VanillaLaunch.class.getResource("/" + VanillaLaunch.class.getName().replace('.', '/') + ".class");

            Collection<PluginMetadata> read = null;

            // In production, let's try to ensure we can find our descriptor even if we're not first on the classpath
            if (classUrl.getProtocol().equals("jar")) {
                // Extract the path of the underlying jar file, and parse it as a path to normalize it
                final String[] classUrlSplit = classUrl.getPath().split("!");
                final Path expectedFile = Paths.get(new URL(classUrlSplit[0]).toURI());

                // Then go through every possible resource
                final Enumeration<URL> manifests =
                        VanillaLaunch.class.getClassLoader().getResources("/META-INF/" + JVMPluginResourceLocatorService.DEFAULT_METADATA_FILENAME);
                while (manifests.hasMoreElements()) {
                    final URL next = manifests.nextElement();
                    if (!next.getProtocol().equals("jar")) {
                        continue;
                    }

                    // And stop when the normalized jar in that resource matches the URL of the jar that loaded VanillaLaunch?
                    final String[] pathSplit = next.getPath().split("!");
                    if (pathSplit.length == 2) {
                        if (Paths.get(new URL(pathSplit[0]).toURI()).equals(expectedFile)) {
                            read = PluginMetadataHelper.builder().build().read(next.openStream());
                            break;
                        }
                    }
                }
            }

            if (read == null) { // other measures failed, fall back to directly querying the classpath
                read = PluginMetadataHelper.builder().build().read(VanillaLaunch.class.getResourceAsStream(
                        "/META-INF/" + JVMPluginResourceLocatorService.DEFAULT_METADATA_FILENAME));
            }
            if (read == null) {
                throw new RuntimeException("Could not determine location for implementation metadata!");
            }

            for (final PluginMetadata metadata : read) {
                this.pluginManager().addDummyPlugin(new DummyPluginContainer(metadata, this.logger(), this));
            }
        } catch (final IOException | URISyntaxException e) {
            throw new RuntimeException("Could not load metadata information for the implementation! This should be impossible!");
        }
    }
}
