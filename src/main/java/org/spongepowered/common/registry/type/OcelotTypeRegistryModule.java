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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.data.type.OcelotTypes;
import org.spongepowered.common.entity.SpongeOcelotType;
import org.spongepowered.common.registry.CatalogRegistryModule;
import org.spongepowered.common.registry.util.RegisterCatalog;

import com.google.common.collect.ImmutableList;

public class OcelotTypeRegistryModule implements CatalogRegistryModule<OcelotType> {

    @RegisterCatalog(OcelotTypes.class)
    private final Map<String, OcelotType> ocelotTypeMap = new HashMap<>();

    @Override
    public Optional<OcelotType> getById(String id) {
        return Optional.ofNullable(this.ocelotTypeMap.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<OcelotType> getAll() {
        return ImmutableList.copyOf(this.ocelotTypeMap.values());
    }

    @Override
    public void registerDefaults() {
        this.ocelotTypeMap.put("wild_ocelot", new SpongeOcelotType(0, "wild_ocelot"));
        this.ocelotTypeMap.put("black_cat", new SpongeOcelotType(1, "black_cat"));
        this.ocelotTypeMap.put("red_cat", new SpongeOcelotType(2, "red_cat"));
        this.ocelotTypeMap.put("siamese_cat", new SpongeOcelotType(3, "siamese_cat"));
        
    }

}
