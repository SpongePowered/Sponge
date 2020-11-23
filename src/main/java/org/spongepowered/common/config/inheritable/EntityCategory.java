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

import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public final class EntityCategory {

    @Setting
    @Comment("Maximum size of an entity's bounding box before removing it. Set to 0 to disable")
    private int maxBoundingBoxSize = 1000;
    @Setting("collision-warn-size")
    @Comment("Number of colliding entities in one spot before logging a warning. Set to 0 to disable")
    private int maxCollisionSize = 200;
    @Setting
    @Comment("Square of the maximum speed of an entity before removing it. Set to 0 to disable")
    private int maxSpeed = 100;
    @Setting
    @Comment("Controls the time in ticks for when an item despawns.")
    private int itemDespawnRate = 6000;
    @Setting
    @Comment("Number of ticks before the fake player entry of a human is removed from the tab list (range of 0 to 100 ticks).")
    private int humanPlayerListRemoveDelay = 10;
    @Setting("entity-painting-respawn-delay")
    @Comment("Number of ticks before a painting is respawned on clients when their art is changed")
    private int paintingRespawnDelaly = 2;
    @Setting("living-soft-despawn-range")
    @Comment("The lower bounded range where living entities near a player may potentially despawn")
    private int softDespawnRange = 32;
    @Setting("living-hard-despawn-range")
    @Comment("The upper bounded range where living entities farther from a player will likely despawn")
    private int hardDespawnRange = 128;
    @Setting("living-soft-despawn-minimum-life")
    @Comment("The amount of seconds before a living entity between the soft and hard despawn ranges from a player to be considered for despawning")
    private int minimumLife = 30;

    public int getMaxSpeed() {
        return this.maxSpeed;
    }

    public void setMaxSpeed(final int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getMaxBoundingBoxSize() {
        return this.maxBoundingBoxSize;
    }

    public void setMaxBoundingBoxSize(final int maxBoundingBoxSize) {
        this.maxBoundingBoxSize = maxBoundingBoxSize;
    }

    public int getMaxCollisionSize() {
        return this.maxCollisionSize;
    }

    public void setMaxCollisionSize(final int maxCollisionSize) {
        this.maxCollisionSize = maxCollisionSize;
    }

    public int getItemDespawnRate() {
        return this.itemDespawnRate;
    }

    public void setItemDespawnRate(final int itemDespawnRate) {
        this.itemDespawnRate = itemDespawnRate;
    }

    public int getHumanPlayerListRemoveDelay() {
        return this.humanPlayerListRemoveDelay;
    }

    public void setHumanPlayerListRemoveDelay(final int delay) {
        this.humanPlayerListRemoveDelay = Math.max(0, Math.min(delay, 100));
    }

    public int getPaintingRespawnDelay() {
        return this.paintingRespawnDelaly;
    }

    public void setPaintingRespawnDelaly(final int paintingRespawnDelaly) {
        this.paintingRespawnDelaly = Math.min(paintingRespawnDelaly, 1);
    }

    public int getSoftDespawnRange() {
        return this.softDespawnRange;
    }

    public void setSoftDespawnRange(final int softDespawnRange) {
        this.softDespawnRange = Math.min(softDespawnRange, 10);
    }

    public int getHardDespawnRange() {
        return this.hardDespawnRange;
    }

    public void setHardDespawnRange(final int hardDespawnRange) {
        this.hardDespawnRange = Math.min(hardDespawnRange, 10);
    }

    public int getMinimumLife() {
        return this.minimumLife;
    }

    public void setMinimumLife(int minimumLife) {
        this.minimumLife = Math.min(minimumLife, 20);
    }
}
