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

import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class CollisionModCategory {

    @Setting("enabled")
    @Comment("If 'false', entity collision rules for this mod will be ignored.")
    private boolean enabled = true;
    @Setting("defaults")
    @Comment("Default maximum collisions used for all entities/blocks unless overridden.")
    private Map<String, Integer> defaultMaxCollisions = new HashMap<>();
    @Setting("blocks")
    private Map<String, Integer> blockList = new HashMap<>();
    @Setting("entities")
    private Map<String, Integer> entityList = new HashMap<>();

    public CollisionModCategory() {
        this.defaultMaxCollisions.put("blocks", 8);
        this.defaultMaxCollisions.put("entities", 8);
    }

    public CollisionModCategory(final String modId) {
        if (modId.equals("minecraft")) {
            this.blockList.put("detector_rail", 1);
            this.blockList.put("heavy_weighted_pressure_plate", 150);
            this.blockList.put("light_weighted_pressure_plate", 15);
            this.blockList.put("mob_spawner", -1);
            this.blockList.put("stone_pressure_plate", 1);
            this.blockList.put("wooden_button", 1);
            this.blockList.put("wooden_pressure_plate", 1);
            this.entityList.put("thrownpotion", -1);
        } else if (modId.equals("botania")) {
            this.entityList.put("spark", -1);
            this.entityList.put("corporeaspark", -1);
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Map<String, Integer> getDefaultMaxCollisions() {
        return this.defaultMaxCollisions;
    }

    public Map<String, Integer> getBlockList() {
        return this.blockList;
    }

    public Map<String, Integer> getEntityList() {
        return this.entityList;
    }
}
