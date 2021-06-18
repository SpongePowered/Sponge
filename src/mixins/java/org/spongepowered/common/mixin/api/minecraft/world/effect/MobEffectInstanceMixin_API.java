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

import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.util.Constants;

@Mixin(net.minecraft.world.effect.MobEffectInstance.class)
public abstract class MobEffectInstanceMixin_API implements PotionEffect {

    // @formatter:off
    @Shadow @Final private MobEffect effect;
    @Shadow private int duration;
    @Shadow private int amplifier;
    @Shadow private boolean ambient;
    @Shadow private boolean visible;
    // @formatter:on

    @Override
    public PotionEffectType type() {
        return (PotionEffectType) this.effect;
    }

    @Override
    public int duration() {
        return this.duration;
    }

    @Override
    public int amplifier() {
        return this.amplifier;
    }

    @Override
    public boolean isAmbient() {
        return this.ambient;
    }

    @Override
    public boolean showsParticles() {
        return this.visible;
    }

    @Override
    public int contentVersion() {
        return Constants.Sponge.Potion.CURRENT_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        final ResourceKey key = (ResourceKey) (Object) Registry.MOB_EFFECT.getKey((MobEffect) this.type());
        
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.Item.Potions.POTION_TYPE, key)
                .set(Constants.Item.Potions.POTION_DURATION, this.duration)
                .set(Constants.Item.Potions.POTION_AMPLIFIER, this.amplifier)
                .set(Constants.Item.Potions.POTION_AMBIANCE, this.ambient)
                .set(Constants.Item.Potions.POTION_SHOWS_PARTICLES, this.visible);
    }
}
