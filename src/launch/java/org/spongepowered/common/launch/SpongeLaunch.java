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


import org.apache.logging.log4j.Level;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.asm.util.VersionNumber;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;

public class SpongeLaunch {

    private SpongeLaunch() {
    }

    private static Path gameDir;
    private static Path pluginsDir;
    private static Path additionalPluginsDir;
    private static Path configDir;
    private static Path spongeConfigDir;
    private static Path pluginConfigDir;
    private static Supplier<InternalLaunchService> iLS;

    public static Path getGameDir() {
        return gameDir;
    }

    public static Path getPluginsDir() {
        return pluginsDir;
    }

    public static Path getAdditionalPluginsDir(final Supplier<String> ecosystemId, final Supplier<String> configuredDirectoryNameSupplier) {
        if (additionalPluginsDir == null) {
            additionalPluginsDir = Paths.get(PathTokens.replace(ecosystemId, configuredDirectoryNameSupplier.get()));
        }

        return additionalPluginsDir;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static Path getPluginConfigDir(final Supplier<String> ecosystemId, final Supplier<String> configuredDirectoryNameSupplier) {
        if (pluginConfigDir == null) {
            pluginConfigDir = Paths.get(PathTokens.replace(ecosystemId, configuredDirectoryNameSupplier.get()));
        }
        return pluginConfigDir;
    }

    public static Path getSpongeConfigDir() {
        return spongeConfigDir;
    }

    public static void initPaths(final Supplier<String> ecosystemId, final File gameDirIn) {
        gameDir = gameDirIn.toPath();
        pluginsDir = gameDir.resolve("mods");
        configDir = gameDir.resolve("config");
        spongeConfigDir = configDir.resolve(ecosystemId.get());
    }

    public static void initializeLaunchService(final InternalLaunchService launchService) {
        iLS = () -> launchService;
    }

    public static void setupMixinEnvironment() {
        MixinBootstrap.init();

        // Register common mixin configurations
        Mixins.addConfiguration("mixins.common.api.json");
        Mixins.addConfiguration("mixins.common.bungeecord.json");
        Mixins.addConfiguration("mixins.common.concurrentcheck.json");
        Mixins.addConfiguration("mixins.common.core.json");
        Mixins.addConfiguration("mixins.common.entityactivation.json");
        Mixins.addConfiguration("mixins.common.entitycollision.json");
        Mixins.addConfiguration("mixins.common.exploit.json");
        Mixins.addConfiguration("mixins.common.movementcheck.json");
        Mixins.addConfiguration("mixins.common.multi-world-command.json");
        Mixins.addConfiguration("mixins.common.optimization.json");
        Mixins.addConfiguration("mixins.common.realtime.json");
        Mixins.addConfiguration("mixins.common.tileentityactivation.json");
        Mixins.addConfiguration("mixins.common.tracker.json");
        Mixins.addConfiguration("mixins.common.vanilla-command.json");
        final VersionNumber environment = VersionNumber.parse(MixinEnvironment.getCurrentEnvironment().getVersion());
        final VersionNumber required = VersionNumber.parse("0.7.11");
        if (required.compareTo(environment) > 0) {
            final InternalLaunchService iLS = SpongeLaunch.iLS.get();
            new PrettyPrinter(60).add("Old Mixin Version Loaded!").centre().hr()
                .add("Hey, sorry, but Sponge requires a newer version of Mixin being loaded, and unfortunately\n"
                     + "with an older version, nothing will work as it should. Please rename the sponge jar to load\n"
                     + "earlier than other coremods, so that Sponge's Mixin version will be loaded (they're backwards\n"
                     + "compatible, but not forwards compatible). We're sorry for the inconvenience, but this is all\n"
                     + "that we can do.")
                .add()
                .add("%s : %s", "Current Loaded Mixin", environment.toString())
                .add("%s : %s", "Required Mixin Version", required.toString())
                .log(iLS.getLaunchLogger().get(), Level.FATAL);
            iLS.getExitHandler().get().terminate("org.spongepowered.core", -1);
        }
    }

    public static void forceEarlyExit(final String spoof, final int exitCode) {
        iLS.get().getExitHandler().get().terminate(spoof, exitCode);
    }

    public static void setupSuperClassTransformer(final Supplier<? extends InternalLaunchService> launchService) {
        final InternalLaunchService iLS = launchService.get();
        iLS.registerSuperclassModification("org.spongepowered.api.entity.ai.task.AbstractAITask",
                "org.spongepowered.common.entity.ai.SpongeEntityAICommonSuperclass");
        iLS.registerSuperclassModification("org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSource",
                "org.spongepowered.common.event.damage.SpongeCommonDamageSource");
        iLS.registerSuperclassModification(
                "org.spongepowered.api.event.cause.entity.damage.source.common.AbstractEntityDamageSource",
                "org.spongepowered.common.event.damage.SpongeCommonEntityDamageSource");
        iLS.registerSuperclassModification(
                "org.spongepowered.api.event.cause.entity.damage.source.common.AbstractIndirectEntityDamageSource",
                "org.spongepowered.common.event.damage.SpongeCommonIndirectEntityDamageSource");
    }

    public static boolean isVanilla() {
        return iLS.get().isVanilla();
    }
}
