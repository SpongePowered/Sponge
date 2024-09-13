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
package org.spongepowered.common.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.api.Sponge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.service.server.permission.SpongePermissionService;

@Mixin(CommandBlock.class)
public abstract class CommandBlockMixin {

    @Inject(method = "useWithoutItem", at = @At(value = "RETURN", ordinal = 1))
    private void impl$checkCommandBlockPermission(final BlockState state, final Level world, final BlockPos pos, final Player player,
            final BlockHitResult hit, final CallbackInfoReturnable<Boolean> cir) {
        // In Vanilla, the command block will never even open, since the client will do the permission check.
        // However, when a plugin provides a permission service, we have to force the op level to 0 on the client, since
        // the server can't tell the client to open it.
        // If the server-side permission check fails, we need to forcibly close it, since it will already have opened on the client.
        if (!world.isClientSide() && !(Sponge.server().serviceProvider().permissionService() instanceof SpongePermissionService)) {
            // CommandBlock GUI opens solely on the client, we need to force it close on cancellation
            ((ServerPlayer) player).connection.send(new ClientboundContainerClosePacket(0));
        }
    }
}
