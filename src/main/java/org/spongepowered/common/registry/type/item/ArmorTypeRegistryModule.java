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

import net.minecraft.item.ArmorItem;
import org.spongepowered.api.data.type.ArmorType;
import org.spongepowered.api.data.type.ArmorTypes;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.type.MinecraftEnumBasedAlternateCatalogTypeRegistryModule;

@RegisterCatalog(ArmorTypes.class)
public class ArmorTypeRegistryModule extends MinecraftEnumBasedAlternateCatalogTypeRegistryModule<ArmorItem.ArmorMaterial, ArmorType> {

    public ArmorTypeRegistryModule() {
        super(new String[] {"minecraft"}, id -> id.equals("chainmail") ? "chain" : id);
    }

    @Override
    protected ArmorItem.ArmorMaterial[] getValues() {
        return ArmorItem.ArmorMaterial.values();
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (ArmorItem.ArmorMaterial armorMaterial : ArmorItem.ArmorMaterial.values()) {
            if (!this.catalogTypeMap.containsKey(enumAs(armorMaterial).getId())) {
                this.catalogTypeMap.put(enumAs(armorMaterial).getId(), enumAs(armorMaterial));
            }
        }
    }
}
