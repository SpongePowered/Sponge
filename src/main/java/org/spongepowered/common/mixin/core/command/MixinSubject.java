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
package org.spongepowered.common.mixin.core.command;

import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityCommandBlock;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeInternalListeners;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.service.permission.SubjectSettingCallback;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * Mixin to provide a common implementation of subject that refers to the
 * installed permissions service for a subject.
 */
@NonnullByDefault
@Mixin(value = {EntityPlayerMP.class, TileEntityCommandBlock.class, EntityMinecartCommandBlock.class, MinecraftServer.class, RConConsoleSource.class,
        SpongeUser.class}, targets = CommandSenderBridge.SIGN_CLICK_SENDER)
public abstract class MixinSubject implements Subject, CommandSourceBridge, SubjectBridge {

    @Nullable
    private SubjectReference thisSubject;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void subjectConstructor(CallbackInfo ci) {
        if (SpongeImpl.isInitialized()) {
            SpongeInternalListeners.getInstance().registerExpirableServiceCallback(PermissionService.class, new SubjectSettingCallback(this));
        }
    }

    @Override
    public void bridge$setSubject(SubjectReference subj) {
        this.thisSubject = subj;
    }

    @Override
    public CompletableFuture<Subject> bridge$loadInternalSubject() {
        return asSubjectReference().resolve();
    }

    @Nullable
    private Subject resolveNullable() {
        if (this.thisSubject == null) {
            Optional<PermissionService> serv = SpongeImpl.getGame().getServiceManager().provide(PermissionService.class);
            if (serv.isPresent()) {
                new SubjectSettingCallback(this).test(serv.get());
            }
        }

        return this.thisSubject == null ? null : this.thisSubject.resolve().join();
    }

    private Subject resolve() {
        Subject ret = resolveNullable();
        if (ret == null) {
            throw new IllegalStateException("No subject reference present for user " + this);
        }
        return ret;
    }

    @Override
    public SubjectReference asSubjectReference() {
        if (this.thisSubject == null) {
            Optional<PermissionService> serv = SpongeImpl.getGame().getServiceManager().provide(PermissionService.class);
            if (serv.isPresent()) {
                new SubjectSettingCallback(this).test(serv.get());
            }
        }

        if (this.thisSubject == null) {
            throw new IllegalStateException("No subject reference present for user " + this);
        }

        return this.thisSubject;
    }

    @Override
    public boolean isSubjectDataPersisted() {
        Subject subj = resolveNullable();
        return subj != null && subj.isSubjectDataPersisted();
    }

    @Override
    public Set<Context> getActiveContexts() {
        Subject subj = resolveNullable();
        return subj == null ? Collections.emptySet() : subj.getActiveContexts();
    }

    @Override
    public Optional<String> getFriendlyIdentifier() {
        Subject subj = resolveNullable();
        return subj == null ? Optional.empty() : subj.getFriendlyIdentifier();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return resolve().getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return resolve().getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return resolve().getTransientSubjectData();
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        Subject subj = resolveNullable();
        if (subj == null) {
            return bridge$permDefault(permission);
        } else {
            return subj.getPermissionValue(contexts, permission);
        }
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        Subject subj = resolveNullable();
        if (subj == null) {
            return bridge$permDefault(permission).asBoolean();
        }

        // these calls are not directly forwarded to the subject, so we can
        // apply permission defaults.
        Tristate ret = getPermissionValue(contexts, permission);
        switch (ret) {
            case UNDEFINED:
                return bridge$permDefault(permission).asBoolean();
            default:
                return ret.asBoolean();
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        // forwarded to the implementation in this class, and not the default
        // in the Subject interface so permission defaults can be applied
        return hasPermission(getActiveContexts(), permission);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, SubjectReference parent) {
        return resolve().isChildOf(contexts, parent);
    }

    @Override
    public boolean isChildOf(SubjectReference parent) {
        return resolve().isChildOf(parent);
    }

    @Override
    public List<SubjectReference> getParents(Set<Context> contexts) {
        return resolve().getParents(contexts);
    }

    @Override
    public List<SubjectReference> getParents() {
        return resolve().getParents();
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String key) {
        return resolve().getOption(contexts, key);
    }

    @Override
    public Optional<String> getOption(String key) {
        return resolve().getOption(key);
    }
}
