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
package org.spongepowered.common.mixin.api.minecraft.world.level.levelgen;

import org.spongepowered.api.world.generation.structure.Structure;
import org.spongepowered.api.world.generation.config.structure.SeparatedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.SpacedStructureConfig;
import org.spongepowered.api.world.generation.config.structure.StructureGenerationConfig;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

@Mixin(StructureSettings.class)
@Implements(@Interface(iface = StructureGenerationConfig.class, prefix = "structureGenerationConfig$"))
public abstract class StructureSettingsMixin_API implements StructureGenerationConfig {

    // @formatter:off
    @Shadow @Nullable public abstract StrongholdConfiguration shadow$stronghold();
    @Shadow public abstract Map<net.minecraft.world.level.levelgen.feature.StructureFeature<?>, StructureFeatureConfiguration> shadow$structureConfig();
    // @formatter:on

    @Intrinsic
    public Optional<SpacedStructureConfig> structureGenerationConfig$stronghold() {
        return Optional.ofNullable((SpacedStructureConfig) this.shadow$stronghold());
    }

    @Override
    public Optional<SeparatedStructureConfig> structure(final Structure structure) {
        return Optional.ofNullable((SeparatedStructureConfig) this.shadow$structureConfig().get(Objects.requireNonNull(structure, "structure")));
    }

    @Override
    public Map<Structure, SeparatedStructureConfig> structures() {
        return Collections.unmodifiableMap((Map<Structure, SeparatedStructureConfig>) (Object) this.shadow$structureConfig());
    }
}
