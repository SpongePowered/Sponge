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

@ConfigSerializable
public class SpawnerCategory extends ConfigCategory {

    @Setting(value = "spawn-limit-ambient")
    private int spawnLimitAmbient = 15;
    @Setting(value = "spawn-limit-animal")
    private int spawnLimitAnimal = 15;
    @Setting(value = "spawn-limit-aquatic")
    private int spawnLimitAquatic = 5;
    @Setting(value = "spawn-limit-monster")
    private int spawnLimitMonster = 70;
    @Setting(value = "tick-rate-ambient")
    private int tickRateAmbient = 400;
    @Setting(value = "tick-rate-animal")
    private int tickRateAnimal = 400;
    @Setting(value = "tick-rate-aquatic")
    private int tickRateAquatic = 400;
    @Setting(value = "tick-rate-monster")
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
