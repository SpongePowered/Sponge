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
package org.spongepowered.common.mixin.api.service.permission;

import net.minecraft.entity.item.minecart.MinecartCommandBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.rcon.RConConsoleSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockTileEntity;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.Contextual;
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
import org.spongepowered.common.bridge.command.CommandSenderBridge;
import org.spongepowered.common.bridge.command.CommandSourceBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.entity.player.SpongeUser;
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
@Mixin(value = {ServerPlayerEntity.class, CommandBlockTileEntity.class, MinecartCommandBlockEntity.class, MinecraftServer.class, RConConsoleSource.class,
        SpongeUser.class}, targets = "net/minecraft/tileentity/TileEntitySign$1")
public abstract class SubjectMixin_API implements Subject {

    @Override
    public boolean isSubjectDataPersisted() {
        return ((SubjectBridge) this).bridge$resolveOptional()
            .map(Subject::isSubjectDataPersisted)
            .orElse(false);
    }

    @Override
    public Set<Context> getActiveContexts() {
        return ((SubjectBridge) this).bridge$resolveOptional()
            .map(Subject::getActiveContexts)
            .orElseGet(Collections::emptySet);
    }

    @Override
    public Optional<String> getFriendlyIdentifier() {
        return ((SubjectBridge) this).bridge$resolveOptional()
            .flatMap(Contextual::getFriendlyIdentifier);
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return ((SubjectBridge) this).bridge$resolve().getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return ((SubjectBridge) this).bridge$resolve().getSubjectData();
    }

    @Override
    public SubjectData getTransientSubjectData() {
        return ((SubjectBridge) this).bridge$resolve().getTransientSubjectData();
    }

    @Override
    public Tristate getPermissionValue(final Set<Context> contexts, final String permission) {
        return ((SubjectBridge) this).bridge$resolveOptional()
            .map(subject -> subject.getPermissionValue(contexts, permission))
            .orElseGet(() -> ((SubjectBridge) this).bridge$permDefault(permission));
    }

    @Override
    public boolean hasPermission(final Set<Context> contexts, final String permission) {
        return ((SubjectBridge) this).bridge$resolveOptional()
            .map(subject -> {
                final Tristate ret = subject.getPermissionValue(contexts, permission);
                if (ret == Tristate.UNDEFINED) {
                    return ((SubjectBridge) this).bridge$permDefault(permission).asBoolean();
                }
                return ret.asBoolean();
            })
            .orElseGet(() -> ((SubjectBridge) this).bridge$permDefault(permission).asBoolean());
    }

    @Override
    public boolean hasPermission(final String permission) {
        // forwarded to the implementation in this class, and not the default
        // in the Subject interface so permission defaults can be applied
        return hasPermission(getActiveContexts(), permission);
    }

    @Override
    public boolean isChildOf(final Set<Context> contexts, final SubjectReference parent) {
        return ((SubjectBridge) this).bridge$resolve().isChildOf(contexts, parent);
    }

    @Override
    public boolean isChildOf(final SubjectReference parent) {
        return ((SubjectBridge) this).bridge$resolve().isChildOf(parent);
    }

    @Override
    public List<SubjectReference> getParents(final Set<Context> contexts) {
        return ((SubjectBridge) this).bridge$resolve().getParents(contexts);
    }

    @Override
    public List<SubjectReference> getParents() {
        return ((SubjectBridge) this).bridge$resolve().getParents();
    }

    @Override
    public Optional<String> getOption(final Set<Context> contexts, final String key) {
        return ((SubjectBridge) this).bridge$resolve().getOption(contexts, key);
    }

    @Override
    public Optional<String> getOption(final String key) {
        return ((SubjectBridge) this).bridge$resolve().getOption(key);
    }
}
