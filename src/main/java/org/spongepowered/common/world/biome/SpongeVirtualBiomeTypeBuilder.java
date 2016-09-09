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
package org.spongepowered.common.world.biome;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.biome.VirtualBiomeType.Builder;

import java.util.function.Function;

public class SpongeVirtualBiomeTypeBuilder implements VirtualBiomeType.Builder {

    private String name;
    private double temperature;
    private double humidity;
    private BiomeType persisted;
    private Function<World, BiomeGenerationSettings> func;

    public SpongeVirtualBiomeTypeBuilder() {
        reset();
    }

    @Override
    public Builder name(String name) {
        this.name = checkNotNull(name, "name");
        return this;
    }

    @Override
    public Builder temperature(double temp) {
        this.temperature = temp;
        return this;
    }

    @Override
    public Builder humidity(double humidity) {
        this.humidity = humidity;
        return this;
    }

    @Override
    public Builder persistedType(BiomeType biome) {
        checkNotNull(biome, "biome");
        checkArgument(!(biome instanceof VirtualBiomeType), "persisted type cannot be a virtual biome");
        this.persisted = biome;
        return this;
    }

    @Override
    public Builder from(VirtualBiomeType value) {
        this.name = value.getName();
        this.temperature = value.getTemperature();
        this.humidity = value.getHumidity();
        return this;
    }

    @Override
    public Builder settingsBuilder(Function<World, BiomeGenerationSettings> settingsBuilder) {
        this.func = checkNotNull(settingsBuilder, "settingsBuilder");
        return this;
    }

    @Override
    public Builder reset() {
        this.name = null;
        this.temperature = 0;
        this.humidity = 0;
        this.persisted = BiomeTypes.VOID;
        this.func = null;
        return this;
    }

    @Override
    public VirtualBiomeType build(String id) throws IllegalStateException {
        checkNotNull(this.name, "name");
        checkNotNull(id, "id");
        checkNotNull(this.persisted, "persistedBiome");
        checkNotNull(this.func, "settings_function");
        checkArgument(!id.isEmpty());
        return new SpongeVirtualBiomeType(id, this.name, this.temperature, this.humidity, this.persisted, this.func);
    }

}
