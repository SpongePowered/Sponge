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
package org.spongepowered.common.mixin.core.authlib;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.regex.Pattern;

@Mixin(value = ProfileSearchResultsResponse.Serializer.class, remap = false)
public abstract class ProfileSearchResultsResponseSerializerMixin {

    @Nullable private Pattern uuidPattern;

    @Inject(method = "deserialize", at = @At(value = "INVOKE", target = "Lcom/google/gson/JsonDeserializationContext;deserialize"
            + "(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;)Ljava/lang/Object;", remap = false), remap = false)
    private void onDeserializeFixInvalidIds(JsonElement json, Type typeOfT, JsonDeserializationContext context,
            CallbackInfoReturnable<ProfileSearchResultsResponse> cir) {
        // Occasionally we'll see a profile with an invalid UUID. The vanilla
        // code, as well as most clients, will break when receiving these
        // UUIDs. We'll suppress these errors for now; if one of them is a real
        // user, Mojang will need to address the situation.
        // See https://bugs.mojang.com/browse/WEB-1290.
        for (Iterator<JsonElement> iterator = json.getAsJsonArray().iterator(); iterator.hasNext(); ) {
            JsonObject element = iterator.next().getAsJsonObject();
            String id = element.get("id").getAsString();
            if (uuidPattern == null) {
                uuidPattern = Pattern.compile("[0-9a-fA-F-]+");
            }
            if (!uuidPattern.matcher(id).matches()) {
                SpongeImpl.getLogger().debug("Received invalid profile from Mojang for username " + element.get("name") + ", skipping");
                iterator.remove();
            }
        }
    }
}
