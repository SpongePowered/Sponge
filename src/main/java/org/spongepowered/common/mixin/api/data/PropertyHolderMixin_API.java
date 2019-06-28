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
package org.spongepowered.common.mixin.api.data;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.property.PropertyHolder;
import org.spongepowered.api.data.property.PropertyStore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;

import java.util.Collection;
import java.util.Optional;

@Mixin({Block.class, Entity.class, TileEntity.class, ItemStack.class})
public abstract class PropertyHolderMixin_API implements PropertyHolder {

    @Override
    public <T extends Property<?, ?>> Optional<T> getProperty(Class<T> propertyClass) {
        final Optional<PropertyStore<T>> optional = SpongeImpl.getPropertyRegistry().getStore(propertyClass);
        if (optional.isPresent()) {
            return optional.get().getFor(this);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Property<?, ?>> getApplicableProperties() {
        return SpongeImpl.getPropertyRegistry().getPropertiesFor(this);

    }
}
