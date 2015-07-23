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

import com.google.common.base.Optional;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListEntry;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.Sponge;

import java.util.Date;

@Mixin(BanEntry.class)
public abstract class MixinBanEntry extends UserListEntry implements Ban {

    @Shadow public String reason;
    @Shadow public Date banStartDate;
    @Shadow public Date banEndDate;
    @Shadow public String bannedBy;

    public MixinBanEntry(Object p_i1146_1_) {
        super(p_i1146_1_);
    }

    @Override
    public BanType getType() {
        if (((Object) this) instanceof UserListBansEntry) {
            return BanType.USER_BAN;
        }
        return BanType.IP_BAN;
    }

    @Override
    public Text.Literal getReason() {
        try {
            return (Text.Literal) Texts.legacy().from(this.reason);
        } catch (TextMessageException e) {
            throw new IllegalStateException("Error parsing ban reason!", e);
        }
    }

    @Override
    public Date getStartDate() {
        return this.banStartDate;
    }

    @Override
    public Optional<CommandSource> getSource() {
        Optional<Player> user;

        if (this.bannedBy.equals("(Unknown)")) {
            return Optional.absent();
        } else if (this.bannedBy.equals("Server")) { // There could be a user called Server, but of course Mojang doesn't care...
            return Optional.of((CommandSource) Sponge.getGame().getServer().getConsole());
        } else if ((user = Sponge.getGame().getServer().getPlayer(this.bannedBy)).isPresent()) {
            return Optional.of((CommandSource) user.get());
        }
        return Optional.absent();
    }

    @Override
    public Optional<Date> getExpirationDate() {
        return Optional.fromNullable(this.banEndDate);
    }

    @Override
    public boolean isIndefinite() {
        return !this.getExpirationDate().isPresent();
    }

}
