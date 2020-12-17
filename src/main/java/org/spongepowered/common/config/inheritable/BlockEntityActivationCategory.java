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
public final class BlockEntityActivationCategory {

    @Setting("auto-populate")
    @Comment("If 'true', newly discovered block entities will be added to this config with default settings.")
    public boolean autoPopulate = false;

    @Setting("default-range")
    @Comment("Default activation block range used for all block entities unless overridden.")
    public int defaultRange = 64;

    @Setting("default-tick-rate")
    @Comment("Default tick rate used for all block entities unless overridden.")
    public int defaultTickRate = 1;

    @Setting
    @Comment("Per-mod overrides. Refer to the minecraft default mod for example.")
    public final Map<String, MobSubCategory> mods = new HashMap<>();

    @ConfigSerializable
    public static final class MobSubCategory {

        @Setting
        @Comment("If 'false', block entity activation rules for this mod will be ignored and always tick.")
        public boolean enabled = true;

        @Setting("default-range")
        public Integer defaultRange;

        @Setting("default-tick-rate")
        public Integer defaultTickRate;

        @Setting()
        public final Map<String, Integer> ranges = new HashMap<>();

        @Setting("tick-rates")
        public final Map<String, Integer> tickRates = new HashMap<>();
    }
}
