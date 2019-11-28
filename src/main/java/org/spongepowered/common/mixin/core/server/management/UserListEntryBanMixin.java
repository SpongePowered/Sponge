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
package org.spongepowered.common.mixin.core.server.management;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.server.management.BanUserListEntryBridge;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.server.management.BanEntry;

@Mixin(BanEntry.class)
public abstract class UserListEntryBanMixin<T> extends UserListEntryMixin<T> implements BanUserListEntryBridge {

    @Shadow @Final @Nullable protected String reason;
    @Shadow @Final protected String bannedBy;

    @Nullable private Text bridge$reason;
    @Nullable private Text bridge$source;
    @Nullable private CommandSource bridge$commandSrc;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void bridge$initializeFields(final CallbackInfo ci) { // Prevent this from being overriden in UserListIPBansEntryMixin
        this.bridge$reason = this.reason == null ? null : SpongeTexts.fromLegacy(this.reason);
        this.bridge$source = SpongeTexts.fromLegacy(this.bannedBy);

        final Optional<Player> user;

        if ("Server".equals(this.bannedBy)) { // There could be a user called Server, but of course Mojang doesn't care...
            this.bridge$commandSrc = SpongeImpl.getGame().getServer().getConsole();
        } else if ((user = Sponge.getGame().getServer().getPlayer(this.bannedBy)).isPresent()) {
            this.bridge$commandSrc = user.get();
        }
    }

    @Override
    public Optional<Text> bridge$getReason() {
        return Optional.ofNullable(this.bridge$reason);
    }

    @Override
    public Optional<Text> bridge$getSource() {
        return Optional.ofNullable(this.bridge$source);
    }

    @Override
    public Optional<CommandSource> bridge$getCmdSource() {
        return Optional.ofNullable(this.bridge$commandSrc);
    }

}
