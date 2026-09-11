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
package org.spongepowered.vanilla.mixin.core.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.commands.Commands;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.common.bridge.server.packs.resources.ResourceManagerBridge;
import org.spongepowered.common.command.brigadier.dispatcher.DelegatingCommandDispatcher;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin_Vanilla {

    @WrapOperation(method = "lambda$loadResources$2", at = @At(value = "NEW", target = "(Lnet/minecraft/server/ReloadableServerRegistries$LoadResult;Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/commands/Commands$CommandSelection;Ljava/util/List;Lnet/minecraft/server/permissions/PermissionSet;Ljava/util/List;)Lnet/minecraft/server/ReloadableServerResources;"))
    private static ReloadableServerResources impl$onCreateResources(
        final ReloadableServerRegistries.LoadResult loadingContext,
        final FeatureFlagSet enabledFeatures,
        final Commands.CommandSelection commandSelection,
        final List postponedTags,
        final PermissionSet functionCompilationPermissions,
        final List newComponents,
        final Operation<ReloadableServerResources> original,
        final @Local(argsOnly = true) ResourceManager resourceManager
    ) {
        final ReloadableServerResources instance = original.call(loadingContext, enabledFeatures, commandSelection, postponedTags, functionCompilationPermissions, newComponents);
        if (instance.getCommands().getDispatcher() instanceof
            final DelegatingCommandDispatcher delegatingCommandDispatcher) {
            delegatingCommandDispatcher.permissionService(((ResourceManagerBridge) resourceManager).bridge$services().permissionService());
        }
        return instance;
    }
}
