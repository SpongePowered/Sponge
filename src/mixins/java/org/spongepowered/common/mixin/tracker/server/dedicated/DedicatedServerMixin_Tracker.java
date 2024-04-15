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
package org.spongepowered.common.mixin.tracker.server.dedicated;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(DedicatedServer.class)
public abstract class DedicatedServerMixin_Tracker {

    @Shadow public abstract int shadow$getSpawnProtectionRadius();

    /**
     * @author zml - March 9th, 2016
     * @author blood - July 7th, 2016 - Add cause tracker handling for throwing pre change block checks
     * @author gabizou - July 7th, 2016 - Update for 1.10's cause tracking changes
     *
     * @reason Change spawn protection to take advantage of Sponge permissions. Rather than affecting only the default world like vanilla, this
     * will apply to any world. Additionally, fire a spawn protection event
     */
    @Overwrite
    public boolean isUnderSpawnProtection(final ServerLevel worldIn, final BlockPos pos, final Player playerIn) {
        // Mods such as ComputerCraft and Thaumcraft check this method before attempting to set a blockstate.
        final PhaseContext<@NonNull ?> context = PhaseTracker.getInstance().getPhaseContext();
        if (!context.isInteraction()) {
            // TODO BLOCK_PROTECTED flag
            if (ShouldFire.CHANGE_BLOCK_EVENT_PRE && SpongeCommonEventFactory.callChangeBlockEventPre((ServerLevelBridge) worldIn, pos, playerIn).isCancelled()) {
                return true;
            }
        }

        final BlockPos spawnPoint = worldIn.getSharedSpawnPos();
        final int protectionRadius = this.shadow$getSpawnProtectionRadius();

        return protectionRadius > 0
                && Math.max(Math.abs(pos.getX() - spawnPoint.getX()), Math.abs(pos.getZ() - spawnPoint.getZ())) <= protectionRadius
                && !((ServerPlayer) playerIn).hasPermission("minecraft.spawn-protection.override");
    }

    @Redirect(method = "handleConsoleInputs", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;performPrefixedCommand(Lnet/minecraft/commands/CommandSourceStack;Ljava/lang/String;)V"))
    private void tracker$onPerformConsoleCommand(final Commands instance, final CommandSourceStack source, final String command) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(Sponge.systemSubject());
            frame.addContext(EventContextKeys.COMMAND, command);
            instance.performPrefixedCommand(source, command);
        }
    }
}
