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

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.event.damage.SpongeDamageType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@RegisterCatalog(DamageTypes.class)
public final class DamageTypeRegistryModule extends AbstractCatalogRegistryModule<DamageType> implements CatalogRegistryModule<DamageType> {

    @Override
    public void registerDefaults() {
        register(CatalogKey.minecraft("attack"), new SpongeDamageType("attack"));
        register(CatalogKey.minecraft("contact"), new SpongeDamageType("contact"));
        register(CatalogKey.minecraft("custom"), new SpongeDamageType("custom"));
        register(CatalogKey.minecraft("drown"), new SpongeDamageType("drown"));
        register(CatalogKey.minecraft("explosive"), new SpongeDamageType("explosive"));
        register(CatalogKey.minecraft("fall"), new SpongeDamageType("fall"));
        register(CatalogKey.minecraft("fire"), new SpongeDamageType("fire"));
        register(CatalogKey.minecraft("generic"), new SpongeDamageType("generic"));
        register(CatalogKey.minecraft("hunger"), new SpongeDamageType("hunger"));
        register(CatalogKey.minecraft("magic"), new SpongeDamageType("magic"));
        register(CatalogKey.minecraft("projectile"), new SpongeDamageType("projectile"));
        register(CatalogKey.minecraft("suffocate"), new SpongeDamageType("suffocate"));
        register(CatalogKey.minecraft("void"), new SpongeDamageType("void"));
        register(CatalogKey.minecraft("sweeping_attack"), new SpongeDamageType("sweeping_attack"));
        register(CatalogKey.minecraft("magma"), new SpongeDamageType("magma"));
    }
}
