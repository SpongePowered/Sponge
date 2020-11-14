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
import org.spongepowered.common.applaunch.config.core.FileMovingConfigurationTransformation;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.config.common.CommonConfig;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.util.Set;

public final class GlobalConfig extends BaseConfig {

    public static final String FILE_NAME = "global.conf";

    // Paths moved to sponge.conf
    private static final Set<NodePath> MIGRATE_SPONGE_PATHS = ImmutableSet.of(
            NodePath.path("world", "auto-player-save-interval"),
            NodePath.path("world", "leaf-decay"),
            NodePath.path("world", "game-profile-query-task-interval"),
            NodePath.path("world", "invalid-lookup-uuids"),
            NodePath.path("general"),
            NodePath.path("sql"),
            NodePath.path("commands"),
            NodePath.path("permission"),
            NodePath.path("modules"),
            NodePath.path("ip-sets"),
            NodePath.path("bungeecord"),
            NodePath.path("exploits"),
            NodePath.path("optimizations"),
            NodePath.path("cause-tracker"),
            NodePath.path("teleport-helper"),
            NodePath.path("broken-mods"),
            NodePath.path("service-registration"),
            NodePath.path("debug"),
            NodePath.path("timings")
    );

    // Paths moved to metrics.conf
    private static final Set<NodePath> MIGRATE_METRICS_PATHS = ImmutableSet.of(
            NodePath.path("metrics")
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
