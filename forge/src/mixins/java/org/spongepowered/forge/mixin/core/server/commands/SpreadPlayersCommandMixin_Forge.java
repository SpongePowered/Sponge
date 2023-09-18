/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://spongepowered.org>
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
 *
 */
package org.spongepowered.forge.mixin.core.server.commands;

import net.minecraft.server.commands.SpreadPlayersCommand;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(SpreadPlayersCommand.class)
public abstract class SpreadPlayersCommandMixin_Forge {

    @Redirect(method = "setPlayerPositions", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraftforge/event/ForgeEventFactory;onEntityTeleportSpreadPlayersCommand(Lnet/minecraft/world/entity/Entity;DDD)Lnet/minecraftforge/event/entity/EntityTeleportEvent$SpreadPlayersCommand;"
    ))
    private static EntityTeleportEvent.SpreadPlayersCommand vanilla$createCauseFrameForTeleport(Entity entity, double targetX, double targetY, double targetZ) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.COMMAND);

            return ForgeEventFactory.onEntityTeleportSpreadPlayersCommand(entity, targetX, targetY, targetZ);
        }
    }
}
