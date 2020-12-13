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
import net.kyori.adventure.text.Component;
import net.minecraft.potion.Effect;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Map;

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

    // @formatter:off
    @Shadow public abstract boolean shadow$isInstantenous();
    @Shadow public abstract ITextComponent shadow$getDisplayName();
    // @formatter:on

    private ResourceKey api$key;

    @Override
    public ResourceKey getKey() {
        if (this.api$key == null) {
            this.api$key = (ResourceKey) (Object) Registry.MOB_EFFECT.getKey((Effect) (Object) this);
        }
        return this.api$key;
    }

    public boolean potionEffectType$isInstant() {
        return this.shadow$isInstantenous();
    }

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.shadow$getDisplayName());
    }

}
