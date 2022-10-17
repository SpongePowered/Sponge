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
package org.spongepowered.common.mixin.api.minecraft.world.level.biome;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.holder.SpongeDataHolder;

import java.util.Collection;

@Mixin(net.minecraft.world.level.biome.Biome.class)
public abstract class BiomeMixin_API implements Biome, SpongeDataHolder {

    @Override
    public DefaultedRegistryType<Biome> registryType() {
        return RegistryTypes.BIOME;
    }

    @Override
    public Collection<Tag<Biome>> tags() {
        return this.registryType().get().tags().filter(this::is).toList();
    }

    @Override
    public boolean is(final Tag<Biome> tag) {
        final Registry<net.minecraft.world.level.biome.Biome> registry = SpongeCommon.server().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        final Holder.Reference<net.minecraft.world.level.biome.Biome> holder = registry.createIntrusiveHolder((net.minecraft.world.level.biome.Biome) (Object) this);
        return holder.is(((TagKey<net.minecraft.world.level.biome.Biome>) (Object) tag));
    }
}
