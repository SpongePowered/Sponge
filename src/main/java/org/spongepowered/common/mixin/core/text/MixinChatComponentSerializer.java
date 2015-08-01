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
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Type;

@Mixin(Serializer.class)
public class MixinChatComponentSerializer {

    @Inject(method = "net.minecraft.util.IChatComponent.Serializer.deserialize(JsonElement, Type, JsonDeserializationContext)",
            at = @At(value = "INVOKE_STRING", target = "com.google.gson.JsonObject.has(String)", shift = Shift.BY, by = -2))
    public void deserializePlaceholder(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_,
            JsonObject jsonobject, Object object) {
        if (jsonobject.has("placeholderKey"))
        {
            String placeholderKey = jsonobject.get("placeholderKey").getAsString();
            if (jsonobject.has("text")) {
                object = new ChatComponentPlaceholder(placeholderKey, jsonobject.get("text").getAsString());
            } else {
                object = new ChatComponentPlaceholder(placeholderKey);
            }
        }
    }

    @Redirect(method = "net.minecraft.util.IChatComponent.Serializer.deserialize(JsonElement, Type, JsonDeserializationContext)",
            at = @At(value = "INVOKE_STRING", target = "com.google.gson.JsonObject.has(String)"))
    public boolean deserializePlaceholder(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_,
            JsonObject jsonobject) {
        return jsonobject.has("text") && !jsonobject.has("placeholderKey");
    }

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
