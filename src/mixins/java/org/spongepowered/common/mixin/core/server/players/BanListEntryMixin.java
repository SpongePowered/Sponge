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
package org.spongepowered.common.mixin.core.server.players;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.players.BanListEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.server.players.BanListEntryBridge;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(BanListEntry.class)
public abstract class BanListEntryMixin<T> extends StoredUserEntryMixin<T> implements BanListEntryBridge {

    // @formatter:off
    @Shadow @Final @Nullable protected String reason;
    @Shadow @Final protected String source;
    // @formatter:on

    @Nullable private Component impl$reason;
    @Nullable private Component impl$source;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void bridge$initializeFields(final CallbackInfo ci) { // Prevent this from being overriden in UserListIPBansEntryMixin
        final LegacyComponentSerializer lcs = LegacyComponentSerializer.legacySection();
        this.impl$reason = this.reason == null ? null : lcs.deserialize(this.reason);
        this.impl$source = lcs.deserialize(this.source);

//        final Optional<Player> user;
//        if ("Server".equals(this.source)) { // There could be a user called Server, but of course Mojang doesn't care...
//            this.bridge$commandSrc = SpongeCommon.game().getServer().getConsole();
//        } else if ((user = Sponge.game().getServer().player(this.bannedBy)).isPresent()) {
//            this.bridge$commandSrc = user.get();
//        }
    }

    @Override
    public Optional<Component> bridge$getReason() {
        return Optional.ofNullable(this.impl$reason);
    }

    @Override
    public Optional<Component> bridge$getSource() {
        return Optional.ofNullable(this.impl$source);
    }

}
