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
package org.spongepowered.forge.mixin.core.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.advancements.AdvancementBridge;

@Mixin(Advancement.Builder.class)
public abstract class Advancement_BuilderMixin implements AdvancementBridge {

    // Serializing causes the following JSON: "rewards": null
    // Deserializing does consider JsonNull to be an error here - so we fix it
    @Redirect(
            method = "fromJson(Lcom/google/gson/JsonObject;Lnet/minecraft/advancements/critereon/DeserializationContext;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lnet/minecraft/advancements/Advancement$Builder;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/gson/JsonObject;has(Ljava/lang/String;)Z",
                    ordinal = 2,
                    remap = false
            )
    )
    private static boolean impl$onHasRewards(final JsonObject p_241043_0_, final String rewards) {
        if (p_241043_0_.has(rewards)) {
            return !p_241043_0_.get(rewards).isJsonNull();
        }
        return false;
    }
}
