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
package org.spongepowered.common.economy;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.service.economy.transaction.TransactionType;

public final class SpongeTransactionType implements TransactionType {

    private final CatalogKey key;

    public SpongeTransactionType(CatalogKey key) {
        this.key = checkNotNull(key);
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof TransactionType)) {
            return false;
        }
        return ((TransactionType) other).getKey().equals(this.getKey());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(this.key);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", this.getKey())
                .toString();
    }
}
