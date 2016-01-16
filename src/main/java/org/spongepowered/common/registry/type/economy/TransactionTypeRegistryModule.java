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
package org.spongepowered.common.registry.type.economy;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.common.economy.SpongeTransactionType;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TransactionTypeRegistryModule implements CatalogRegistryModule<TransactionType>, SpongeAdditionalCatalogRegistryModule<TransactionType> {

    @RegisterCatalog(TransactionTypes.class)
    public final Map<String, TransactionType> transactionTypeMappings = new HashMap<>();

    @Override
    public Optional<TransactionType> getById(String id) {
        return Optional.ofNullable(this.transactionTypeMappings.get(Preconditions.checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<TransactionType> getAll() {
        return ImmutableList.copyOf(this.transactionTypeMappings.values());
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(TransactionType extraCatalog) {
        this.transactionTypeMappings.put(Preconditions.checkNotNull(extraCatalog).getId(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        this.transactionTypeMappings.put("deposit", new SpongeTransactionType("deposit"));
        this.transactionTypeMappings.put("withdraw", new SpongeTransactionType("withdraw"));
        this.transactionTypeMappings.put("transfer",new SpongeTransactionType("transfer"));
    }
}
