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
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.item.EnchantmentRegistryModule.Holder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class EnchantmentRegistryModule implements SpongeAdditionalCatalogRegistryModule<EnchantmentType>, AlternateCatalogRegistryModule<EnchantmentType> {

    public static EnchantmentRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @RegisterCatalog(EnchantmentTypes.class)
    private final Map<String, EnchantmentType> enchantmentMappings = new HashMap<>();

    @Override
    public Optional<EnchantmentType> getById(String id) {
        checkNotNull(id);
        if (!id.contains(":")) {
            id = "minecraft:" + id; // assume vanilla
        }
        return Optional.ofNullable(this.enchantmentMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<EnchantmentType> getAll() {
        return ImmutableList.copyOf(this.enchantmentMappings.values());
    }

    @Override
    public Map<String, EnchantmentType> provideCatalogMap() {
        Map<String, EnchantmentType> newMap = new HashMap<>();
        for (Map.Entry<String, EnchantmentType> entry : this.enchantmentMappings.entrySet()) {
            newMap.put(entry.getKey().replace("minecraft:", ""), entry.getValue());
        }
        return newMap;
    }

    @Override
    public void registerDefaults() {
        for (ResourceLocation key: net.minecraft.enchantment.Enchantment.field_185264_b.keySet()) {
            this.enchantmentMappings.put(key.toString(), (EnchantmentType) net.minecraft.enchantment.Enchantment.field_185264_b.getOrDefault(key));
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (ResourceLocation key: net.minecraft.enchantment.Enchantment.field_185264_b.keySet()) {
            net.minecraft.enchantment.Enchantment enchantment = net.minecraft.enchantment.Enchantment.field_185264_b.getOrDefault(key);
            if (enchantment == null) {
                continue;
            }
            if (!this.enchantmentMappings.containsValue(enchantment)) {
                final String name = enchantment.getName().replace("enchantment.", "");
                this.enchantmentMappings.put(name.toLowerCase(Locale.ENGLISH), (EnchantmentType) enchantment);
            }
        }

    }

    EnchantmentRegistryModule() {
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(EnchantmentType extraCatalog) {
        checkNotNull(extraCatalog, "EnchantmentType cannot be null!");
        this.enchantmentMappings.put(extraCatalog.getId(), extraCatalog);
    }

    public void registerFromGameData(String s, EnchantmentType obj) {
        checkNotNull(obj, "EnchantmentType cannot be null!");
        this.enchantmentMappings.put(s, obj);
    }

    static final class Holder {
        static final EnchantmentRegistryModule INSTANCE = new EnchantmentRegistryModule();
    }
}
