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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.lang.reflect.Type;

@Mixin(Serializer.class)
public class MixinChatComponentSerializer {

    @Redirect(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/util/IChatComponent;",
            at = @At(value = "NEW", target = "class=net.minecraft.util.ChatComponentText.ChatComponentText", ordinal=0))
    public ChatComponentText deserializePlaceholder(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) {
        return new ChatComponentText("TEST");
    }

    @Redirect(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/util/IChatComponent;",
            at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonObject;has(Ljava/lang/String;)Z", ordinal=0))
    public boolean deserializeTextOrPlaceholder(JsonObject jsonobject, String key) {
        return jsonobject.has("text") || jsonobject.has("placeholderKey");
    }

//    @Inject(method = "serialize(Lnet/minecraft/util/IChatComponent;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
//            at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonObject;addProperty(Ljava/lang/String;Ljava/lang/String;)V", ordinal=0, shift = Shift.AFTER),
//            locals=LocalCapture.CAPTURE_FAILHARD)
//    public void serializePlaceholder(IChatComponent p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_,
//            CallbackInfoReturnable<IChatComponent> cir, JsonObject jsonobject) {
//        if (p_serialize_1_ instanceof ChatComponentPlaceholder)
//        {
//            jsonobject.addProperty("placeholderKey", ((ChatComponentPlaceholder) p_serialize_1_).getPlaceholderKey());
//        }
//    }

//    @Redirect(method = "serialize(Lnet/minecraft/util/IChatComponent;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
//            at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z"))
//    public boolean serializePlaceholderInstead(IChatComponent p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_,
//                    JsonObject jsonobject) {
//        return p_serialize_1_.getChatStyle().isEmpty() && !(p_serialize_1_ instanceof ChatComponentPlaceholder);
//    }

    // TODO: Move this somewhere else
    public static class ChatComponentPlaceholder extends ChatComponentText {

        private final String placeholderKey;

        public ChatComponentPlaceholder(String placeholderKey) {
            this(placeholderKey, "{" + placeholderKey + "}");
        }

        public ChatComponentPlaceholder(String placeholderKey, String msg) {
            super(msg);
            this.placeholderKey = placeholderKey;
        }

        public String getPlaceholderKey() {
            return this.placeholderKey;
        }

    }

}
