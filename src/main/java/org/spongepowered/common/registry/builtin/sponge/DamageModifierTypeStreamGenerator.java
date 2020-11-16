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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageModifierType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class DamageModifierTypeStreamGenerator {

    public static Stream<DamageModifierType> stream() {
        final List<DamageModifierType> types = new ArrayList<>();
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("weapon_enchantment")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("offensive_potion_effect")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("defensive_potion_effect")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("negative_potion_effect")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("hard_hat")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("shield")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("armor")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("armor_enchantment")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("magic")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("difficulty")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("absorption")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("critical_hit")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("attack_cooldown")));
        types.add(new SpongeDamageModifierType(ResourceKey.sponge("sweeping")));
        return types.stream();
    }

    private DamageModifierTypeStreamGenerator() {}
}
