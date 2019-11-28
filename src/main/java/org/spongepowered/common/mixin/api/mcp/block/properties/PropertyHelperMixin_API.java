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
package org.spongepowered.common.mixin.api.mcp.block.properties;

import net.minecraft.block.properties.AbstractProperty;
import net.minecraft.block.properties.IProperty;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.registry.provider.BlockPropertyIdProvider;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * This is retained solely for simplification not having to perform any
 * lookups to the {@link BlockTypeRegistryModule#getIdFor(IProperty)}.
 *
 * @param <T> The type of comparable
 */
@Mixin(value = AbstractProperty.class)
public abstract class PropertyHelperMixin_API<T extends Comparable<T>> implements StateProperty<T> {

    @Nullable private String api$IdString = null;

    @SuppressWarnings("rawtypes")
    @Override
    public String getId() {
        if (this.api$IdString == null) {
            this.api$IdString =  BlockPropertyIdProvider.getIdFor((IProperty) this);
        }
        return this.api$IdString;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<T> parseValue(String value) {
        return Optional.ofNullable(((IProperty<T>) this).parseValue(value).orNull());
    }

}
