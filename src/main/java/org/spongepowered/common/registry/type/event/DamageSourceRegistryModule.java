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

import net.minecraft.util.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;

@RegistrationDependency(DamageTypeRegistryModule.class)
public final class DamageSourceRegistryModule implements RegistryModule {

    public static DamageSource IGNORED_DAMAGE_SOURCE;
    public static DamageSource DAMAGESOURCE_POISON;
    public static DamageSource DAMAGESOURCE_MELTING;

    @Override
    public void registerDefaults() {
        try {
            // These need to be instantiated after the DamageTypeRegistryModule has had a chance to register
            // the damage types, otherwise it will fail and have invalid types.
            DAMAGESOURCE_POISON = (new DamageSource("poison")).setDamageBypassesArmor().setMagicDamage();
            DAMAGESOURCE_MELTING = (new DamageSource("melting")).setDamageBypassesArmor().setFireDamage();
            IGNORED_DAMAGE_SOURCE = new DamageSource("spongespecific").setDamageBypassesArmor().setDamageAllowedInCreativeMode();
            DamageSources.class.getDeclaredField("DRAGON_BREATH").set(null, DamageSource.DRAGON_BREATH);
            DamageSources.class.getDeclaredField("DROWNING").set(null, DamageSource.DROWN);
            DamageSources.class.getDeclaredField("FALLING").set(null, DamageSource.FALL);
            DamageSources.class.getDeclaredField("FIRE_TICK").set(null, DamageSource.ON_FIRE);
            DamageSources.class.getDeclaredField("GENERIC").set(null, DamageSource.GENERIC);
            DamageSources.class.getDeclaredField("MAGIC").set(null, DamageSource.MAGIC);
            DamageSources.class.getDeclaredField("MELTING").set(null, DAMAGESOURCE_MELTING);
            DamageSources.class.getDeclaredField("POISON").set(null, DAMAGESOURCE_POISON);
            DamageSources.class.getDeclaredField("STARVATION").set(null, DamageSource.STARVE);
            DamageSources.class.getDeclaredField("WITHER").set(null, DamageSource.WITHER);
            DamageSources.class.getDeclaredField("VOID").set(null, DamageSource.OUT_OF_WORLD);
            DamageSources.class.getDeclaredField("WALL_COLLISION").set(null, DamageSource.FLY_INTO_WALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
