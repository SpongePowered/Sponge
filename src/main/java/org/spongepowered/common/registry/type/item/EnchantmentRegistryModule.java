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
package org.spongepowered.common.registry.type.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.Enchantments;
import org.spongepowered.common.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.AlternateCatalogRegistryModule;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.AdditionalRegistration;
import org.spongepowered.common.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EnchantmentRegistryModule implements AlternateCatalogRegistryModule<Enchantment> {

    @RegisterCatalog(Enchantments.class)
    private final Map<String, Enchantment> enchantmentMappings = new HashMap<>();

    @Override
    public Optional<Enchantment> getById(String id) {
        return Optional.ofNullable(this.enchantmentMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<Enchantment> getAll() {
        return ImmutableList.copyOf(this.enchantmentMappings.values());
    }

    @Override
    public Map<String, Enchantment> provideCatalogMap() {
        Map<String, Enchantment> newMap = new HashMap<>();
        for (Map.Entry<String, Enchantment> entry : this.enchantmentMappings.entrySet()) {
            newMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return newMap;
    }

    @Override
    public void registerDefaults() {
        this.enchantmentMappings.put("protection", (Enchantment) net.minecraft.enchantment.Enchantment.protection);
        this.enchantmentMappings.put("fire_protection", (Enchantment) net.minecraft.enchantment.Enchantment.fireProtection);
        this.enchantmentMappings.put("feather_falling", (Enchantment) net.minecraft.enchantment.Enchantment.featherFalling);
        this.enchantmentMappings.put("blast_protection", (Enchantment) net.minecraft.enchantment.Enchantment.blastProtection);
        this.enchantmentMappings.put("projectile_protection", (Enchantment) net.minecraft.enchantment.Enchantment.projectileProtection);
        this.enchantmentMappings.put("respiration", (Enchantment) net.minecraft.enchantment.Enchantment.respiration);
        this.enchantmentMappings.put("aqua_affinity", (Enchantment) net.minecraft.enchantment.Enchantment.aquaAffinity);
        this.enchantmentMappings.put("thorns", (Enchantment) net.minecraft.enchantment.Enchantment.thorns);
        this.enchantmentMappings.put("depth_strider", (Enchantment) net.minecraft.enchantment.Enchantment.depthStrider);
        this.enchantmentMappings.put("sharpness", (Enchantment) net.minecraft.enchantment.Enchantment.sharpness);
        this.enchantmentMappings.put("smite", (Enchantment) net.minecraft.enchantment.Enchantment.smite);
        this.enchantmentMappings.put("bane_of_arthropods", (Enchantment) net.minecraft.enchantment.Enchantment.baneOfArthropods);
        this.enchantmentMappings.put("knockback", (Enchantment) net.minecraft.enchantment.Enchantment.knockback);
        this.enchantmentMappings.put("fire_aspect", (Enchantment) net.minecraft.enchantment.Enchantment.fireAspect);
        this.enchantmentMappings.put("looting", (Enchantment) net.minecraft.enchantment.Enchantment.looting);
        this.enchantmentMappings.put("efficiency", (Enchantment) net.minecraft.enchantment.Enchantment.efficiency);
        this.enchantmentMappings.put("silk_touch", (Enchantment) net.minecraft.enchantment.Enchantment.silkTouch);
        this.enchantmentMappings.put("unbreaking", (Enchantment) net.minecraft.enchantment.Enchantment.unbreaking);
        this.enchantmentMappings.put("fortune", (Enchantment) net.minecraft.enchantment.Enchantment.fortune);
        this.enchantmentMappings.put("power", (Enchantment) net.minecraft.enchantment.Enchantment.power);
        this.enchantmentMappings.put("punch", (Enchantment) net.minecraft.enchantment.Enchantment.punch);
        this.enchantmentMappings.put("flame", (Enchantment) net.minecraft.enchantment.Enchantment.flame);
        this.enchantmentMappings.put("infinity", (Enchantment) net.minecraft.enchantment.Enchantment.infinity);
        this.enchantmentMappings.put("luck_of_the_sea", (Enchantment) net.minecraft.enchantment.Enchantment.luckOfTheSea);
        this.enchantmentMappings.put("lure", (Enchantment) net.minecraft.enchantment.Enchantment.lure);
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (net.minecraft.enchantment.Enchantment enchantment : net.minecraft.enchantment.Enchantment.enchantmentsList) {
            if (enchantment == null) {
                continue;
            }
            if (!this.enchantmentMappings.containsValue((Enchantment) enchantment)) {
                final String name = enchantment.getName().replace("enchantment.", "");
                this.enchantmentMappings.put(name.toLowerCase(), (Enchantment) enchantment);
            }
        }

    }
}
