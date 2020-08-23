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
package org.spongepowered.applaunch.config.common;

import ninja.leaping.configurate.objectmapping.Setting;
import org.spongepowered.applaunch.config.core.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sponge's core configuration.
 *
 * <p>The options in this file are non-inheritable.</p>
 *
 * <p>Because of how early in the engine lifecycle this configuration is loaded,
 * the configuration and its categories <em>may not reference
 * Minecraft classes</em></p>
 */
public class CommonConfig implements Config {

    public static final String FILE_NAME = "sponge.conf";

    @Setting
    private GeneralCategory general = new GeneralCategory();

    @Setting(comment = "Configuration options related to the SQL manager, including connection aliases etc")
    private SqlCategory sql = new SqlCategory();

    @Setting
    private CommandsCategory commands = new CommandsCategory();

    @Setting
    private PermissionCategory permission = new PermissionCategory();

    @Setting(value = "modules")
    private ModuleCategory mixins = new ModuleCategory();

    @Setting("ip-sets")
    private Map<String, List<String>> ipSets = new HashMap<>();

    @Setting(value = "bungeecord")
    private BungeeCordCategory bungeeCord = new BungeeCordCategory();

    @Setting
    private ExploitCategory exploits = new ExploitCategory();

    @Setting(value = "optimizations")
    private OptimizationCategory optimizations = new OptimizationCategory();

    @Setting(value = "cause-tracker")
    private PhaseTrackerCategory causeTracker = new PhaseTrackerCategory();

    @Setting(value = "teleport-helper", comment = "Blocks to blacklist for safe teleportation.")
    private TeleportHelperCategory teleportHelper = new TeleportHelperCategory();

    @Setting(value = "broken-mods", comment = "Stopgap measures for dealing with broken mods")
    private BrokenModCategory brokenMods = new BrokenModCategory();

    @Setting(value = "service-registration",
            comment = "Enables server owners to require specific plugins to provide Sponge services")
    private ServicesCategory servicesCategory = new ServicesCategory();

    @Setting
    private DebugCategory debug = new DebugCategory();

    @Setting
    private TimingsCategory timings = new TimingsCategory();

    @Setting
    private WorldCategory world = new WorldCategory();


    public CommonConfig() {
        super();
    }

    public GeneralCategory getGeneral() {
        return this.general;
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

    public ServicesCategory getServicesCategory() {
        return this.servicesCategory;
    }


    public ExploitCategory getExploits() {
        return this.exploits;
    }

    public OptimizationCategory getOptimizations() {
        return this.optimizations;
    }

    /* TODO(zml): Reimplement this when bringing in SpongeContextCalculator from invalid
    public Map<String, Predicate<InetAddress>> getIpSets() {
        return ImmutableMap.copyOf(Maps.transformValues(this.ipSets, Predicates::and));
    }

    public Predicate<InetAddress> getIpSet(String name) {
        return this.ipSets.containsKey(name) ? Predicates.and(this.ipSets.get(name)) : null;
    }
     */

    public PhaseTrackerCategory getPhaseTracker() {
        return this.causeTracker;
    }

    public TeleportHelperCategory getTeleportHelper() {
        return this.teleportHelper;
    }

    public DebugCategory getDebug() {
        return this.debug;
    }

    public TimingsCategory getTimings() {
        return this.timings;
    }

    public WorldCategory getWorld() {
        return this.world;
    }

}
