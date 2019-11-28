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

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.registry.util.RegistrationDependency;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.chat.ChatVisibilities;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.common.bridge.entity.player.EnumChatVisibilityBridge;
import org.spongepowered.common.registry.type.MinecraftEnumBasedAlternateCatalogTypeRegistryModule;

@RegisterCatalog(ChatVisibilities.class)
@RegistrationDependency(ChatTypeRegistryModule.class)
public final class ChatVisibilityRegistryModule extends MinecraftEnumBasedAlternateCatalogTypeRegistryModule<PlayerEntity.EnumChatVisibility, ChatVisibility>{

    @Override
    public void registerDefaults() {
        this.setChatTypes();
    }

    private void setChatTypes() {
        // We can't do this in the EnumChatVisibility constructor, since the registry isn't initialized then
        PlayerEntity.EnumChatVisibility FULL = PlayerEntity.EnumChatVisibility.FULL;
        PlayerEntity.EnumChatVisibility SYSTEM = PlayerEntity.EnumChatVisibility.SYSTEM;
        PlayerEntity.EnumChatVisibility HIDDEN = PlayerEntity.EnumChatVisibility.HIDDEN;

        ((EnumChatVisibilityBridge) (Object) FULL).bridge$setChatTypes(ImmutableSet.copyOf(Sponge.getRegistry().getAllOf(ChatType.class)));
        ((EnumChatVisibilityBridge) (Object) SYSTEM).bridge$setChatTypes(ImmutableSet.of(ChatTypes.SYSTEM, ChatTypes.ACTION_BAR));
        ((EnumChatVisibilityBridge) (Object) HIDDEN).bridge$setChatTypes(ImmutableSet.of());
    }

    @AdditionalRegistration
    public void customRegistration() {
        for (PlayerEntity.EnumChatVisibility visibility : PlayerEntity.EnumChatVisibility.values()) {
            if (!this.catalogTypeMap.containsKey(enumAs(visibility).getId())) {
                this.catalogTypeMap.put(enumAs(visibility).getId(), enumAs(visibility));
            }
        }
    }

    @Override
    protected PlayerEntity.EnumChatVisibility[] getValues() {
        return PlayerEntity.EnumChatVisibility.values();
    }

}
