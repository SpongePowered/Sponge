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
package org.spongepowered.common.mixin.api.mcp.util;

import net.kyori.adventure.text.Component;
import net.minecraft.util.HandSide;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.type.HandPreference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.adventure.SpongeAdventure;

import javax.annotation.Nullable;

@Mixin(HandSide.class)
public abstract class HandSideMixin_API implements HandPreference {

    @Shadow private ITextComponent handName;

    @Nullable private ResourceKey api$key;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void api$setKeyAndTranslation(String enumName, int ordinal, ITextComponent name, CallbackInfo ci) {
        this.api$key = ResourceKey.of(SpongeCommon.getActivePlugin(), ((TranslationTextComponent) name).getKey().replace("options.mainHand.", ""));
    }

    @Override
    public ResourceKey getKey() {
        return this.api$key;
    }

    @Override
    public Component asComponent() {
        return SpongeAdventure.asAdventure(this.handName);
    }
}
