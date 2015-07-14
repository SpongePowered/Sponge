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

import com.google.common.base.Optional;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.api.entity.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.context.Context;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.command.MinecraftCommandWrapper;
import org.spongepowered.common.entity.player.SpongeUser;
import org.spongepowered.common.interfaces.IMixinSubject;
import org.spongepowered.common.service.permission.SubjectSettingCallback;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Mixin to provide a common implementation of subject that refers to the installed permissions service for a subject.
 */
@NonnullByDefault
@Mixin(value = {EntityPlayerMP.class, CommandBlockLogic.class, MinecraftServer.class, RConConsoleSource.class, SpongeUser.class},
        targets = "net/minecraft/tileentity/TileEntitySign$2")
public abstract class MixinSubject implements CommandSource, ICommandSender, IMixinSubject {

    @Nullable
    private Subject thisSubject;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void subjectConstructor(CallbackInfo ci) {
        if (Sponge.isInitialized()) {
            Sponge.getGame().getServiceManager().potentiallyProvide(PermissionService.class).executeWhenPresent(new SubjectSettingCallback(this));
        }
    }

    @Override
    public void setSubject(Subject subj) {
        this.thisSubject = subj;
    }

    @Nullable
    private Subject internalSubject() {
        if (this.thisSubject == null) {
            Optional<PermissionService> serv = Sponge.getGame().getServiceManager().provide(PermissionService.class);
            if (serv.isPresent()) {
                new SubjectSettingCallback(this).apply(serv.get());
            }
        }
        return this.thisSubject;
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        if (this instanceof User && !((User) this).isOnline()) {
            return Optional.absent();
        }
        return Optional.<CommandSource>of(this);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        Subject subj = internalSubject();
        if (subj == null) {
            throw new IllegalStateException("No subject present for user " + this);
        } else {
            return subj.getContainingCollection();
        }
    }

    @Override
    public SubjectData getSubjectData() {
        Subject subj = internalSubject();
        if (subj == null) {
            throw new IllegalStateException("No subject present for user " + this);
        } else {
            return subj.getSubjectData();
        }
    }

    @Override
    public SubjectData getTransientSubjectData() {
        Subject subj = internalSubject();
        if (subj == null) {
            throw new IllegalStateException("No subject present for user " + this);
        } else {
            return subj.getTransientSubjectData();
        }
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        Subject subj = internalSubject();
        if (subj == null) {
            return this.permDefault(permission).asBoolean();
        } else {
            Tristate ret = getPermissionValue(contexts, permission);
            switch (ret) {
                case UNDEFINED:
                    return this.permDefault(permission).asBoolean();
                default:
                    return ret.asBoolean();
            }
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return hasPermission(getActiveContexts(), permission);
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        Subject subj = internalSubject();
        return subj == null ? this.permDefault(permission) : subj.getPermissionValue(contexts, permission);
    }

    @Override
    public boolean isChildOf(Subject parent) {
        Subject subj = internalSubject();
        return subj != null && subj.isChildOf(parent);
    }

    @Override
    public boolean isChildOf(Set<Context> contexts, Subject parent) {
        Subject subj = internalSubject();
        return subj != null && subj.isChildOf(contexts, parent);
    }

    @Override
    public List<Subject> getParents() {
        Subject subj = internalSubject();
        return subj == null ? Collections.<Subject>emptyList() : subj.getParents();
    }

    @Override
    public List<Subject> getParents(Set<Context> contexts) {
        Subject subj = internalSubject();
        return subj == null ? Collections.<Subject>emptyList() : subj.getParents(contexts);
    }

    @Override
    public Set<Context> getActiveContexts() {
        Subject subj = internalSubject();
        return subj == null ? Collections.<Context>emptySet() : subj.getActiveContexts();
    }

    @Override
    public boolean canCommandSenderUseCommand(int permissionLevel, String commandName) {
        for (CommandMapping mapping : Sponge.getGame().getCommandDispatcher().getAll(commandName)) {
            if (mapping.getCallable() instanceof MinecraftCommandWrapper) { // best we can do :/
                return hasPermission(((MinecraftCommandWrapper) mapping.getCallable()).getCommandPermission());
            }
        }
        return this.permDefault(commandName).asBoolean();
    }
}
