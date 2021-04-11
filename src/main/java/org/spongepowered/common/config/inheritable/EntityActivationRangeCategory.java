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
public final class EntityActivationRangeCategory {

    @Setting("global-ranges")
    @Comment("Default activation ranges used for all entities unless overridden.")
    public final Map<String, Integer> globalRanges = new HashMap<>();

    @Setting
    @Comment("Per-mod overrides. Refer to the minecraft default mod for example.")
    public final Map<String, ModSubCategory> mods = new HashMap<>();

    @Setting("auto-populate")
    @Comment("If 'true', newly discovered entities will be added to this config with a default value.")
    public final boolean autoPopulate = false;

    public EntityActivationRangeCategory() {
        this.globalRanges.put("ambient", 32);
        this.globalRanges.put("aquatic", 32);
        this.globalRanges.put("creature", 32);
        this.globalRanges.put("monster", 32);
        this.globalRanges.put("misc", 16);
    }

    public boolean autoPopulate() {
        return this.autoPopulate;
    }

    @ConfigSerializable
    public static final class ModSubCategory {

        @Setting("default-ranges")
        @Comment("Default activation ranges used for all entities unless overridden.")
        public final Map<String, Integer> defaultRanges = new HashMap<>();

        @Setting
        public final Map<String, Integer> entities = new HashMap<>();

        @Setting
        @Comment("If 'false', entity activation rules for this mod will be ignored and always tick.")
        public boolean enabled = true;

        public ModSubCategory() {
            this.defaultRanges.put("ambient", 32);
            this.defaultRanges.put("aquatic", 32);
            this.defaultRanges.put("creature", 32);
            this.defaultRanges.put("monster", 32);
            this.defaultRanges.put("misc", 16);
        }
    }
}
