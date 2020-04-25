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
package org.spongepowered.common.launch;

import static org.spongepowered.common.SpongeImpl.ECOSYSTEM_ID;

import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.VersionNumber;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.launch.transformer.SpongeSuperclassRegistry;
import org.spongepowered.common.mixin.handler.TerminateVM;
import org.spongepowered.common.util.PathTokens;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpongeLaunch {

    private SpongeLaunch() {
    }

    public static final String SUPERCLASS_TRANSFORMER = "org.spongepowered.common.launch.transformer.SpongeSuperclassTransformer";

    private static Path gameDir;
    private static Path pluginsDir;
    private static Path additionalPluginsDir;
    private static Path configDir;
    private static Path spongeConfigDir;
    private static Path pluginConfigDir;

    public static Path getGameDir() {
        return gameDir;
    }

    public static Path getPluginsDir() {
        return pluginsDir;
    }

    public static Path getAdditionalPluginsDir() {
        if (additionalPluginsDir == null) {
            additionalPluginsDir = Paths.get(PathTokens.replace(SpongeImpl.getGlobalConfigAdapter().getConfig().getGeneral().pluginsDir()));
        }

        return additionalPluginsDir;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static Path getPluginConfigDir() {
        if (pluginConfigDir == null) {
            pluginConfigDir = Paths.get(PathTokens.replace(SpongeImpl.getGlobalConfigAdapter().getConfig().getGeneral().configDir()));
        }
        return pluginConfigDir;
    }

    public static Path getSpongeConfigDir() {
        return spongeConfigDir;
    }

    public static void initPaths(File gameDirIn) {
        gameDir = gameDirIn.toPath();
        pluginsDir = gameDir.resolve("mods");
        configDir = gameDir.resolve("config");
        spongeConfigDir = configDir.resolve(ECOSYSTEM_ID);
    }

    public static void addJreExtensionsToClassPath() {
        // Make sure JRE extensions are loaded using the system class loader
        Launch.classLoader.addClassLoaderExclusion("jdk.");

        /*
         * By default Launchwrapper inherits the class path from the system class loader.
         * However, JRE extensions (e.g. Nashorn in the jre/lib/ext directory) are not part
         * of the class path of the system class loader.
         * Instead, they're loaded using a parent class loader (Launcher.ExtClassLoader).
         * Currently, Launchwrapper does not fall back to the parent class loader if it's
         * unable to find a class on its class path. To make the JRE extensions usable for
         * plugins we manually add the URLs from the ExtClassLoader to Launchwrapper's
         * class path.
        */
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader == null) {
            return;
        }

        classLoader = classLoader.getParent(); // Launcher.ExtClassLoader
        if (classLoader instanceof URLClassLoader) {
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                Launch.classLoader.addURL(url);
            }
        }
    }

    public static void setupMixinEnvironment() {
        MixinBootstrap.init();

        final VersionNumber environment = VersionNumber.parse(MixinEnvironment.getCurrentEnvironment().getVersion());
        final VersionNumber required = VersionNumber.parse("0.8");
        if (required.compareTo(environment) > 0) {
            new PrettyPrinter(80)
                    .add()
                    .add("Oh dear. Another mod loaded an older version of Mixin that Sponge cannot use!")
                    .add()
                    .hr('-')
                    .add()
                    .add("One of your mods that loaded before Sponge bundles an older version of Mixin.")
                    .add("This prevents Sponge from loading its own version, which is newer than the")
                    .add("currently loaded version.")
                    .add()
                    .add("Rename your Sponge jar file so that the filename starts with \"__aaa\". This")
                    .add("will cause Sponge and its bundled Mixin version to load first.")
                    .add()
                    .hr('-')
                    .add()
                    .add("The minimum version of Mixin Sponge requires is: %s", required.toString())
                    .add("but the currently loaded version is:             %s", environment.toString())
                    .add()
                    .log(SpongeImpl.getLogger(), Level.FATAL);
            TerminateVM.terminate("net.minecraftforge.fml", -1);
        }

        // Register common mixin configurations
        Mixins.addConfiguration("mixins.common.api.json");
        Mixins.addConfiguration("mixins.common.bungeecord.json");
        Mixins.addConfiguration("mixins.common.concurrentchecks.json");
        Mixins.addConfiguration("mixins.common.core.json");
        Mixins.addConfiguration("mixins.common.entityactivation.json");
        Mixins.addConfiguration("mixins.common.entitycollisions.json");
        Mixins.addConfiguration("mixins.common.exploit.json");
        Mixins.addConfiguration("mixins.common.movementchecks.json");
        Mixins.addConfiguration("mixins.common.multi-world-command.json");
        Mixins.addConfiguration("mixins.common.optimization.json");
        Mixins.addConfiguration("mixins.common.realtime.json");
        Mixins.addConfiguration("mixins.common.tileentityactivation.json");
        Mixins.addConfiguration("mixins.common.tracking.json");
        Mixins.addConfiguration("mixins.common.vanilla-command.json");
    }

    public static void setupSuperClassTransformer() {
        SpongeSuperclassRegistry.registerSuperclassModification("org.spongepowered.api.entity.ai.task.AbstractAITask",
                "org.spongepowered.common.entity.ai.SpongeEntityAICommonSuperclass");
        SpongeSuperclassRegistry.registerSuperclassModification("org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSource",
                "org.spongepowered.common.event.damage.SpongeCommonDamageSource");
        SpongeSuperclassRegistry.registerSuperclassModification(
                "org.spongepowered.api.event.cause.entity.damage.source.common.AbstractEntityDamageSource",
                "org.spongepowered.common.event.damage.SpongeCommonEntityDamageSource");
        SpongeSuperclassRegistry.registerSuperclassModification(
                "org.spongepowered.api.event.cause.entity.damage.source.common.AbstractIndirectEntityDamageSource",
                "org.spongepowered.common.event.damage.SpongeCommonIndirectEntityDamageSource");
    }

}
