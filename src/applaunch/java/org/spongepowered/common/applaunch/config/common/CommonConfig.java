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
package org.spongepowered.common.applaunch.config.common;

import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.common.applaunch.config.core.Config;
import org.spongepowered.common.applaunch.config.core.IpSet;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Sponge's core configuration.
 *
 * <p>The options in this file are non-inheritable.</p>
 *
 * <p>Because of how early in the engine lifecycle this configuration is loaded,
 * the configuration and its categories <em>may not reference
 * Minecraft classes</em></p>
 */
public final class CommonConfig implements Config {

    public static final String FILE_NAME = "sponge.conf";

    @Setting
    public final GeneralCategory general = new GeneralCategory();

    @Setting
    @Comment("Configuration options related to the SQL manager, including connection aliases etc")
    public final SqlCategory sql = new SqlCategory();

    @Setting
    public final CommandsCategory commands = new CommandsCategory();

    @Setting
    public final PermissionCategory permissions = new PermissionCategory();

    @Setting
    public final ModuleCategory modules = new ModuleCategory();

    @Setting("ip-sets")
    private final Map<String, List<IpSet>> ipSets = new HashMap<>();

    @Setting
    public final IpForwardingCategory ipForwarding = new IpForwardingCategory();

    @Setting
    public final ExploitCategory exploits = new ExploitCategory();

    @Setting
    public final OptimizationCategory optimizations = new OptimizationCategory();

    @Setting("phase-tracker")
    public final PhaseTrackerCategory phaseTracker = new PhaseTrackerCategory();

    @Setting("teleport-helper")
    @Comment("Blocks to blacklist for safe teleportation.")
    public final TeleportHelperCategory teleportHelper = new TeleportHelperCategory();

    @Setting
    @Comment("Enables server owners to require specific plugins to provide Sponge services")
    public final ServicesCategory services = new ServicesCategory();

    @Setting
    public final DebugCategory debug = new DebugCategory();

    @Setting
    public final TimingsCategory timings = new TimingsCategory();

    @Setting
    public final WorldCategory world = new WorldCategory();

    public static ConfigurationTransformation transformation() {
        return ConfigurationTransformation.versionedBuilder()
            .makeVersion(1, builder -> {
                // Update IP forwarding
                builder.addAction(NodePath.path("modules", "bungeecord"), TransformAction.rename("ip-forwarding"))
                    .addAction(NodePath.path("bungeecord"), TransformAction.rename("ip-forwarding"))
                    .addAction(NodePath.path("bungeecord", "ip-forwarding"), (path, value) -> {
                        if (value.getBoolean()) {
                            value.parent().node("mode").set(IpForwardingCategory.Mode.LEGACY);
                        }
                        value.set(null);
                        return null;
                    });
            })
            .build();
    }

    public Map<String, Predicate<InetAddress>> getIpSets() {
        final Map<String, Predicate<InetAddress>> result = new HashMap<>(this.ipSets.size());
        for (final Map.Entry<String, List<IpSet>> entry : this.ipSets.entrySet()) {
            result.put(entry.getKey(), CommonConfig.allOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }

    public Predicate<InetAddress> getIpSet(final String name) {
        return this.ipSets.containsKey(name) ? CommonConfig.allOf(this.ipSets.get(name)) : null;
    }

    private static <T> Predicate<T> allOf(final List<? extends Predicate<T>> elements) {
        @SuppressWarnings("unchecked")
        final Predicate<T>[] sets = elements.toArray(new Predicate[0]);
        return addr -> {
            for (final Predicate<T> set : sets) {
                if (!set.test(addr)) {
                    return false;
                }
            }
            return true;
        };
    }
}
