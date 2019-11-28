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
package org.spongepowered.common.registry.type.block;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.state.IntegerStateProperties;
import org.spongepowered.api.state.IntegerStateProperty;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixCheckCatalogRegistryModule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RegisterCatalog(IntegerStateProperties.class)
public final class IntegerTraitRegistryModule
    extends AbstractPrefixCheckCatalogRegistryModule<IntegerStateProperty>
        implements SpongeAdditionalCatalogRegistryModule<IntegerStateProperty>, AlternateCatalogRegistryModule<IntegerStateProperty> {

    public static IntegerTraitRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(IntegerStateProperty extraCatalog) {
        this.catalogTypeMap.put(extraCatalog.getId().toLowerCase(Locale.ENGLISH), extraCatalog);
    }

    public void registerBlock(String id, BlockType block, IntegerStateProperty property) {
        checkNotNull(id, "Id was null!");
        this.catalogTypeMap.put(id.toLowerCase(Locale.ENGLISH), property);
        final String propertyId = block.getId().toLowerCase(Locale.ENGLISH) + "_" + property.getName().toLowerCase(Locale.ENGLISH);
        this.catalogTypeMap.put(propertyId, property);
    }

    IntegerTraitRegistryModule() {
        super("minecraft");
    }

    @Override
    public Map<String, IntegerStateProperty> provideCatalogMap() {
        Map<String, IntegerStateProperty> map = new HashMap<>();
        for (Map.Entry<String, IntegerStateProperty> enumTraitEntry : this.catalogTypeMap.entrySet()) {
            map.put(enumTraitEntry.getKey().replace("minecraft:", ""), enumTraitEntry.getValue());
        }
        return map;
    }

    private static final class Holder {
        final static IntegerTraitRegistryModule INSTANCE = new IntegerTraitRegistryModule();
    }
}
