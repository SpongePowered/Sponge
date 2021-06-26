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
package org.spongepowered.common.mixin.core.world.level.block.entity;

import com.mojang.authlib.GameProfile;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.level.block.entity.SkullBlockEntityBridge;
import org.spongepowered.common.profile.SpongeGameProfile;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;

@Mixin(SkullBlockEntity.class)
public abstract class SkullBlockEntityMixin extends BlockEntity implements TickableBlockEntity, SkullBlockEntityBridge {

    @Shadow private GameProfile owner;

    private @Nullable CompletableFuture<?> impl$currentProfileFuture;

    public SkullBlockEntityMixin(final BlockEntityType<?> type) {
        super(type);
    }

    private void cancelResolveFuture() {
        if (this.impl$currentProfileFuture != null) {
            this.impl$currentProfileFuture.cancel(true);
            this.impl$currentProfileFuture = null;
        }
    }

    @Override
    public void bridge$setUnresolvedPlayerProfile(final @Nullable GameProfile owner) {
        this.cancelResolveFuture();
        this.owner = owner;
        this.setChanged();
    }

    /**
     * @reason Don't block the main thread while attempting to lookup a game profile.
     */
    @Redirect(method = "updateOwnerProfile()V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/SkullBlockEntity;updateGameprofile(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/GameProfile;"))
    private GameProfile onUpdateProfile(final GameProfile input) {
        this.cancelResolveFuture();
        if (input == null) {
            return null;
        }
        if (input.isComplete() && input.getProperties().containsKey("textures")) {
            return input;
        }
        final GameProfileManager manager = Sponge.server().gameProfileManager();
        CompletableFuture<org.spongepowered.api.profile.GameProfile> future = null;
        if (input.getId() != null) {
            future = manager.profile(input.getId());
        } else if (!StringUtil.isNullOrEmpty(input.getName())) {
            future = manager.profile(input.getName());
        }
        if (future == null) {
            return input;
        }
        future.thenAcceptAsync(profile -> {
            this.owner = SpongeGameProfile.toMcProfile(profile);
            this.setChanged();
        }, SpongeCommon.server());
        this.impl$currentProfileFuture = future;
        return input;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.cancelResolveFuture();
    }
}
