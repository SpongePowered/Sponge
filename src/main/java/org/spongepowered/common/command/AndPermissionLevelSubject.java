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
package org.spongepowered.common.command;

import com.google.common.collect.ImmutableSet;
import net.minecraft.command.ICommandSender;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.service.permission.base.FixedParentMemorySubjectData;
import org.spongepowered.common.service.permission.base.SpongeBaseSubject;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AndPermissionLevelSubject extends SpongeBaseSubject {

    private final PermissionService service;
    private final CommandSource delegate;
    private final MemorySubjectData opLevelData;

    public AndPermissionLevelSubject(ICommandSender opLevelSubject, CommandSource delegate) {
        this.delegate = delegate;
        this.service = SpongeImpl.getGame().getServiceManager().provideUnchecked(PermissionService.class);
        this.opLevelData = new FixedParentMemorySubjectData(this.service, delegate.asSubjectReference());
        CommandPermissions.populateMinecraftPermissions(opLevelSubject, this.opLevelData);
        for (Map.Entry<String, Boolean> permission : ImmutableSet.copyOf(this.opLevelData.getPermissions(SubjectData.GLOBAL_CONTEXT).entrySet())) {
            if (permission.getValue()) {
                this.opLevelData.setPermission(SubjectData.GLOBAL_CONTEXT, permission.getKey(), Tristate.UNDEFINED);
            }
        }
    }

    @Override
    public PermissionService getService() {
        return this.service;
    }

    @Override
    public String getIdentifier() {
        return this.delegate.getIdentifier();
    }

    @Override
    public Set<Context> getActiveContexts() {
        return this.delegate.getActiveContexts();
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        return Optional.empty();
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.delegate.getContainingCollection();
    }

    @Override
    public SubjectData getSubjectData() {
        return this.delegate.getSubjectData();
    }

    @Override
    public MemorySubjectData getTransientSubjectData() {
        return this.opLevelData;
    }

    @Override
    public boolean hasPermission(Set<Context> contexts, String permission) {
        final Tristate value = getPermissionValue(contexts, permission);
        if (value == Tristate.UNDEFINED) {
            Subject target = this.delegate;
            while (target instanceof SpongeProxySource) {
                target = ((SpongeProxySource) target).getSubjectDelegate();
            }
            if (target instanceof SubjectBridge) {
                return ((SubjectBridge) target).bridge$permDefault(permission).asBoolean();
            }
        }
        return value.asBoolean();
    }

}
