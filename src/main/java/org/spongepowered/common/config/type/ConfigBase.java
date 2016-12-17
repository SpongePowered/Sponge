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
import org.spongepowered.common.config.category.BlockCapturingCategory;
import org.spongepowered.common.config.category.BlockTrackingCategory;
import org.spongepowered.common.config.category.DebugCategory;
import org.spongepowered.common.config.category.EntityActivationRangeCategory;
import org.spongepowered.common.config.category.EntityCategory;
import org.spongepowered.common.config.category.EntityCollisionCategory;
import org.spongepowered.common.config.category.GeneralCategory;
import org.spongepowered.common.config.category.LoggingCategory;
import org.spongepowered.common.config.category.TileEntityActivationCategory;
import org.spongepowered.common.config.category.TimingsCategory;
import org.spongepowered.common.config.category.WorldCategory;

public abstract class ConfigBase {

    @Setting(value = "block-tracking")
    private BlockTrackingCategory blockTracking = new BlockTrackingCategory();
    @Setting(value = "block-capturing")
    private BlockCapturingCategory blockCapturing = new BlockCapturingCategory();
    @Setting
    private DebugCategory debug = new DebugCategory();
    @Setting(value = "entity")
    private EntityCategory entity = new EntityCategory();
    @Setting(value = "entity-activation-range")
    private EntityActivationRangeCategory entityActivationRange = new EntityActivationRangeCategory();
    @Setting(value = "entity-collisions")
    private EntityCollisionCategory entityCollisionCategory = new EntityCollisionCategory();
    @Setting
    private GeneralCategory general = new GeneralCategory();
    @Setting
    private LoggingCategory logging = new LoggingCategory();
    @Setting
    protected WorldCategory world = new WorldCategory();
    @Setting(value = "tileentity-activation")
    private TileEntityActivationCategory tileEntityActivationCategory = new TileEntityActivationCategory();
    @Setting
    private TimingsCategory timings = new TimingsCategory();

    @Setting(value = "config-enabled", comment =  "This setting does nothing in the global config. In dimension/world configs, it allows the config to override config(s) that it inherits from")
    protected boolean configEnabled = false;

    public ConfigBase() {

    }

    public BlockTrackingCategory getBlockTracking() {
        return this.blockTracking;
    }

    public BlockCapturingCategory getBlockCapturing() {
        return this.blockCapturing;
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

    public WorldCategory getWorld() {
        return this.world;
    }

    public TileEntityActivationCategory getTileEntityActivationRange() {
        return this.tileEntityActivationCategory;
    }

    public TimingsCategory getTimings() {
        return this.timings;
    }

    public boolean isConfigEnabled() {
        return this.configEnabled;
    }

    public void setConfigEnabled(boolean configEnabled) {
        this.configEnabled = configEnabled;
    }
}
