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
package org.spongepowered.common.mixin.core.network.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.HolderLookup;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.adventure.AdventureTextComponent;

@Mixin(net.minecraft.network.chat.Component.Serializer.class)
public abstract class Component_SerializerMixin {
    @Shadow
    static JsonElement shadow$serialize(final net.minecraft.network.chat.Component text, HolderLookup.Provider $$1) {
        throw new UnsupportedOperationException("Shadowed createLegacyDisconnectPacket");
    }

    @Shadow @Final private static Gson GSON;

    @Inject(method = "serialize", at = @At("HEAD"), cancellable = true)
    private static void impl$writeComponentText(final net.minecraft.network.chat.Component text, final HolderLookup.Provider $$1, final CallbackInfoReturnable<JsonElement> cir) {
        if(text instanceof AdventureTextComponent atc) {
            final net.minecraft.network.chat.@Nullable Component converted = ((AdventureTextComponent) text).deepConvertedIfPresent();
            if(converted != null) {
                cir.setReturnValue(Component_SerializerMixin.shadow$serialize(text, $$1));
            } else {
                // TODO actually fix this
                // cir.setReturnValue(ctx.serialize(((AdventureTextComponent) text).wrapped(), Component.class));
            }
        }
    }

    // inject into the anonymous function to build a gson instance
    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "com/google/gson/GsonBuilder.disableHtmlEscaping()Lcom/google/gson/GsonBuilder;", remap = false), remap = false)
    private static GsonBuilder impl$injectAdventureGson(final GsonBuilder gson) {
        gson.disableHtmlEscaping();
        GsonComponentSerializer.gson().populator().apply(gson);
        return gson;
    }

    @Inject(method = "serialize", at = @At("HEAD"), cancellable = true)
    private static void impl$redirectSerialization(final net.minecraft.network.chat.Component component, HolderLookup.Provider $$1, final CallbackInfoReturnable<JsonElement> cir) {
        if (component instanceof AdventureTextComponent atc) {
            cir.setReturnValue(GSON.toJsonTree(atc.wrapped()));
        }
    }

}
