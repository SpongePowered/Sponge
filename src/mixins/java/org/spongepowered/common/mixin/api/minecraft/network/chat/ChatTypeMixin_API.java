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
package org.spongepowered.common.mixin.api.minecraft.network.chat;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.network.chat.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;

import java.util.Optional;

@Mixin(ChatType.class)
public abstract class ChatTypeMixin_API implements org.spongepowered.api.adventure.ChatType {

    // @formatter:off

    @Shadow @Final private ChatTypeDecoration chat;
    @Shadow @Final private ChatTypeDecoration narration;

    // @formatter:on

    @Override
    public @NonNull Bound bind(@NonNull final ComponentLike name, @Nullable final ComponentLike target) {
        var ret = new ChatType.Bound(Holder.direct((ChatType) (Object) this), SpongeAdventure.asVanilla(name.asComponent()),
                Optional.ofNullable(target).map(ComponentLike::asComponent).map(SpongeAdventure::asVanilla));
        return (net.kyori.adventure.chat.ChatType.Bound) (Object) ret;
    }

    @Override
    public String translationKey() {
        return this.chat.translationKey();
    }

    @Override
    public Style style() {
        return SpongeAdventure.asAdventure(this.chat.style());
    }

    @Mixin(ChatType.Bound.class)
    public static abstract class BoundMixin_API implements net.kyori.adventure.chat.ChatType.Bound {

        // @formatter:off
        @Shadow public abstract Component shadow$name();
        @Shadow public abstract Optional<Component> shadow$targetName();
        @Shadow public abstract Holder<ChatType> shadow$chatType();
        // @formatter:on

        @Override
        public net.kyori.adventure.chat.@NonNull ChatType type() {
            return (net.kyori.adventure.chat.ChatType) (Object) this.shadow$chatType().value();
        }

        @Override
        public net.kyori.adventure.text.@NonNull Component name() {
            return SpongeAdventure.asAdventure(this.shadow$name());
        }

        @Override
        public net.kyori.adventure.text.@Nullable Component target() {
            return this.shadow$targetName().map(SpongeAdventure::asAdventure).orElse(null);
        }

    }
}
