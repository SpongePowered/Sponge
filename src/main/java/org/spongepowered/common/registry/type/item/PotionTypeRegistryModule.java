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
import org.spongepowered.api.item.potion.PotionType;
import org.spongepowered.api.item.potion.PotionTypes;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class PotionTypeRegistryModule implements SpongeAdditionalCatalogRegistryModule<PotionType>,
        AlternateCatalogRegistryModule<PotionType> {

    public static PotionTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private final List<PotionType> potionList = new ArrayList<>();

    @RegisterCatalog(PotionTypes.class)
    private final Map<String, PotionType> potionTypeMap = new HashMap<>();

    @Override
    public Map<String, PotionType> provideCatalogMap() {
        Map<String, PotionType> potionTypeMap = new HashMap<>();
        for (Map.Entry<String, PotionType> entry : this.potionTypeMap.entrySet()) {
            potionTypeMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return potionTypeMap;
    }


    @Override
    public Optional<PotionType> getById(String id) {
        if (!checkNotNull(id).contains(":")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.potionTypeMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<PotionType> getAll() {
        return ImmutableList.copyOf(this.potionList);
    }

    @Override
    public void registerDefaults() {
        for (net.minecraft.potion.Potion potion : net.minecraft.potion.Potion.field_185176_a) {
            if (potion != null) {
                PotionType potionType = (PotionType) potion;
                this.potionList.add(potionType);
                this.potionTypeMap.put(net.minecraft.potion.Potion.field_185176_a.func_177774_c(potion).toString(), potionType);
            }
        }
    }

    @AdditionalRegistration
    public void additionalRegistration() { // I'm guessing that this should work very well.
        for (net.minecraft.potion.Potion potion : net.minecraft.potion.Potion.field_185176_a) {
            if (potion != null) {
                PotionType potionType = (PotionType) potion;
                if (!this.potionList.contains(potionType)) {
                    this.potionList.add(potionType);
                    this.potionTypeMap.put(net.minecraft.potion.Potion.field_185176_a.func_177774_c(potion).toString(), potionType);
                }
            }
        }
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(PotionType extraCatalog) {
    }

    public void registerFromGameData(String id, PotionType itemType) {
        this.potionTypeMap.put(id.toLowerCase(Locale.ENGLISH), itemType);
    }

    PotionTypeRegistryModule() {

    }

    private static final class Holder {
        static final PotionTypeRegistryModule INSTANCE = new PotionTypeRegistryModule();
    }
}
