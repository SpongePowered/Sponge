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

import net.minecraft.block.material.MaterialColor;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.Color;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.math.GenericMath;

@Mixin(net.minecraft.item.DyeColor.class)
public abstract class DyeColorMixin_API implements DyeColor {

    @Shadow public abstract float[] shadow$getColorComponentValues();

    private CatalogKey api$key;
    private Translation api$translation;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void api$setKeyAndTranslation(String enumName, int ordinal, int idIn, String translationKeyIn, int colorValueIn, MaterialColor mapColorIn,
        int fireworkColorIn, int textColorIn, CallbackInfo ci) {
        final PluginContainer container = SpongeImplHooks.getActiveModContainer();
        this.api$key = container.createCatalogKey(translationKeyIn);
        this.api$translation = new SpongeTranslation("color.minecraft." + translationKeyIn);
    }

    @Override
    public CatalogKey getKey() {
        return this.api$key;
    }

    @Override
    public Translation getTranslation() {
        return this.api$translation;
    }

    @Override
    public Color getColor() {
        float[] components = this.shadow$getColorComponentValues();
        int r = GenericMath.floor(components[0] * 255);
        int g = GenericMath.floor(components[1] * 255);
        int b = GenericMath.floor(components[2] * 255);
        return Color.ofRgb(r, g, b);
    }
}
