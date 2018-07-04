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
import org.spongepowered.common.config.category.BungeeCordCategory;
import org.spongepowered.common.config.category.PhaseTrackerCategory;
import org.spongepowered.common.config.category.CommandsCategory;
import org.spongepowered.common.config.category.ExploitCategory;
import org.spongepowered.common.config.category.GlobalGeneralCategory;
import org.spongepowered.common.config.category.GlobalWorldCategory;
import org.spongepowered.common.config.category.ModuleCategory;
import org.spongepowered.common.config.category.MovementChecksCategory;
import org.spongepowered.common.config.category.OptimizationCategory;
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

    @Setting
    private CommandsCategory commands = new CommandsCategory();

    @Setting(value = "modules")
    private ModuleCategory mixins = new ModuleCategory();

    @Setting("ip-sets")
    private Map<String, List<IpSet>> ipSets = new HashMap<>();

    @Setting(value = "bungeecord")
    private BungeeCordCategory bungeeCord = new BungeeCordCategory();

    @Setting
    private ExploitCategory exploits = new ExploitCategory();

    @Setting(value = "optimizations")
    private OptimizationCategory optimizations = new OptimizationCategory();

    @Setting
    protected GlobalGeneralCategory general = new GlobalGeneralCategory();

    @Setting
    protected GlobalWorldCategory world = new GlobalWorldCategory();

    @Setting(value = "cause-tracker")
    private PhaseTrackerCategory causeTracker = new PhaseTrackerCategory();

    @Setting(value = "teleport-helper", comment = "Blocks to blacklist for safe teleportation.")
    private TeleportHelperCategory teleportHelper = new TeleportHelperCategory();

    @Setting("movement-checks")
    private MovementChecksCategory movementChecks = new MovementChecksCategory();

    public GlobalConfig() {
        super();
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

}
