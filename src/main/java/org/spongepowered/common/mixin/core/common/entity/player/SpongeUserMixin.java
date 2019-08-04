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
package org.spongepowered.common.mixin.core.common.entity.player;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.data.InvulnerableTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.entity.player.SpongeUser;

import java.util.Optional;

@Mixin(value = SpongeUser.class, remap = false)
public abstract class SpongeUserMixin implements SubjectBridge, InvulnerableTrackedBridge, VanishableBridge {


    @Shadow private boolean invulnerable;
    @Shadow private boolean isInvisible;
    @Shadow private boolean isVanished;
    @Shadow private boolean isVanishCollide;
    @Shadow private boolean isVanishTarget;

    @Shadow public abstract void markDirty();

    @Override
    public void bridge$setInvulnerable(final boolean value) {
        final Optional<Player> playerOpt = ((User) this).getPlayer();
        if (playerOpt.isPresent()) {
            ((InvulnerableTrackedBridge) playerOpt.get()).bridge$setInvulnerable(value);
            return;
        }
        this.invulnerable = value;
        this.markDirty();
    }

    @Override
    public boolean bridge$getIsInvulnerable() {
        return this.invulnerable;
    }

    @Override
    public void bridge$setVanished(final boolean vanished) {
        final Optional<Player> playerOpt = ((User) this).getPlayer();
        if (playerOpt.isPresent()) {
            ((VanishableBridge) playerOpt.get()).bridge$setVanished(vanished);
            return;
        }
        this.isVanished = vanished;
        this.markDirty();
    }

    @Override
    public boolean bridge$isVanished() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isVanished()).orElseGet(() -> this.isVanished);
    }

    @Override
    public boolean bridge$isInvisible() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isInvisible()).orElseGet(() -> this.isInvisible);
    }

    @Override
    public void bridge$setInvisible(final boolean invisible) {
        final Optional<Player> player = ((User) this).getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setInvisible(invisible);
            return;
        }
        this.isInvisible = invisible;
    }

    @Override
    public boolean bridge$isUncollideable() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isUncollideable()).orElseGet(() -> this.isVanishCollide);
    }

    @Override
    public void bridge$setUncollideable(final boolean uncollideable) {
        final Optional<Player> player = ((User) this).getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setUncollideable(uncollideable);
            return;
        }
        this.isVanishCollide = uncollideable;
    }

    @Override
    public boolean bridge$isUntargetable() {
        return ((User) this).getPlayer().map(player -> ((VanishableBridge) player).bridge$isUntargetable()).orElseGet(() -> this.isVanishTarget);
    }

    @Override
    public void bridge$setUntargetable(final boolean untargetable) {
        final Optional<Player> player = ((User) this).getPlayer();
        if (player.isPresent()) {
            ((VanishableBridge) player.get()).bridge$setUntargetable(untargetable);
            return;
        }
        this.isVanishTarget = untargetable;
    }

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_USER;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.FALSE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("isOnline", ((User) this).isOnline())
                .add("profile", ((User) this).getProfile())
                .toString();
    }
}
