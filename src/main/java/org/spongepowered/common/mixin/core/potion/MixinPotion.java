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
package org.spongepowered.common.mixin.core.potion;

import com.google.common.collect.ImmutableMap;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.interfaces.potion.IMixinPotion;
import org.spongepowered.common.registry.type.effect.PotionEffectTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Map;

import javax.annotation.Nullable;

@Mixin(Potion.class)
@Implements(@Interface(iface = PotionEffectType.class, prefix = "potion$"))
public abstract class MixinPotion implements PotionEffectType, IMixinPotion {

    @Shadow public abstract String shadow$getName();
    @Shadow public abstract boolean shadow$isInstant();

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

    private Translation translation;
    private Translation potionTranslation;
    @Nullable private CatalogKey key;

    public CatalogKey potion$getKey() {
        if (this.key == null) { // Just in case???
            this.key = (CatalogKey) (Object) IRegistry.MOB_EFFECT.getKey((Potion) (Object) this);
        }
        return this.key;
    }


    @Intrinsic
    public boolean potion$isInstant() {
        return shadow$isInstant();
    }

    @Intrinsic
    public String potion$getName() {
        return this.shadow$getName();
    }

    @Override
    public Translation getTranslation() {
        // Maybe move this to a @Inject at the end of the constructor
        if (this.translation == null) {
            this.translation = new SpongeTranslation(shadow$getName());
        }
        return this.translation;
    }

    // TODO: Remove this from the API or change return type to Optional
    @Override
    public Translation getPotionTranslation() {
        if (this.potionTranslation == null) {
            String name = shadow$getName();
            this.potionTranslation = new SpongeTranslation("potion." + potionMapping.getOrDefault(name, "effect.missing"));
        }
        return this.potionTranslation;
    }

    @Override
    public void setId(ResourceLocation id) {
        this.key = (CatalogKey) (Object) id;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/IRegistry;register(ILnet/minecraft/util/ResourceLocation;Ljava/lang/Object;)V"))
    private static void onPotionRegister(IRegistry registry, int id, ResourceLocation location, Object potion) {
        final Potion mcPotion = (Potion) potion;
        ((IMixinPotion) mcPotion).setId(location);
        PotionEffectTypeRegistryModule.getInstance().registerFromGameData(location, (PotionEffectType) mcPotion);
        registry.register(id, location, potion);
    }

}
