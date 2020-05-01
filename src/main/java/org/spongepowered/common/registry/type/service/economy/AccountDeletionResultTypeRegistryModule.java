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
package org.spongepowered.common.registry.type.service.economy;

import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.service.economy.account.AccountDeletionResultTypes;
import org.spongepowered.common.economy.SpongeAccountDeletionResultType;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

@RegisterCatalog(AccountDeletionResultTypes.class)
public class AccountDeletionResultTypeRegistryModule
        extends AbstractPrefixAlternateCatalogTypeRegistryModule<AccountDeletionResultType>
        implements SpongeAdditionalCatalogRegistryModule<AccountDeletionResultType> {

    public AccountDeletionResultTypeRegistryModule() {
        super("sponge");
    }

    @Override
    public boolean allowsApiRegistration() {
        return true;
    }

    @Override
    public void registerAdditionalCatalog(AccountDeletionResultType extraCatalog) {
        if (!this.catalogTypeMap.containsKey(extraCatalog.getId())) {
            this.catalogTypeMap.put(extraCatalog.getId(), extraCatalog);
        }
    }

    @Override
    public void registerDefaults() {
        register(new SpongeAccountDeletionResultType("sponge:absent", "Absent"));
        register(new SpongeAccountDeletionResultType("sponge:failed", "Failed"));
        register(new SpongeAccountDeletionResultType("sponge:success", "Success"));
        register(new SpongeAccountDeletionResultType("sponge:unsupported", "Unsupported"));
        register(new SpongeAccountDeletionResultType("sponge:undeletable", "Undeletable"));
    }
}
