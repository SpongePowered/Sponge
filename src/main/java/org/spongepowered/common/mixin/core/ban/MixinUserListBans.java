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

import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.server.management.UserListEntry;
import org.spongepowered.api.GameProfile;
import org.spongepowered.api.block.tileentity.Banner;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.ban.IMixinBanLogic;
import org.spongepowered.common.interfaces.ban.IMixinUserListBans;

import java.io.File;
import java.util.Collection;
import java.util.Map;

@Mixin(UserListBans.class)
public abstract class MixinUserListBans extends UserList implements IMixinUserListBans {

    public MixinUserListBans(File saveFile) {
        super(saveFile);
    }

    @Redirect(method = "getKeys", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;", remap = false))
    public Collection onValues(Map map) {
        // Multimap doesn't extend Map, so we need to redirect the call to values()
        return this.getEntries().values();
    }

    @Redirect(method = "isUsernameBanned", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;", remap = false))
    public Collection onGetBannedUsers(Map map) {
        // Multimap doesn't extend Map, so we need to redirect the call to values()
        return this.getEntries().values();
    }

    @Inject(method = "isUsernameBanned", at = @At("HEAD"))
    public void onIsUsernameBanned(String username, CallbackInfoReturnable<com.mojang.authlib.GameProfile> ci) {
        this.removeExpired();
    }

    @Override
    public Collection<UserListBansEntry> getBans(User user) {
        return (Collection) this.getEntries().get(this.getObjectKey(user.getProfile()));
    }

    @Override
    public boolean isBanned(User user) {
        this.removeExpired();
        return this.values.containsKey(this.getObjectKey(user.getProfile()));
    }

    @Override
    public void pardon(User user) {
        for (UserListBansEntry entry: this.getBans(user)) {
            this.removeEntry(entry.getValue());
        }
    }

}
