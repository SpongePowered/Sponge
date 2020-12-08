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
package org.spongepowered.common.registry.builtin.vanilla;

import net.minecraft.enchantment.Enchantments;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class EnchantmentSupplier {

    private EnchantmentSupplier() {
    }

    public static void registerSuppliers(final SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(EnchantmentType.class, "aqua_affinity", () -> (EnchantmentType) Enchantments.AQUA_AFFINITY)
            .registerSupplier(EnchantmentType.class, "bane_of_arthropods", () -> (EnchantmentType) Enchantments.BANE_OF_ARTHROPODS)
            .registerSupplier(EnchantmentType.class, "binding_curse", () -> (EnchantmentType) Enchantments.BINDING_CURSE)
            .registerSupplier(EnchantmentType.class, "blast_protection", () -> (EnchantmentType) Enchantments.BLAST_PROTECTION)
            .registerSupplier(EnchantmentType.class, "channeling", () -> (EnchantmentType) Enchantments.CHANNELING)
            .registerSupplier(EnchantmentType.class, "depth_strider", () -> (EnchantmentType) Enchantments.DEPTH_STRIDER)
            .registerSupplier(EnchantmentType.class, "efficiency", () -> (EnchantmentType) Enchantments.BLOCK_EFFICIENCY)
            .registerSupplier(EnchantmentType.class, "feather_falling", () -> (EnchantmentType) Enchantments.FALL_PROTECTION)
            .registerSupplier(EnchantmentType.class, "fire_aspect", () -> (EnchantmentType) Enchantments.FIRE_ASPECT)
            .registerSupplier(EnchantmentType.class, "fire_protection", () -> (EnchantmentType) Enchantments.FIRE_PROTECTION)
            .registerSupplier(EnchantmentType.class, "flame", () -> (EnchantmentType) Enchantments.FLAMING_ARROWS)
            .registerSupplier(EnchantmentType.class, "fortune", () -> (EnchantmentType) Enchantments.BLOCK_FORTUNE)
            .registerSupplier(EnchantmentType.class, "frost_walker", () -> (EnchantmentType) Enchantments.FROST_WALKER)
            .registerSupplier(EnchantmentType.class, "impaling", () -> (EnchantmentType) Enchantments.IMPALING)
            .registerSupplier(EnchantmentType.class, "infinity", () -> (EnchantmentType) Enchantments.INFINITY_ARROWS)
            .registerSupplier(EnchantmentType.class, "knockback", () -> (EnchantmentType) Enchantments.KNOCKBACK)
            .registerSupplier(EnchantmentType.class, "looting", () -> (EnchantmentType) Enchantments.MOB_LOOTING)
            .registerSupplier(EnchantmentType.class, "loyalty", () -> (EnchantmentType) Enchantments.LOYALTY)
            .registerSupplier(EnchantmentType.class, "luck_of_the_sea", () -> (EnchantmentType) Enchantments.FISHING_LUCK)
            .registerSupplier(EnchantmentType.class, "lure", () -> (EnchantmentType) Enchantments.FISHING_SPEED)
            .registerSupplier(EnchantmentType.class, "mending", () -> (EnchantmentType) Enchantments.MENDING)
            .registerSupplier(EnchantmentType.class, "multishot", () -> (EnchantmentType) Enchantments.MULTISHOT)
            .registerSupplier(EnchantmentType.class, "piercing", () -> (EnchantmentType) Enchantments.PIERCING)
            .registerSupplier(EnchantmentType.class, "power", () -> (EnchantmentType) Enchantments.POWER_ARROWS)
            .registerSupplier(EnchantmentType.class, "projectile_protection", () -> (EnchantmentType) Enchantments.PROJECTILE_PROTECTION)
            .registerSupplier(EnchantmentType.class, "protection", () -> (EnchantmentType) Enchantments.ALL_DAMAGE_PROTECTION)
            .registerSupplier(EnchantmentType.class, "punch", () -> (EnchantmentType) Enchantments.PUNCH_ARROWS)
            .registerSupplier(EnchantmentType.class, "quick_charge", () -> (EnchantmentType) Enchantments.QUICK_CHARGE)
            .registerSupplier(EnchantmentType.class, "respiration", () -> (EnchantmentType) Enchantments.RESPIRATION)
            .registerSupplier(EnchantmentType.class, "riptide", () -> (EnchantmentType) Enchantments.RIPTIDE)
            .registerSupplier(EnchantmentType.class, "sharpness", () -> (EnchantmentType) Enchantments.SHARPNESS)
            .registerSupplier(EnchantmentType.class, "silk_touch", () -> (EnchantmentType) Enchantments.SILK_TOUCH)
            .registerSupplier(EnchantmentType.class, "smite", () -> (EnchantmentType) Enchantments.SMITE)
            .registerSupplier(EnchantmentType.class, "soul_speed", () -> (EnchantmentType) Enchantments.SOUL_SPEED)
            .registerSupplier(EnchantmentType.class, "sweeping", () -> (EnchantmentType) Enchantments.SWEEPING_EDGE)
            .registerSupplier(EnchantmentType.class, "thorns", () -> (EnchantmentType) Enchantments.THORNS)
            .registerSupplier(EnchantmentType.class, "unbreaking", () -> (EnchantmentType) Enchantments.UNBREAKING)
            .registerSupplier(EnchantmentType.class, "vanishing_curse", () -> (EnchantmentType) Enchantments.VANISHING_CURSE)
        ;
    }
}
