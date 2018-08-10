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
package org.spongepowered.common.registry.type;

import net.minecraft.item.EnumDyeColor;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

import java.util.Optional;

@RegisterCatalog(DyeColors.class)
public final class DyeColorRegistryModule extends AbstractCatalogRegistryModule<DyeColor> {

    public static DyeColorRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void registerDefaults() {
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            this.map.put(CatalogKey.minecraft(dyeColor.getName()), (DyeColor) (Object) dyeColor);
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (EnumDyeColor dyeColor : EnumDyeColor.values()) {
            if (!this.map.containsValue(dyeColor)) {
                this.map.put(CatalogKey.minecraft(dyeColor.getName()), (DyeColor) (Object) dyeColor);
            }
        }
    }

    public static Optional<DyeColor> fromId(int id) {
        for (EnumDyeColor color : EnumDyeColor.values()) {
            if (color.getDyeDamage() == id) {
                return Optional.of((DyeColor) (Object) color);
            }
        }
        return Optional.empty();
    }

    DyeColorRegistryModule() { }

    private static final class Holder {
        static final DyeColorRegistryModule INSTANCE = new DyeColorRegistryModule();
    }
}
