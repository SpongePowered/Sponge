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
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeServer;
import org.spongepowered.common.accessor.server.management.PlayerProfileCache_ProfileEntryAccessor;
import org.spongepowered.common.bridge.server.management.PlayerProfileCacheBridge;
import org.spongepowered.common.bridge.server.management.PlayerProfileCache_ProfileEntryBridge;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(PlayerProfileCache.class)
public abstract class PlayerProfileCacheMixin implements PlayerProfileCacheBridge {

    @Shadow private void shadow$addEntry(final com.mojang.authlib.GameProfile profile, @Nullable final Date expiry) {}
    @Shadow @Final private Map<UUID, PlayerProfileCache_ProfileEntryAccessor> uuidToProfileEntryMap;
    @Shadow @Final private Map<String, PlayerProfileCache_ProfileEntryAccessor> usernameToProfileEntryMap;

    private boolean impl$canSave = false;

    @Override
    public Optional<PlayerProfileCache_ProfileEntryBridge> bridge$getEntry(final UUID uniqueId) {
        Objects.requireNonNull(uniqueId, "uniqueId");

        PlayerProfileCache_ProfileEntryAccessor accessor = this.uuidToProfileEntryMap.get(uniqueId);
        if (accessor == null) {
            return Optional.empty();
        }

        if (accessor.accessor$getExpirationDate().getTime() < System.currentTimeMillis()) {
            this.uuidToProfileEntryMap.remove(uniqueId, accessor);
            this.usernameToProfileEntryMap.remove(accessor.accessor$getProfile().getName(), accessor);
            return Optional.empty();
        }

        return Optional.of((PlayerProfileCache_ProfileEntryBridge) accessor);
    }

    @Override
    public Optional<PlayerProfileCache_ProfileEntryBridge> bridge$getEntry(final String name) {
        Objects.requireNonNull(name, "name");

        final String lowerName = name.toLowerCase(Locale.ROOT);
        PlayerProfileCache_ProfileEntryAccessor accessor = this.usernameToProfileEntryMap.get(lowerName);
        if (accessor == null) {
            return Optional.empty();
        }

        if (accessor.accessor$getExpirationDate().getTime() < System.currentTimeMillis()) {
            this.uuidToProfileEntryMap.remove(accessor.accessor$getProfile().getId(), accessor);
            this.usernameToProfileEntryMap.remove(lowerName, accessor);
            return Optional.empty();
        }

        return Optional.of((PlayerProfileCache_ProfileEntryBridge) accessor);
    }

    @Override
    public void bridge$add(final com.mojang.authlib.GameProfile profile, final boolean full, final boolean signed) {
        Objects.requireNonNull(profile, "profile");

        PlayerProfileCache_ProfileEntryAccessor accessor = this.uuidToProfileEntryMap.get(profile.getId());
        final com.mojang.authlib.GameProfile current = accessor == null ? null : accessor.accessor$getProfile();
        // Don't allow basic game profiles to overwrite the contents if already
        // an entry exists that is full.
        if (current != null && Objects.equals(current.getId(), profile.getId()) &&
                Objects.equals(current.getName(), profile.getName()) && !full) {
            return;
        }

        this.shadow$addEntry(profile, null);
        accessor = this.uuidToProfileEntryMap.get(profile.getId());
        if (accessor == null || accessor.accessor$getProfile() != profile) {
            return;
        }
        final PlayerProfileCache_ProfileEntryBridge bridge = (PlayerProfileCache_ProfileEntryBridge) accessor;
        bridge.bridge$setSigned(signed);
        bridge.bridge$setIsFull(full);
    }

    @Override
    public void bridge$add(final GameProfile profile, final boolean full, final boolean signed) {
        Objects.requireNonNull(profile, "profile");

        PlayerProfileCache_ProfileEntryAccessor accessor = this.uuidToProfileEntryMap.get(profile.getUniqueId());
        final com.mojang.authlib.GameProfile current = accessor == null ? null : accessor.accessor$getProfile();
        final com.mojang.authlib.GameProfile mcProfile = SpongeGameProfile.toMcProfile(profile);
        // Don't allow basic game profiles to overwrite the contents if already
        // an entry exists that is full.
        if (current != null && Objects.equals(current.getId(), mcProfile.getId()) &&
                Objects.equals(current.getName(), mcProfile.getName()) && !full) {
            return;
        }

        this.shadow$addEntry(mcProfile, null);
        accessor = this.uuidToProfileEntryMap.get(profile.getUniqueId());
        if (accessor == null || accessor.accessor$getProfile() != mcProfile) {
            return;
        }
        ((PlayerProfileCache_ProfileEntryBridge) accessor).bridge$set(profile, full, signed);
    }

    @Override
    public void bridge$setCanSave(final boolean flag) {
        this.impl$canSave = flag;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/management/PlayerProfileCache;load()V"))
    private void impl$callLoadAfterServerCreated(final PlayerProfileCache playerProfileCache) {
        // NOOP
    }

    @Inject(method = "addEntry(Lcom/mojang/authlib/GameProfile;Ljava/util/Date;)V", at = @At(value = "RETURN"))
    private void impl$UpdateCacheUsername(final com.mojang.authlib.GameProfile profile, final Date date, final CallbackInfo ci) {
        ((SpongeServer) Sponge.getServer()).getUsernameCache().setUsername(profile.getId(), profile.getName());
    }

    @Redirect(method = "lookupProfile(Lcom/mojang/authlib/GameProfileRepository;Ljava/lang/String;)Lcom/mojang/authlib/GameProfile;",
            at = @At(
                value = "INVOKE",
                target = "Lcom/mojang/authlib/GameProfileRepository;findProfilesByNames([Ljava/lang/String;Lcom/mojang/authlib/Agent;Lcom/mojang/authlib/ProfileLookupCallback;)V",
                remap = false))
    private static void impl$LookUpViaSponge(final GameProfileRepository repository, final String[] names,
            final Agent agent, final ProfileLookupCallback callback) {
        final GameProfileManager profileManager = Sponge.getServer().getGameProfileManager();
        profileManager.getBasicProfile(names[0])
                .whenComplete((profile, ex) -> {
                    if (ex != null) {
                        callback.onProfileLookupFailed(new com.mojang.authlib.GameProfile(null, names[0]),
                                ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
                    } else {
                        callback.onProfileLookupSucceeded(SpongeGameProfile.toMcProfile(profile));
                    }
                });
    }

    @Inject(method = "save", at = @At("HEAD"), cancellable = true)
    private void impl$IgnoreSavingIfCancelled(final CallbackInfo ci) {
        if (!this.impl$canSave) {
            ci.cancel();
        }
    }
}
