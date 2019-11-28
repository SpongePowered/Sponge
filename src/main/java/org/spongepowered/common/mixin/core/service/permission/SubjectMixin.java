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
package org.spongepowered.common.mixin.core.service.permission;

import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockTileEntity;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeInternalListeners;
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.service.permission.SubjectSettingCallback;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * Mixin to provide a common implementation of subject that refers to the
 * installed permissions service for a subject.
 */
@NonnullByDefault
@Mixin(value = {ServerPlayerEntity.class, CommandBlockTileEntity.class, MinecartCommandBlockEntity.class, MinecraftServer.class, RConConsoleSource.class,
        SpongeUser.class}, targets = "net/minecraft/tileentity/TileEntitySign$1")
public abstract class SubjectMixin implements CommandSourceBridge, SubjectBridge {

    @Nullable
    private SubjectReference impl$subjectReference;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void subjectConstructor(final CallbackInfo ci) {
        if (SpongeImpl.isInitialized()) {
            SpongeInternalListeners.getInstance().registerExpirableServiceCallback(PermissionService.class, new SubjectSettingCallback(this));
        }
    }

    @Override
    public void bridge$setSubject(final SubjectReference subj) {
        this.impl$subjectReference = subj;
    }

    @Override
    public Optional<Subject> bridge$resolveOptional() {
        if (this.impl$subjectReference == null) {
            final Optional<PermissionService> serv = SpongeImpl.getGame().getServiceManager().provide(PermissionService.class);
            serv.ifPresent(permissionService -> new SubjectSettingCallback(this).test(permissionService));
        }

        return Optional.ofNullable(this.impl$subjectReference).map(SubjectReference::resolve).map(CompletableFuture::join);
    }

    @Override
    public Subject bridge$resolve() {
        return bridge$resolveOptional()
            .orElseThrow(() -> new IllegalStateException("No subject reference present for user " + this));
    }
}
