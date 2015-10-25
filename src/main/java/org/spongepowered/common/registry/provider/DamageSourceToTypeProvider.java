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
import java.util.Map;
import java.util.Optional;

public class DamageSourceToTypeProvider implements TypeProvider<String, DamageType> {

    public static DamageSourceToTypeProvider getInstance() {
        return Holder.INSTANCE;
    }

    private DamageSourceToTypeProvider() {

        damageSourceToTypeMappings.put("anvil", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("arrow", DamageTypes.ATTACK);
        damageSourceToTypeMappings.put("cactus", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("drown", DamageTypes.DROWN);
        damageSourceToTypeMappings.put("fall", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("fallingblock", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("generic", DamageTypes.GENERIC);
        damageSourceToTypeMappings.put("indirectmagic", DamageTypes.MAGIC);
        damageSourceToTypeMappings.put("infire", DamageTypes.FIRE);
        damageSourceToTypeMappings.put("inwall", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("lava", DamageTypes.FIRE);
        damageSourceToTypeMappings.put("lightningbolt", DamageTypes.PROJECTILE);
        damageSourceToTypeMappings.put("magic", DamageTypes.MAGIC);
        damageSourceToTypeMappings.put("mob", DamageTypes.ATTACK);
        damageSourceToTypeMappings.put("onfire", DamageTypes.FIRE);
        damageSourceToTypeMappings.put("outofworld", DamageTypes.VOID);
        damageSourceToTypeMappings.put("player", DamageTypes.ATTACK);
        damageSourceToTypeMappings.put("starve", DamageTypes.HUNGER);
        damageSourceToTypeMappings.put("thorns", DamageTypes.MAGIC);
        damageSourceToTypeMappings.put("thrown", DamageTypes.CONTACT);
        damageSourceToTypeMappings.put("wither", DamageTypes.MAGIC);
    }

    public static final Map<String, DamageType> damageSourceToTypeMappings = new HashMap<>();

    @Override
    public Optional<DamageType> get(String key) {
        return Optional.ofNullable(damageSourceToTypeMappings.get(checkNotNull(key).toLowerCase()));
    }

    @Override
    public Optional<String> getKey(DamageType value) {
        throw new UnsupportedOperationException("We do not support this!");
    }

    public void addCustom(String in) {
        damageSourceToTypeMappings.put(in, DamageTypes.CUSTOM);
    }

    private static final class Holder {
        private static final DamageSourceToTypeProvider INSTANCE = new DamageSourceToTypeProvider();
    }
}
