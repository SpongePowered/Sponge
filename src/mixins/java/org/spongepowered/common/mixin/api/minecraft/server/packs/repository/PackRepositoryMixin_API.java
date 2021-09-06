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
package org.spongepowered.common.mixin.api.minecraft.server.packs.repository;

import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.api.resource.pack.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin_API implements org.spongepowered.api.resource.pack.PackRepository {

    // @formatter:off
    @Shadow private Map<String, net.minecraft.server.packs.repository.Pack> available;
    @Shadow public abstract Collection<net.minecraft.server.packs.repository.Pack> shadow$getAvailablePacks();
    @Shadow public abstract Collection<net.minecraft.server.packs.repository.Pack> shadow$getSelectedPacks();
    //@formatter:on

    @Override
    public Collection<Pack> all() {
        return (Collection<Pack>) (Object) this.shadow$getAvailablePacks();
    }

    @Override
    public Collection<Pack> disabled() {
        final List<Pack> disabled = new ArrayList<>();
        for (final Map.Entry<String, net.minecraft.server.packs.repository.Pack> entry : this.available.entrySet()) {
            boolean selected = false;

            for (final net.minecraft.server.packs.repository.Pack selectedPack : this.shadow$getSelectedPacks()) {
                if (selectedPack == entry.getValue()) {
                    selected = true;
                    break;
                }
            }

            if (!selected) {
                disabled.add((Pack) entry.getValue());
            }
        }

        return disabled;
    }

    @Override
    public Collection<Pack> enabled() {
        return (Collection<Pack>) (Object) this.shadow$getSelectedPacks();
    }

    @Override
    public Optional<Pack> pack(final String id) {
        return Optional.ofNullable((Pack) this.available.get(Objects.requireNonNull(id, "id")));
    }
}
