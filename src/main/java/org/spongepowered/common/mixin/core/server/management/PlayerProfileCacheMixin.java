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

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.server.management.PlayerProfileCache;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.util.SpongeUsernameCache;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("SuspiciousMethodCalls")
@Mixin(PlayerProfileCache.class)
public abstract class PlayerProfileCacheMixin implements PlayerProfileCacheBridge {

    @Shadow public abstract void save();
    // Thread-safe queue
    private Queue<com.mojang.authlib.GameProfile> impl$profiles = new ConcurrentLinkedQueue<>();
    private boolean impl$canSave = false;

    @Override
    public void bridge$setCanSave(final boolean flag) {
        this.impl$canSave = flag;
    }

    @Inject(method = "addEntry(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;)V", at = @At(value = "RETURN"))
    private void impl$UpdateCacheUsername(final com.mojang.authlib.GameProfile profile, final Date date, final CallbackInfo ci) {
        SpongeUsernameCache.setUsername(profile.getId(), profile.getName());
    }

    @Redirect(method = "addEntry(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;remove(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$RemoveFromOurQueue(final Deque<com.mojang.authlib.GameProfile> list, final Object obj) {
        return this.impl$profiles.remove(obj);
    }

    @Redirect(method = "addEntry(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;addFirst(Ljava/lang/Object;)V", remap = false))
    private void impl$onAddEntryAdd(final Deque<com.mojang.authlib.GameProfile> list, final Object obj) {
        this.impl$profiles.add((com.mojang.authlib.GameProfile) obj);
    }

    @Redirect(method = "getGameProfileForUsername",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;remove(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$onGetGameProfileForUsernameRemove1(final Deque<com.mojang.authlib.GameProfile> list, final Object obj) {
        return this.impl$profiles.remove(obj);
    }

    @Redirect(method = "getGameProfileForUsername",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;addFirst(Ljava/lang/Object;)V", remap = false))
    private void impl$addToOurProfiles(final Deque<com.mojang.authlib.GameProfile> list, final Object obj) {
        this.impl$profiles.add((com.mojang.authlib.GameProfile) obj);
    }

    @Redirect(method = "getByUUID",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;remove(Ljava/lang/Object;)Z", remap = false))
    private boolean impl$RemoveFromOurProfiles(final Deque<com.mojang.authlib.GameProfile> list, final Object obj) {
        return this.impl$profiles.remove(obj);
    }

    @Redirect(method = "getByUUID",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;addFirst(Ljava/lang/Object;)V", remap = false))
    private void impl$useOurProfiles(final Deque<com.mojang.authlib.GameProfile> list, final Object obj) {
        this.impl$profiles.add((com.mojang.authlib.GameProfile) obj);
    }

    @Redirect(method = "getEntriesWithLimit",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<com.mojang.authlib.GameProfile> impl$useOurIterator(final Deque<com.mojang.authlib.GameProfile> list) {
        return this.impl$profiles.iterator();
    }

    @Redirect(method = "load",
        at = @At(value = "INVOKE", target = "Ljava/util/Deque;clear()V", remap = false))
    private void impl$ClearOurProfiles(final Deque<com.mojang.authlib.GameProfile> list) {
        this.impl$profiles.clear();
    }

    @Redirect(method = "lookupProfile(Lcom/mojang/authlib/GameProfileRepository;Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;",
            at = @At(
                value = "INVOKE",
                target = "Lcom/mojang/authlib/GameProfileRepository;findProfilesByNames([Ljava/lang/String;Lcom/mojang/authlib/Agent;Lcom/mojang/authlib/ProfileLookupCallback;)V",
                remap = false))
    private static void impl$LookUpViaSponge(
        final GameProfileRepository repository, final String[] names, final Agent agent, final ProfileLookupCallback callback) {
        GameProfileCache cache = null;
        try {
            cache = Sponge.getServer().getGameProfileManager().getCache();
        } catch (Throwable t) {
            // ignore
        }

        if (cache == null || cache instanceof PlayerProfileCache) {
            repository.findProfilesByNames(names, agent, callback);
        } else {
            // The method we're redirecting into obtains the resulting GameProfile from
            // the callback here.
            callback.onProfileLookupSucceeded((com.mojang.authlib.GameProfile) cache.getOrLookupByName(names[0]).orElse(null));
        }
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void impl$IgnoreSavingIfCancelled(final CallbackInfo ci) {
        if (!this.impl$canSave) {
            ci.cancel();
        }
    }

}
