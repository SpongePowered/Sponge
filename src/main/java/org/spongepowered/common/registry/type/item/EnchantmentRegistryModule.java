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
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@RegisterCatalog(EnchantmentTypes.class)
public final class EnchantmentRegistryModule extends AbstractCatalogRegistryModule<EnchantmentType> implements SpongeAdditionalCatalogRegistryModule<EnchantmentType>, AlternateCatalogRegistryModule<EnchantmentType> {

    public static EnchantmentRegistryModule getInstance() {
        return Holder.INSTANCE;
    }


    @Override
    public void registerDefaults() {
        for (ResourceLocation key: net.minecraft.enchantment.Enchantment.REGISTRY.getKeys()) {
            this.map.put((CatalogKey) (Object) key, (EnchantmentType) net.minecraft.enchantment.Enchantment.REGISTRY.get(key));
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (ResourceLocation key : net.minecraft.enchantment.Enchantment.REGISTRY.getKeys()) {
            net.minecraft.enchantment.Enchantment enchantment = net.minecraft.enchantment.Enchantment.REGISTRY.get(key);
            if (enchantment == null) {
                continue;
            }
            if (!this.map.containsValue(enchantment)) {
                this.map.put((CatalogKey) (Object) key, (EnchantmentType) enchantment);
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
        this.map.put(extraCatalog.getKey(), extraCatalog);
    }

    public void registerFromGameData(ResourceLocation s, EnchantmentType obj) {
        checkNotNull(obj, "EnchantmentType cannot be null!");
        this.map.put((CatalogKey) (Object) s, obj);
    }

    static final class Holder {
        static final EnchantmentRegistryModule INSTANCE = new EnchantmentRegistryModule();
    }
}
