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
package org.spongepowered.common.mixin.api.minecraft.world.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityCategory;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Collection;
import java.util.stream.Collectors;

@Mixin(net.minecraft.world.entity.EntityType.class)
public abstract class EntityTypeMixin_API<T extends Entity> implements EntityType<T> {

    // @formatter:off
    @Shadow @Final private Holder.Reference<net.minecraft.world.entity.EntityType<?>> builtInRegistryHolder;

    @Shadow public abstract net.minecraft.network.chat.Component shadow$getDescription();
    @Shadow public abstract boolean shadow$canSpawnFarFromPlayer();
    @Shadow public abstract boolean shadow$canSerialize();
    @Shadow public abstract boolean shadow$fireImmune();
    @Shadow public abstract boolean shadow$canSummon();
    @Shadow public abstract MobCategory shadow$getCategory();
    // @formatter:on

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.shadow$getDescription());
    }

    @Override
    public boolean isTransient() {
        return !this.shadow$canSerialize();
    }

    @Override
    public boolean isFlammable() {
        return !this.shadow$fireImmune();
    }

    @Override
    public boolean canSpawnAwayFromPlayer() {
        return this.shadow$canSpawnFarFromPlayer();
    }

    @Override
    public boolean isSummonable() {
        return this.shadow$canSummon();
    }

    @Override
    public DefaultedRegistryType<EntityType<?>> registryType() {
        return RegistryTypes.ENTITY_TYPE;
    }

    @Override
    public Collection<Tag<EntityType<?>>> tags() {
        return this.registryType().get().tags().filter(this::is).collect(Collectors.toSet());
    }

    @Override
    public boolean is(Tag<EntityType<?>> tag) {
        return this.builtInRegistryHolder.is((TagKey<net.minecraft.world.entity.EntityType<?>>) (Object) tag);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public EntityCategory category() {
        return (EntityCategory) (Object) this.shadow$getCategory();
    }
}
