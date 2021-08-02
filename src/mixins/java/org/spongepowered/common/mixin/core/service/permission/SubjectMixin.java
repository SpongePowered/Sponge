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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.entity.player.SpongeUserView;
import org.spongepowered.common.service.server.permission.SubjectHelper;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.rcon.RconConsoleSource;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

/**
 * Mixin to provide a common implementation of subject that refers to the
 * installed permissions service for a subject.
 */
@Mixin(value = {ServerPlayer.class, CommandBlockEntity.class, MinecartCommandBlock.class, RconConsoleSource.class, SpongeUserView.class})
public abstract class SubjectMixin implements SubjectBridge {

    @Nullable
    private SubjectReference impl$subjectReference;

    @Override
    public void bridge$setSubject(final SubjectReference subj) {
        this.impl$subjectReference = subj;
    }

    @Override
    public Optional<SubjectReference> bridge$resolveReferenceOptional() {
        if (this.impl$subjectReference == null) {
            SubjectHelper.applySubject(this, this.bridge$getSubjectCollectionIdentifier());
        }
        return Optional.ofNullable(this.impl$subjectReference);
    }

    @Override
    public Optional<Subject> bridge$resolveOptional() {
        return this.bridge$resolveReferenceOptional().map(SubjectReference::resolve).map(CompletableFuture::join);
    }

    @Override
    public Subject bridge$resolve() {
        return this.bridge$resolveOptional()
            .orElseThrow(() -> new IllegalStateException("No subject reference present for user " + this));
    }
}
