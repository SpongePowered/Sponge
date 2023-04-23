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
package org.spongepowered.common.config.inheritable;

import org.spongepowered.common.applaunch.config.core.Config;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

public abstract class BaseConfig implements Config {

    @Setting
    public final EntityCategory entity = new EntityCategory();

    @Setting("entity-activation-range")
    public final EntityActivationRangeCategory entityActivationRange = new EntityActivationRangeCategory();

    @Setting("spawner")
    @Comment("Used to control spawn limits around a player's view distance.")
    public final SpawnerCategory spawner = new SpawnerCategory();

    @Setting("movement-checks")
    public final MovementChecksCategory movementChecks = new MovementChecksCategory();

    @Setting
    public final WorldCategory world = new WorldCategory();

    public static ConfigurationTransformation transformation() {
        return ConfigurationTransformation.versionedBuilder()
            .addVersion(2, BaseConfig.buildOneToTwo())
            // move everything out of sponge subcategory
            .addVersion(1, BaseConfig.buildInitialToOne())
            .build();
    }

    static ConfigurationTransformation buildOneToTwo() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("world", "portal-agents"), TransformAction.remove())
            .build();
    }

    static ConfigurationTransformation buildInitialToOne() {
        return ConfigurationTransformation.builder()
            .addAction(NodePath.path("sponge"), (path, value) -> new Object[0])
            .build();
    }
}
