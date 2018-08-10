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
package org.spongepowered.common.event.registry;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.CatalogRegistryModule;

public class SpongeGameRegistryRegisterEvent<T extends CatalogType> implements GameRegistryEvent.Register<T> {

    private final Cause cause;
    private final Class<T> catalogType;
    private final AdditionalCatalogRegistryModule<T> registryModule;

    public SpongeGameRegistryRegisterEvent(Cause cause, Class<T> catalogType,
            AdditionalCatalogRegistryModule<T> registryModule) {
        this.cause = cause;
        this.catalogType = catalogType;
        this.registryModule = registryModule;
    }

    @Override
    public Class<T> getCatalogType() {
        return this.catalogType;
    }

    @Override
    public CatalogRegistryModule<T> getRegistryModule() {
        return this.registryModule;
    }

    @Override
    public void register(T catalogType) {
        this.registryModule.registerAdditionalCatalog(catalogType);
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public TypeToken<T> getGenericType() {
        return TypeToken.of(this.catalogType);
    }
}
