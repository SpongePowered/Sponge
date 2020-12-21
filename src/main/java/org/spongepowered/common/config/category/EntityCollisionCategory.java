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
package org.spongepowered.common.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public class EntityCollisionCategory extends ConfigCategory {

    @Setting(value = "auto-populate",
            comment = "If 'true', newly discovered entities/blocks will be added to this config with a default value.")
    private boolean autoPopulate = false;

    @Setting(value = "max-entities-within-aabb", comment = ""
            + "Maximum amount of entities any given entity or block can collide with.\n"
            + "This improves performance when there are more than 8 entities on top of each other\n"
            + "such as a 1x1 spawn pen.\n"
            + "Set to '0' to disable.")
    private int maxEntitiesWithinAABB = 8;

    @Setting(value = "mods", comment = "Per-mod overrides. Refer to the minecraft default mod for example.")
    private Map<String, CollisionModCategory> modList = new HashMap<>();

    public EntityCollisionCategory() {
        this.modList.put("minecraft", new CollisionModCategory("minecraft"));
    }

    public boolean autoPopulateData() {
        return this.autoPopulate;
    }

    public Map<String, CollisionModCategory> getModList() {
        return this.modList;
    }

    public int getMaxEntitiesWithinAABB() {
        return this.maxEntitiesWithinAABB;
    }

    public void setMaxEntitiesWithinAABB(int maxEntities) {
        this.maxEntitiesWithinAABB = maxEntities;
    }

}
