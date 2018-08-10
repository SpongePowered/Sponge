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
package org.spongepowered.common.registry.type.event;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.event.damage.SpongeDamageModifierType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@RegisterCatalog(DamageModifierTypes.class)
public class DamageModifierTypeRegistryModule extends AbstractCatalogRegistryModule<DamageModifierType>
    implements CatalogRegistryModule<DamageModifierType> {

    @Override
    public void registerDefaults() {
        // TODO - convert these to properly use the sponge id.
        register(CatalogKey.minecraft("weapon_enchantment"), new SpongeDamageModifierType("Weapon Enchantment", "weapon_enchantment"));
        register(CatalogKey.minecraft("offensive_potion_effect"), new SpongeDamageModifierType("Offensive PotionEffect", "offensive_potion_effect"));
        register(CatalogKey.minecraft("defensive_potion_effect"), new SpongeDamageModifierType("Defensive PotionEffect", "defensive_potion_effect"));
        register(CatalogKey.minecraft("negative_potion_effect"), new SpongeDamageModifierType("Negative PotionEffect", "negative_potion_effect"));
        register(CatalogKey.minecraft("hard_hat"), new SpongeDamageModifierType("Hard Hat", "hard_hat"));
        final SpongeDamageModifierType shield = new SpongeDamageModifierType("Shield", "shield");
        register(CatalogKey.minecraft("shield"), shield);
        register(CatalogKey.minecraft("armor"), new SpongeDamageModifierType("Armor", "armor"));
        register(CatalogKey.minecraft("armor_enchantment"), new SpongeDamageModifierType("Armor Enchantment", "armor_enchantment"));
        register(CatalogKey.minecraft("magic"), new SpongeDamageModifierType("Magic", "magic"));
        register(CatalogKey.minecraft("difficulty"), new SpongeDamageModifierType("Difficulty", "difficulty"));
        register(CatalogKey.minecraft("absorption"), new SpongeDamageModifierType("Absorption", "absorption"));
        register(CatalogKey.minecraft("critical_hit"), new SpongeDamageModifierType("Critical Hit", "critical_hit"));
        register(CatalogKey.minecraft("attack_cooldown"), new SpongeDamageModifierType("Attack Cooldown", "attack_cooldown"));
        final DamageModifierType sweeping = new SpongeDamageModifierType("Sweeping", "sweeping");
        register(CatalogKey.minecraft("sweeping"), sweeping);
    }
}
