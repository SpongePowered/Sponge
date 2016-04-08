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
package org.spongepowered.common.registry.type.event;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.event.damage.SpongeDamageType;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

public final class DamageTypeRegistryModule implements CatalogRegistryModule<DamageType> {

    @RegisterCatalog(DamageTypes.class)
    private final ImmutableMap<String, DamageType> damageTypeMappings = new ImmutableMap.Builder<String, DamageType>()
        .put("attack", new SpongeDamageType("attack"))
        .put("contact", new SpongeDamageType("contact"))
        .put("custom", new SpongeDamageType("custom"))
        .put("drown", new SpongeDamageType("drown"))
        .put("explosive", new SpongeDamageType("explosive"))
        .put("fall", new SpongeDamageType("fall"))
        .put("fire", new SpongeDamageType("fire"))
        .put("generic", new SpongeDamageType("generic"))
        .put("hunger", new SpongeDamageType("hunger"))
        .put("magic", new SpongeDamageType("magic"))
        .put("projectile", new SpongeDamageType("projectile"))
        .put("suffocate", new SpongeDamageType("suffocate"))
        .put("void", new SpongeDamageType("void"))
        .build();

    @Override
    public Optional<DamageType> getById(String id) {
        return Optional.ofNullable(this.damageTypeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<DamageType> getAll() {
        return ImmutableList.copyOf(this.damageTypeMappings.values());
    }

}
