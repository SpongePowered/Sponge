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

@ConfigSerializable
public final class SpawnerCategory {

    @Setting("spawn-limits")
    public final SpawnLimitsSubCategory spawnLimits = new SpawnLimitsSubCategory();

    @Setting("tick-rates")
    public final TickRatesSubCategory tickRates = new TickRatesSubCategory();

    @ConfigSerializable
    public static final class SpawnLimitsSubCategory {

        @Setting
        @Comment("The number of ambients the spawner can potentially spawn around a player.")
        public int ambient = 15;

        @Setting
        @Comment("The number of creatures the spawner can potentially spawn around a player.")
        public int creature = 10;

        @Setting("underground-aquatic-creature")
        @Comment("The number of underground aquatic creatures the spawner can potentially spawn around a player.")
        public int undergroundAquaticCreature = 5;

        @Setting("aquatic-creature")
        @Comment("The number of aquatic creatures the spawner can potentially spawn around a player.")
        public int aquaticCreature = 5;

        @Setting("aquatic-ambient")
        @Comment("The number of aquatic ambients the spawner can potentially spawn around a player.")
        public int aquaticAmbient = 20;

        @Setting
        @Comment("The number of monsters the spawner can potentially spawn around a player.")
        public int monster = 70;
    }

    @ConfigSerializable
    public static final class TickRatesSubCategory {

        @Setting
        @Comment("The ambient spawning tick rate. Default: 1")
        public int ambient = 1;

        @Setting
        @Comment("The creature spawning tick rate. Default: 1")
        public int creature = 1;

        @Setting("underground-aquatic-creature")
        @Comment("The underground aquatic creature spawning tick rate. Default: 1")
        public int undergroundAquaticCreature = 1;

        @Setting("aquatic-creature")
        @Comment("The aquatic creature spawning tick rate. Default: 1")
        public int aquaticCreature = 1;

        @Setting("aquatic-ambient")
        @Comment("The aquatic ambient spawning tick rate. Default: 1")
        public int aquaticAmbient = 1;

        @Setting
        @Comment("The monster ambient spawning tick rate. Default: 1")
        public int monster = 1;
    }
}
