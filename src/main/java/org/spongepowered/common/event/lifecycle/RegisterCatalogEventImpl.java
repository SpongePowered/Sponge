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
package org.spongepowered.common.event.lifecycle;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterCatalogEvent;
import org.spongepowered.api.registry.DuplicateRegistrationException;
import org.spongepowered.common.registry.SpongeCatalogRegistry;

public final class RegisterCatalogEventImpl<C extends CatalogType> extends AbstractLifecycleEvent implements RegisterCatalogEvent<C> {

    private final TypeToken<C> token;

    public RegisterCatalogEventImpl(final Cause cause, final Game game, final TypeToken<C> token) {
        super(cause, game);
        this.token = token;
    }

    @Override
    public TypeToken<C> getGenericType() {
        return this.token;
    }

    @Override
    public C register(C catalog) throws DuplicateRegistrationException {
        Preconditions.checkNotNull(catalog);

        return ((SpongeCatalogRegistry) Sponge.getRegistry().getCatalogRegistry()).registerCatalog(this.token, catalog);
    }

    @Override
    public String toString() {
        return "RegisterCatalogEvent{cause=" + this.cause + ", type=" + this.token + "}";
    }
}
