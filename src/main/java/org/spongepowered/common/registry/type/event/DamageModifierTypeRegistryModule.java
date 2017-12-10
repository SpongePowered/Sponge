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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierType;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.event.damage.SpongeDamageModifierType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DamageModifierTypeRegistryModule implements CatalogRegistryModule<DamageModifierType> {

    @RegisterCatalog(DamageModifierTypes.class)
    private final Map<String, DamageModifierType> modifierTypeMap = new HashMap<>();

    @Override
    public Optional<DamageModifierType> getById(String id) {
        return Optional.ofNullable(this.modifierTypeMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<DamageModifierType> getAll() {
        return ImmutableList.copyOf(this.modifierTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        // TODO - convert these to properly use the sponge id.
        this.modifierTypeMap.put("weapon_enchantment", new SpongeDamageModifierType("Weapon Enchantment", "weapon_enchantment"));
        this.modifierTypeMap.put("offensive_potion_effect", new SpongeDamageModifierType("Offensive PotionEffect", "offensive_potion_effect"));
        this.modifierTypeMap.put("defensive_potion_effect", new SpongeDamageModifierType("Defensive PotionEffect", "defensive_potion_effect"));
        this.modifierTypeMap.put("negative_potion_effect", new SpongeDamageModifierType("Negative PotionEffect", "negative_potion_effect"));
        this.modifierTypeMap.put("hard_hat", new SpongeDamageModifierType("Hard Hat", "hard_hat"));
        this.modifierTypeMap.put("shield", new SpongeDamageModifierType("Shield", "shield"));
        this.modifierTypeMap.put("blocking", this.modifierTypeMap.get("shield"));
        this.modifierTypeMap.put("armor", new SpongeDamageModifierType("Armor", "armor"));
        this.modifierTypeMap.put("armor_enchantment", new SpongeDamageModifierType("Armor Enchantment", "armor_enchantment"));
        this.modifierTypeMap.put("magic", new SpongeDamageModifierType("Magic", "magic"));
        this.modifierTypeMap.put("difficulty", new SpongeDamageModifierType("Difficulty", "difficulty"));
        this.modifierTypeMap.put("absorption", new SpongeDamageModifierType("Absorption", "absorption"));
        this.modifierTypeMap.put("critical_hit", new SpongeDamageModifierType("Critical Hit", "critical_hit"));
        this.modifierTypeMap.put("attack_cooldown", new SpongeDamageModifierType("Attack Cooldown", "attack_cooldown"));
        final DamageModifierType sweeping = new SpongeDamageModifierType("Sweeping", "sweeping");
        this.modifierTypeMap.put("sweeping", sweeping);
        this.modifierTypeMap.put("sweaping", sweeping); // TODO: remove
    }
}
