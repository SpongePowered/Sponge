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
package org.spongepowered.common.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.particle.ParticleOption;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.world.PositionSource;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.HashMap;
import java.util.Map;

public final class ParticleOptionUtil {

    /**
     * Generates the default particle options for a given internal ParticleType (non-numerical particles).
     */
    public static ImmutableMap<ParticleOption<?>, Object> generateDefaultsForNamed(ParticleType<?> type) {
        final Map<ParticleOption<?>, Object> options = new HashMap<>();

        options.put(ParticleOptions.OFFSET.get(), Vector3d.ZERO);
        options.put(ParticleOptions.QUANTITY.get(), 1);

        if (type == ParticleTypes.BLOCK || type == ParticleTypes.BLOCK_MARKER
                || type == ParticleTypes.FALLING_DUST || type == ParticleTypes.DUST_PILLAR) {
            options.put(ParticleOptions.BLOCK_STATE.get(), BlockTypes.AIR.get().defaultState());
        } else if (type == ParticleTypes.ITEM) {
            options.put(ParticleOptions.ITEM_STACK_SNAPSHOT.get(), ItemStack.of(ItemTypes.STONE).asImmutable());
        } else if (type == ParticleTypes.DUST) {
            options.put(ParticleOptions.COLOR.get(), Color.RED);
            options.put(ParticleOptions.SCALE.get(), 1.0d);
        } else if (type == ParticleTypes.DUST_COLOR_TRANSITION) {
            options.put(ParticleOptions.COLOR.get(), Color.RED);
            options.put(ParticleOptions.TO_COLOR.get(), Color.RED);
            options.put(ParticleOptions.SCALE.get(), 1.0d);
        } else if (type == ParticleTypes.SCULK_CHARGE) {
            options.put(ParticleOptions.ROLL.get(), 0);
        } else if (type == ParticleTypes.SHRIEK) {
            options.put(ParticleOptions.DELAY.get(), 0);
        } else if (type == ParticleTypes.VIBRATION) {
            options.put(ParticleOptions.DESTINATION.get(), PositionSource.of(Vector3i.ZERO));
            options.put(ParticleOptions.TRAVEL_TIME.get(), 0);
        } else if (type == ParticleTypes.ENTITY_EFFECT) {
            options.put(ParticleOptions.COLOR.get(), Color.RED);
            options.put(ParticleOptions.OPACITY.get(), 1.0d);
        }

        return ImmutableMap.copyOf(options);
    }

    private ParticleOptionUtil() {
    }
}
