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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.event.damage.SpongeDamageType;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.Locale;

@RegisterCatalog(DamageTypes.class)
public final class DamageTypeRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<DamageType> implements AdditionalCatalogRegistryModule<DamageType> {

    public DamageTypeRegistryModule() {
        super("minecraft");
    }

    @Override
    public void registerAdditionalCatalog(final DamageType extraCatalog) {
        final String id = checkNotNull(extraCatalog).getId();
        final String key = id.toLowerCase(Locale.ENGLISH);
        checkArgument(!key.contains("sponge:"), "Cannot register spoofed Damage Type!");
        checkArgument(!key.contains("minecraft:"), "Cannot register spoofed Damage Type!");
        checkArgument(!this.catalogTypeMap.containsKey(key), "Cannot register an already registered EventContextKey: %s", key);
        this.catalogTypeMap.put(key, extraCatalog);
    }

    @Override
    public void registerDefaults() {

        register(new SpongeDamageType("minecraft:attack", "attack"));
        register(new SpongeDamageType("minecraft:contact", "contact"));
        register(new SpongeDamageType("minecraft:custom", "custom"));
        register(new SpongeDamageType("minecraft:drown", "drown"));
        register(new SpongeDamageType("minecraft:explosive", "explosive"));
        register(new SpongeDamageType("minecraft:fall", "fall"));
        register(new SpongeDamageType("minecraft:fire", "fire"));
        register(new SpongeDamageType("minecraft:generic", "generic"));
        register(new SpongeDamageType("minecraft:hunger", "hunger"));
        register(new SpongeDamageType("minecraft:magic", "magic"));
        register(new SpongeDamageType("minecraft:projectile", "projectile"));
        register(new SpongeDamageType("minecraft:suffocate", "suffocate"));
        register(new SpongeDamageType("minecraft:void", "void"));
        register(new SpongeDamageType("minecraft:sweeping_attack", "sweeping_attack"));
        register(new SpongeDamageType("minecraft:magma", "magma"));

    }
}
