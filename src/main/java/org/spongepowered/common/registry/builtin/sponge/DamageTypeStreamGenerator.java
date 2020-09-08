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
package org.spongepowered.common.registry.builtin.sponge;

import net.minecraft.util.DamageSource;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageType;
import org.spongepowered.common.accessor.util.DamageSourceAccessor;

import java.util.stream.Stream;

public final class DamageTypeStreamGenerator {

    public static final DamageSource IGNORED_DAMAGE_SOURCE = DamageSourceAccessor.accessor$createDamageSource("sponge:ignored");

    private DamageTypeStreamGenerator() {}

    public static Stream<DamageType> stream() {
        return Stream.of(
                new SpongeDamageType(ResourceKey.minecraft("attack")),
                new SpongeDamageType(ResourceKey.minecraft("contact")),
                new SpongeDamageType(ResourceKey.minecraft("custom")),
                new SpongeDamageType(ResourceKey.minecraft("drown")),
                new SpongeDamageType(ResourceKey.minecraft("explosive")),
                new SpongeDamageType(ResourceKey.minecraft("fall")),
                new SpongeDamageType(ResourceKey.minecraft("fire")),
                new SpongeDamageType(ResourceKey.minecraft("generic")),
                new SpongeDamageType(ResourceKey.minecraft("hunger")),
                new SpongeDamageType(ResourceKey.minecraft("magic")),
                new SpongeDamageType(ResourceKey.minecraft("projectile")),
                new SpongeDamageType(ResourceKey.minecraft("suffocate")),
                new SpongeDamageType(ResourceKey.minecraft("void")),
                new SpongeDamageType(ResourceKey.minecraft("sweeping_attack")),
                new SpongeDamageType(ResourceKey.minecraft("magma"))
        );
    }
}
