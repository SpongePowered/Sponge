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

import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.event.damage.SpongeDamageType;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;

@RegisterCatalog(DamageTypes.class)
public final class DamageTypeRegistryModule extends AbstractCatalogRegistryModule<DamageType> {

    @Override
    public void registerDefaults() {
        this.map.put("attack", new SpongeDamageType("attack"));
        this.map.put("contact", new SpongeDamageType("contact"));
        this.map.put("custom", new SpongeDamageType("custom"));
        this.map.put("drown", new SpongeDamageType("drown"));
        this.map.put("explosive", new SpongeDamageType("explosive"));
        this.map.put("fall", new SpongeDamageType("fall"));
        this.map.put("fire", new SpongeDamageType("fire"));
        this.map.put("generic", new SpongeDamageType("generic"));
        this.map.put("hunger", new SpongeDamageType("hunger"));
        this.map.put("magic", new SpongeDamageType("magic"));
        this.map.put("projectile", new SpongeDamageType("projectile"));
        this.map.put("suffocate", new SpongeDamageType("suffocate"));
        this.map.put("void", new SpongeDamageType("void"));
        this.map.put("sweeping_attack", new SpongeDamageType("sweeping_attack"));
        this.map.put("magma", new SpongeDamageType("magma"));
    }

}
