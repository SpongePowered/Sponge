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
package org.spongepowered.common.config.metrics;

import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.applaunch.config.core.Config;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.plugin.PluginContainer;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class MetricsConfig implements Config {

    public static final String FILE_NAME = SpongeConfigs.METRICS_NAME;

    @Setting("global-state")
    @Comment("The global collection state that should be respected by all plugins that have no specified "
             + "collection state. If undefined then it is treated as disabled.")
    public Tristate globalState = Tristate.UNDEFINED;

    @Setting
    @Comment("Plugin-specific collection states that override the global collection state.")
    public final Map<String, Tristate> plugins = new HashMap<>();

    public Tristate getCollectionState(final PluginContainer container) {
        final Tristate pluginState = this.plugins.get(container.metadata().id());
        return pluginState == null ? Tristate.UNDEFINED : pluginState;
    }

    public static ConfigurationTransformation transformation() {
        return ConfigurationTransformation.versionedBuilder()
                .makeVersion(1, b ->
                        b.addAction(NodePath.path("metrics"), (path, value) -> new Object[0]))
                .build();
    }
}
