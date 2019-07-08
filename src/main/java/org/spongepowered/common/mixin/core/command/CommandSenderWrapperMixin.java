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

import net.minecraft.command.CommandSenderWrapper;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.command.AndPermissionLevelSubject;
import org.spongepowered.common.command.SpongeProxySource;
import org.spongepowered.common.command.WrapperCommandSource;
import org.spongepowered.common.bridge.command.CommandSenderBridge;

import javax.annotation.Nullable;

@Mixin(CommandSenderWrapper.class)
@NonnullByDefault
public abstract class CommandSenderWrapperMixin implements ICommandSender, CommandSenderBridge {

    @Shadow @Final private ICommandSender delegate;
    @Shadow @Final @Nullable private Vec3d positionVector;
    @Shadow @Final @Nullable private Integer permissionLevel;

    private SpongeProxySource impl$proxySource;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpSources(final ICommandSender delegateIn, final Vec3d positionVectorIn, final BlockPos positionIn,
        final Integer permissionLevelIn, final Entity entityIn, final Boolean sendCommandFeedbackIn, final CallbackInfo ci) {
        final CommandSource wrappedDelegate = WrapperCommandSource.of(this.delegate);
        final Subject subjectDelegate;
        if (this.permissionLevel == null) {
            subjectDelegate = wrappedDelegate;
        } else {
            subjectDelegate = new AndPermissionLevelSubject(this, wrappedDelegate);
        }
        if (this.positionVector != null || wrappedDelegate instanceof Locatable) {
            this.impl$proxySource = new SpongeProxySource.Located(this, wrappedDelegate, subjectDelegate);
        } else {
            this.impl$proxySource = new SpongeProxySource(this, wrappedDelegate, subjectDelegate);
        }
    }

    @Override
    public CommandSource bridge$asCommandSource() {
        return this.impl$proxySource;
    }
}
