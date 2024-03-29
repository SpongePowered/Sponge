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
package org.spongepowered.common.mixin.core.server.dedicated;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.dedicated.DedicatedPlayerList;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.entity.player.LoginPermissions;
import org.spongepowered.common.service.server.permission.SpongePermissionService;

@Mixin(DedicatedPlayerList.class)
public abstract class DedicatedPlayerListMixin extends PlayerList {

    public DedicatedPlayerListMixin(final MinecraftServer server, final LayeredRegistryAccess<RegistryLayer> registryAccess,
                                    final PlayerDataStorage playerIo, final int maxPlayers) {
        super(server, registryAccess, playerIo, maxPlayers);
    }

    @Inject(method = "isWhiteListed", at = @At("HEAD"), cancellable = true)
    private void impl$checkForWhitelistBypassPermission(final GameProfile profile, final CallbackInfoReturnable<Boolean> ci) {
        if (!this.isUsingWhitelist() || this.getWhiteList().isWhiteListed(profile)) {
            ci.setReturnValue(true);
            return;
        }
        final PermissionService permissionService = Sponge.server().serviceProvider().permissionService();
        Subject subject = permissionService.userSubjects().subject(profile.getId().toString()).orElse(null);
        if (subject == null) {
            subject = permissionService.userSubjects().defaults();
        }

        ci.setReturnValue(subject.hasPermission(LoginPermissions.BYPASS_WHITELIST_PERMISSION));
    }

    @Inject(method = "canBypassPlayerLimit", at = @At("HEAD"), cancellable = true)
    private void impl$checkForPlayerLimitBypassPermission(final GameProfile profile, final CallbackInfoReturnable<Boolean> ci) {
        final PermissionService permissionService = Sponge.server().serviceProvider().permissionService();
        Subject subject = permissionService.userSubjects().subject(profile.getId().toString()).orElse(null);
        if (subject == null) {
            subject = permissionService.userSubjects().defaults();
        }

        final Tristate tristate = subject.permissionValue(LoginPermissions.BYPASS_PLAYER_LIMIT_PERMISSION);
        // Use the op list if the permission isn't overridden for the subject and
        // if we are still using the default permission service
        if (tristate == Tristate.UNDEFINED && permissionService instanceof SpongePermissionService) {
            return;
        }
        ci.setReturnValue(tristate.asBoolean());
    }
}
