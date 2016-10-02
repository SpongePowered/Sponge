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
package org.spongepowered.common.mixin.core.ban;

import net.minecraft.server.management.UserListEntry;
import net.minecraft.server.management.UserListEntryBan;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.text.SpongeTexts;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@SuppressWarnings("rawtypes")
@Mixin(UserListEntryBan.class)
public abstract class MixinBanEntry<T> extends UserListEntry<T> implements Ban {

    public MixinBanEntry(T p_i1146_1_) {
        super(p_i1146_1_);
    }

    @Shadow @Final private String reason;
    @Shadow @Final private String bannedBy;
    @Shadow @Final private Date banStartDate;
    @Shadow @Final private Date banEndDate;

    private Optional<Text> spongeReason;
    private Text source;

    private Optional<CommandSource> commandSource = Optional.empty();

    @SuppressWarnings("deprecation")
    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onInitMixinBanEntry(CallbackInfo ci) { // Prevent this from being overriden in MixinIPBanEntry
        this.spongeReason = this.reason == null ? Optional.empty() : Optional.of(SpongeTexts.fromLegacy(this.reason));
        this.source = SpongeTexts.fromLegacy(this.bannedBy);

        this.setSource();
    }

    private void setSource() {
        Optional<Player> user;

        if (this.bannedBy.equals("Server")) { // There could be a user called Server, but of course Mojang doesn't care...
            this.commandSource = Optional.of(SpongeImpl.getGame().getServer().getConsole());
        } else if ((user = Sponge.getGame().getServer().getPlayer(this.bannedBy)).isPresent()) {
            this.commandSource = Optional.of(user.get());
        }
    }

    @Override
    public Optional<Text> getReason() {
        return this.spongeReason;
    }

    @Override
    public Instant getCreationDate() {
        return this.banStartDate.toInstant();
    }

    @Override
    public Optional<Text> getBanSource() {
        return Optional.of(this.source);
    }

    @Override
    public Optional<CommandSource> getBanCommandSource() {
        return this.commandSource;
    }

    @Override
    public Optional<Instant> getExpirationDate() {
        return Optional.ofNullable(this.banEndDate == null ? null : this.banEndDate.toInstant());
    }
}
