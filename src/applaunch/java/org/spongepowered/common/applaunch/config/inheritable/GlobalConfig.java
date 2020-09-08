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
package org.spongepowered.common.applaunch.config.inheritable;

import com.google.common.collect.ImmutableSet;
import ninja.leaping.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.common.applaunch.config.core.FileMovingConfigurationTransformation;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.config.common.CommonConfig;

import java.util.Set;

public final class GlobalConfig extends BaseConfig {

    public static final String FILE_NAME = "global.conf";

    private static Object[] p(final Object... els) {
        return els;
    }

    // Paths moved to sponge.conf
    private static final Set<Object[]> MIGRATE_SPONGE_PATHS = ImmutableSet.of(
            p("world", "auto-player-save-interval"),
            p("world", "leaf-decay"),
            p("world", "game-profile-query-task-interval"),
            p("world", "invalid-lookup-uuids"),
            p("general"),
            p("sql"),
            p("commands"),
            p("permission"),
            p("modules"),
            p("ip-sets"),
            p("bungeecord"),
            p("exploits"),
            p("optimizations"),
            p("cause-tracker"),
            p("teleport-helper"),
            p("broken-mods"),
            p("service-registration"),
            p("debug"),
            p("timings")
    );

    // Paths moved to metrics.conf
    private static final Set<Object[]> MIGRATE_METRICS_PATHS = ImmutableSet.of(
            p("metrics")
    );

    public GlobalConfig() {
        super();
    }

    @Override
    protected ConfigurationTransformation buildInitialToOne() {
        return ConfigurationTransformation.chain(
                super.buildInitialToOne(), // parent conversion
                new FileMovingConfigurationTransformation(MIGRATE_SPONGE_PATHS, // move to broken-out configuration files
                        SpongeConfigs.createLoader(SpongeConfigs.getDirectory().resolve(CommonConfig.FILE_NAME)), true),
                new FileMovingConfigurationTransformation(MIGRATE_METRICS_PATHS,
                        SpongeConfigs.createLoader(SpongeConfigs.getDirectory().resolve(SpongeConfigs.METRICS_NAME)), true)
        );
    }
}
