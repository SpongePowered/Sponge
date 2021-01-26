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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.command.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntityMixin implements SubjectBridge, CommandSourceProviderBridge {

    @Shadow public abstract CommandSourceStack shadow$createCommandSourceStack(final @Nullable ServerPlayer player);

    @Override
    public String bridge$getSubjectCollectionIdentifier() {
        return PermissionService.SUBJECTS_COMMAND_BLOCK;
    }

    @Override
    public Tristate bridge$permDefault(final String permission) {
        return Tristate.TRUE;
    }

    @Override
    public CommandSourceStack bridge$getCommandSource(final Cause cause) {
        return this.shadow$createCommandSourceStack(cause.first(ServerPlayer.class).orElse(null));
    }

}
