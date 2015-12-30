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

import net.minecraft.potion.Potion;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(Potion.class)
@Implements(@Interface(iface = PotionEffectType.class, prefix = "potion$"))
public abstract class MixinPotion implements PotionEffectType {

    @Shadow private String name;

    @Override
    @Shadow
    public abstract boolean isInstant();

    private Translation translation;
    private Translation potionTranslation;

    public String potion$getId() {
        return this.name;
    }

    public String potion$getName() {
        return this.name;
    }

    @Intrinsic
    public boolean potion$isInstant() {
        return this.isInstant();
    }

    @Shadow(prefix = "shadow$")
    public abstract String shadow$getName();

    @Override
    public Translation getTranslation() {
        // Maybe move this to a @Inject at the end of the constructor
        if (this.translation == null) {
            this.translation = new SpongeTranslation(shadow$getName());
        }
        return this.translation;
    }

    @Intrinsic
    @Override
    public Translation getPotionTranslation() {
        // Maybe move this to a @Inject at the end of the constructor
        if (this.potionTranslation == null) {
            this.potionTranslation = new SpongeTranslation(shadow$getName() + ".postfix");
        }
        return this.potionTranslation;
    }

}
