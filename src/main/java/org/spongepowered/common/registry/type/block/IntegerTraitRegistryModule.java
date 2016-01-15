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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.IntegerTrait;
import org.spongepowered.api.block.trait.IntegerTraits;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class IntegerTraitRegistryModule implements SpongeAdditionalCatalogRegistryModule<IntegerTrait>, AlternateCatalogRegistryModule<IntegerTrait> {

    @RegisterCatalog(IntegerTraits.class)
    private Map<String, IntegerTrait> integerTraitMap = new HashMap<>();

    public static IntegerTraitRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(IntegerTrait extraCatalog) {
        this.integerTraitMap.put(extraCatalog.getId().toLowerCase(), extraCatalog);
    }

    @Override
    public Optional<IntegerTrait> getById(String id) {
        return Optional.ofNullable(this.integerTraitMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<IntegerTrait> getAll() {
        return ImmutableList.copyOf(this.integerTraitMap.values());
    }

    public void registerBlock(String id, BlockType block, IntegerTrait property) {
        checkNotNull(id, "Id was null!");
        checkNotNull(block, "Block was null!");
        checkNotNull(property, "Property was null!");
        this.integerTraitMap.put(id.toLowerCase() + "_" + property.getName().toLowerCase(), property);
    }

    private IntegerTraitRegistryModule() { }

    @Override
    public Map<String, IntegerTrait> provideCatalogMap() {
        Map<String, IntegerTrait> map = new HashMap<>();
        for (Map.Entry<String, IntegerTrait> enumTraitEntry : this.integerTraitMap.entrySet()) {
            map.put(enumTraitEntry.getKey().replace("minecraft:", ""), enumTraitEntry.getValue());
        }
        return map;
    }

    private static final class Holder {
        private final static IntegerTraitRegistryModule INSTANCE = new IntegerTraitRegistryModule();
    }
}
