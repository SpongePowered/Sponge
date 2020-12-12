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
package org.spongepowered.common.mixin.core.client.util.text;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.adventure.NativeComponentRenderer;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.text.html.Option;

@Mixin(TranslationTextComponent.class)
public abstract class TranslationTextComponentMixin extends TextComponent {

    @Shadow @Final private String key;
    private String impl$lastLocale;
    private ITextComponent impl$translated = this;

    @Inject(method = "visitSelf(Lnet/minecraft/util/text/ITextProperties$IStyledTextAcceptor;Lnet/minecraft/util/text/Style;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private <T> void impl$translateForRendering(final IStyledTextAcceptor<T> visitor, final Style style, final CallbackInfoReturnable<Optional<T>> ci) {
        final String currentLocale = Minecraft.getInstance().options.languageCode;
        if (!Objects.equals(currentLocale, this.impl$lastLocale)) { // retranslate
            this.impl$lastLocale = currentLocale;
            final Locale actualLocale = Locales.of(currentLocale);

            // Only do a deep copy if actually necessary
            if (GlobalTranslator.get().translate(this.key, actualLocale) != null) {
                this.impl$translated = NativeComponentRenderer.apply(this, Locales.of(currentLocale));
            } else {
                this.impl$translated = this;
            }
        }

        // If the result is a non-translated component, then Adventure found a translation that we should use
        if (!(this.impl$translated instanceof TranslationTextComponent)) {
            ci.setReturnValue(this.impl$translated.visitSelf(visitor, style));
        }
    }
}
