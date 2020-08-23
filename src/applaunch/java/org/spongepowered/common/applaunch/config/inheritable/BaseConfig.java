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

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.common.applaunch.config.core.Config;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseConfig implements Config {

    @Setting
    protected WorldCategory world = new WorldCategory();
    @Setting(value = "player-block-tracker")
    private PlayerBlockTracker playerBlockTracker = new PlayerBlockTracker();
    @Setting(value = "entity")
    private EntityCategory entity = new EntityCategory();
    @Setting(value = "entity-activation-range")
    private EntityActivationRangeCategory entityActivationRange = new EntityActivationRangeCategory();
    @Setting(value = "entity-collisions")
    private EntityCollisionCategory entityCollisionCategory = new EntityCollisionCategory();
    @Setting
    private LoggingCategory logging = new LoggingCategory();
    @Setting(value = "spawner", comment = "Used to control spawn limits around players. \n"
                                        + "Note: The radius uses the lower value of mob spawn range and server's view distance.")
    private SpawnerCategory spawner = new SpawnerCategory();
    @Setting(value = "tileentity-activation")
    private BlockEntityActivationCategory blockEntityActivationCategory = new BlockEntityActivationCategory();
    @Setting(value = "world-generation-modifiers", comment = "World Generation Modifiers to apply to the world")
    private final List<String> worldModifiers = new ArrayList<>();
    @Setting("movement-checks")
    private MovementChecksCategory movementChecks = new MovementChecksCategory();

    public PlayerBlockTracker getBlockTracking() {
        return this.playerBlockTracker;
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

    public LoggingCategory getLogging() {
        return this.logging;
    }

    public SpawnerCategory getSpawner() {
        return this.spawner;
    }

    public WorldCategory getWorld() {
        return this.world;
    }

    public BlockEntityActivationCategory getTileEntityActivationRange() {
        return this.blockEntityActivationCategory;
    }

    public List<String> getWorldGenModifiers() {
        return this.worldModifiers;
    }

    public MovementChecksCategory getMovementChecks() {
        return this.movementChecks;
    }

    @Override
    public ConfigurationTransformation getTransformation() {
        return ConfigurationTransformation.versionedBuilder()
                .addVersion(2, this.buildOneToTwo())
                // move everything out of sponge subcategory
                .addVersion(1, this.buildInitialToOne())
                .build();
    }

    protected ConfigurationTransformation buildOneToTwo() {
        return ConfigurationTransformation.builder()
                .addAction(new Object[] { "world", "portal-agents" }, (path, value) -> {
                    value.setValue(null);
                    return null;
                })
                .build();
    }

    protected ConfigurationTransformation buildInitialToOne() {
        return ConfigurationTransformation.builder()
                .addAction(new Object[] {"sponge"}, (path, value) -> new Object[0])
                .build();
    }
}

