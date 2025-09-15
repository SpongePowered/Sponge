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
package org.spongepowered.common.item;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.TeleportRandomlyConsumeEffect;
import org.spongepowered.api.data.type.ItemAction;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.tag.Tag;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SpongeItemActionFactory implements ItemAction.Factory {

    @Override
    public ItemAction.ApplyEffects applyEffects(final double chance, final List<PotionEffect> effects) {
        Objects.requireNonNull(effects, "effects");
        if (chance < 0 || chance > 1) {
            throw new IllegalArgumentException("chance must be in range [0; 1]: " + chance);
        }
        return (ItemAction.ApplyEffects) (Object) new ApplyStatusEffectsConsumeEffect((List) List.copyOf(effects), (float) chance);
    }

    @Override
    public ItemAction.RemoveEffects removeEffects(final Set<PotionEffectType> effectTypes) {
        Objects.requireNonNull(effectTypes, "effectTypes");
        final var holderSet = HolderSet.direct(
            effectType -> BuiltInRegistries.MOB_EFFECT.wrapAsHolder((MobEffect) effectType),
            effectTypes);
        return (ItemAction.RemoveEffects) (Object) new RemoveStatusEffectsConsumeEffect(holderSet);
    }

    @Override
    public ItemAction.RemoveEffects removeEffects(final Tag<PotionEffectType> effectTypeTag) {
        Objects.requireNonNull(effectTypeTag, "effectTypeTag");
        final var tag = (TagKey<MobEffect>) (Object) effectTypeTag;
        final var holderSet = BuiltInRegistries.MOB_EFFECT.get(tag).map(hs -> (HolderSet<MobEffect>) hs).orElse(HolderSet.empty());
        return (ItemAction.RemoveEffects) (Object) new RemoveStatusEffectsConsumeEffect(holderSet);
    }

    @Override
    public ItemAction.ClearEffects clearEffects() {
        return (ItemAction.ClearEffects) (Object) ClearAllStatusEffectsConsumeEffect.INSTANCE;
    }

    @Override
    public ItemAction.PlaySound playSound(final SoundType soundType) {
        Objects.requireNonNull(soundType, "soundType");
        return (ItemAction.PlaySound) (Object) new PlaySoundConsumeEffect(BuiltInRegistries.SOUND_EVENT.wrapAsHolder((SoundEvent) (Object) soundType));
    }

    @Override
    public ItemAction.TeleportRandomly teleportRandomly(final double distance) {
        if (distance <= 0) {
            throw new IllegalArgumentException("distance must be positive: " + distance);
        }
        return (ItemAction.TeleportRandomly) (Object) new TeleportRandomlyConsumeEffect((float) distance * 2);
    }
}
