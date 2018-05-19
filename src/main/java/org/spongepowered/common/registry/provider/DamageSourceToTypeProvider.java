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
import org.spongepowered.common.registry.TypeProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DamageSourceToTypeProvider implements TypeProvider<String, DamageType> {

    public static DamageSourceToTypeProvider getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<String, DamageType> damageSourceToTypeMappings = new HashMap<>();

    DamageSourceToTypeProvider() {

        this.damageSourceToTypeMappings.put("anvil", DamageTypes.CONTACT);
        this.damageSourceToTypeMappings.put("arrow", DamageTypes.PROJECTILE);
        this.damageSourceToTypeMappings.put("cactus", DamageTypes.CONTACT);
        this.damageSourceToTypeMappings.put("cramming", DamageTypes.CONTACT);
        this.damageSourceToTypeMappings.put("dragonbreath", DamageTypes.MAGIC);
        this.damageSourceToTypeMappings.put("drown", DamageTypes.DROWN);
        this.damageSourceToTypeMappings.put("explosion.player", DamageTypes.EXPLOSIVE);
        this.damageSourceToTypeMappings.put("fall", DamageTypes.FALL);
        this.damageSourceToTypeMappings.put("fallingblock", DamageTypes.CONTACT);
        this.damageSourceToTypeMappings.put("fireworks", DamageTypes.EXPLOSIVE);
        this.damageSourceToTypeMappings.put("flyintowall", DamageTypes.CONTACT);
        this.damageSourceToTypeMappings.put("generic", DamageTypes.GENERIC);
        this.damageSourceToTypeMappings.put("hotfloor", DamageTypes.MAGMA);
        this.damageSourceToTypeMappings.put("indirectmagic", DamageTypes.MAGIC);
        this.damageSourceToTypeMappings.put("infire", DamageTypes.FIRE);
        this.damageSourceToTypeMappings.put("inwall", DamageTypes.SUFFOCATE);
        this.damageSourceToTypeMappings.put("lava", DamageTypes.FIRE);
        this.damageSourceToTypeMappings.put("lightningbolt", DamageTypes.PROJECTILE);
        this.damageSourceToTypeMappings.put("magic", DamageTypes.MAGIC);
        this.damageSourceToTypeMappings.put("mob", DamageTypes.ATTACK);
        this.damageSourceToTypeMappings.put("onfire", DamageTypes.FIRE);
        this.damageSourceToTypeMappings.put("outofworld", DamageTypes.VOID);
        this.damageSourceToTypeMappings.put("player", DamageTypes.ATTACK);
        this.damageSourceToTypeMappings.put("starve", DamageTypes.HUNGER);
        this.damageSourceToTypeMappings.put("thorns", DamageTypes.MAGIC);
        this.damageSourceToTypeMappings.put("thrown", DamageTypes.PROJECTILE);
        this.damageSourceToTypeMappings.put("wither", DamageTypes.MAGIC);
    }

    @Override
    public Optional<DamageType> get(String key) {
        return Optional.ofNullable(this.damageSourceToTypeMappings.get(checkNotNull(key).toLowerCase(Locale.ENGLISH)));
    }

    public DamageType getOrCustom(String key) {
        final DamageType damageType = this.damageSourceToTypeMappings.get(checkNotNull(key).toLowerCase(Locale.ENGLISH));
        if (damageType == null) {
            addCustom(key);
            return DamageTypes.CUSTOM;
        }
        return damageType;
    }

    @Override
    public Optional<String> getKey(DamageType value) {
        throw new UnsupportedOperationException("We do not support this!");
    }

    public void addCustom(String in) {
        this.damageSourceToTypeMappings.put(in.toLowerCase(Locale.ENGLISH), DamageTypes.CUSTOM);
    }

    private static final class Holder {
        static final DamageSourceToTypeProvider INSTANCE = new DamageSourceToTypeProvider();
    }
}
