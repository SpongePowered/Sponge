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
package org.spongepowered.common.world;

import net.minecraft.world.WorldProvider;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.common.bridge.world.DimensionTypeBridge;
import org.spongepowered.common.mixin.core.world.WorldProviderAccessor;

public class SpongeDimension implements Dimension {

    private final WorldProvider worldProvider;

    public SpongeDimension(WorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    @Override
    public DimensionType getType() {
        return (DimensionType) (Object) this.worldProvider.func_186058_p();
    }

    @Override
    public GeneratorType getGeneratorType() {
        return (GeneratorType) ((WorldProviderAccessor) this.worldProvider).accessor$getTerrainType();
    }

    @Override
    public boolean allowsPlayerRespawns() {
        return this.worldProvider.func_76567_e();
    }

    @Override
    public int getMinimumSpawnHeight() {
        return this.worldProvider.func_76557_i();
    }

    @Override
    public boolean doesWaterEvaporate() {
        return this.worldProvider.func_177500_n();
    }

    @Override
    public boolean hasSky() {
        return !this.worldProvider.func_177495_o();
    }

    @Override
    public Context getContext() {
        return ((DimensionTypeBridge) (Object) this.worldProvider.func_186058_p()).bridge$getContext();
    }

    // These methods are overwritten in SpongeForge

    @Override
    public int getHeight() {
        return this.worldProvider.func_177495_o() ? 128 : 256;
    }

    @Override
    public int getBuildHeight() {
        return 256;
    }
}
