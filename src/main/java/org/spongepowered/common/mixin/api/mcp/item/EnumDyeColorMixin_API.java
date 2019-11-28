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
package org.spongepowered.common.mixin.api.mcp.item;

import com.flowpowered.math.GenericMath;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.Color;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.translation.SpongeTranslation;

@Mixin(net.minecraft.item.DyeColor.class)
@Implements(@Interface(iface = DyeColor.class, prefix = "dye$"))
public abstract class EnumDyeColorMixin_API implements DyeColor {

    @Shadow public abstract String shadow$getTranslationKey();
    @Shadow public abstract String shadow$getName();

    @Shadow public abstract float[] getColorComponentValues();

    private Translation api$translation;

    @Intrinsic
    public String dye$getName() {
        return this.shadow$getTranslationKey();
    }

    public String dye$getId() {
        return shadow$getName();
    }

    public Color dye$getColor() {
        float[] color = getColorComponentValues();
        int r = GenericMath.floor(color[0] * 255);
        int g = GenericMath.floor(color[1] * 255);
        int b = GenericMath.floor(color[2] * 255);
        return Color.ofRgb(r, g, b);
    }

    public Translation dye$getTranslation() {
        // Maybe move this to a @Inject at the end of the constructor
        if (this.api$translation == null) {
            this.api$translation = new SpongeTranslation("item.dyePowder." + this.shadow$getTranslationKey() + ".name");
        }
        return this.api$translation;
    }

}
