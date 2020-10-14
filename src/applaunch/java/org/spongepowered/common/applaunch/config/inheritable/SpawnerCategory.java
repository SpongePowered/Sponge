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

@ConfigSerializable
public final class SpawnerCategory {

    @Setting("spawn-limit-ambient")
    @Comment("The number of ambients the spawner can potentially spawn around a player.")
    private int spawnLimitAmbient = 15;
    @Setting("spawn-limit-animal")
    @Comment("The number of animals the spawner can potentially spawn around a player.")
    private int spawnLimitAnimal = 15;
    @Setting("spawn-limit-aquatic")
    @Comment("The number of aquatics the spawner can potentially spawn around a player.")
    private int spawnLimitAquatic = 5;
    @Setting("spawn-limit-monster")
    @Comment("The number of monsters the spawner can potentially spawn around a player.")
    private int spawnLimitMonster = 70;
    @Setting("tick-rate-ambient")
    @Comment("The ambient spawning tick rate. Default: 400")
    private int tickRateAmbient = 400;
    @Setting("tick-rate-animal")
    @Comment("The animal spawning tick rate. Default: 400")
    private int tickRateAnimal = 400;
    @Setting("tick-rate-aquatic")
    @Comment("The aquatic spawning tick rate. Default: 1")
    private int tickRateAquatic = 1;
    @Setting("tick-rate-monster")
    @Comment("The monster spawning tick rate. Default: 1")
    private int tickRateMonster = 1;

    public SpawnerCategory() {
    }

    public int getAnimalSpawnLimit() {
        return this.spawnLimitAnimal;
    }

    public int getAmbientSpawnLimit() {
        return this.spawnLimitAmbient;
    }

    public int getAquaticSpawnLimit() {
        return this.spawnLimitAquatic;
    }

    public int getMonsterSpawnLimit() {
        return this.spawnLimitMonster;
    }

    public int getAnimalTickRate() {
        return this.tickRateAnimal;
    }

    public int getAmbientTickRate() {
        return this.tickRateAmbient;
    }

    public int getAquaticTickRate() {
        return this.tickRateAquatic;
    }

    public int getMonsterTickRate() {
        return this.tickRateMonster;
    }
}
