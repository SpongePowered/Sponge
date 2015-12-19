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
package org.spongepowered.common.registry.type.effect;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PotionEffectTypeRegistryModule implements CatalogRegistryModule<PotionEffectType> {

    private final List<PotionEffectType> potionList = new ArrayList<>();

    @RegisterCatalog(PotionEffectTypes.class)
    private final Map<String, PotionEffectType> potionEffectTypeMap = new HashMap<>();

    @Override
    public Optional<PotionEffectType> getById(String id) {
        return Optional.ofNullable(this.potionEffectTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<PotionEffectType> getAll() {
        return ImmutableList.copyOf(this.potionList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerDefaults() {
        for (Potion potion : Potion.potionTypes) {
            if (potion != null) {
                PotionEffectType potionEffectType = (PotionEffectType) potion;
                this.potionList.add(potionEffectType);
                this.potionEffectTypeMap.put(potion.getName().toLowerCase(), potionEffectType);
            }
        }
        ((Map<ResourceLocation, Potion>) Potion.field_180150_I).entrySet().stream()
            .filter(entry -> !this.potionEffectTypeMap.containsKey(entry.getKey().getResourcePath().toLowerCase()))
            .forEach(entry -> {
                this.potionList.add((PotionEffectType) entry.getValue());
                this.potionEffectTypeMap.put(entry.getKey().getResourcePath().toLowerCase(), (PotionEffectType) entry.getValue());
            });
    }

    @SuppressWarnings("unchecked")
    @AdditionalRegistration
    public void additionalRegistration() { // I'm guessing that this should work very well.
        for (Potion potion : Potion.potionTypes) {
            if (potion != null) {
                PotionEffectType potionEffectType = (PotionEffectType) potion;
                if (!this.potionList.contains(potionEffectType)) {
                    this.potionList.add(potionEffectType);
                    this.potionEffectTypeMap.put(potion.getName().toLowerCase(), potionEffectType);
                }
            }
        }
        ((Map<ResourceLocation, Potion>) Potion.field_180150_I).entrySet().stream()
            .filter(entry -> !this.potionEffectTypeMap.containsKey(entry.getKey().getResourcePath().toLowerCase()))
            .forEach(entry -> {
                this.potionList.add((PotionEffectType) entry.getValue());
                this.potionEffectTypeMap.put(entry.getKey().getResourcePath().toLowerCase(), (PotionEffectType) entry.getValue());
            });
    }
}
