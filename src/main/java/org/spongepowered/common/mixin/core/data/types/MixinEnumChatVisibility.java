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
package org.spongepowered.common.mixin.core.data.types;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.chat.ChatVisibility;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.IMixinEnumChatVisibility;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;

@Mixin(EntityPlayer.EnumChatVisibility.class)
public abstract class MixinEnumChatVisibility implements ChatVisibility, IMixinEnumChatVisibility {

    @Shadow @Final private String resourceKey;
    @Nullable private Translation translation;
    private Set<ChatType> visibleChatTypes;
    private CatalogKey key;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void construct(String name, int i, int i2, String s2, CallbackInfo ci) {
        this.visibleChatTypes = Sets.newHashSet();

        this.key = CatalogKey.of(SpongeImplHooks.getModIdFromClass(this.getClass()), ((EntityPlayer.EnumChatVisibility) (Object) this).name().toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void setChatTypes(Set<ChatType> chatTypes) {
        this.visibleChatTypes = chatTypes;
    }

    @Override
    public boolean isVisible(ChatType type) {
        checkNotNull(type, "type");
        return this.visibleChatTypes.contains(type);
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public String getName() {
        return this.resourceKey;
    }

    @Override
    public Translation getTranslation() {
        if (this.translation == null) {
            this.translation = new SpongeTranslation(this.resourceKey);
        }
        return this.translation;
    }

}
