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

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.registry.type.effect.PotionEffectTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import javax.annotation.Nullable;

@Mixin(Potion.class)
@Implements(@Interface(iface = PotionEffectType.class, prefix = "potion$"))
public abstract class PotionMixin_API implements PotionEffectType {

    @Shadow @Final public static RegistryNamespaced<ResourceLocation, Potion> REGISTRY;

    @Shadow public abstract String shadow$getName();
    @Shadow public abstract boolean shadow$isInstant();

    @Nullable private Translation api$translation;
    @Nullable private Translation api$potionTranslation;
    @Nullable private String spongeResourceID;

    @Intrinsic
    public String potion$getId() {
        if (this.spongeResourceID == null) {
            final ResourceLocation location = REGISTRY.func_177774_c((Potion) (Object) this);
            if (location == null) {
                this.spongeResourceID = "unknown";
            } else {
                this.spongeResourceID = location.toString();
            }
        }
        return this.spongeResourceID;
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
        if (this.api$translation == null) {
            this.api$translation = new SpongeTranslation(shadow$getName());
        }
        return this.api$translation;
    }

    // TODO: Remove this from the API or change return type to Optional
    @Override
    public Translation getPotionTranslation() {
        if (this.api$potionTranslation == null) {
            String name = shadow$getName();
            final String potionId = "potion." + PotionEffectTypeRegistryModule.potionMapping.getOrDefault(name, "effect.missing");
            this.api$potionTranslation = new SpongeTranslation(potionId);
        }
        return this.api$potionTranslation;
    }

}
