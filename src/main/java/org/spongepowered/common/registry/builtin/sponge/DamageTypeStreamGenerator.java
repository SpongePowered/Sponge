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
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.common.event.damage.SpongeDamageType;
import org.spongepowered.common.mixin.accessor.util.DamageSourceAccessor;

import java.util.stream.Stream;

public final class DamageTypeStreamGenerator {

    public static final DamageSource IGNORED_DAMAGE_SOURCE = DamageSourceAccessor.accessor$createDamageSource("sponge:ignored");

    private DamageTypeStreamGenerator() {}

    public static Stream<DamageType> stream() {
        return Stream.of(
                new SpongeDamageType(CatalogKey.minecraft("attack")),
                new SpongeDamageType(CatalogKey.minecraft("contact")),
                new SpongeDamageType(CatalogKey.minecraft("custom")),
                new SpongeDamageType(CatalogKey.minecraft("drown")),
                new SpongeDamageType(CatalogKey.minecraft("explosive")),
                new SpongeDamageType(CatalogKey.minecraft("fall")),
                new SpongeDamageType(CatalogKey.minecraft("fire")),
                new SpongeDamageType(CatalogKey.minecraft("generic")),
                new SpongeDamageType(CatalogKey.minecraft("hunger")),
                new SpongeDamageType(CatalogKey.minecraft("magic")),
                new SpongeDamageType(CatalogKey.minecraft("projectile")),
                new SpongeDamageType(CatalogKey.minecraft("suffocate")),
                new SpongeDamageType(CatalogKey.minecraft("void")),
                new SpongeDamageType(CatalogKey.minecraft("sweeping_attack")),
                new SpongeDamageType(CatalogKey.minecraft("magma"))
        );
    }
}
