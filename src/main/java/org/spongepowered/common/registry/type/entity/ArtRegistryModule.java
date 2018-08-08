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
package org.spongepowered.common.registry.type.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.entity.item.EntityPainting;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.type.Arts;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.type.MinecraftEnumBasedCatalogTypeModule;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ArtRegistryModule extends MinecraftEnumBasedCatalogTypeModule<EntityPainting.EnumArt, Art> implements CatalogRegistryModule<Art> {

    @Override
    public void registerDefaults() {
        for (EntityPainting.EnumArt art : EntityPainting.EnumArt.values()) {
            this.map.put(((Art) (Object) art).getKey(), (Art) (Object) art);
        }
    }

    @CustomCatalogRegistration
    public void customRegistration() {
        registerDefaults();
        RegistryHelper.mapFields(Arts.class, field -> {
            String name = field.replace("_", "");
            return this.map.get(CatalogKey.minecraft(name));
        });
    }

    @AdditionalRegistration
    public void registerAdditionals() {
        for (EntityPainting.EnumArt art : EntityPainting.EnumArt.values()) {
            if (!this.map.containsValue(art)) {
                this.map.put(((Art) (Object) art).getKey(), (Art) (Object) art);
            }
        }
    }

    @Override
    protected EntityPainting.EnumArt[] getValues() {
        return EntityPainting.EnumArt.values();
    }
}
