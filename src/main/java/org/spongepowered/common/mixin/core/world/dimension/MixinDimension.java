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
package org.spongepowered.common.mixin.core.world.dimension;

import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.DimensionConfig;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.interfaces.world.IMixinDimension;

@NonnullByDefault
@Mixin(net.minecraft.world.dimension.Dimension.class)
public abstract class MixinDimension implements IMixinDimension, Dimension {

    @Shadow protected World world;
    @Shadow(prefix = "shadow$") public abstract net.minecraft.world.dimension.DimensionType shadow$getType();
    @Shadow public abstract boolean canRespawnHere();
    @Shadow public abstract boolean doesWaterVaporize();
    @Shadow public abstract WorldBorder createWorldBorder();
    @Shadow public abstract boolean isNether();

    @Override
    public DimensionType getType() {
        return (DimensionType) this.shadow$getType();
    }

    @Override
    public boolean allowsPlayerRespawns() {
        return this.canRespawnHere();
    }

    @Override
    public boolean doesWaterEvaporate() {
        return this.doesWaterVaporize();
    }

    @Override
    public boolean hasSky() {
        return !isNether();
    }

    @Override
    public Context getContext() {
        return ((IMixinDimensionType) this.shadow$getType()).getGlobalDimensionType().getContext();
    }

    @Override
    public WorldBorder createServerWorldBorder() {
        return createWorldBorder();
    }

    /**
     * @author Zidane
     * @reason Check configs when dropping a chunk to see if we should keep the spawn loaded
     */
    @Overwrite
    public boolean canDropChunk(int x, int z) {
        final boolean isSpawnChunk = this.world.isSpawnChunk(x, z);

        return !isSpawnChunk || !SpongeImplHooks.shouldKeepSpawnLoaded(this.world.dimension.getType());
    }
}
