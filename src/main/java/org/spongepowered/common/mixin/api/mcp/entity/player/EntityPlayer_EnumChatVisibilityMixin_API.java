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
package org.spongepowered.common.mixin.api.mcp.entity.player;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.entity.player.EnumChatVisibilityBridge;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Locale;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.EnumChatVisibility.class)
public abstract class EntityPlayer_EnumChatVisibilityMixin_API implements ChatVisibility {

    @Shadow @Final private String resourceKey;

    private String api$id;
    private Translation api$translation;

    @Override
    public boolean isVisible(final ChatType type) {
        checkNotNull(type, "type");
        return ((EnumChatVisibilityBridge) this).bridge$getVisibleChatTypes().contains(type);
    }

    @Override
    public String getId() {
        if (this.api$id == null) {
            this.api$id = SpongeImplHooks.getModIdFromClass(this.getClass()) + ":" + ((PlayerEntity.EnumChatVisibility) (Object) this).name().toLowerCase(Locale.ENGLISH);
        }
        return this.api$id;
    }

    @Override
    public String getName() {
        return this.api$translation.get();
    }

    @Override
    public Translation getTranslation() {
        if (this.api$translation == null) {
            this.api$translation = new SpongeTranslation(this.resourceKey);
        }
        return this.api$translation;
    }

}
