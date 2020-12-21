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

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class EntityCollisionCategory {

    @Setting
    @Comment("Per-mod overrides. Refer to the minecraft default mod for example.")
    public final Map<String, ModSubCategory> mods = new HashMap<>();

    @Setting("auto-populate")
    @Comment("If 'true', newly discovered entities/blocks will be added to this config with a default value.")
    public boolean autoPopulate = false;

    @Setting("max-entities-within-aabb")
    @Comment("Maximum amount of entities any given entity or block can collide with. This improves \n"
        + "performance when there are more than 8 entities on top of each other such as a 1x1 \n"
        + "spawn pen. Set to 0 to disable.")
    public int maxEntitiesWithinAABB = 8;

    public EntityCollisionCategory() {
        this.mods.put("minecraft", new ModSubCategory("minecraft"));
    }

    @ConfigSerializable
    public static final class ModSubCategory {

        @Setting("entity-default")
        public Integer entityDefault = 8;

        @Setting("block-default")
        public Integer blockDefault = 8;

        @Setting
        public final Map<String, Integer> blocks = new HashMap<>();

        @Setting
        public final Map<String, Integer> entities = new HashMap<>();

        @Setting
        @Comment("If 'false', entity collision rules for this mod will be ignored.")
        public boolean enabled = true;

        public ModSubCategory() {
        }

        public ModSubCategory(final String namespace) {
            if (namespace.equals("minecraft")) {
                this.blocks.put("detector_rail", 1);
                this.blocks.put("heavy_weighted_pressure_plate", 150);
                this.blocks.put("light_weighted_pressure_plate", 15);
                this.blocks.put("mob_spawner", -1);
                this.blocks.put("stone_pressure_plate", 1);
                this.blocks.put("wooden_button", 1);
                this.blocks.put("wooden_pressure_plate", 1);
                this.entities.put("thrownpotion", -1);
            }
        }
    }
}
