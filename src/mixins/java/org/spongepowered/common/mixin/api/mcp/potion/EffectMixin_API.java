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
package org.spongepowered.common.mixin.api.mcp.potion;

import com.google.common.collect.ImmutableMap;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Map;

import javax.annotation.Nullable;

@Mixin(Effect.class)
@Implements(@Interface(iface = PotionEffectType.class, prefix = "potionEffectType$"))
public abstract class EffectMixin_API implements PotionEffectType {

    private static final Map<String, String> potionMapping = ImmutableMap.<String, String>builder()
            .put("effect.damageBoost", "effect.strength")
            .put("effect.fireResistance", "effect.fire_resistance")
            .put("effect.harm", "effect.harming")
            .put("effect.heal", "effect.healing")
            .put("effect.invisibility", "effect.invisibility")
            .put("effect.jump", "effect.leaping")
            .put("effect.luck", "effect.luck")
            .put("effect.moveSlowdown", "effect.slowness")
            .put("effect.moveSpeed", "effect.swiftness")
            .put("effect.nightVision", "effect.night_vision")
            .put("effect.poison", "effect.poison")
            .put("effect.regeneration", "effect.regeneration")
            .put("effect.waterBreathing", "effect.water_breathing")
            .put("effect.weakness", "effect.weakness")
            .build();


    @Shadow public abstract String shadow$getName();
    @Shadow public abstract boolean shadow$isInstant();

    @Nullable private Translation api$translation;
    @Nullable private Translation api$potionTranslation;
    private ResourceKey api$key;

    @Override
    public ResourceKey getKey() {
        if (this.api$key == null) {
            this.api$key = (ResourceKey) (Object) Registry.EFFECTS.getKey((Effect) (Object) this);
        }
        return this.api$key;
    }

    @Intrinsic
    public boolean potionEffectType$isInstant() {
        return this.shadow$isInstant();
    }

    @Override
    public Translation getTranslation() {
        if (this.api$translation == null) {
            this.api$translation = new SpongeTranslation(this.shadow$getName());
        }
        return this.api$translation;
    }

    // TODO: Minecraft 1.14 - Remove this from the API or change return type to Optional
    // TODO: potionMapping is not up to date
    @Override
    public Translation getPotionTranslation() {
        if (this.api$potionTranslation == null) {
            String name = this.shadow$getName();
            final String potionId = "potion." + potionMapping.getOrDefault(name, "effect.missing");
            this.api$potionTranslation = new SpongeTranslation(potionId);
        }
        return this.api$potionTranslation;
    }

}
