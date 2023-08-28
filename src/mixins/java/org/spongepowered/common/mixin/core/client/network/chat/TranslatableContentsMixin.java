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

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.translation.GlobalTranslator;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.adventure.NativeComponentRenderer;

import java.util.List;
import java.util.Locale;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {

    // @formatter:off
    @Shadow @Final private String key;
    @Shadow private List<FormattedText> decomposedParts;
    // @formatter:on

    private Component impl$translated = null;

    @Inject(method = "decompose()V", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/network/chat/contents/TranslatableContents;decomposedWith:Lnet/minecraft/locale/Language;", shift = At.Shift.AFTER), cancellable = true)
    private <T> void impl$translateForRendering(final CallbackInfo ci) {
        // TODO investigate
        if (Minecraft.getInstance() == null) {
            return;
        }
        final Locale actualLocale = Locales.of(Minecraft.getInstance().options.languageCode);
        final Component toTranslate = MutableComponent.create((TranslatableContents) (Object) this);

        // Only do a deep copy if actually necessary
        if (GlobalTranslator.translator().translate(this.key, actualLocale) != null) {
            this.impl$translated = NativeComponentRenderer.apply(toTranslate, actualLocale);
        } else {
            this.impl$translated = toTranslate;
        }

        // If the result is a non-translated component, then Adventure found a translation that we should use
        if (!(this.impl$translated.getContents() instanceof TranslatableContents)) {
          this.decomposedParts = ImmutableList.of(this.impl$translated);
          ci.cancel();
        }
    }
}
