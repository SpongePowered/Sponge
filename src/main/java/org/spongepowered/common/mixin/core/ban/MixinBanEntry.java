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

import com.google.gson.JsonObject;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.UserListEntry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;

import java.util.Date;
import java.util.Optional;

@Mixin(BanEntry.class)
public abstract class MixinBanEntry extends UserListEntry implements Ban {

    public MixinBanEntry(Object p_i1146_1_) {
        super(p_i1146_1_);
    }

    @Shadow private String reason;
    @Shadow private String bannedBy;
    @Shadow private Date banStartDate;
    @Shadow private Date banEndDate;

    private Text spongeReason;
    private Text source;

    private Optional<CommandSource> commandSource = Optional.empty();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        this.spongeReason = Texts.of();
        this.source = Texts.of();

        try {
            this.spongeReason = Texts.legacy().from(this.reason);
        } catch (TextMessageException e) {
            SpongeImpl.getLogger().error("Error parsing legacy color codes for ban reason!", e);
        }

        try {
            this.source = Texts.legacy().from(this.bannedBy);
        } catch (TextMessageException e) {
            SpongeImpl.getLogger().error("Error parsing legacy color codes for ban source!", e);
        }
        this.setSource();
    }

    private void setSource() {
        Optional<Player> user;

        if (this.bannedBy.equals("Server")) { // There could be a user called Server, but of course Mojang doesn't care...
            this.commandSource = Optional.of(SpongeImpl.getGame().getServer().getConsole());
        } else if ((user = Sponge.getGame().getServer().getPlayer(this.bannedBy)).isPresent()) {
            this.commandSource = Optional.of((CommandSource) user.get());
        }
    }

    @Override
    public Text getReason() {
        return this.spongeReason;
    }

    @Override
    public Date getCreationDate() {
        return this.banStartDate;
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
    public Optional<Date> getExpirationDate() {
        return Optional.of(this.banEndDate);
    }

}
