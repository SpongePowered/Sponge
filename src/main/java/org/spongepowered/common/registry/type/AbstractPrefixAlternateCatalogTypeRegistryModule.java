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

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

public abstract class AbstractPrefixAlternateCatalogTypeRegistryModule<T extends CatalogType>
        extends AbstractPrefixCheckCatalogRegistryModule<T>
        implements AlternateCatalogRegistryModule<T> {

    private final String[] modIdToFilter;
    @Nullable private final Function<String, String> catalogIdModifierFunction;

    protected AbstractPrefixAlternateCatalogTypeRegistryModule(String defaultModId) {
        this(defaultModId, new String[] {defaultModId + ":"});
    }

    protected AbstractPrefixAlternateCatalogTypeRegistryModule(String defaultModIdToPrepend, String[] array) {
        this(defaultModIdToPrepend, array, null);
    }

    protected AbstractPrefixAlternateCatalogTypeRegistryModule(String defaultModIdToPrepend, String[] array,
            @Nullable Function<String, String> catalogIdModifierFunction) {
        super(defaultModIdToPrepend);
        this.modIdToFilter = array;
        this.catalogIdModifierFunction = catalogIdModifierFunction;
    }

    @Override
    public Map<String, T> provideCatalogMap() {
        final HashMap<String, T> map = new HashMap<>();
        for (Map.Entry<String, T> entry : this.catalogTypeMap.entrySet()) {
            String catalogId = entry.getKey();
            catalogId = catalogId.replace(this.defaultModIdToPrepend + ":", "");
            for (String s : this.modIdToFilter) {
                catalogId = catalogId.replace(s, "");
            }
            if (this.catalogIdModifierFunction != null) {
                catalogId = this.catalogIdModifierFunction.apply(catalogId);
            }
            map.put(catalogId, entry.getValue());
            map.put(entry.getValue().getName().toLowerCase(), entry.getValue());
        }
        return map;
    }
}
