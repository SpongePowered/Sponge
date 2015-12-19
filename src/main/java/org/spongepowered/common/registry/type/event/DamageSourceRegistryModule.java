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

import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegistrationDependency;

@RegistrationDependency(DamageTypeRegistryModule.class)
public final class DamageSourceRegistryModule implements RegistryModule {

    public static net.minecraft.util.DamageSource DAMAGESOURCE_POISON;
    public static net.minecraft.util.DamageSource DAMAGESOURCE_MELTING;

    @Override
    public void registerDefaults() {
        try {
            DAMAGESOURCE_POISON = (new net.minecraft.util.DamageSource("poison")).setDamageBypassesArmor().setMagicDamage();
            DAMAGESOURCE_MELTING = (new net.minecraft.util.DamageSource("melting")).setDamageBypassesArmor().setFireDamage();
            DamageSources.class.getDeclaredField("DROWNING").set(null, (DamageSource) net.minecraft.util.DamageSource.drown);
            DamageSources.class.getDeclaredField("FALLING").set(null, (DamageSource) net.minecraft.util.DamageSource.fall);
            DamageSources.class.getDeclaredField("FIRE_TICK").set(null, (DamageSource) net.minecraft.util.DamageSource.onFire);
            DamageSources.class.getDeclaredField("GENERIC").set(null, (DamageSource) net.minecraft.util.DamageSource.generic);
            DamageSources.class.getDeclaredField("MAGIC").set(null, (DamageSource) net.minecraft.util.DamageSource.magic);
            DamageSources.class.getDeclaredField("MELTING").set(null, DAMAGESOURCE_MELTING);
            DamageSources.class.getDeclaredField("POISON").set(null, DAMAGESOURCE_POISON);
            DamageSources.class.getDeclaredField("STARVATION").set(null, (DamageSource) net.minecraft.util.DamageSource.starve);
            DamageSources.class.getDeclaredField("WITHER").set(null, (DamageSource) net.minecraft.util.DamageSource.wither);
            DamageSources.class.getDeclaredField("VOID").set(null, (DamageSource) net.minecraft.util.DamageSource.outOfWorld);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
