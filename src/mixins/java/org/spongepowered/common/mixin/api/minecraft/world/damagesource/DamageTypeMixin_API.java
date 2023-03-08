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
package org.spongepowered.common.mixin.api.minecraft.world.damagesource;

import net.minecraft.world.damagesource.DamageEffects;
import org.spongepowered.api.event.cause.entity.damage.DamageEffect;
import org.spongepowered.api.event.cause.entity.damage.DamageScaling;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = net.minecraft.world.damagesource.DamageType.class)
public abstract class DamageTypeMixin_API implements DamageType {

    // @formatter:off
    @Shadow @Final private String msgId;
    @Shadow @Final private float exhaustion;
    @Shadow @Final private net.minecraft.world.damagesource.DamageScaling scaling;
    @Shadow @Final private DamageEffects effects;

    // @formatter:on


    @Override
    public String name() {
        return this.msgId;
    }

    @Override
    public double exhaustion() {
        return this.exhaustion;
    }

    @Override
    public DamageScaling scaling() {
        return (DamageScaling) (Object) this.scaling;
    }

    @Override
    public DamageEffect effect() {
        return (DamageEffect) (Object) this.effects;
    }

    @Override
    public boolean is(final Tag<DamageType> tag) {
        final Set<DamageType> matchingTypes = RegistryTypes.DAMAGE_TYPE.get().taggedValues(tag);
        return matchingTypes.contains(this);
    }

    @Override
    public DefaultedRegistryType<DamageType> registryType() {
        return RegistryTypes.DAMAGE_TYPE;
    }

    @Override
    public Collection<Tag<DamageType>> tags() {
        return this.registryType().get().tags().filter(this::is).collect(Collectors.toSet());
    }
}
