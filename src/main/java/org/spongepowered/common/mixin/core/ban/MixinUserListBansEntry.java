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
import net.minecraft.server.management.UserListBansEntry;

import org.spongepowered.api.GameProfile;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.service.user.UserStorage;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.entity.player.SpongeUser;

@Mixin(UserListBansEntry.class)
public abstract class MixinUserListBansEntry extends MixinBanEntry implements Ban.User {

    private org.spongepowered.api.entity.player.User spongeUser;

    public MixinUserListBansEntry(Object p_i1146_1_) {
        super(p_i1146_1_);
    }

    @Override
    public org.spongepowered.api.entity.player.User getUser() {
        if (this.spongeUser == null) {
            this.resolveUser();
        }
        return this.spongeUser;
    }

    private void resolveUser() {
        Optional<org.spongepowered.api.entity.player.User> optUser = Sponge.getGame().getServiceManager().provideUnchecked(UserStorage.class).get((GameProfile) this.value);
        if (optUser.isPresent()) {
            this.spongeUser = optUser.get();
        }
        // This entry might not be present in the normal ban map, if another ban already exists for the user
        // Because of this, SpongeUserStorage might not find it, and it's necessary to create one ourselves.
        this.spongeUser = (org.spongepowered.api.entity.player.User) new SpongeUser((com.mojang.authlib.GameProfile) this.value);
    }

}
