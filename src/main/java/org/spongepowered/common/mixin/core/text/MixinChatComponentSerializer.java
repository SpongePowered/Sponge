
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

package org.spongepowered.common.mixin.core.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IChatComponent.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.text.ChatComponentPlaceholder;

import java.lang.reflect.Type;
import java.util.List;

@Mixin(Serializer.class)
public abstract class MixinChatComponentSerializer {

    @Shadow
    public abstract IChatComponent deserialize(JsonElement jsonElement, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_);

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/util/IChatComponent;",
            at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonElement;getAsJsonObject()Lcom/google/gson/JsonObject;", ordinal = 0, shift = Shift.BY, by = 3, remap = false),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void deserializePlaceholder(JsonElement jsonElement, Type type, JsonDeserializationContext context,
            CallbackInfoReturnable<IChatComponent> cir, JsonObject jsonObject) {
        if (jsonObject.has("placeholderKey")) {
            final String placeholderKey = jsonObject.get("placeholderKey").getAsString();
            final ChatComponentPlaceholder deserialized = new ChatComponentPlaceholder(placeholderKey);

            // Default stuff - START
            if (jsonObject.has("extra")) {
                JsonArray extraElements = jsonObject.getAsJsonArray("extra");

                if (extraElements.size() == 0) {
                    throw new JsonParseException("Unexpected empty array of components");
                }
                // Sponge -- strip out the first extra element if it has been added to the extra (for vanilla compat)
                boolean first = true;
                for (JsonElement extraElement : extraElements) {
                    if (first && jsonObject.has("fallbackAsExtra")) {
                        deserialized.setFallback(this.deserialize(extraElement, type, context));
                    } else {
                        deserialized.appendSibling(this.deserialize(extraElement, type, context));
                    }
                    first = false;
                }
            }

            deserialized.setChatStyle(context.<ChatStyle>deserialize(jsonElement, ChatStyle.class));
            // Default stuff - END

            cir.setReturnValue(deserialized);
        }
    }

    @Shadow
    abstract void serializeChatStyle(ChatStyle style, JsonObject object, JsonSerializationContext ctx);

    @Shadow
    public abstract JsonElement serialize(IChatComponent chatComponent, Type type, JsonSerializationContext context);

    @Inject(method = "serialize(Lnet/minecraft/util/IChatComponent;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At(value = "HEAD"),
            cancellable = true)
    public void serializePlaceholder(IChatComponent chatComponent, Type type, JsonSerializationContext context,
            CallbackInfoReturnable<JsonElement> cir) {
        if (chatComponent instanceof ChatComponentPlaceholder) {
            final ChatComponentPlaceholder placeholder = (ChatComponentPlaceholder) chatComponent;
            final JsonObject serialized = new JsonObject();
            serialized.addProperty("placeholderKey", placeholder.getTransformerKey());

            if (placeholder.getFallback() == null) {
                serialized.addProperty("text", placeholder.getUnformattedTextForChat());
            } else {
                serialized.addProperty("text", "");
            }

            // Default stuff - START
            if (!chatComponent.getChatStyle().isEmpty()) {
                this.serializeChatStyle(chatComponent.getChatStyle(), serialized, context);
            }

            @SuppressWarnings("unchecked")
            List<IChatComponent> siblings = chatComponent.getSiblings();
            if (!siblings.isEmpty() || placeholder.getFallback() != null) {
                JsonArray extraElements = new JsonArray();
                if (placeholder.getFallback() != null) {
                    final IChatComponent fallback = placeholder.getFallback();
                    serialized.addProperty("fallbackAsExtra", true);
                    extraElements.add(this.serialize(fallback, fallback.getClass(), context));
                }

                for (IChatComponent sibling : siblings) {
                    extraElements.add(this.serialize(sibling, sibling.getClass(), context));
                }

                serialized.add("extra", extraElements);
            }
            // Default stuff - END

            cir.setReturnValue(serialized);
        }
    }

}
