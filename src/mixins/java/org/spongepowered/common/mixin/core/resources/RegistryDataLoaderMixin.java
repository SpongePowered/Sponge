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
package org.spongepowered.common.mixin.core.resources;

import com.mojang.serialization.Decoder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.api.adventure.ChatTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {

    @SuppressWarnings("unchecked")
    @Inject(method = "loadContentsFromManager", at = @At("RETURN"))
    private static <E> void impl$afterLoadRegistryContents(
            final ResourceManager $$0,
            final RegistryOps.RegistryInfoLookup $$1,
            final WritableRegistry<E> $$2,
            final Decoder<E> $$3,
            final Map<ResourceKey<?>, Exception> $$4,
            final CallbackInfo ci)
    {
        if (Registries.CHAT_TYPE.equals($$2.key())) {
            final ChatTypeDecoration narration = ChatTypeDecoration.withSender("chat.type.text.narrate");
            $$2.register(ResourceKey.create($$2.key(), (ResourceLocation) (Object) ChatTypes.CUSTOM_CHAT.location()), (E) new ChatType(ChatTypeDecoration.withSender("%s%s"), narration), RegistrationInfo.BUILT_IN);
            $$2.register(ResourceKey.create($$2.key(), (ResourceLocation) (Object) ChatTypes.CUSTOM_MESSAGE.location()), (E) new ChatType(ChatTypeDecoration.teamMessage("%s%s%s"), narration), RegistrationInfo.BUILT_IN);
        }
    }
}
