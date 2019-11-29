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
package org.spongepowered.common.mixin.api.mcp.world.dimension;

import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.dimension.DimensionTypeBridge;

@Mixin(Dimension.class)
@Implements(value = @Interface(iface = org.spongepowered.api.world.dimension.Dimension.class, prefix = "dimension$"))
public abstract class DimensionMixin_API {

    @Shadow @Final private DimensionType type;
    @Shadow public abstract boolean shadow$canRespawnHere();
    @Shadow public abstract boolean shadow$doesWaterVaporize();
    @Shadow public abstract boolean shadow$isNether();
    @Shadow public abstract boolean shadow$hasSkyLight();
    @Shadow public abstract boolean shadow$isSurfaceWorld();

    public org.spongepowered.api.world.dimension.DimensionType dimension$getType() {
        return ((DimensionTypeBridge) this.type).bridge$getSpongeDimensionType();
    }

    public TerrainGenerator<?> dimension$createGenerator() {
        // TODO 1.14 - Dimension -> TerrainGenerator
        return null;
    }

    public boolean dimension$allowsPlayerRespawns() {
        return this.shadow$canRespawnHere();
    }

    public boolean dimension$doesWaterEvaporate() {
        return this.shadow$doesWaterVaporize();
    }

    public boolean dimension$hasSky() {
        return !this.shadow$isNether();
    }

    public boolean dimension$hasSkyLight() {
        return this.shadow$hasSkyLight();
    }

    public boolean dimension$isSurfaceLike() {
        return this.shadow$isSurfaceWorld();
    }
}
