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

import static org.spongepowered.asm.mixin.MixinEnvironment.CompatibilityLevel.JAVA_8;
import static org.spongepowered.common.SpongeImpl.ECOSYSTEM_ID;

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.common.launch.transformer.SpongeSuperclassRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SpongeLaunch {

    private SpongeLaunch() {
    }

    public static final String SUPERCLASS_TRANSFORMER = "org.spongepowered.common.launch.transformer.SpongeSuperclassTransformer";

    private static final Path gameDir = Paths.get("");
    private static final Path pluginsDir = gameDir.resolve("mods");
    private static final Path configDir = gameDir.resolve("config");
    private static final Path spongeConfigDir = configDir.resolve(ECOSYSTEM_ID);

    public static Path getGameDir() {
        return gameDir;
    }

    public static Path getPluginsDir() {
        return pluginsDir;
    }

    public static Path getConfigDir() {
        return configDir;
    }

    public static Path getSpongeConfigDir() {
        return spongeConfigDir;
    }

    public static MixinEnvironment setupMixinEnvironment() {
        MixinBootstrap.init();
        MixinEnvironment.setCompatibilityLevel(JAVA_8);

        // Register common mixin configurations
        return MixinEnvironment.getDefaultEnvironment()
                .addConfiguration("mixins.common.api.json")
                .addConfiguration("mixins.common.core.json")
                .addConfiguration("mixins.common.bungeecord.json")
                .addConfiguration("mixins.common.entityactivation.json")
                .addConfiguration("mixins.common.entitycollisions.json")
                .addConfiguration("mixins.common.exploit.json")
                .addConfiguration("mixins.common.optimization.json")
                ;
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
