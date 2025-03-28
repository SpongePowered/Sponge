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
package org.spongepowered.common.mixin.api.minecraft.world.effect;

import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Collection;

@Mixin(MobEffect.class)
@Implements(@Interface(iface = PotionEffectType.class, prefix = "potionEffectType$", remap = Remap.NONE))
public abstract class MobEffectMixin_API implements PotionEffectType {

    // @formatter:off
    @Shadow public abstract boolean shadow$isInstantenous();
    @Shadow public abstract net.minecraft.network.chat.Component shadow$getDisplayName();
    // @formatter:on

    public boolean potionEffectType$isInstant() {
        return this.shadow$isInstantenous();
    }

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.shadow$getDisplayName());
    }

    @Override
    public DefaultedRegistryType<PotionEffectType> registryType() {
        return RegistryTypes.POTION_EFFECT_TYPE;
    }

    @Override
    public Collection<Tag<PotionEffectType>> tags() {
        return this.registryType().get().tags().filter(this::is).toList();
    }

    @Override
    public boolean is(final Tag<PotionEffectType> tag) {
        final Registry<MobEffect> registry = SpongeCommon.vanillaRegistry(Registries.MOB_EFFECT);
        final Holder.Reference<MobEffect> holder = registry.createIntrusiveHolder((MobEffect) (Object) this);
        return holder.is(((TagKey<MobEffect>) (Object) tag));
    }
}
