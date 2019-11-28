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
package org.spongepowered.common.registry.type.text;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.text.ChatType;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.common.registry.type.MinecraftEnumBasedAlternateCatalogTypeRegistryModule;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

@RegisterCatalog(ChatTypes.class)
public final class ChatTypeRegistryModule extends MinecraftEnumBasedAlternateCatalogTypeRegistryModule<ChatType,org.spongepowered.api.text.chat.ChatType> {

    public ChatTypeRegistryModule() {
        super(new String[] {"minecraft"});
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (ChatType chatType: ChatType.values()) {
            if (!this.catalogTypeMap.containsKey(enumAs(chatType).getId())) {
                this.catalogTypeMap.put(enumAs(chatType).getId(), enumAs(chatType));
            }
        }
    }


    @Override
    protected ChatType[] getValues() {
        return ChatType.values();
    }
}
