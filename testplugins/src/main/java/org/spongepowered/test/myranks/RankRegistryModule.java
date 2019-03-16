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
package org.spongepowered.test.myranks;

import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.test.myranks.api.Rank;
import org.spongepowered.test.myranks.api.Ranks;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RankRegistryModule implements AdditionalCatalogRegistryModule<Rank> {

    @RegisterCatalog(Ranks.class)
    private final Map<String, Rank> rankMap = new HashMap<>();

    @Override
    public void registerDefaults() {
        register(new RankImpl("user", "User"));
        register(new RankImpl("staff", "Staff"));
    }

    private void register(Rank rank) {
        this.rankMap.put(rank.getId(), rank);
    }

    @Override
    public void registerAdditionalCatalog(Rank extraCatalog) {
        if (!this.rankMap.containsKey(extraCatalog.getId())) {
            register(extraCatalog);
        }
    }

    @Override
    public Optional<Rank> getById(String id) {
        return Optional.ofNullable(this.rankMap.get(id));
    }

    @Override
    public Collection<Rank> getAll() {
        return Collections.unmodifiableCollection(this.rankMap.values());
    }
}
