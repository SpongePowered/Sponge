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
package org.spongepowered.common.mixin.api.mcp.world.biome;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeDecorator;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.biome.BiomeBridge;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.populators.WrappedBiomeDecorator;

@Mixin(Biome.class)
public abstract class BiomeMixin_API implements BiomeType {

    @Shadow @Final private String biomeName;
    @Shadow @Final private float temperature;
    @Shadow @Final private float rainfall;
    @Shadow public BiomeDecorator decorator;


    @Override
    public BiomeGenerationSettings createDefaultGenerationSettings(org.spongepowered.api.world.World world) {
        SpongeBiomeGenerationSettings gensettings = new SpongeBiomeGenerationSettings();
        gensettings.getPopulators().clear();
        gensettings.getGenerationPopulators().clear();
        gensettings.getGroundCoverLayers().clear();
        ((BiomeBridge) this).bridge$buildPopulators((World) world, gensettings);
        if (!getClass().getName().startsWith("net.minecraft")) {
            gensettings.getPopulators().add(new WrappedBiomeDecorator((Biome) (Object) this));
        } else if (!this.decorator.getClass().getName().startsWith("net.minecraft")) {
            gensettings.getPopulators().add(new WrappedBiomeDecorator(this.decorator));
        }
        return gensettings;
    }

    @Override
    public String getName() {
        return this.biomeName;
    }

    @Override
    public final String getId() {
        return ((BiomeBridge) this).bridge$getId();
    }

    @Override
    public double getTemperature() {
        return this.temperature;
    }

    @Override
    public double getHumidity() {
        return this.rainfall;
    }
}
