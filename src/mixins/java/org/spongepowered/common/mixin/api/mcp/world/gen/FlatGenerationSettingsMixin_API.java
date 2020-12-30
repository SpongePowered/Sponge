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
package org.spongepowered.common.mixin.api.mcp.world.gen;

import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import org.spongepowered.api.world.generation.settings.FlatGeneratorSettings;
import org.spongepowered.api.world.generation.settings.flat.LayerSettings;
import org.spongepowered.api.world.generation.settings.structure.StructureGenerationSettings;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

@Mixin(FlatGenerationSettings.class)
@Implements(@Interface(iface = FlatGeneratorSettings.class, prefix = "flatGeneratorSettings$"))
public abstract class FlatGenerationSettingsMixin_API implements FlatGeneratorSettings {

    // @formatter:off
    @Shadow private boolean decoration;
    @Shadow private boolean addLakes;

    @Shadow public abstract DimensionStructuresSettings shadow$structureSettings();
    @Shadow public abstract List<FlatLayerInfo> shadow$getLayersInfo();
    // @formatter:on

    @Intrinsic
    public StructureGenerationSettings structureSettings() {
        return (StructureGenerationSettings) this.shadow$structureSettings();
    }

    @Override
    public List<LayerSettings> layers() {
        return (List<LayerSettings>) (Object) this.shadow$getLayersInfo();
    }

    @Override
    public Optional<LayerSettings> layer(int height) {
        return Optional.ofNullable((LayerSettings) this.shadow$getLayersInfo().get(height));
    }

    @Override
    public boolean performDecoration() {
        return this.decoration;
    }

    @Override
    public boolean populateLakes() {
        return this.addLakes;
    }
}
