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
package org.spongepowered.common.mixin.core.client.network.chat;

import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
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

@Mixin(TranslatableComponent.class)
public abstract class TranslatableComponentMixin {

    @Shadow @Final private String key;
    private String impl$lastLocale;
    private Component impl$translated = (Component) this;

    @Inject(method = "visitSelf(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;", at = @At("HEAD"), cancellable = true)
    private <T> void impl$translateForRendering(final FormattedText.StyledContentConsumer<T> visitor, final Style style, final CallbackInfoReturnable<Optional<T>> ci) {
        final String currentLocale = Minecraft.getInstance().options.languageCode;
        if (!Objects.equals(currentLocale, this.impl$lastLocale)) { // retranslate
            this.impl$lastLocale = currentLocale;
            final Locale actualLocale = Locales.of(currentLocale);

            // Only do a deep copy if actually necessary
            if (GlobalTranslator.get().translate(this.key, actualLocale) != null) {
                this.impl$translated = NativeComponentRenderer.apply((Component) this, Locales.of(currentLocale));
            } else {
                this.impl$translated = (Component) this;
            }
        }

        // If the result is a non-translated component, then Adventure found a translation that we should use
        if (!(this.impl$translated instanceof TranslatableComponent)) {
            ci.setReturnValue(this.impl$translated.visitSelf(visitor, style));
        }
    }
}
