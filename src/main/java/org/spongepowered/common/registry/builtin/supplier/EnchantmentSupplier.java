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
package org.spongepowered.common.registry.builtin.supplier;

import net.minecraft.enchantment.Enchantments;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class EnchantmentSupplier {

    private EnchantmentSupplier() {
    }

    public static void registerSuppliers(SpongeCatalogRegistry registry) {
        registry
            .registerSupplier(EnchantmentType.class, "protection", () -> (EnchantmentType) Enchantments.PROTECTION)
            .registerSupplier(EnchantmentType.class, "fire_protection", () -> (EnchantmentType) Enchantments.FIRE_PROTECTION)
            .registerSupplier(EnchantmentType.class, "feather_falling", () -> (EnchantmentType) Enchantments.FEATHER_FALLING)
            .registerSupplier(EnchantmentType.class, "blast_protection", () -> (EnchantmentType) Enchantments.BLAST_PROTECTION)
            .registerSupplier(EnchantmentType.class, "projectile_protection", () -> (EnchantmentType) Enchantments.PROJECTILE_PROTECTION)
            .registerSupplier(EnchantmentType.class, "respiration", () -> (EnchantmentType) Enchantments.RESPIRATION)
            .registerSupplier(EnchantmentType.class, "aqua_affinity", () -> (EnchantmentType) Enchantments.AQUA_AFFINITY)
            .registerSupplier(EnchantmentType.class, "thorns", () -> (EnchantmentType) Enchantments.THORNS)
            .registerSupplier(EnchantmentType.class, "depth_strider", () -> (EnchantmentType) Enchantments.DEPTH_STRIDER)
            .registerSupplier(EnchantmentType.class, "frost_walker", () -> (EnchantmentType) Enchantments.FROST_WALKER)
            .registerSupplier(EnchantmentType.class, "binding_curse", () -> (EnchantmentType) Enchantments.BINDING_CURSE)
            .registerSupplier(EnchantmentType.class, "sharpness", () -> (EnchantmentType) Enchantments.SHARPNESS)
            .registerSupplier(EnchantmentType.class, "smite", () -> (EnchantmentType) Enchantments.SMITE)
            .registerSupplier(EnchantmentType.class, "bane_of_arthropods", () -> (EnchantmentType) Enchantments.BANE_OF_ARTHROPODS)
            .registerSupplier(EnchantmentType.class, "knockback", () -> (EnchantmentType) Enchantments.KNOCKBACK)
            .registerSupplier(EnchantmentType.class, "fire_aspect", () -> (EnchantmentType) Enchantments.FIRE_ASPECT)
            .registerSupplier(EnchantmentType.class, "looting", () -> (EnchantmentType) Enchantments.LOOTING)
            .registerSupplier(EnchantmentType.class, "sweeping", () -> (EnchantmentType) Enchantments.SWEEPING)
            .registerSupplier(EnchantmentType.class, "efficiency", () -> (EnchantmentType) Enchantments.EFFICIENCY)
            .registerSupplier(EnchantmentType.class, "silk_touch", () -> (EnchantmentType) Enchantments.SILK_TOUCH)
            .registerSupplier(EnchantmentType.class, "unbreaking", () -> (EnchantmentType) Enchantments.UNBREAKING)
            .registerSupplier(EnchantmentType.class, "fortune", () -> (EnchantmentType) Enchantments.FORTUNE)
            .registerSupplier(EnchantmentType.class, "power", () -> (EnchantmentType) Enchantments.POWER)
            .registerSupplier(EnchantmentType.class, "punch", () -> (EnchantmentType) Enchantments.PUNCH)
            .registerSupplier(EnchantmentType.class, "flame", () -> (EnchantmentType) Enchantments.FLAME)
            .registerSupplier(EnchantmentType.class, "infinity", () -> (EnchantmentType) Enchantments.INFINITY)
            .registerSupplier(EnchantmentType.class, "luck_of_the_sea", () -> (EnchantmentType) Enchantments.LUCK_OF_THE_SEA)
            .registerSupplier(EnchantmentType.class, "lure", () -> (EnchantmentType) Enchantments.LURE)
            .registerSupplier(EnchantmentType.class, "loyalty", () -> (EnchantmentType) Enchantments.LOYALTY)
            .registerSupplier(EnchantmentType.class, "impaling", () -> (EnchantmentType) Enchantments.IMPALING)
            .registerSupplier(EnchantmentType.class, "riptide", () -> (EnchantmentType) Enchantments.RIPTIDE)
            .registerSupplier(EnchantmentType.class, "channeling", () -> (EnchantmentType) Enchantments.CHANNELING)
            .registerSupplier(EnchantmentType.class, "multishot", () -> (EnchantmentType) Enchantments.MULTISHOT)
            .registerSupplier(EnchantmentType.class, "quick_charge", () -> (EnchantmentType) Enchantments.QUICK_CHARGE)
            .registerSupplier(EnchantmentType.class, "piercing", () -> (EnchantmentType) Enchantments.PIERCING)
            .registerSupplier(EnchantmentType.class, "mending", () -> (EnchantmentType) Enchantments.MENDING)
            .registerSupplier(EnchantmentType.class, "vanishing_curse", () -> (EnchantmentType) Enchantments.VANISHING_CURSE)
        ;
    }
}
