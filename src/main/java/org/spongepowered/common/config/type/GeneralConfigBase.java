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

import ninja.leaping.configurate.objectmapping.Setting;
import org.spongepowered.common.config.category.PlayerBlockTracker;
import org.spongepowered.common.config.category.DebugCategory;
import org.spongepowered.common.config.category.EntityActivationRangeCategory;
import org.spongepowered.common.config.category.EntityCategory;
import org.spongepowered.common.config.category.EntityCollisionCategory;
import org.spongepowered.common.config.category.GeneralCategory;
import org.spongepowered.common.config.category.LoggingCategory;
import org.spongepowered.common.config.category.SpawnerCategory;
import org.spongepowered.common.config.category.TileEntityActivationCategory;
import org.spongepowered.common.config.category.TimingsCategory;
import org.spongepowered.common.config.category.WorldCategory;

import java.util.ArrayList;
import java.util.List;

public class GeneralConfigBase extends ConfigBase {

    @Setting(comment = "Configuration options that will affect all worlds.")
    protected WorldCategory world = new WorldCategory();

    @Setting(value = "player-block-tracker",
            comment = "Configuration options related to tracking player interactions with blocks")
    private PlayerBlockTracker playerBlockTracker = new PlayerBlockTracker();

    @Setting(comment = "Configuratiom options related to debugging features that are disabled by default.")
    private DebugCategory debug = new DebugCategory();

    @Setting(value = "entity", comment = "Configuration options related to entities and their performance impact.")
    private EntityCategory entity = new EntityCategory();

    @Setting(value = "entity-activation-range",
            comment = "Allows the configuration of the default entity activation ranges.")
    private EntityActivationRangeCategory entityActivationRange = new EntityActivationRangeCategory();

    @Setting(value = "entity-collisions", comment = "Configuration options related to entity collision checks.")
    private EntityCollisionCategory entityCollisionCategory = new EntityCollisionCategory();

    @Setting(
            comment = "Contains general configuration options for Sponge that don't fit into a specific classification")
    private GeneralCategory general = new GeneralCategory();

    @Setting(comment = "Configuration option related to logging certain action such as chunk loading.")
    private LoggingCategory logging = new LoggingCategory();

    @Setting(value = "spawner", comment = ""
            + "Used to control spawn limits around players.\n"
            + "Note: The radius uses the lower value of mob spawn range and server's view distance.")
    private SpawnerCategory spawner = new SpawnerCategory();

    @Setting(value = "tileentity-activation",
            comment = "Configuration options related to activation ranges of tile entities.")
    private TileEntityActivationCategory tileEntityActivationCategory = new TileEntityActivationCategory();

    @Setting(comment = ""
            + "Module to run Aikar's Timings, profile your server performance and get a sharable web result.\n"
            + "https://github.com/aikar/timings#aikars-minecraft-timings-viewer-v2")
    private TimingsCategory timings = new TimingsCategory();

    @Setting(value = "world-generation-modifiers", comment = "World Generation Modifiers to apply to the world")
    private final List<String> worldModifiers = new ArrayList<>();

    public PlayerBlockTracker getBlockTracking() {
        return this.playerBlockTracker;
    }

    public DebugCategory getDebug() {
        return this.debug;
    }

    public EntityCategory getEntity() {
        return this.entity;
    }

    public EntityActivationRangeCategory getEntityActivationRange() {
        return this.entityActivationRange;
    }

    public EntityCollisionCategory getEntityCollisionCategory() {
        return this.entityCollisionCategory;
    }

    public GeneralCategory getGeneral() {
        return this.general;
    }

    public LoggingCategory getLogging() {
        return this.logging;
    }

    public SpawnerCategory getSpawner() {
        return this.spawner;
    }

    public WorldCategory getWorld() {
        return this.world;
    }

    public TileEntityActivationCategory getTileEntityActivationRange() {
        return this.tileEntityActivationCategory;
    }

    public TimingsCategory getTimings() {
        return this.timings;
    }

    public List<String> getWorldGenModifiers() {
        return this.worldModifiers;
    }

}
