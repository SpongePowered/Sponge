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
package org.spongepowered.common.hooks;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.common.world.dimension.SpongeDimensionType;

/**
 * Dimension hooks to handle differences in logic between Sponge's Multi-World system
 * and a platform's version of it.
 */
public interface DimensionHooks {

    /**
     * Asks the platform if the provided {@link SpongeDimensionType dimension type} should
     * generate a spawn on load as a default (typically a specific world's config file will
     * veto this post initial world creation)
     *
     * <p>Sponge's DimensionType is not a 1:1 mapping to Mojang's {@link DimensionType} and
     * it is left up to the platform to calculate the correlation between the two and determine
     * the appropriate return value</p>
     *
     * @param dimensionType The type
     * @return True to generate spawn on load as a default
     */
    default boolean doesGenerateSpawnOnLoad(final SpongeDimensionType dimensionType) {
        return DimensionTypes.OVERWORLD.get() == dimensionType;
    }

    default DimensionType getRespawnDimension(final ServerPlayerEntity entity, final DimensionType dimensionType, final boolean conqueredEnd) {
        final ServerWorld world = entity.getServer().getWorld(entity.dimension);
        if (world == null) {
            // Should be impossible but be defensive
            return dimensionType;
        }

        return world.getDimension().canRespawnHere() ? entity.dimension : dimensionType;
    }
}
