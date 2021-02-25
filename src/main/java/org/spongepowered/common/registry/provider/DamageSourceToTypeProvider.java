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
package org.spongepowered.common.registry.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class DamageSourceToTypeProvider {

    public static final DamageSourceToTypeProvider INSTANCE = new DamageSourceToTypeProvider();

    private final Map<String, Supplier<? extends DamageType>> mappings = new HashMap<>();

    DamageSourceToTypeProvider() {
        this.mappings.put("anvil", DamageTypes.CONTACT);
        this.mappings.put("arrow", DamageTypes.PROJECTILE);
        this.mappings.put("cactus", DamageTypes.CONTACT);
        this.mappings.put("cramming", DamageTypes.CONTACT);
        this.mappings.put("dragonbreath", DamageTypes.MAGIC);
        this.mappings.put("drown", DamageTypes.DROWN);
        this.mappings.put("explosion.player", DamageTypes.EXPLOSIVE);
        this.mappings.put("fall", DamageTypes.FALL);
        this.mappings.put("fallingblock", DamageTypes.CONTACT);
        this.mappings.put("fireworks", DamageTypes.EXPLOSIVE);
        this.mappings.put("flyintowall", DamageTypes.CONTACT);
        this.mappings.put("generic", DamageTypes.GENERIC);
        this.mappings.put("hotfloor", DamageTypes.MAGMA);
        this.mappings.put("indirectmagic", DamageTypes.MAGIC);
        this.mappings.put("infire", DamageTypes.FIRE);
        this.mappings.put("inwall", DamageTypes.SUFFOCATE);
        this.mappings.put("lava", DamageTypes.FIRE);
        this.mappings.put("lightningbolt", DamageTypes.PROJECTILE);
        this.mappings.put("magic", DamageTypes.MAGIC);
        this.mappings.put("mob", DamageTypes.ATTACK);
        this.mappings.put("onfire", DamageTypes.FIRE);
        this.mappings.put("outofworld", DamageTypes.VOID);
        this.mappings.put("player", DamageTypes.ATTACK);
        this.mappings.put("starve", DamageTypes.HUNGER);
        this.mappings.put("thorns", DamageTypes.MAGIC);
        this.mappings.put("thrown", DamageTypes.PROJECTILE);
        this.mappings.put("wither", DamageTypes.MAGIC);
    }

    public Optional<Supplier<? extends DamageType>> get(final String key) {
        return Optional.ofNullable(this.mappings.get(checkNotNull(key).toLowerCase(Locale.ENGLISH)));
    }

    public Supplier<? extends DamageType> getOrCustom(String key) {
        final Supplier<? extends DamageType> damageType = this.mappings.get(checkNotNull(key).toLowerCase(Locale.ENGLISH));
        if (damageType == null) {
            this.addCustom(key);
            return DamageTypes.CUSTOM;
        }
        return damageType;
    }

    public Optional<String> getKey(DamageType value) {
        throw new UnsupportedOperationException("We do not support this!");
    }

    public void addCustom(String in) {
        this.mappings.put(in.toLowerCase(Locale.ENGLISH), DamageTypes.CUSTOM);
    }
}
