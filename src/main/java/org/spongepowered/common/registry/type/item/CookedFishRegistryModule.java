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
import net.minecraft.item.ItemFishFood;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.CookedFishes;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeCookedFish;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CookedFishRegistryModule implements AlternateCatalogRegistryModule<CookedFish> {

    @RegisterCatalog(CookedFishes.class)
    private final Map<String, CookedFish> fishMap = new HashMap<>();

    @Override
    public Map<String, CookedFish> provideCatalogMap() {
        Map<String, CookedFish> fishMap = new HashMap<>();
        for (Map.Entry<String, CookedFish> entry : this.fishMap.entrySet()) {
            fishMap.put(entry.getKey().replace("cooked.", ""), entry.getValue());
        }
        return fishMap;
    }

    @Override
    public Optional<CookedFish> getById(String id) {
        return Optional.ofNullable(this.fishMap.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<CookedFish> getAll() {
        return ImmutableList.copyOf(this.fishMap.values());
    }

    @Override
    public void registerDefaults() {
        for (ItemFishFood.FishType fishType : ItemFishFood.FishType.values()) {
            if (fishType.func_150973_i()) {
                CookedFish cooked = new SpongeCookedFish(fishType.name(),
                        new SpongeTranslation("item.fish." + fishType.func_150972_b() + ".cooked.name"), fishType);
                this.fishMap.put(cooked.getId().toLowerCase(Locale.ENGLISH), cooked);
            }
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (ItemFishFood.FishType fishType : ItemFishFood.FishType.values()) {
            if (fishType.func_150973_i() && !this.fishMap.containsKey(fishType.name().toLowerCase(Locale.ENGLISH))) {
                CookedFish cooked = new SpongeCookedFish(fishType.name(),
                        new SpongeTranslation("item.fish." + fishType.func_150972_b() + ".cooked.name"), fishType);
                this.fishMap.put(cooked.getId().toLowerCase(Locale.ENGLISH), cooked);
            }
        }
    }

}
