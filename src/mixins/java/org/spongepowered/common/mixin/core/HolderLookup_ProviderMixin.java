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
package org.spongepowered.common.mixin.core;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.ValueNotFoundException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.resources.ResourceKeyAccessor;

import java.util.Optional;
import java.util.stream.Stream;

@Mixin(value = HolderLookup.Provider.class, priority = 999)
public interface HolderLookup_ProviderMixin extends RegistryHolder {

    @Shadow <T> Optional<? extends HolderLookup.RegistryLookup<T>> shadow$lookup(net.minecraft.resources.ResourceKey<? extends net.minecraft.core.Registry<? extends T>> arg);

    @Shadow Stream<HolderLookup.RegistryLookup<?>> shadow$listRegistries();

    @Override
    default <T> Registry<T> registry(final RegistryType<T> type) {
        return this.findRegistry(type)
            .orElseThrow(() -> new ValueNotFoundException(String.format("No '%s' registry has been defined in root '%s'", type.location(), type.root())));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default <T> Optional<Registry<T>> findRegistry(final RegistryType<T> type) {
        return (Optional) this.shadow$lookup(ResourceKeyAccessor.invoker$create((ResourceLocation) (Object) type.root(), (ResourceLocation) (Object) type.location()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    default Stream<Registry<?>> streamRegistries() {
        return (Stream) this.shadow$listRegistries();
    }

    @Override
    default Stream<Registry<?>> streamRegistries(final ResourceKey root) {
        return this.streamRegistries().filter(r -> r.type().root().equals(root));
    }
}
