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

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
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
import org.spongepowered.common.accessor.server.players.GameProfileCache_GameProfileInfoAccessor;
import org.spongepowered.common.bridge.server.players.GameProfileCacheBridge;
import org.spongepowered.common.bridge.server.players.GameProfileCache_GameProfileInfoBridge;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.players.GameProfileCache;

@Mixin(GameProfileCache.class)
public abstract class GameProfileCacheMixin implements GameProfileCacheBridge {

    // @formatter:off
    @Shadow public void shadow$add(final com.mojang.authlib.GameProfile profile) {}
    @Shadow @Final private Map<UUID, GameProfileCache_GameProfileInfoAccessor> profilesByUUID;
    @Shadow @Final private Map<String, GameProfileCache_GameProfileInfoAccessor> profilesByName;
    // @formatter:on

    private boolean impl$canSave = false;

    @Override
    public Optional<GameProfileCache_GameProfileInfoBridge> bridge$getEntry(final UUID uniqueId) {
        final GameProfileCache_GameProfileInfoAccessor accessor = this.profilesByUUID.get(Objects.requireNonNull(uniqueId, "uniqueId"));
        if (accessor == null) {
            return Optional.empty();
        }

        if (accessor.invoker$getExpirationDate().getTime() < System.currentTimeMillis()) {
            this.profilesByUUID.remove(uniqueId, accessor);
            this.profilesByName.remove(accessor.invoker$getProfile().getName(), accessor);
            return Optional.empty();
        }

        return Optional.of((GameProfileCache_GameProfileInfoBridge) accessor);
    }

    @Override
    public Optional<GameProfileCache_GameProfileInfoBridge> bridge$getEntry(final String name) {
        final String lowerName = Objects.requireNonNull(name, "name").toLowerCase(Locale.ROOT);
        final GameProfileCache_GameProfileInfoAccessor accessor = this.profilesByName.get(lowerName);
        if (accessor == null) {
            return Optional.empty();
        }

        if (accessor.invoker$getExpirationDate().getTime() < System.currentTimeMillis()) {
            this.profilesByUUID.remove(accessor.invoker$getProfile().getId(), accessor);
            this.profilesByName.remove(lowerName, accessor);
            return Optional.empty();
        }

        return Optional.of((GameProfileCache_GameProfileInfoBridge) accessor);
    }

    @Override
    public void bridge$add(final com.mojang.authlib.GameProfile profile, final boolean full, final boolean signed) {
        GameProfileCache_GameProfileInfoAccessor accessor = this.profilesByUUID.get(Objects.requireNonNull(profile, "profile").getId());
        final com.mojang.authlib.GameProfile current = accessor == null ? null : accessor.invoker$getProfile();
        // Don't allow basic game profiles to overwrite the contents if already
        // an entry exists that is full.
        if (current != null && Objects.equals(current.getId(), profile.getId()) &&
                Objects.equals(current.getName(), profile.getName()) && !full) {
            return;
        }

        this.shadow$add(profile);
        accessor = this.profilesByUUID.get(profile.getId());
        if (accessor == null || accessor.invoker$getProfile() != profile) {
            return;
        }
        final GameProfileCache_GameProfileInfoBridge bridge = (GameProfileCache_GameProfileInfoBridge) accessor;
        bridge.bridge$setSigned(signed);
        bridge.bridge$setIsFull(full);
    }

    @Override
    public void bridge$add(final GameProfile profile, final boolean full, final boolean signed) {
        GameProfileCache_GameProfileInfoAccessor accessor = this.profilesByUUID.get(Objects.requireNonNull(profile, "profile").uniqueId());
        final com.mojang.authlib.GameProfile current = accessor == null ? null : accessor.invoker$getProfile();
        final com.mojang.authlib.GameProfile mcProfile = SpongeGameProfile.toMcProfile(profile);
        // Don't allow basic game profiles to overwrite the contents if already
        // an entry exists that is full.
        if (current != null && Objects.equals(current.getId(), mcProfile.getId()) &&
                Objects.equals(current.getName(), mcProfile.getName()) && !full) {
            return;
        }

        this.shadow$add(mcProfile);
        accessor = this.profilesByUUID.get(profile.uniqueId());
        if (accessor == null || accessor.invoker$getProfile() != mcProfile) {
            return;
        }
        ((GameProfileCache_GameProfileInfoBridge) accessor).bridge$set(profile, full, signed);
    }

    @Override
    public void bridge$setCanSave(final boolean flag) {
        this.impl$canSave = flag;
    }

    @Inject(method = "add", at = @At(value = "RETURN"))
    private void impl$updateCacheUsername(final com.mojang.authlib.GameProfile profile, final CallbackInfo ci) {
        if (profile.getName() != null) {
            ((SpongeServer) Sponge.server()).getUsernameCache().setUsername(profile.getId(), profile.getName());
        }
    }

    @Redirect(method = "lookupGameProfile",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/GameProfileRepository;findProfilesByNames([Ljava/lang/String;Lcom/mojang/authlib/Agent;Lcom/mojang/authlib/ProfileLookupCallback;)V",
            remap = false
        )
    )
    private static void impl$lookUpViaSponge(final GameProfileRepository repository, final String[] names,
            final Agent agent, final ProfileLookupCallback callback) {
        final GameProfileManager profileManager = Sponge.server().gameProfileManager();
        profileManager.basicProfile(names[0])
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
    private void impl$ignoreSavingIfCancelled(final CallbackInfo ci) {
        if (!this.impl$canSave) {
            ci.cancel();
        }
    }
}
