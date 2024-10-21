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
package org.spongepowered.common.mixin.api.minecraft.world.item;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.item.ItemCooldowns_CooldownInstanceAccessor;
import org.spongepowered.common.bridge.world.item.ItemCooldownsBridge;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;

@Mixin(ItemCooldowns.class)
public abstract class ItemCooldownsMixin_API implements org.spongepowered.api.entity.living.player.CooldownTracker {

    // @formatter:off
    @Shadow private int tickCount;

    @Shadow public abstract boolean shadow$isOnCooldown(ItemStack stack);
    @Shadow public abstract float shadow$getCooldownPercent(ItemStack stack, float partialTicks);
    @Shadow public abstract void shadow$addCooldown(final ItemStack stack, final int ticks);
    @Shadow public abstract void shadow$addCooldown(final ResourceLocation group, final int ticks);
    @Shadow @Final private Map<ResourceLocation, ItemCooldowns_CooldownInstanceAccessor> cooldowns;

    // @formatter:on


    @Shadow public abstract ResourceLocation getCooldownGroup(final ItemStack $$0);

    @Override
    public boolean hasCooldown(final org.spongepowered.api.item.inventory.ItemStack stack) {
        Objects.requireNonNull(stack, "ItemStack cannot be null!");
        return this.shadow$isOnCooldown(ItemStackUtil.toNative(stack));
    }

    /**
     * see {@link ItemCooldowns#getCooldownPercent(ItemStack, float)}
     */
    @Override
    public boolean hasCooldown(final ResourceKey group) {
        Objects.requireNonNull(group, "group cannot be null!");
        return impl$getCooldownPercent((Object) group) > 0;
    }

    private float impl$getCooldownPercent(final Object group) {
        final var cooldown = this.cooldowns.get((ResourceLocation) group);
        if (cooldown != null) {
            float $$4 = (float)(cooldown.accessor$endTime() - cooldown.accessor$startTime());
            float $$5 = (float)cooldown.accessor$endTime() - ((float)this.tickCount);
            return Mth.clamp($$5 / $$4, 0.0F, 1.0F);
        }
        return 0;
    }

    @Override
    public Optional<Ticks> cooldown(final org.spongepowered.api.item.inventory.ItemStack stack) {
        Objects.requireNonNull(stack, "ItemStack cannot be null!");
        final var group = this.getCooldownGroup(ItemStackUtil.toNative(stack));
        return this.cooldown((ResourceKey) (Object) group);
    }

    @Override
    public Optional<Ticks> cooldown(final ResourceKey group) {
        Objects.requireNonNull(group, "group cannot be null!");
        final var cooldown = this.cooldowns.get(group);

        if (cooldown != null) {
            final int remainingCooldown = cooldown.accessor$endTime() - this.tickCount;
            if (remainingCooldown > 0) {
                return Optional.of(Ticks.of(remainingCooldown));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean setCooldown(final org.spongepowered.api.item.inventory.ItemStack stack, final Ticks ticks) {
        Objects.requireNonNull(stack, "ItemStack cannot be null!");
        this.shadow$addCooldown(ItemStackUtil.toNative(stack), SpongeTicks.toSaturatedIntOrInfinite(ticks));
        return ((ItemCooldownsBridge) this).bridge$getSetCooldownResult();
    }

    @Override
    public boolean setCooldown(final ResourceKey group, final Ticks ticks) {
        Objects.requireNonNull(group, "group cannot be null!");
        this.shadow$addCooldown((ResourceLocation) (Object) group, SpongeTicks.toSaturatedIntOrInfinite(ticks));
        return ((ItemCooldownsBridge) this).bridge$getSetCooldownResult();
    }
    @Override
    public boolean resetCooldown(final org.spongepowered.api.item.inventory.ItemStack stack) {
        return this.setCooldown(stack, Ticks.zero());
    }

    @Override
    public boolean resetCooldown(final ResourceKey group) {
        return this.setCooldown(group, Ticks.zero());
    }

    @Override
    public OptionalDouble fractionRemaining(final org.spongepowered.api.item.inventory.ItemStack stack) {
        Objects.requireNonNull(stack, "ItemStack cannot be null!");
        final float cooldown = this.shadow$getCooldownPercent(ItemStackUtil.toNative(stack), 0);

        if (cooldown > 0.0F) {
            return OptionalDouble.of(cooldown);
        }
        return OptionalDouble.empty();
    }

    @Override
    public OptionalDouble fractionRemaining(final ResourceKey group) {
        Objects.requireNonNull(group, "group cannot be null!");
        final float cooldown = this.impl$getCooldownPercent(group);
        if (cooldown > 0.0F) {
            return OptionalDouble.of(cooldown);
        }
        return OptionalDouble.empty();
    }
}
