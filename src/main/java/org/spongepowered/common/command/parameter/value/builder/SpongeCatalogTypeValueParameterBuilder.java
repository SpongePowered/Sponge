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
package org.spongepowered.common.command.parameter.value.builder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.common.command.parameter.value.CatalogTypeValueParameter;

import java.util.List;

import javax.annotation.Nullable;

public class SpongeCatalogTypeValueParameterBuilder implements VariableValueParameters.CatalogedTypeBuilder {

    @Nullable private Class<? extends CatalogType> catalogTypeClass = null;
    private final List<String> prefixes = Lists.newArrayList();

    @Override
    public VariableValueParameters.CatalogedTypeBuilder setCatalogedType(Class<? extends CatalogType> catalogedType) {
        this.catalogTypeClass = catalogedType;
        return this;
    }

    @Override
    public VariableValueParameters.CatalogedTypeBuilder prefix(String prefix) {
        this.prefixes.add(prefix);
        return this;
    }

    @Override
    public ValueParameter build() {
        Preconditions.checkState(this.catalogTypeClass != null, "catalogType");
        return new CatalogTypeValueParameter<>(this.catalogTypeClass, this.prefixes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public VariableValueParameters.CatalogedTypeBuilder from(ValueParameter value) {
        Preconditions.checkArgument(value instanceof CatalogTypeValueParameter, "Must be of type CatalogTypeValueParameter");
        CatalogTypeValueParameter cv = (CatalogTypeValueParameter) value;
        this.catalogTypeClass = cv.getCatalogType();
        this.prefixes.clear();
        Iterable<String> is = cv.getPrefixes();
        is.forEach(this::prefix);
        return this;
    }

    @Override
    public VariableValueParameters.CatalogedTypeBuilder reset() {
        this.catalogTypeClass = null;
        this.prefixes.clear();
        return this;
    }
}
