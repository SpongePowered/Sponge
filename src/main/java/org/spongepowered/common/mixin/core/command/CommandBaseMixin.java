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

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.command.ICommandBridge;

import javax.annotation.Nullable;

@Mixin(CommandBase.class)
public abstract class CommandBaseMixin implements ICommandBridge, ICommand {

    private boolean impl$expandedSelector;
    @Nullable private String impl$namespacedAlias = null;

    @Override
    public boolean bridge$isExpandedSelector() {
        return this.impl$expandedSelector;
    }

    @Override
    public void bridge$setExpandedSelector(final boolean expandedSelector) {
        this.impl$expandedSelector = expandedSelector;
    }

    @Override
    public void bridge$updateNamespacedAlias(final String ownerId) {
        this.impl$namespacedAlias = ownerId + ":" + getName();
    }

    @Redirect(method = "checkPermission", at = @At(value = "INVOKE", target = "Lnet/minecraft/command/CommandBase;getName()Ljava/lang/String;"))
    private String onCheckPermissionGetNameCall(final CommandBase thisObject) {
        return this.impl$namespacedAlias == null ? getName() : this.impl$namespacedAlias;
    }

}
