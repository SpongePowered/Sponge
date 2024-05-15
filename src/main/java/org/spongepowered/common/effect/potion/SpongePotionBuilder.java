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
package org.spongepowered.common.effect.potion;


import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.Preconditions;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Objects;
import java.util.Optional;

@DefaultQualifier(NonNull.class)
public final class SpongePotionBuilder extends AbstractDataBuilder<PotionEffect> implements PotionEffect.Builder {

    private @Nullable PotionEffectType potionType;
    private Ticks duration = Ticks.zero();
    private int amplifier;
    private boolean isAmbient;
    private boolean showParticles;
    private boolean showIcon;

    public SpongePotionBuilder() {
        super(PotionEffect.class, Constants.Sponge.Potion.CURRENT_VERSION);
        this.reset();
    }

    @Override
    public PotionEffect.Builder from(final PotionEffect holder) {
        this.potionType = Objects.requireNonNull(holder).type();
        this.duration = holder.duration();
        this.amplifier = holder.amplifier();
        this.isAmbient = holder.isAmbient();
        this.showParticles = holder.showsParticles();
        this.showIcon = holder.showsIcon();
        return this;
    }

    @Override
    protected Optional<PotionEffect> buildContent(final DataView container) throws InvalidDataException {
        Objects.requireNonNull(container);
        if (!container.contains(Constants.Item.Potions.POTION_TYPE) || !container.contains(Constants.Item.Potions.POTION_DURATION)
            || !container.contains(Constants.Item.Potions.POTION_AMPLIFIER) || !container.contains(Constants.Item.Potions.POTION_AMBIANCE)
            || !container.contains(Constants.Item.Potions.POTION_SHOWS_PARTICLES)) {
            return Optional.empty();
        }
        final String effectName = container.getString(Constants.Item.Potions.POTION_TYPE).get();
        final Optional<PotionEffectType> optional = Sponge.game().registry(RegistryTypes.POTION_EFFECT_TYPE).findValue(ResourceKey.resolve(effectName));
        if (!optional.isPresent()) {
            throw new InvalidDataException("The container has an invalid potion type name: " + effectName);
        }
        final Ticks duration = SpongeTicks.ticksOrInfinite(container.getInt(Constants.Item.Potions.POTION_DURATION).get());
        final int amplifier = container.getInt(Constants.Item.Potions.POTION_AMPLIFIER).get();
        final boolean ambience = container.getBoolean(Constants.Item.Potions.POTION_AMBIANCE).get();
        final boolean particles = container.getBoolean(Constants.Item.Potions.POTION_SHOWS_PARTICLES).get();
        final boolean showsIcon = container.getBoolean(Constants.Item.Potions.POTION_SHOWS_ICON).orElse(true);
        final PotionEffect.Builder builder = new SpongePotionBuilder();

        return Optional.of(builder.potionType(optional.get())
                .showParticles(particles)
                .showIcon(showsIcon)
                .duration(duration)
                .amplifier(amplifier)
                .ambient(ambience)
                .build());
    }

    @Override
    public PotionEffect.@NonNull Builder potionType(final @NonNull PotionEffectType potionEffectType) {
        Objects.requireNonNull(potionEffectType, "Potion effect type cannot be null");
        this.potionType = potionEffectType;
        return this;
    }

    @Override
    public PotionEffect.Builder duration(final @NonNull Ticks duration) {
        if (!duration.isInfinite() && duration.ticks() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.duration = duration;
        return this;
    }

    @Override
    public PotionEffect.Builder amplifier(final int amplifier) {
        this.amplifier = amplifier;
        return this;
    }

    @Override
    public PotionEffect.Builder ambient(final boolean ambience) {
        this.isAmbient = ambience;
        return this;
    }

    @Override
    public PotionEffect.Builder showParticles(final boolean showParticles) {
        this.showParticles = showParticles;
        return this;
    }

    @Override
    public PotionEffect.Builder showIcon(final boolean showIcon) {
        this.showIcon = showIcon;
        return this;
    }

    @Override
    public PotionEffect build() throws IllegalStateException {
        Preconditions.checkState(this.potionType != null, "Potion type has not been set");
        if (!this.duration.isInfinite() && this.duration.ticks() <= 0) {
            throw new IllegalStateException("Duration has not been set");
        }
        return (PotionEffect) new MobEffectInstance(Holder.direct((MobEffect) this.potionType),
                SpongeTicks.toSaturatedIntOrInfinite(this.duration),
                this.amplifier,
                this.isAmbient,
                this.showParticles,
                this.showIcon);
    }

    @Override
    public PotionEffect.Builder reset() {
        this.potionType = null;
        this.amplifier = 0;
        this.duration = Ticks.zero();
        this.isAmbient = true;
        this.showParticles = true;
        this.showIcon = true;
        return this;
    }
}
