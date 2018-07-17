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

import net.minecraft.creativetab.CreativeTabs;
import org.spongepowered.api.item.ItemGroup;
import org.spongepowered.api.item.ItemGroups;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ItemGroupRegistryModule implements CatalogRegistryModule<ItemGroup> {

    @RegisterCatalog(ItemGroups.class)
    private final Map<String, ItemGroup> itemGroupMap = new HashMap<>();

    @Override
    public Optional<ItemGroup> getById(String id) {
        String key = checkNotNull(id).toLowerCase(Locale.ENGLISH);
        return Optional.ofNullable(this.itemGroupMap.get(key));
    }

    @Override
    public Collection<ItemGroup> getAll() {
        return Collections.unmodifiableCollection(this.itemGroupMap.values());
    }

    @Override
    public void registerDefaults() {
        this.itemGroupMap.put("brewing", (ItemGroup) CreativeTabs.BREWING);
        this.itemGroupMap.put("building_blocks", (ItemGroup) CreativeTabs.BUILDING_BLOCKS);
        this.itemGroupMap.put("combat", (ItemGroup) CreativeTabs.COMBAT);
        this.itemGroupMap.put("decorations", (ItemGroup) CreativeTabs.DECORATIONS);
        this.itemGroupMap.put("food", (ItemGroup) CreativeTabs.FOOD);
        this.itemGroupMap.put("materials", (ItemGroup) CreativeTabs.MATERIALS);
        this.itemGroupMap.put("misc", (ItemGroup) CreativeTabs.MISC);
        this.itemGroupMap.put("redstone", (ItemGroup) CreativeTabs.REDSTONE);
        this.itemGroupMap.put("tools", (ItemGroup) CreativeTabs.TOOLS);
        this.itemGroupMap.put("transportation", (ItemGroup) CreativeTabs.TRANSPORTATION);
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (ItemGroup itemGroup : this.itemGroupMap.values()) {
            String key = itemGroup.getId().toLowerCase(Locale.ENGLISH);
            if (!this.itemGroupMap.containsKey(key)) {
                this.itemGroupMap.put(key, itemGroup);
            }
        }
    }
}
