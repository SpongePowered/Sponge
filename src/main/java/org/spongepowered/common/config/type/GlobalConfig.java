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
package org.spongepowered.common.config.type;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import ninja.leaping.configurate.objectmapping.Setting;
import org.spongepowered.common.config.category.BrokenModCategory;
import org.spongepowered.common.config.category.BungeeCordCategory;
import org.spongepowered.common.config.category.CommandsCategory;
import org.spongepowered.common.config.category.ExploitCategory;
import org.spongepowered.common.config.category.GlobalGeneralCategory;
import org.spongepowered.common.config.category.GlobalWorldCategory;
import org.spongepowered.common.config.category.MetricsCategory;
import org.spongepowered.common.config.category.ModuleCategory;
import org.spongepowered.common.config.category.MovementChecksCategory;
import org.spongepowered.common.config.category.OptimizationCategory;
import org.spongepowered.common.config.category.PermissionCategory;
import org.spongepowered.common.config.category.PhaseTrackerCategory;
import org.spongepowered.common.config.category.SqlCategory;
import org.spongepowered.common.config.category.TeleportHelperCategory;
import org.spongepowered.common.util.IpSet;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class GlobalConfig extends GeneralConfigBase {

    @Setting(comment = "Configuration options related to the Sql service, including connection aliases etc")
    private SqlCategory sql = new SqlCategory();

    @Setting(comment = "Configuration options related to commands, including command aliases and hidden commands.")
    private CommandsCategory commands = new CommandsCategory();

    @Setting(comment = "Configuration options related to permissions and permission handling")
    private PermissionCategory permission = new PermissionCategory();

    @Setting(value = "modules", comment = ""
            + "Sponge provides a number of modules that allow for enabling or disabling\n"
            + "certain specific features. These may be enabled or disabled below.\n\n"
            + "Any changes here require a server restart as modules are applied at startup.")
    private ModuleCategory mixins = new ModuleCategory();

    @Setting(value = "ip-sets", comment = ""
            + "Automatically assigns (permission) contexts to users that use any of the given ips.\n"
            + "This can be used to restrict/grant permissions, based on the player's source or target ip.")
    private Map<String, List<IpSet>> ipSets = new HashMap<>();

    @Setting(value = "bungeecord", comment = ""
            + "Controls how Sponge interacts with server proxies, such as BungeeCord and Velocity.\n"
            + "Requires that the 'bungeecord' module is enabled.")
    private BungeeCordCategory bungeeCord = new BungeeCordCategory();

    @Setting(comment = "Configuration option related to sponge provided exploit patches")
    private ExploitCategory exploits = new ExploitCategory();

    @Setting(value = "optimizations", comment = ""
            + "Configuration options related to sponge provided performance optimizations.")
    private OptimizationCategory optimizations = new OptimizationCategory();

    @Setting(comment = ""
            + "Contains general configuration options for Sponge that don't fit into a specific classification")
    protected GlobalGeneralCategory general = new GlobalGeneralCategory();

    @Setting(comment = "Configuration options that will affect all worlds.")
    protected GlobalWorldCategory world = new GlobalWorldCategory();

    @Setting(value = "cause-tracker", comment = "Configuration options related to Sponge's cause tracking system")
    private PhaseTrackerCategory causeTracker = new PhaseTrackerCategory();

    @Setting(value = "teleport-helper", comment = "Blocks to blacklist for safe teleportation.")
    private TeleportHelperCategory teleportHelper = new TeleportHelperCategory();

    @Setting(value = "movement-checks", comment = ""
            + "Configuration options related to minecraft's movement checks, that can be enabled or disabled.")
    private MovementChecksCategory movementChecks = new MovementChecksCategory();

    @Setting(value = "broken-mods", comment = "Stopgap measures for dealing with broken mods")
    private BrokenModCategory brokenMods = new BrokenModCategory();

    @Setting(value = "metrics", comment = "Configuration options related to metric collection.")
    private MetricsCategory metricsCategory = new MetricsCategory();

    public GlobalConfig() {
        super();
    }

    public BrokenModCategory getBrokenMods() {
        return this.brokenMods;
    }

    public BungeeCordCategory getBungeeCord() {
        return this.bungeeCord;
    }

    public SqlCategory getSql() {
        return this.sql;
    }

    public CommandsCategory getCommands() {
        return this.commands;
    }

    public PermissionCategory getPermission() {
        return this.permission;
    }

    public ModuleCategory getModules() {
        return this.mixins;
    }

    public Map<String, Predicate<InetAddress>> getIpSets() {
        return ImmutableMap.copyOf(Maps.transformValues(this.ipSets, new Function<List<IpSet>, Predicate<InetAddress>>() {
            @Nullable
            @Override
            public Predicate<InetAddress> apply(List<IpSet> input) {
                return Predicates.and(input);
            }
        }));
    }

    public ExploitCategory getExploits() {
        return this.exploits;
    }

    public OptimizationCategory getOptimizations() {
        return this.optimizations;
    }

    public Predicate<InetAddress> getIpSet(String name) {
        return this.ipSets.containsKey(name) ? Predicates.and(this.ipSets.get(name)) : null;
    }

    @Override
    public GlobalGeneralCategory getGeneral() {
        return this.general;
    }

    @Override
    public GlobalWorldCategory getWorld() {
        return this.world;
    }

    public PhaseTrackerCategory getPhaseTracker() {
        return this.causeTracker;
    }

    public TeleportHelperCategory getTeleportHelper() {
        return this.teleportHelper;
    }

    public MovementChecksCategory getMovementChecks() {
        return this.movementChecks;
    }

    public MetricsCategory getMetricsCategory() {
        return this.metricsCategory;
    }

}
